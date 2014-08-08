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
import java.io.IOException;

/**
 * The default FFMPEG executable locator, which exports on disk the FFMPEG
 * executable bundled with the library distributions. It should work both for
 * windows and many Linux distributions. If it doesn't, try compiling your own
 * FFMPEG executable and plug it in with a custom {@link FFMPEGLocator}.
 * 
 * @author Carlo Pelliccia
 */
public class LinuxFFMPEGLocator extends FFMPEGLocator {

	/**
	 * The FFMPEG executable file path.
	 */
	private final String path;

	private final static String FFMPEG_BINARY = "/be/hogent/tarsos/transcoder/resources/ffmpeg_linux";

	public LinuxFFMPEGLocator() {
		if (pickMe()) {
			path = copyFFMPEG();
		} else {
			path = null;
		}
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

	@Override
	public boolean pickMe() {
		String os = System.getProperty("os.name").toLowerCase();
		String arch = System.getProperty("os.arch");
		// binary is for x86 or amd64 or x86_64 architectures (
		// see http://lopica.sourceforge.net/os.html
		return os.contains("linux") && (arch.contains("86") || arch.contains("64"));
	}
}
