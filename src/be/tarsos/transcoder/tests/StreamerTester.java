package be.hogent.tarsos.transcoder.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.Test;

import be.hogent.tarsos.transcoder.Attributes;
import be.hogent.tarsos.transcoder.DefaultAttributes;
import be.hogent.tarsos.transcoder.Streamer;
import be.hogent.tarsos.transcoder.Transcoder;
import be.hogent.tarsos.transcoder.ffmpeg.EncoderException;

public class StreamerTester {
	

	@Test
	public void testTranscodingAndStreaming() throws EncoderException, LineUnavailableException, IOException, UnsupportedAudioFileException, InterruptedException{
		for (File file : new File("audio/input/formats/").listFiles()) {
			if (file.isFile()) {
				compareTranscodingAndStreaming(file.getAbsolutePath());
			}
		}
	}
	/**
	 * Streaming and transcoding the same file should yield the exact same results. To test this an mp3 is decoded and resampled and via transcoding and via streaming. 
	 */
	 
	public void compareTranscodingAndStreaming(String source) throws EncoderException, LineUnavailableException, IOException, UnsupportedAudioFileException, InterruptedException{
		System.out.println("Testing: " + source);
		//Set the transcoding to WAV PCM, 16bits LE, 16789Hz (to make sure resampling is done). 
		Attributes attributes = DefaultAttributes.WAV_PCM_S16LE_STEREO_44KHZ.getAttributes();
		attributes.setSamplingRate(16789);
		
		//Save the transcoded file:
		File temporaryTranscoded = File.createTempFile("temporaryTranscoded", ".wav");
		temporaryTranscoded = new File("transcoded.wav");
		Transcoder.transcode(source, temporaryTranscoded.getAbsolutePath(), attributes);
		AudioInputStream transcodedAudioInputStream = AudioSystem.getAudioInputStream(temporaryTranscoded);
		
		//Stream the same file with on the fly decoding:		
		AudioInputStream streamedAudioInputStream = Streamer.stream(source, attributes);
		
		byte[] streamBuffer = new byte[1024];
		byte[] transcodedBuffer = new byte[streamBuffer.length];
		
		int sampleCounter = 0;
		while (streamedAudioInputStream.available() > streamBuffer.length && transcodedAudioInputStream.available() > streamBuffer.length){
			
			streamedAudioInputStream.read(streamBuffer);
			transcodedAudioInputStream.read(transcodedBuffer);
			
			for(int i = 0 ; i < streamBuffer.length; i++){
				sampleCounter++;
				assertEquals("Difference at sample: " + sampleCounter,transcodedBuffer[i],streamBuffer[i]);
			}
		}
		streamedAudioInputStream.close();
		transcodedAudioInputStream.close();
		temporaryTranscoded.delete();
	}
	
	/**
	 * Play a random http stream.
	 */
	public void playStream() throws EncoderException, LineUnavailableException, IOException, UnsupportedAudioFileException, InterruptedException{
		String source;
		
		SourceDataLine line;
		DataLine.Info info;
		
		//The source stream
		source = "http://mp3.streampower.be/stubru-high.mp3";
		source = "http://mp3.streampower.be/klara-high.mp3";
		
		//Set the transcoding to WAV PCM, 16bits LE, 16789Hz (to make sure resampling is done). 
		Attributes attributes = DefaultAttributes.WAV_PCM_S16LE_STEREO_44KHZ.getAttributes();
		attributes.setSamplingRate(16789);
		
		
		//Stream the same file with on the fly decoding:		
		AudioInputStream streamedAudioInputStream = Streamer.stream(source, attributes);
		AudioFormat audioFormat = Streamer.streamAudioFormat(attributes);
		
		byte[] streamBuffer = new byte[1024];
		
		info = new DataLine.Info(SourceDataLine.class, audioFormat);
		line = (SourceDataLine) AudioSystem.getLine(info);
		line.open(audioFormat);
		line.start();
				
		while (streamedAudioInputStream.available() > streamBuffer.length){
			int bytesRead = streamedAudioInputStream.read(streamBuffer);
			int bytesWrote = line.write(streamBuffer, 0, streamBuffer.length);
			assertEquals("The number of bytes read should match the number of bytes written to the dataline", bytesRead,bytesWrote);
		}
		line.close();
		streamedAudioInputStream.close();
		
		
	}
}
