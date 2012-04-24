package be.hogent.tarsos.transcoder.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;

import be.hogent.tarsos.transcoder.Attributes;
import be.hogent.tarsos.transcoder.DefaultAttributes;
import be.hogent.tarsos.transcoder.Transcoder;
import be.hogent.tarsos.transcoder.ffmpeg.EncoderException;

/**
 * Check if the library works as expected.
 * 
 * @author Joren Six
 */
public class TranscoderTester {
	private final static String INPUT_FILE = "audio/input/tone/tone_10s.wav";

	@Test
	public void testTranscoding() throws EncoderException {
		List<DefaultAttributes> list = new ArrayList<DefaultAttributes>();
		list.add(DefaultAttributes.WAV_PCM_S16LE_MONO_44KHZ);
		list.add(DefaultAttributes.WAV_PCM_S16LE_STEREO_44KHZ);
		// Transcode INPUT_FILE to and from every encoding defined by DEFAULT
		// ATTRIBUTES
		for (DefaultAttributes target : list) {
			// Transcode the input file
			String outputFile = "audio/output/out_" + target.name() + "."
					+ target.getAttributes().getFormat();
			Transcoder.transcode(INPUT_FILE, outputFile, target);
			for (DefaultAttributes otherTarget : list) {
				String otherOutputFile = "audio/output/other_out_" + otherTarget.name() + "."
						+ otherTarget.getAttributes().getFormat();
				Transcoder.transcode(outputFile, otherOutputFile, otherTarget);
			}
		}

		// Encode some other formats.
		for (File source : new File("audio/input/formats/").listFiles()) {
			if (source.isFile()) {
				for (DefaultAttributes targetEncoding : DefaultAttributes.values()) {
					String targetName = "audio/output/" + source.getName() + "_" + targetEncoding.name()
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

		for (File file : new File("audio/input/formats/").listFiles()) {
			if (file.isFile()) {
				Attributes attr = Transcoder.getInfo(file.getAbsolutePath());
				assertTrue(attr.getDuration() != -1);
				assertTrue(attr.getChannels() != null);
				assertTrue(attr.getFormat() != null);
			}
		}
		for (File tempOutputFile : new File("audio/output/").listFiles()) {
			if (tempOutputFile.isFile()) {
				Attributes attr = Transcoder.getInfo(tempOutputFile.getAbsolutePath());
				assertTrue(attr.getDuration() != -1);
				assertTrue(attr.getChannels() != null);
				assertTrue(attr.getFormat() != null);
			}
		}
	}
	
	@Test
	public void testWMA() throws EncoderException{
		DefaultAttributes target = DefaultAttributes.WAV_PCM_S16LE_MONO_44KHZ;
		String inputFile = "/media/share/olmo/Ivor Darreg/Detwelvulate/19 40 Tones per Octave - FM Timbres, Chimes and Bells.wma";
		String outputFile = "/tmp/out." + target.getAttributes().getFormat();
		Transcoder.transcode(inputFile, outputFile, target);
	}
	
	@Test
	public void testFlac() throws EncoderException{
		DefaultAttributes target = DefaultAttributes.WAV_PCM_S16LE_MONO_44KHZ;
		String inputFile = "/home/joren/Music/Fred Hersch & Bill Frisell/(1999) Songs We Know/10. Wave.flac";
		String outputFile = "/tmp/out." + target.getAttributes().getFormat();
		Transcoder.transcode(inputFile, outputFile, target);
		assertTrue(new File(outputFile).exists());
	}

	/**
	 * Delete all generated test files.
	 */
	@AfterClass
	public static void cleanOutputDirectory() {
		for (File tempOutputFile : new File("audio/output/").listFiles()) {
			if (tempOutputFile.isFile()) {
				//tempOutputFile.delete();
			}
		}
	}
}
