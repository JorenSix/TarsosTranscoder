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

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import be.tarsos.transcoder.Attributes;

/**
 * Main class of the package. Instances can encode audio and video streams.
 * 
 * @author Carlo Pelliccia
 * @author Joren Six
 */
public class Encoder {

	private static final Logger LOG = Logger.getLogger(Encoder.class.getName());

	private static ArrayList<FFMPEGLocator> locators = new ArrayList<FFMPEGLocator>();

	public static void addFFMPEGLocator(FFMPEGLocator locator) {
		locators.add(locator);
	}
	

	public static boolean hasLocators() {
		return locators.size() > 0;
	}	

	/**
	 * This regexp is used to parse the ffmpeg output about the bit rate value
	 * of a stream.
	 */
	private static final Pattern BIT_RATE_PATTERN = Pattern.compile("(\\d+)\\s+kb/s",
			Pattern.CASE_INSENSITIVE);

	/**
	 * This regexp is used to parse the ffmpeg output about the sampling rate of
	 * an audio stream.
	 */
	private static final Pattern SAMPLING_RATE_PATTERN = Pattern.compile("(\\d+)\\s+Hz",
			Pattern.CASE_INSENSITIVE);

	/**
	 * This regexp is used to parse the ffmpeg output about the channels number
	 * of an audio stream.
	 */
	private static final Pattern CHANNELS_PATTERN = Pattern.compile("(mono|stereo|.*(\\d+).*channels)",
			Pattern.CASE_INSENSITIVE);

	/**
	 * The locator of the ffmpeg executable used by this encoder.
	 */
	private FFMPEGLocator locator;

	/**
	 * It builds an encoder using a locator instance to
	 * locate the ffmpeg executable to use.
	 */
	public Encoder() {
		for (FFMPEGLocator loc : locators) {
			if (loc.pickMe()) {
				this.locator = loc;
			}
		}
		if (this.locator == null) {
			throw new Error("Could not find an ffmpeg locator for this operating system.");
		}
	}

	/**
	 * Returns a set informations about a multimedia file, if its format is
	 * supported for decoding.
	 * 
	 * @param source
	 *            The source multimedia file.
	 * @return A set of informations about the file and its contents.
	 * @throws InputFormatException
	 *             If the format of the source file cannot be recognized and
	 *             decoded.
	 * @throws EncoderException
	 *             If a problem occurs calling the underlying ffmpeg executable.
	 */
	public Attributes getInfo(File source) throws InputFormatException, EncoderException {
		FFMPEGExecutor ffmpeg = locator.createExecutor();
		ffmpeg.addArgument("-i");
		ffmpeg.addFileArgument(source.getAbsolutePath());
		try {
			String out = ffmpeg.execute();
			return parseAudioAttributes(source, out);
		} catch (IOException e) {
			throw new EncoderException(e);
		}
	}

	/**
	 * Private utility. It parses the ffmpeg output, extracting informations
	 * about a source multimedia file.
	 * 
	 * @param source
	 *            The source multimedia file.
	 * @param reader
	 *            The ffmpeg output channel.
	 * @return A set of informations about the source multimedia file and its
	 *         contents.
	 * @throws InputFormatException
	 *             If the format of the source file cannot be recognized and
	 *             decoded.
	 * @throws EncoderException
	 *             If a problem occurs calling the underlying ffmpeg executable.
	 */
	private Attributes parseAudioAttributes(File source,String contents) throws InputFormatException,
			EncoderException {
		Pattern p1 = Pattern.compile(".*\\s*Input #0, (\\w+).+$\\s*.*", Pattern.CASE_INSENSITIVE
				| Pattern.MULTILINE | Pattern.UNIX_LINES);
		Pattern p2 = Pattern.compile(".*\\s*Duration: (\\d\\d):(\\d\\d):(\\d\\d)\\.(\\d).*",
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNIX_LINES);
		Pattern p3 = Pattern.compile(".*\\s*Stream #\\S+: ((?:Audio)|(?:Video)|(?:Data)): (.*)\\s*.*",
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNIX_LINES);

		boolean noMatch = true;
		Attributes info = new Attributes();


		Matcher m = p1.matcher(contents);
		if (m.find()) {
			noMatch = false;
			String format = m.group(1);
			info.setFormat(format);
		}

		m = p2.matcher(contents);
		if (m.find()) {
			noMatch = false;
			long hours = Integer.parseInt(m.group(1));
			long minutes = Integer.parseInt(m.group(2));
			long seconds = Integer.parseInt(m.group(3));
			long dec = Integer.parseInt(m.group(4));
			long duration = dec * 100L + seconds * 1000L + minutes * 60L * 1000L + hours * 60L * 60L * 1000L;
			info.setDuration(duration);
		}

		m = p3.matcher(contents);
		if (m.find()) {
			noMatch = false;
			String type = m.group(1);
			String specs = m.group(2);
			if ("Audio".equalsIgnoreCase(type)) {

				StringTokenizer st = new StringTokenizer(specs, ",");
				for (int i = 0; st.hasMoreTokens(); i++) {
					String token = st.nextToken().trim();
					if (i == 0) {
						info.setFormat(token);
					} else {
						boolean parsed = false;
						// Sampling rate.
						Matcher m2 = SAMPLING_RATE_PATTERN.matcher(token);
						if (!parsed && m2.find()) {
							int samplingRate = Integer.parseInt(m2.group(1));
							info.setSamplingRate(samplingRate);
							parsed = true;
						}
						// Channels.
						m2 = CHANNELS_PATTERN.matcher(token);
						if (!parsed && m2.find()) {
							String ms = m2.group(1);
							if ("mono".equalsIgnoreCase(ms)) {
								info.setChannels(1);
							} else if ("stereo".equalsIgnoreCase(ms)) {
								info.setChannels(2);
							} else {
								info.setChannels(Integer.valueOf(m2.group(2)));
							}
							parsed = true;
						}
						// Bit rate.
						m2 = BIT_RATE_PATTERN.matcher(token);
						if (!parsed && m2.find()) {
							int bitRate = Integer.parseInt(m2.group(1));
							info.setBitRate(bitRate);
							parsed = true;
						}
					}
				}
			}
		}

		if (noMatch) {
			throw new InputFormatException();
		}
		return info;
	}

