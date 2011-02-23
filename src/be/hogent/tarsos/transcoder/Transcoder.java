package be.hogent.tarsos.transcoder;

import java.io.File;
import java.util.logging.Logger;

import be.hogent.tarsos.transcoder.ffmpeg.Encoder;
import be.hogent.tarsos.transcoder.ffmpeg.EncoderException;
import be.hogent.tarsos.transcoder.ffmpeg.InputFormatException;
import be.hogent.tarsos.transcoder.ffmpeg.LinuxFFMPEGLocator;
import be.hogent.tarsos.transcoder.ffmpeg.MacFFMPEGLocator;
import be.hogent.tarsos.transcoder.ffmpeg.PathFFMPEGLocator;
import be.hogent.tarsos.transcoder.ffmpeg.WindowsFFMPEGLocator;

/**
 * The main interface to transcode audio.
 * 
 * @author Joren Six
 */
public class Transcoder {

	private static final Logger LOG = Logger.getLogger(Transcoder.class.getName());

	/**
	 * Already added locators to encoder?
	 */
	private static boolean initialized = false;

	/**
	 * Adds default locators to encoder.
	 */
	private static void initialize() {
		if (!initialized) {
			Encoder.addFFMPEGLocator(new WindowsFFMPEGLocator());
			Encoder.addFFMPEGLocator(new MacFFMPEGLocator());
			Encoder.addFFMPEGLocator(new LinuxFFMPEGLocator());
			Encoder.addFFMPEGLocator(new PathFFMPEGLocator());
			initialized = true;
		}
	}

	/**
	 * A private constructor to hide the default.
	 */
	private Transcoder() {

	}

	/**
	 * Transcodes audio. It converts source to target with the defined
	 * attributes.
	 * 
	 * @param source
	 *            The path to the source audio file.
	 * @param target
	 *            The path to the target audio file.
	 * @param targetEncoding
	 *            A description of the encoding parameters.
	 * @throws EncoderException
	 *             If something goes wrong in the encoding process.
	 */
	public static void transcode(final String source, final String target, final Attributes targetEncoding)
			throws EncoderException {
		transcode(new File(source), new File(target), targetEncoding);
	}

	/**
	 * Transcodes audio. It converts source to target with the defined
	 * attributes.
	 * 
	 * @param source
	 *            The path to the source audio file.
	 * @param target
	 *            The path to the target audio file.
	 * @param targetEncoding
	 *            A description of the encoding parameters.
	 * @throws EncoderException
	 *             If something goes wrong in the encoding process.
	 */
	public static void transcode(final String source, final String target,
			final DefaultAttributes targetEncoding) throws EncoderException {
		transcode(source, target, targetEncoding.getAttributes());
	}

	/**
	 * Transcodes audio. It converts source to target with the defined
	 * attributes.
	 * 
	 * @param source
	 *            The path to the source audio file.
	 * @param target
	 *            The path to the target audio file.
	 * @param targetEncoding
	 *            A description of the encoding parameters.
	 * @throws EncoderException
	 *             If something goes wrong in the encoding process.
	 */
	public static void transcode(final File source, final File target, final DefaultAttributes targetEncoding)
			throws EncoderException {
		transcode(source, target, targetEncoding.getAttributes());
	}

	/**
	 * Transcodes audio. It converts source to target with the defined
	 * attributes.
	 * 
	 * @param source
	 *            The path to the source audio file.
	 * @param target
	 *            The path to the target audio file.
	 * @param targetEncoding
	 *            A description of the encoding parameters.
	 * @throws EncoderException
	 *             If something goes wrong in the encoding process.
	 */
	public static void transcode(final File source, final File target, final Attributes targetEncoding)
			throws EncoderException {
		// sanity checks
		if (!source.exists()) {
			throw new IllegalArgumentException(source + " does not exist. It should"
					+ " be a readable audiofile.");
		}
		if (source.isDirectory()) {
			throw new IllegalArgumentException(source + " is a directory. It should "
					+ "be a readable audiofile.");
		}
		if (!source.canRead()) {
			throw new IllegalArgumentException(source
					+ " can not be read, check file permissions. It should be a readable audiofile.");
		}

		initialize();
		// encode the source to directory
		final Encoder e = new Encoder();
		LOG.info("Try to transcode " + source + " to " + target);
		e.encode(source, target, targetEncoding);
		LOG.info("Successfully transcoded " + source + " to " + target);
	}

