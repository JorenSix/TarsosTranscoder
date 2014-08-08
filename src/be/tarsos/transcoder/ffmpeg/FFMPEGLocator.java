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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Abstract class whose derived concrete instances are used by {@link Encoder}
 * to locate the FFMPEG executable path.
 * 
 * @author Carlo Pelliccia
 * @see Encoder
 */
public abstract class FFMPEGLocator {

	/**
	 * Decides if this FFMPEG locator should be used. It can check e.g. the
	 * platform or architecture.
	 * 
	 * @return True if this locator should be used (on this platform), false
	 *         otherwise.
	 */
	public abstract boolean pickMe();

	/**
	 * This method should return the path of a ffmpeg executable suitable for
	 * the current machine.
	 * 
	 * @return The path of the FFMPEG executable.
	 */
	protected abstract String getFFMPEGExecutablePath();

	/**
	 * It returns a brand new {@link FFMPEGExecutor}, ready to be used in a
	 * FFMPEG call.
	 * 
	 * @return A newly instanced {@link FFMPEGExecutor}, using this locator to
	 *         call the FFMPEG executable.
	 */
	protected FFMPEGExecutor createExecutor() {
		return new FFMPEGExecutor(getFFMPEGExecutablePath());
	}

	/**
	 * Copies a file bundled in the package to the supplied destination.
	 * 
	 * @param resource
	 *            The name of the bundled file.
	 * @param dest
	 *            The destination.
	 * @throws RuntimeException
	 *             If an unexpected error occurs.
	 */
	public void copyFile(final String resource, final File dest) {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = FFMPEGLocator.class.getResourceAsStream(resource);
			output = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int l;
			while ((l = input.read(buffer)) != -1) {
				output.write(buffer, 0, l);
			}
		} catch (IOException e) {
			throw new RuntimeException("Cannot write file " + dest.getAbsolutePath());
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (Throwable t) {
					// ignore
				}
			}
			if (input != null) {
				try {
					input.close();
				} catch (Throwable t) {
					// ignore
				}
			}
		}
	}

	/**
	 * Makes a file (on disk) executable by calling 'chmod 755 path'.
	 * 
	 * @param executablePath
	 *            The name of the file on disk.
	 * @param chmodPath
	 *            The path for the chmod executable. E.g. "/bin/chmod"
	 * @throws IOException
	 *             If an unexpected error occurs.
	 */
	public void chmodPlusX(final String executablePath, final String chmodPath) throws IOException {
		Runtime runtime = Runtime.getRuntime();
		File executable = new File(executablePath);
		File chmodFile = new File(chmodPath);
		if (!chmodFile.exists()) {
			String msg = String.format("Could not set the executable bit for %s chmod not found at %s.",
					executable.getAbsolutePath(), chmodFile.getAbsolutePath());
			throw new Error(msg);
		}
		if (!executable.exists()) {
			String msg = String.format("Could not set the executable bit for %s: file not found.",
					executable.getAbsolutePath());
			throw new Error(msg);
		}
		// Make sure executable is ... executable by executing chmod and setting
		// the executable bit.
		runtime.exec(new String[] { chmodFile.getAbsolutePath(), "755", executable.getAbsolutePath() });
	}
}
