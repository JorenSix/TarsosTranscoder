package be.tarsos.transcoder.ffmpeg;

import java.io.File;
import java.io.IOException;

/**
 * This class provides an ffmpeg binary for Mac OS X. Other platforms are
 * supported by the JAVE default ffmpeg locator. FFMPEG should work on Windows,
 * Linux and Mac OS X.
 * 
 * @author Joren Six
 */
public final class MacFFMPEGLocator extends FFMPEGLocator {

	private final String path;

	private final static String FFMPEG_BINARY = "/be/hogent/tarsos/transcoder/resources/ffmpeg_mac";

	public MacFFMPEGLocator() {
		if (pickMe()) {
			path = copyFFMPEG();
		} else {
			path = null;
		}
	}

	@Override
	public boolean pickMe() {
		// returns true on mac os x
		String os = System.getProperty("os.name").toLowerCase();
		return os.contains("mac");
	}

	/**
	 * Copies the executable to disk, makes it... executable and returns the
	 * path. If the target file already exists the path is returned.
	 * 
	 * @return The path of the FFMPEG executable.
	 */
	private String copyFFMPEG() {
		// create a temp dir and mark it for deletion on exit.
		File temp = new File(System.getProperty("java.io.tmpdir"), "tarsos-ffmpeg");
		if (!temp.exists()) {
			temp.mkdirs();
			temp.deleteOnExit();
		}

		// ffmpeg executable export on disk.
		File ffmpeg = new File(temp, "ffmpeg");
		if (!ffmpeg.exists()) {
			copyFile(FFMPEG_BINARY, ffmpeg);
		}

		try {
			chmodPlusX(ffmpeg.getAbsolutePath(), "/bin/chmod");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ffmpeg.getAbsolutePath();
	}

	@Override
	protected String getFFMPEGExecutablePath() {
		return path;
	}
}
