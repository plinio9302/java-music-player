/**
 * The PlayListDL class manages a collection of songs stored in a DoublyLinkedSongList.
 * It supports adding, searching, navigating, and controlling audio playback
 * with the help of the AudioPlayer class.
 */
public class PlayListDL {
    private DoublyLinkedSongList dlList;  
    private DoublyLinkedSongNode current;           // pointer to the "current" song
    private int currentIndex;

    /**
     * Construct a new PlayListDL backed by the given DoublyLinkedSongList.
     * @param dlList the linked dlList of songs
     */
    public PlayListDL(DoublyLinkedSongList dlList) {
        this.dlList = dlList;
        if(!dlList.isEmpty()){
            this.current = dlList.getHeadNode();
            this.currentIndex = 0;
        }
    }

    // ----------------- PlayListDL Basics -------------

    /**
     * Add a song to the end of the PlayListDL.
     * @param song the song to add
     */
    public void addLast(Song song) {
        dlList.addLast(song);
    }

    /**
     * Add a song to the begining of the PlayListDL.
     * @param song the song to add
     */
    public void addFirst(Song song) {
        dlList.addFirst(song);
    }

    public boolean hasNext() {

        return current != null && current.getNext() != null; 
    }

    public boolean hasPrevious() {
        return current != null && current.getPrev() != null;
    }

    /**
     * Display all songs in the PlayListDL to the console.
     */
    public void displayPlayListDL() {
        dlList.displayForward();
    }

    /**
     * Return the underlying DoublyLinkedSongList.
     * @return the linked dlList of songs
     */
    public DoublyLinkedSongList getPlayListDL() {
        return dlList;
    }

    /**
     * Return the current song, or null if none is set.
     * @return the current song
     */
    public Song getCurrentSong() {
        return (current != null) ? current.getSong() : null;
    }

    public int getCurrentIndex() {
        return this.currentIndex;
    }


    /**
     * Compute the total duration of the PlayListDL.
     * @return total duration formatted as mm:ss
     */
    public String getTotalDuration() {
        int counter = 0;
        DoublyLinkedSongNode node = dlList.getHeadNode();
        while (node != null) {
            counter += node.getSong().getDuration();
            node = node.getNext();
        }

        int minutes = counter / 60;
        int seconds = counter % 60;

        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Set the "current" song by index.
     * @param index the index of the song (0-based)
     */
    public void jumpToSong(int index, AudioPlayer player) {
        if (index < 0 || index >= dlList.size()) {
            throw new IndexOutOfBoundsException("Wrong Index: " + index);
        }

        DoublyLinkedSongNode node = dlList.nodeAt(index);
        current = node;
        currentIndex = index;
        player.playAudio(current.getSong());
    }


    /**
     * Check if the PlayListDL contains any songs.
     * @return true if non-empty, false otherwise
     */
    public boolean hasSongs() {
        return !dlList.isEmpty();
    }

    // ---------------- Audio Controls -----------------

    /**
     * Advance to the next song (if any) and start playback.
     * @param player the AudioPlayer used to play the song
     */
    public void nextSong(AudioPlayer player) {
        if(!ensureCurrent() || current.getNext() == null){
            System.out.println("No next song in the PlayListDL.");
            return ;
        }
        current = current.getNext();  // move forward
        currentIndex++;
        player.playAudio(current.getSong());

    }

    public void previousSong(AudioPlayer player) {

        if(!ensureCurrent() || current.getPrev() == null){
            System.out.println("No previous song in the PlayListDL.");
            return ;
        }

        current = current.getPrev();  // move backward
        currentIndex--;
        player.playAudio(current.getSong());

    }

    /** Ensures we have a current node (defaults to head). */
    private boolean ensureCurrent() {
        if(current == null && !dlList.isEmpty()){
            current = dlList.getHeadNode();
            currentIndex = 0;
        }
        return current != null ; 
    }

    /**
     * Stop audio playback using the given AudioPlayer.
     * @param player the AudioPlayer instance
     */
    public void stopPlayback(AudioPlayer player) {
        player.stopAudio();
    }

    /**
     * Play the current song. If no current song is set,
     * start with the first song in the PlayListDL.
     * @param player the AudioPlayer used to play the song
     */

    public void playCurrentSong(AudioPlayer player) {
        if (current == null) {
            // if nothing has been set, default to head
            current = dlList.getHeadNode();
        }

        if (current != null) {
            player.playAudio(current.getSong());
        } else {
            System.out.println("PlayListDL is empty. No song to play.");
        }
    }

    

    // ---------------- Search & Stats ----------------

    /**
     * Find a song by its title.
     * @param title the song title to search for
     * @return the matching Song, or null if not found
     */
    public Song findSongByTitle(String title) {
        if (dlList.isEmpty()) {
            return null;
        }

        DoublyLinkedSongNode current = dlList.getHeadNode();
        while (current != null) {
            String currentTitle = current.getSong().getTitle();
            if (currentTitle.equalsIgnoreCase(title)) {
                return current.getSong();
            }
            current = current.getNext();
        }

        return null;
    }

    /**
     * Find all songs by a given artist.
     * Returns a new PlayListDL containing only those songs.
     * @param artist the artist name
     * @return a new PlayListDL of songs by the artist
     */
    public PlayListDL findSongsByArtist(String artist) {
        DoublyLinkedSongList artistList = new DoublyLinkedSongList();

        DoublyLinkedSongNode current = dlList.getHeadNode();
        while (current != null) {
            String curArtist = current.getSong().getArtist();
            if (curArtist != null && curArtist.equalsIgnoreCase(artist)) {
                artistList.addLast(current.getSong());
            }
            current = current.getNext();
        }

        return new PlayListDL(artistList);
    }

    /**
     * Print basic statistics: total number of songs and total duration.
     */
    public void getPlayListDLStatistics() {
        int totalSongs = 0;
        int totalDuration = 0;

        DoublyLinkedSongNode current = dlList.getHeadNode();
        while (current != null) {
            totalSongs++;
            totalDuration += current.getSong().getDuration();
            current = current.getNext();
        }

        int minutes = totalDuration / 60;
        int seconds = totalDuration % 60;

        System.out.println("Total songs: " + totalSongs);
        System.out.println("Total duration: " + minutes + ":" + String.format("%02d", seconds));
    }

    // ---------------- Advanced Features ----------------

    /**
     * Start playback from the first song in the PlayListDL,
     * moving through each song sequentially.
     * @param player the AudioPlayer to play songs
     */
    public void playFromStart(AudioPlayer player) {
        current = dlList.getHeadNode(); // reset to first song
        while (current != null) {
            player.playAudio(current.getSong());

            // move to next song
            current = current.getNext();
        }
    }

    /**
     * Play a random song from the PlayListDL.
     * @param player the AudioPlayer to play the song
     */
    public void playRandom(AudioPlayer player) {
        if (dlList.isEmpty()) {
            System.out.println("PlayListDL is empty. Nothing to play.");
            return;
        }

        // random index [0, size)
        int index = (int) (Math.random() * dlList.size());

        DoublyLinkedSongNode node = dlList.getHeadNode();
        for (int i = 0; i < index; i++) {
            node = node.getNext();
        }

        current = node; // set current to that random node
        player.playAudio(current.getSong());
    }
}