package be.tarsos.transcoder.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;

import be.tarsos.transcoder.Attributes;
import be.tarsos.transcoder.DefaultAttributes;
import be.tarsos.transcoder.Transcoder;
import be.tarsos.transcoder.ffmpeg.EncoderException;

/**
 * Check if the library works as expected.
 * 
 * @author Joren Six
 */
public class TranscoderTester {
	private final static String SLASH = System.getProperty("file.separator");
	private final static String INPUT_FILE = "audio"+ SLASH + "input"+ SLASH + "tone"+ SLASH + "tone_10s.wav";

	@Test
	public void testTranscoding() throws EncoderException {
		
		
		List<DefaultAttributes> list = new ArrayList<DefaultAttributes>();
		list.add(DefaultAttributes.WAV_PCM_S16LE_MONO_44KHZ);
		list.add(DefaultAttributes.WAV_PCM_S16LE_STEREO_44KHZ);
		
		for (DefaultAttributes target : list) {
			// Transcode the input file
			
			String outputFile = "audio"+ SLASH + "output"+ SLASH +"out_" + target.name() + "."
					+ target.getAttributes().getFormat();
			Transcoder.transcode(INPUT_FILE, outputFile, target);
			for (DefaultAttributes otherTarget : list) {
				String otherOutputFile = "audio"+ SLASH + "output"+ SLASH + "other_out_" + otherTarget.name() + "."
						+ otherTarget.getAttributes().getFormat();
				Transcoder.transcode(outputFile, otherOutputFile, otherTarget);
			}
		}

		// Encode some other formats.
		for (File source : new File("audio"+ SLASH + "input" + SLASH + "formats"+ SLASH).listFiles()) {
			if (source.isFile()) {
				for (DefaultAttributes targetEncoding : list) {
					String targetName = "audio"+ SLASH + "output"+ SLASH + source.getName() + "_" + targetEncoding.name()
							+ "." + targetEncoding.getAttributes().getFormat();
					File target = new File(targetName);
					Transcoder.transcode(source, target, targetEncoding);
				}
			}
		}
		
	}

	/**
	 * Check if encoding fails on empty files.
	 * 
	 * @throws EncoderException
	 *             If the method results in an expected exception.
	 */
	@Test(expected = EncoderException.class)
	public void testEncoderException() throws EncoderException {
		File newFile = new File("hmm");
		try {
			newFile.createNewFile();
			newFile.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Transcoder.transcode(newFile, new File("out"), DefaultAttributes.MP3_192KBS_MONO_44KHZ);
	}

	/**
	 * Tries to gain information about the converted files.
	 */
	@Test
	public void testGetInfo() {
	
		for (File file : new File("audio"+ SLASH + "input"+ SLASH + "formats" + SLASH ).listFiles()) {
			if (file.isFile()) {
				Attributes attr = Transcoder.getInfo(file.getAbsolutePath());
				assertTrue(attr.getDuration() != -1);
				assertTrue(attr.getChannels() != null);
				assertTrue(attr.getFormat() != null);
			}
		}
		for (File tempOutputFile : new File("audio"+ SLASH + "output"+ SLASH ).listFiles()) {
			if (tempOutputFile.isFile()) {
				Attributes attr = Transcoder.getInfo(tempOutputFile.getAbsolutePath());
				assertTrue(attr.getDuration() != -1);
				assertTrue(attr.getChannels() != null);
				assertTrue(attr.getFormat() != null);
			}
		}
	}
	

	/**
	 * Delete all generated test files.
	 */
	@AfterClass
	public static void cleanOutputDirectory() {
		for (File tempOutputFile : new File("audio"+ SLASH + "output"+ SLASH).listFiles()) {
			if (tempOutputFile.isFile()) {
				tempOutputFile.delete();
			}
		}
	}
}
