/**
 * Represents a single song with metadata and file path.
 * Fields: title, artist, duration (in seconds), and absolute file path.
 */
public class Song {
    private String title;
    private String artist;
    private int duration;              // duration in seconds
    private String absoluteFilePath;   // absolute path to the audio file

    /**
     * Constructor to create a Song object with all details.
     */
    public Song(String title, String artist, int duration, String absoluteFilePath) {
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.absoluteFilePath = absoluteFilePath;
    }

    // ----------------------- Setters -----------------------

    /** Set song title */
    public void setTitle(String title){
        this.title = title;
    }

    /** Set song artist */
    public void setArtist(String artist){
        this.artist = artist;
    }

    /** Set song duration in seconds */
    public void setDuration(int duration){
        this.duration = duration;
    }

    /** Set absolute file path for the audio file */
    public void setAbsoluteFilePath(String absoluteFilePath){
        this.absoluteFilePath = absoluteFilePath;
    }

    // ----------------------- Getters -----------------------

    /** Get song title */
    public String getTitle(){
        return this.title;
    }

    /** Get song artist */
    public String getArtist(){
        return this.artist;
    }

    /** Get song duration (in seconds) */
    public int getDuration(){
        return this.duration;
    }

    /** Get absolute file path */
    public String getAbsoluteFilePath(){
        return this.absoluteFilePath;
    }

    // ----------------------- Overrides -----------------------

    /**
     * Two songs are considered equal if they have the same title and artist.
     * (Duration and file path are ignored for equality check.)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Song)) {
            return false;
        }
        Song s = (Song) obj;
        return this.getTitle().equals(s.getTitle()) &&
               this.getArtist().equals(s.getArtist());
    }

    /**
     * Return a human-readable string with all song details.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("\nTitle: ").append(getTitle());
        s.append("\nArtist: ").append(getArtist());
        s.append("\nDuration: ").append(getDuration()).append(" seconds");
        s.append("\nFile Path: ").append(getAbsoluteFilePath());
        return s.toString();
    }
}