	/**
	 * Checks if transcoding is required: it fetches information about the file
	 * 'target' and checks if the file has the expected format, number of
	 * channels and sampling rate. If not then transcoding is required. If
	 * target is not found then transcoding is also required.
	 * 
	 * @param target
	 *            the path to the transcoded file or file to transcode
	 * @param channels
	 *            Defines the number of channels the transcoded file should
	 *            have.
	 * @param samplingRate
	 *            Defines the samplingrate the transcoded file should have.
	 * @return false if the file is already transcoded as per the requested
	 *         parameters, false otherwise.
	 */
	public static boolean transcodingRequired(final String target, final Attributes targetEncoding) {
		initialize();

		final File targetFile = new File(target);
		// if the file does not exist transcoding is always required
		boolean transcodingRequired = !targetFile.exists();

		// if the file exists, check the format
		if (targetFile.exists()) {
			// no need to transcode if attributes match.
			transcodingRequired = !matchesEncoding(target, targetEncoding);
		}

		return transcodingRequired;
	}

	/**
	 * Check if target matches encoding attributes defined by attributes.
	 * 
	 * @param target
	 *            The path to the target.
	 * @param targetEncoding
	 *            The encoding attributes.
	 * @return True if target matches the encoding, false otherwise.
	 */
	private static boolean matchesEncoding(final String target, final Attributes targetEncoding) {
		final Attributes info = getInfo(target);
		final int currentSamplingRate = info.getSamplingRate();
		final int currentNumberOfChannels = info.getChannels();
		final String currentDecoder = info.getFormat();
		final boolean samplingRateMatches = currentSamplingRate == targetEncoding.getSamplingRate();
		final boolean numberOfChannelsMatches = currentNumberOfChannels == targetEncoding.getChannels();
		final boolean codecMatches = currentDecoder.equalsIgnoreCase(targetEncoding.getCodec());
		return samplingRateMatches && numberOfChannelsMatches && codecMatches;
	}

	/**
	 * Returns information about an audio file: the sampling rate, the number of
	 * channels, the decoder, ...
	 * 
	 * @param file
	 *            the file to get the info for
	 * @return the info for the file.
	 */
	public static Attributes getInfo(final String file) {
		initialize();
		Attributes info = null;
		try {
			final Encoder e = new Encoder();
			info = e.getInfo(new File(file));
		} catch (final InputFormatException e1) {
			LOG.severe("Unknown input file format: " + file);
			e1.printStackTrace();
		} catch (final EncoderException e1) {
			LOG.warning("Could not get information about:" + file);
		}
		return info;
	}

	public static void main(String args[]) {
		if (args.length != 3) {
			printHelp();
		}
		File inputFile = new File(args[0]);
		File outputFile = new File(args[1]);

		DefaultAttributes encodingAttributes = null;
		try {
			encodingAttributes = DefaultAttributes.valueOf(args[2]);
		} catch (IllegalArgumentException e) {
			System.err.println("Please make sure " + args[2] + " is a correct value\n");
			printHelp();
		}

		if (inputFile.exists() && inputFile.canRead() && encodingAttributes != null) {
			try {
				transcode(inputFile, outputFile, encodingAttributes);
			} catch (EncoderException e) {
				System.err.println("Transcoding error: make sure input " + inputFile + " is an audio file.");
			}
		} else {
			System.err.println("Make sure input " + inputFile + " is readable and output " + outputFile
					+ " writable. Also check encoding attributes: " + args[2]);
			System.out.println();
			printHelp();
		}
	}

	private static void printHelp() {
		System.out.println("USAGE: java -jar transcoder.jar inputfile outputfile encoding");
		System.out.println("  with encoding one of:");
		for (DefaultAttributes attribute : DefaultAttributes.values()) {
			System.out.println("     " + attribute.name());
		}
	}
}
