/*
 * JAVE - A Java Audio/Video Encoder (based on FFMPEG)
 * 
 * Copyright (C) 2008-2009 Carlo Pelliccia (www.sauronsoftware.it)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package be.tarsos.transcoder.ffmpeg;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import be.tarsos.transcoder.Attributes;

/**
 * A ffmpeg process wrapper.
 * 
 * @author Carlo Pelliccia
 */
class FFMPEGExecutor {
	
	/**
	 * Log messages.
	 */
	private static final Logger LOG = Logger.getLogger(FFMPEGExecutor.class.getName());

	/**
	 * The path of the ffmpeg executable.
	 */
	private final String ffmpegExecutablePath;

	/**
	 * Arguments for the executable.
	 */
	private final ArrayList<String> args = new ArrayList<String>();
	private final ArrayList<Boolean> argIsFile = new ArrayList<Boolean>();
	

	/**
	 * It build the executor.
	 * 
	 * @param ffmpegExecutablePath
	 *            The path of the ffmpeg executable.
	 */
	public FFMPEGExecutor(String ffmpegExecutablePath) {
		this.ffmpegExecutablePath = ffmpegExecutablePath;
	}

	/**
	 * Adds an argument to the ffmpeg executable call.
	 * 
	 * @param arg
	 *            The argument.
	 */
	public void addArgument(String arg) {
		args.add(arg);
		argIsFile.add(false);
	}
	
	/**
	 * Add a file to the ffmpeg executable call.
	 * @param arg
	 */
	public void addFileArgument(String arg){
		args.add(arg);
		argIsFile.add(true);
	}

	/**
	 * Executes the ffmpeg process with the previous given arguments.
	 * 
	 * @return The standard output of the child process.
	 * 
	 * @throws IOException
	 *             If the process call fails.
	 */
	public String execute() throws IOException {
		CommandLine cmdLine = new CommandLine(ffmpegExecutablePath);
		
		int fileNumber=0;
		Map<String,File> map = new HashMap<String,File>();
		for (int i = 0 ;i<args.size();i++) {
			final String arg = args.get(i);
			final Boolean isFile = argIsFile.get(i);
			if(isFile){
				String key = "file" + fileNumber;
				map.put(key, new File(arg));
				cmdLine.addArgument("'${" + key + "}'",false);
				fileNumber++;
			} else {
				cmdLine.addArgument(arg);
			}
		}		
		cmdLine.setSubstitutionMap(map);
		LOG.fine("Execute: " + cmdLine);		
		DefaultExecutor executor = new DefaultExecutor();
		//5minutes wait
		ExecuteWatchdog watchdog = new ExecuteWatchdog(60 * 1000 * 5);
		executor.setWatchdog(watchdog);
		ByteArrayOutputStream out =  new ByteArrayOutputStream();
		executor.setStreamHandler(new PumpStreamHandler(out));
		int[] exitValues = {0,1};
		executor.setExitValues(exitValues);
		executor.execute(cmdLine);
		return out.toString();		
	}
	
	
	public AudioInputStream pipe(Attributes attributes) throws EncoderException {
		String pipeEnvironment;
		String pipeArgument;
		File pipeLogFile;
		int pipeBuffer;
		
		if(System.getProperty("os.name").indexOf("indows") > 0 ){
			pipeEnvironment = "cmd.exe";
			pipeArgument = "/C";
		}else{
			pipeEnvironment = "/bin/bash";
			pipeArgument = "-c";
		}
		pipeLogFile = new File("decoder_log.txt");
		//buffer 1/4 second of audio.
		pipeBuffer = attributes.getSamplingRate()/4;
		
		AudioFormat audioFormat = Encoder.getTargetAudioFormat(attributes);
		
		String command = toString();
		
		ProcessBuilder pb = new ProcessBuilder(pipeEnvironment, pipeArgument , command);

		pb.redirectError(Redirect.appendTo(pipeLogFile));
	
		LOG.fine("Starting piped decoding process" );
		final Process process;
		try {
			process = pb.start();
		} catch (IOException e1) {
			throw new EncoderException("Problem starting piped sub process: " + e1.getMessage());
		}
			
		InputStream stdOut = new BufferedInputStream(process.getInputStream(), pipeBuffer);
		
		//read and ignore the 46 byte wav header, only pipe the pcm samples to the audioinputstream
		byte[] header = new byte[46];
		double sleepSeconds = 0;
		double timeoutLimit = 20; //seconds
		
		try {
			while(stdOut.available() < header.length){
				try {
					Thread.sleep(100);
					sleepSeconds += 0.1;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(sleepSeconds > timeoutLimit){
					throw new Error("Could not read from pipe within " + timeoutLimit + " seconds: timeout!");
				}
			}
			int bytesRead = stdOut.read(header);
			if(bytesRead != header.length){
				throw new EncoderException("Could not read complete WAV-header from pipe. This could result in mis-aligned frames!");
			}
		} catch (IOException e1) {
			throw new EncoderException("Problem reading from piped sub process: " + e1.getMessage());
		}
		
		final AudioInputStream audioStream = new AudioInputStream(stdOut, audioFormat, AudioSystem.NOT_SPECIFIED);
		
		//This thread waits for the end of the subprocess.
		new Thread(new Runnable(){
			public void run() {
				try {
					process.waitFor();
					LOG.fine("Finished piped decoding process");
				} catch (InterruptedException e) {
					LOG.severe("Interrupted while waiting for sub process exit.");
					e.printStackTrace();
				}
			}},"Decoding Pipe Reader").start();
			return audioStream;		
	}
	
	
	public String toString(){
		CommandLine cmdLine = new CommandLine(ffmpegExecutablePath);
		
		int fileNumber=0;
		Map<String,File> map = new HashMap<String,File>();
		for (int i = 0 ;i<args.size();i++) {
			final String arg = args.get(i);
			final Boolean isFile = argIsFile.get(i);
			if(isFile){
				String key = "file" + fileNumber;
				map.put(key, new File(arg));
				cmdLine.addArgument("${" + key + "}",false);
				fileNumber++;
			} else {
				cmdLine.addArgument(arg);
			}
		}		
		cmdLine.setSubstitutionMap(map);
		return cmdLine.toString();	
	}
	
}
