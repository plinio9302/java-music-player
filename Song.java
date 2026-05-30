import java.util.Objects;

/**
 * Represents a single song with its metadata and the path to its audio file.
 *
 * <p>Two {@code Song} objects are considered equal if they share the same
 * title and artist (case-sensitive). Duration and file path are intentionally
 * excluded from equality so that the same logical song can be located
 * regardless of where it is stored on disk.</p>
 *
 * @author Plinio Durango
 * @version 2.0
 */
public class Song {

    private String title;
    private String artist;
    /** Duration in seconds. */
    private int    duration;
    /** Absolute path to the audio file (WAV or AIFF). */
    private String absoluteFilePath;

    /**
     * Constructs a Song with all fields populated.
     *
     * @param title            the song title
     * @param artist           the performing artist
     * @param duration         duration in seconds
     * @param absoluteFilePath absolute path to the audio file
     */
    public Song(String title, String artist, int duration, String absoluteFilePath) {
        this.title            = title;
        this.artist           = artist;
        this.duration         = duration;
        this.absoluteFilePath = absoluteFilePath;
    }

    // ----------------------------------------------------------------
    // Setters
    // ----------------------------------------------------------------

    /** @param title the new song title */
    public void setTitle(String title)                       { this.title = title; }

    /** @param artist the new artist name */
    public void setArtist(String artist)                     { this.artist = artist; }

    /** @param duration the new duration in seconds */
    public void setDuration(int duration)                    { this.duration = duration; }

    /** @param absoluteFilePath the new absolute file path */
    public void setAbsoluteFilePath(String absoluteFilePath) { this.absoluteFilePath = absoluteFilePath; }

    // ----------------------------------------------------------------
    // Getters
    // ----------------------------------------------------------------

    /** @return the song title */
    public String getTitle()            { return title; }

    /** @return the artist name */
    public String getArtist()           { return artist; }

    /** @return the duration in seconds */
    public int getDuration()            { return duration; }

    /** @return the absolute path to the audio file */
    public String getAbsoluteFilePath() { return absoluteFilePath; }

    // ----------------------------------------------------------------
    // Object overrides
    // ----------------------------------------------------------------

    /**
     * Two songs are equal if they share the same title and artist.
     * Duration and file path are excluded from the comparison.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)                  return true;
        if (!(obj instanceof Song other)) return false;
        return Objects.equals(title, other.title)
            && Objects.equals(artist, other.artist);
    }

    /**
     * Hash code consistent with {@link #equals}: based on title and artist only.
     */
    @Override
    public int hashCode() {
        return Objects.hash(title, artist);
    }

    /**
     * Returns a human-readable multi-line string with all song details.
     *
     * @return formatted song information
     */
    @Override
    public String toString() {
        return String.format(
            "Title: %s%nArtist: %s%nDuration: %d s%nFile: %s",
            title, artist, duration, absoluteFilePath);
    }
}
