import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * AudioPlayer is a helper class that uses Java’s built-in
 * {@link javax.sound.sampled} API to load, play, pause, resume, stop,
 * and adjust volume for audio clips (WAV/AIFF).
 *
 * <p>It is designed to work with {@link Song} objects, which provide
 * an absolute file path to the audio file.</p>
 */
public class AudioPlayer {

    /** The Clip object that actually plays the audio. */
    private Clip clip;

    /** The currently loaded audio data stream. */
    private AudioInputStream audioStream;

    /** Where playback is currently paused (microseconds). */
    private Long currentFrame = 0L;

    /** The Song object currently being played. */
    private Song currentSong;

    /**
     * Loads and plays the given song from the beginning,
     * or resumes from the last paused position if already loaded.
     *
     * @param song the Song to play (must provide a valid absolute file path)
     */
    public void playAudio(Song song) {
        try {
            // If no song is loaded, or a new song is requested
            if (currentSong == null || !currentSong.equals(song)) {
                // Close old clip if something is already open
                if (clip != null && clip.isOpen()) clip.close();

                // Load new audio file
                File audioFile = new File(song.getAbsoluteFilePath());
                audioStream = AudioSystem.getAudioInputStream(audioFile);
                clip = AudioSystem.getClip();
                clip.open(audioStream);

                currentSong = song;
                currentFrame = 0L; // reset playback
            }

            // Resume from currentFrame (0 if fresh)
            clip.setMicrosecondPosition(currentFrame);
            clip.start();
            displayCurrentSongInfo();

        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported format: " + song.getAbsoluteFilePath());
        } catch (LineUnavailableException | IOException e) {
            System.err.println("Could not play: " + song.getAbsoluteFilePath()
                               + " (" + e.getMessage() + ")");
        }
    }

    /**
     * Stops playback and resets position back to the start of the song.
     * Prints the name of the song that was stopped.
     */
    public void stopAudio() {
        if (clip != null) {
            clip.stop();
            clip.setMicrosecondPosition(0);
            currentFrame = 0L;
            System.out.println("Stopped: " +
                (currentSong != null ? currentSong.getTitle() : "No song"));
        }
    }

    /**
     * Pauses playback and remembers the position so it can be resumed later.
     * Prints the timestamp where playback was paused.
     */
    public void pauseAudio() {
        if (clip != null && clip.isRunning()) {
            currentFrame = clip.getMicrosecondPosition();
            clip.stop();
            System.out.println("Paused at " + (currentFrame / 1_000_000.0) + " sec");
        }
    }

    /**
     * Resumes playback from the last paused position.
     * Prints the title of the resumed song.
     */
    public void resumeAudio() {
        if (clip != null && !clip.isRunning()) {
            clip.setMicrosecondPosition(currentFrame);
            clip.start();
            System.out.println("Resumed: " +
                (currentSong != null ? currentSong.getTitle() : "Unknown"));
        }
    }

    /**
     * Checks if audio is currently playing.
     *
     * @return true if audio is playing, false otherwise
     */
    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    /**
     * Sets playback volume based on a percentage (0–100).
     *
     * @param percent volume percentage (0 = mute, 100 = max)
     * @throws IllegalStateException if no clip is loaded
     */
    public void setVolumePercent(int percent) {
        if (clip == null) throw new IllegalStateException("No clip loaded");
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

        // Convert linear percent into decibel range
        float min = gain.getMinimum(); // usually -80.0 dB
        float max = gain.getMaximum(); // usually +6.0 dB
        float range = max - min;
        float value = min + (range * percent / 100.0f);

        gain.setValue(value);
    }

    /**
     * Displays information about the currently loaded song.
     * If no song is loaded, prints a placeholder message.
     */
    public void displayCurrentSongInfo() {
        if (currentSong != null) {
            System.out.println("Now playing: " +
                currentSong.getTitle() + " — " + currentSong.getArtist());
        } else {
            System.out.println("No song loaded.");
        }
    }

    /**
     * Closes the audio clip and releases system resources.
     * Should be called when exiting the program.
     */
    public void close() {
        if (clip != null) {
            clip.close();
        }
    }
}