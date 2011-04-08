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
package be.hogent.tarsos.transcoder.ffmpeg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * A ffmpeg process wrapper.
 * 
 * @author Carlo Pelliccia
 */
class FFMPEGExecutor {

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
	 * @throws IOException
	 *             If the process call fails.
	 */
	public String execute(int expectedExitValue) throws IOException {
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
		System.out.println("execute: " + cmdLine);
		
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
}
