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
package be.hogent.tarsos.transcoder;

/**
 * Attributes controlling the audio attributes process.
 * 
 * @author Carlo Pelliccia
 */
public class Attributes {

	/**
	 * The codec name for the attributes process.
	 */
	private String codec = null;

	/**
	 * The stream duration in millis. If less than 0 this information is not
	 * available.
	 */
	private long duration = -1;

	/**
	 * The bit rate value for the attributes process. If null or not specified a
	 * default value will be picked.
	 */
	private Integer bitRate = null;

	/**
	 * The samplingRate value for the attributes process. If null or not
	 * specified a default value will be picked.
	 */
	private Integer samplingRate = null;

	/**
	 * The channels value (1=mono, 2=stereo) for the attributes process. If null
	 * or not specified a default value will be picked.
	 */
	private Integer channels = null;

	/**
	 * The volume value for the attributes process. If null or not specified a
	 * default value will be picked. If 256 no volume change will be performed.
	 */
	private Integer volume = null;

	public Attributes(final String format, final String codec, final Integer samplingRate,
			final Integer channels, final Integer bitRate, final Integer volume) {
		setBitRate(bitRate);
		setChannels(channels);
		setSamplingRate(samplingRate);
		setCodec(codec);
		setVolume(volume);
		setFormat(format);
	}

	public Attributes(final String format, final String codec, final Integer samplingRate,
			final Integer channels, final Integer bitRate) {
		this(format, codec, samplingRate, channels, bitRate, null);
	}

	public Attributes(final String format, final String codec, final Integer samplingRate,
			final Integer channels) {
		this(format, codec, samplingRate, channels, null, null);
	}

	public Attributes() {

	}

	/**
	 * Returns the codec name for the attributes process.
	 * 
	 * @return The codec name for the attributes process.
	 */
	public String getCodec() {
		return codec;
	}

	/**
	 * Sets the codec name for the attributes process. If null or not specified
	 * the encoder will perform a direct stream copy.
	 * 
	 * @param codec
	 *            The codec name for the attributes process.
	 */
	public void setCodec(String codec) {
		this.codec = codec;
	}

	/**
	 * Returns the bitrate value for the attributes process.
	 * 
	 * @return The bitrate value for the attributes process.
	 */
	public Integer getBitRate() {
		return bitRate;
	}

	/**
	 * Sets the bitrate value for the attributes process. If null or not
	 * specified a default value will be picked.
	 * 
	 * @param bitRate
	 *            The bitrate value for the attributes process.
	 */
	public void setBitRate(Integer bitRate) {
		this.bitRate = bitRate;
	}

	/**
	 * Returns the samplingRate value for the attributes process.
	 * 
	 * @return the samplingRate The samplingRate value for the attributes
	 *         process.
	 */
	public Integer getSamplingRate() {
		return samplingRate;
	}

	/**
	 * Sets the samplingRate value for the attributes process. If null or not
	 * specified a default value will be picked.
	 * 
	 * @param samplingRate
	 *            The samplingRate value for the attributes process.
	 */
	public void setSamplingRate(Integer samplingRate) {
		this.samplingRate = samplingRate;
	}

	/**
	 * Returns the channels value (1=mono, 2=stereo) for the attributes process.
	 * 
	 * @return The channels value (1=mono, 2=stereo) for the attributes process.
	 */
	public Integer getChannels() {
		return channels;
	}

	/**
	 * Sets the channels value (1=mono, 2=stereo) for the attributes process. If
	 * null or not specified a default value will be picked.
	 * 
	 * @param channels
	 *            The channels value (1=mono, 2=stereo) for the attributes
	 *            process.
	 */
	public void setChannels(Integer channels) {
		this.channels = channels;
	}

	/**
	 * Returns the volume value for the attributes process.
	 * 
	 * @return The volume value for the attributes process.
	 */
	public Integer getVolume() {
		return volume;
	}

	/**
	 * Sets the volume value for the attributes process. If null or not
	 * specified a default value will be picked. If 256 no volume change will be
	 * performed.
	 * 
	 * @param volume
	 *            The volume value for the attributes process.
	 */
	public void setVolume(Integer volume) {
		this.volume = volume;
	}

	/**
	 * The format name for the encoded target multimedia file. Be sure this
	 * format is supported (see {@link Encoder#getSupportedEncodingFormats()}.
	 */
	private String format = null;

	/**
	 * Returns the format name for the encoded target multimedia file.
	 * 
	 * @return The format name for the encoded target multimedia file.
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Sets the format name for the encoded target multimedia file. Be sure this
	 * format is supported (see {@link Encoder#getSupportedEncodingFormats()}.
	 * 
	 * @param format
	 *            The format name for the encoded target multimedia file.
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * Returns the stream duration in millis. If less than 0 this information is
	 * not available.
	 * 
	 * @return The stream duration in millis. If less than 0 this information is
	 *         not available.
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Sets the stream duration in millis.
	 * 
	 * @param duration
	 *            The stream duration in millis.
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		return String.format(
				"%s format=%s, codec=%s, bitrate=%s, samplingrate=%s,duration=%s, channels=%s , volume=%s",
				getClass().getName(), format, codec, bitRate, samplingRate, duration, channels, volume);
	}

}
