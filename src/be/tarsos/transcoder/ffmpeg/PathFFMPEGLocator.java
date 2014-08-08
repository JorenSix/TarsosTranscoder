package be.tarsos.transcoder.ffmpeg;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides tries to find an ffmpeg binary in the PATH
 * 
 * @author Joren Six
 */
public final class PathFFMPEGLocator extends FFMPEGLocator {

	private final String path;

	public PathFFMPEGLocator() {
		if (pickMe()) {
			path = "ffmpeg";
		} else {
			path = null;
		}
	}

	Boolean ffmpegInPath = null;

	@Override
	public boolean pickMe() {
		if (ffmpegInPath == null) {
			FFMPEGExecutor executor = new FFMPEGExecutor("ffmpeg");
			executor.addArgument("-version");
			try {
				String out = executor.execute();				
				Pattern versionPattern = Pattern.compile(".*Version.*", Pattern.CASE_INSENSITIVE
						| Pattern.MULTILINE | Pattern.UNIX_LINES);
				Matcher versionMatcher = versionPattern.matcher(out);
				ffmpegInPath = versionMatcher.find();
			} catch (IOException e) {
				ffmpegInPath = false;
			}
		}
		return ffmpegInPath.booleanValue();
	}

	@Override
	protected String getFFMPEGExecutablePath() {
		return path;
	}
}
