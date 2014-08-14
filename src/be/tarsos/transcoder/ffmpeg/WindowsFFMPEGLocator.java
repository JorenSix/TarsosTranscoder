package be.tarsos.transcoder.ffmpeg;

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

import java.io.File;

/**
 * The default ffmpeg executable locator, which exports on disk the ffmpeg
 * executable bundled with the library distributions. It should work both for
 * windows and many linux distributions. If it doesn't, try compiling your own
 * ffmpeg executable and plug it in JAVE with a custom {@link FFMPEGLocator}.
 * 
 * @author Carlo Pelliccia
 */
public class WindowsFFMPEGLocator extends FFMPEGLocator {

	/**
	 * The ffmpeg executable file path.
	 */
	private final String path;

	private final static String FFMPEG_BINARY = "/be/tarsos/transcoder/resources/ffmpeg_win";

	/**
	 * It builds the default FFMPEGLocator, exporting the ffmpeg executable on a
	 * temp file.
	 */
	public WindowsFFMPEGLocator() {
		if (pickMe()) {
			path = copyFFMPEG();
		} else {
			path = null;
		}
	}

	private String copyFFMPEG() {
		// create a temp dir and mark it for deletion on exit.
		File temp = new File(System.getProperty("java.io.tmpdir"), "tarsos-ffmpeg");
		if (!temp.exists()) {
			temp.mkdirs();
			temp.deleteOnExit();
		}

		// ffmpeg executable export on disk.
		File ffmpeg = new File(temp, "ffmpeg.exe");
		if (!ffmpeg.exists()) {
			copyFile(FFMPEG_BINARY, ffmpeg);
		}
		return ffmpeg.getAbsolutePath();
	}

	@Override
	protected String getFFMPEGExecutablePath() {
		return path;
	}

	@Override
	public boolean pickMe() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.contains("windows");
	}

}
