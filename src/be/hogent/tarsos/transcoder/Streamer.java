package be.hogent.tarsos.transcoder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import be.hogent.tarsos.transcoder.ffmpeg.Encoder;
import be.hogent.tarsos.transcoder.ffmpeg.EncoderException;
import be.hogent.tarsos.transcoder.ffmpeg.LinuxFFMPEGLocator;
import be.hogent.tarsos.transcoder.ffmpeg.MacFFMPEGLocator;
import be.hogent.tarsos.transcoder.ffmpeg.PathFFMPEGLocator;
import be.hogent.tarsos.transcoder.ffmpeg.WindowsFFMPEGLocator;

/**
 * The main interface to stream audio.
 * 
 * @author Joren Six
 */
public class Streamer {
	
	/**
	 * Adds default locators to encoder.
	 */
	private static void initialize() {
		if(!Encoder.hasLocators()) {
			Encoder.addFFMPEGLocator(new WindowsFFMPEGLocator());
			Encoder.addFFMPEGLocator(new MacFFMPEGLocator());
			Encoder.addFFMPEGLocator(new LinuxFFMPEGLocator());
			Encoder.addFFMPEGLocator(new PathFFMPEGLocator());
		}
	}
 
	private Streamer(){
	}
	
	public static AudioInputStream stream(final String source, final Attributes targetEncoding)
			throws EncoderException {
		initialize();
		return new Encoder().stream(source, targetEncoding);
	}
	
	public static AudioFormat streamAudioFormat( final Attributes targetEncoding)
			throws EncoderException {
		return Encoder.getTargetAudioFormat(targetEncoding);
	}

}