	/**
	 * Re-encode a multimedia file.
	 * 
	 * @param source
	 *            The source multimedia file. It cannot be null. Be sure this
	 *            file can be decoded.
	 * @param target
	 *            The target multimedia re-encoded file. It cannot be null. If
	 *            this file already exists, it will be overwrited.
	 * @param attributes
	 *            A set of attributes for the attributes process.
	 * @throws IllegalArgumentException
	 *             If both audio and video parameters are null.
	 * 
	 * @throws EncoderException
	 *             If a problems occurs during the attributes process.
	 */
	public void encode(File source, File target, Attributes attributes) throws EncoderException {
		if (attributes == null) {
			throw new IllegalArgumentException("Audio attributes are null");
		}
		
		target = target.getAbsoluteFile();
		target.getParentFile().mkdirs();
		FFMPEGExecutor ffmpeg = construcExecutor(attributes, source.getAbsolutePath());

		//add output file
		ffmpeg.addArgument("-y");
		ffmpeg.addFileArgument(target.getAbsolutePath());
		
		try {
			String out = ffmpeg.execute();
			LOG.fine(out);
		} catch (IOException e) {
			throw new EncoderException(e);
		}

		if (target.length() == 0) {
			throw new EncoderException(String.format(
					"The size of the target (%s) is zero bytes, something went wrong.",
					target.getAbsolutePath()));
		} else {
			long sourceDuration = getInfo(source).getDuration();
			long targetDuration = getInfo(target).getDuration();
			
			if (targetDuration > 0 && sourceDuration > 0 && Math.abs(sourceDuration - targetDuration) > 3000) {
				throw new EncoderException(
						String.format(
								"Source and target should have similar duration (source %s duration: %s ms, target %s duration: %s ms).",
								source.getAbsolutePath(), sourceDuration, target.getAbsolutePath(),
								targetDuration));
			}
		}
	}
	
	public AudioInputStream stream(String source, Attributes attributes) throws EncoderException {

		if (attributes == null) {
			throw new IllegalArgumentException("Audio attributes are null");
		} if(!attributes.getFormat().equalsIgnoreCase("wav")){
			throw new IllegalArgumentException("Streaming only supports the wav format, not  " + attributes.getFormat());
		}
		
		//Create an ffmpeg executor
		FFMPEGExecutor ffmpeg = construcExecutor(attributes, source);
		
		//Pipe the output to stdout
		ffmpeg.addArgument("pipe:1");
		
		//The command to execute is this:
		LOG.fine("Will pipe stream output using the following command:");
		LOG.fine(ffmpeg.toString());
		
		return ffmpeg.pipe(attributes);
	}
	
	private FFMPEGExecutor construcExecutor(Attributes attributes,String source){
		FFMPEGExecutor ffmpeg = locator.createExecutor();
		
		Integer seekTime = attributes.getSeekTime();
		if (seekTime != null) {
			ffmpeg.addArgument("-ss");
			int S = seekTime; //ms
			int s = S/1000;   //sec
			int m = s/60;     //min
			int h = m/60;     //hr
			ffmpeg.addArgument(String
				.format("%2d:%2d:%2d.%3d", h%100, m%60, s%60, S%1000)
				.replaceAll(" ","0"));
		}
		
		ffmpeg.addArgument("-i");
		ffmpeg.addArgument(source);

		// no video
		ffmpeg.addArgument("-vn");

		String codec = attributes.getCodec();
		if (codec != null) {
			ffmpeg.addArgument("-acodec");
			ffmpeg.addArgument(codec);
		}
		Integer bitRate = attributes.getBitRate();
		if (bitRate != null) {
			ffmpeg.addArgument("-ab");
			ffmpeg.addArgument(String.valueOf(bitRate.intValue()));
		}
		Integer channels = attributes.getChannels();
		if (channels != null) {
			ffmpeg.addArgument("-ac");
			ffmpeg.addArgument(String.valueOf(channels.intValue()));
		}
		Integer samplingRate = attributes.getSamplingRate();
		if (samplingRate != null) {
			ffmpeg.addArgument("-ar");
			ffmpeg.addArgument(String.valueOf(samplingRate.intValue()));
		}
		Integer volume = attributes.getVolume();
		if (volume != null) {
			ffmpeg.addArgument("-vol");
			ffmpeg.addArgument(String.valueOf(volume.intValue()));
		}

		ffmpeg.addArgument("-f");
		ffmpeg.addArgument(attributes.getFormat());
		
		
		return ffmpeg;
	}
	
	/**
	 * Constructs the target audio format. The audio format is one channel
	 * signed PCM of a given sample rate.
	 * 
	 * @param attributes
	 *            The audio format (sample rate, format, bit depth, channels,...) to convert to.
	 * @return The audio format after conversion.
	 */
	public static AudioFormat getTargetAudioFormat(Attributes attributes) {
		AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
	        		attributes.getSamplingRate(), 
	        		16, 
	        		attributes.getChannels(), 
	        		2 * attributes.getChannels(), 
	        		attributes.getSamplingRate(), 
	                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));
		 return audioFormat;
	}

}
