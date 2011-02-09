package be.hogent.tarsos.transcoder;

/**
 * A list of default encoding options. Encoding and decoding in these formats
 * should be supported by the ffmpeg binary.
 * 
 * @author Joren Six
 */
public enum DefaultAttributes {
	/**
	 * Ogg, 44.1kHz sampling rate, one channel (mono), no volume change.
	 */
	OGG_MONO_44KHZ(new Attributes("ogg", "libvorbis", 44100, 1)),
	/**
	 * Ogg, 44.1kHz sampling rate, two channels (stereo), no volume change.
	 */
	OGG_STEREO_44KHZ(new Attributes("ogg", "libvorbis", 44100, 2)),
	/**
	 * Flac, 44.1kHz sampling rate, two channels (stereo), no volume change.
	 */
	FLAC_STEREO_44KHZ(new Attributes("flac", "flac", 44100, 2)),
	/**
	 * Flac, 44.1kHz sampling rate, one channel (mono), no volume change.
	 */
	FLAC_MONO_44KHZ(new Attributes("flac", "flac", 44100, 1)),
	/**
	 * MP3, 320kb/s bit rate, 44.1kHz sampling rate, one channels (mono), no
	 * volume change.
	 */
	MP3_320KBS_MONO_44KHZ(new Attributes("mp3", "libmp3lame", 44100, 1, 320000)),
	/**
	 * MP3, 320kb/s bit rate, 44.1kHz sampling rate, two channels (stereo), no
	 * volume change.
	 */
	MP3_320KBS_STEREO_44KHZ(new Attributes("mp3", "libmp3lame", 44100, 2, 320000)),
	/**
	 * MP3, 192kb/s bit rate, 44.1kHz sampling rate, one channels (mono), no
	 * volume change.
	 */
	MP3_192KBS_MONO_44KHZ(new Attributes("mp3", "libmp3lame", 44100, 1, 192000)),
	/**
	 * MP3, 192kb/s bit rate, 44.1kHz sampling rate, two channels (stereo), no
	 * volume change.
	 */
	MP3_192KBS_STEREO_44KHZ(new Attributes("mp3", "libmp3lame", 44100, 2, 192000)),
	/**
	 * MP3, 128kb/s bit rate, 44.1kHz sampling rate, one channels (mono), no
	 * volume change.
	 */
	MP3_128KBS_MONO_44KHZ(new Attributes("mp3", "libmp3lame", 44100, 1, 128000)),
	/**
	 * MP3, 128kb/s bit rate, 44.1kHz sampling rate, two channels (stereo), no
	 * volume change.
	 */
	MP3_128KBS_STEREO_44KHZ(new Attributes("mp3", "libmp3lame", 44100, 2, 128000)),
	/**
	 * WAV PCM Signed 16 bit Little Endian, one channel (mono), 22050Hz sampling
	 * rate, no volume change.
	 */
	WAV_PCM_S16LE_MONO_22KHZ(new Attributes("wav", "pcm_s16le", 22050, 1)),
	/**
	 * WAV PCM Signed 16 bit Little Endian, one channel (mono), 22050Hz sampling
	 * rate, no volume change.
	 */
	WAV_PCM_S16LE_STEREO_22KHZ(new Attributes("wav", "pcm_s16le", 22050, 2)),
	/**
	 * WAV PCM Signed 16 bit Little Endian, one channel (mono), 44.1kHz sampling
	 * rate, no volume change.
	 */
	WAV_PCM_S16LE_MONO_44KHZ(new Attributes("wav", "pcm_s16le", 44100, 1)),
	/**
	 * WAV PCM Signed 16 bit Little Endian, two channel (stereo), 44.1kHz
	 * sampling rate, no volume change.
	 */
	WAV_PCM_S16LE_STEREO_44KHZ(new Attributes("wav", "pcm_s16le", 44100, 2));

	final Attributes attributes;

	private DefaultAttributes(Attributes newAttributes) {
		attributes = newAttributes;
	}

	public Attributes getAttributes() {
		return attributes;
	}

}
