import java.util.InputMismatchException;
import java.util.Scanner;
import javax.swing.SwingUtilities;

/**
 * Main class provides the menu-driven interface for managing songs, playlists, 
 * and controlling audio playback. Also supports launching the Swing GUI.
 *
 * Features:
 *  - Load playlists from CSV files.
 *  - Manipulate songs using a custom linked list.
 *  - Manage and play songs through a playlist wrapper.
 *  - Control audio (play, stop, pause, resume, volume, etc.).
 *  - Search for songs and view playlist statistics.
 *  - Save/export playlist to CSV.
 *  - Launch GUI that displays and plays the current playlist.
 */
public class Main {
    /** Shared scanner for reading user input across menus. */
    private static Scanner input = new Scanner(System.in);

    /**
     * Entry point of the program. Displays the main menu and
     * dispatches control to the appropriate submenu based on user choice.
     */
    public static void main(String[] args){
        DoublyLinkedSongList dlList = new DoublyLinkedSongList();   // core data structure (singly)
        PlayListDL playList = new PlayListDL(dlList);       // wrapper around dlList
        AudioPlayer player = new AudioPlayer();             // handles playback

        while (true) {
            printMenu();                     // display top menu
            int choice = intValidation();    // validate numeric input

            // enforce valid range (0 = GUI; 1..7 = CLI options)
            if (choice < 0 || choice > 7) {
                System.out.println("Please type numbers from 0 to 7");
                continue; // back to menu
            }

            if (choice == 7) { // quit
                System.out.println("Quitting");
                break;
            }

            // ---------- Launch GUI ----------
            if (choice == 0) {

                // Launch the GUI on the EDT
                final DoublyLinkedSongList snapshot = dlList;    // Making a final reference
                SwingUtilities.invokeLater(() -> {
                    MusicPlayerGUI view = new MusicPlayerGUI();
                    view.setPlaylistData(snapshot);
                    view.setVisible(true);
                });

                // Return to CLI main menu after launching GUI window
                continue;
            }

            // ---------- Submenus ----------
            if(choice == 1) {        // File operations
                while(true) {
                    fileOperations(); // display submenu
                    int usersInput = intValidation();
                    if (usersInput < 1 || usersInput > 5) {
                        System.out.println("Please type numbers from 1 to 5");
                        continue;
                    } else if (usersInput == 5) { // exit submenu
                        System.out.println("Back to the main menu");
                        break;
                    }

                    // load a playlist from one CSV file
                    if (usersInput == 1){
                        System.out.println("Please type the nam of the file");
                        String fileNAme = stringValidation();
                        dlList = FileUtils.loadPlayListDLFromCSV(fileNAme);
                        if(!dlList.isEmpty()){
                            System.out.println("Your playlist has been successfully load ");
                        }
                        playList = new PlayListDL(dlList); // refresh playlist wrapper
                    }
                    // load multiple CSVs into one linked list
                    else if(usersInput == 2) {
                        int totalAdded = FileUtils.loadMultipleCSVsAndCombine(dlList, input);
                        playList = new PlayListDL(dlList);
                        System.out.println("Added " + totalAdded + " songs. Total: " + dlList.size());
                    }
                    // validate if files exist on disk
                    else if(usersInput ==3) {
                        int totalMissing = FileUtils.validateAudioFiles(dlList);
                        System.out.println("Total Missing/invalid: " + totalMissing + ".");
                    }
                    // append songs from another CSV
                    else if(usersInput == 4) {
                        System.out.print("CSV path: ");
                        String path = input.nextLine().trim();

                        DoublyLinkedSongList loaded = FileUtils.loadPlayListDLFromCSV(path);
                        DoublyLinkedSongNode current = loaded.getHeadNode();
                        int addedSongs = 0;
                        while(current != null) {
                            dlList.addLast(current.getSong());
                            current = current.getNext();
                            addedSongs++;
                        }
                        playList = new PlayListDL(dlList);

                        System.out.println("Appended " + addedSongs + " song(s) from " + path);
                        System.out.println("playList now has " + dlList.size() + " song(s).");
                    }
                }
            }

            else if(choice == 2){    // dlList operations (low-level)
                while(true) {
                    DoublyLinkedSongListOperation(); // print options
                    int usersInput = intValidation();
                    if (usersInput < 1 || usersInput > 13) {
                        System.out.println("Please type numbers from 1 to 13");
                        continue;
                    } else if (usersInput == 13) {
                        System.out.println("Back to the main menu");
                        break;
                    }

                    // add song to front of list
                    else if(usersInput == 1) {
                        Song song = promptSong();
                        if (song == null) continue;
                        dlList.addFirst(song);
                    }
                    // add song to end of list
                    else if(usersInput == 2) {
                        Song song = promptSong();
                        if (song == null) continue;
                        dlList.addLast(song);
                    }
                    // remove first song
                    else if(usersInput == 3){
                        System.out.println("Removed: " + dlList.removeFirst());
                    }
                    // remove last song
                    else if(usersInput == 4){
                        System.out.println("Removed: " + dlList.removeLast());
                    }
                    // check size and emptiness
                    else if(usersInput == 5){
                        int size = dlList.size();
                        boolean isEmpty = dlList.isEmpty();
                        if(!isEmpty){
                            System.out.println("You have: " + size + " song/s in the list");
                        } else {
                            System.out.println("Your list is empty");
                        }
                    }
                    // print all songs
                    else if(usersInput == 6){
                        dlList.displayForward();
                    }
                    // get a song at a specific index
                    else if(usersInput == 7) {
                        System.out.print("Enter position of the song: ");
                        int index = intValidation();
                        try {
                            Song songAtIndex = dlList.get(index);
                            System.out.println("Song at index " + index + " is: " + songAtIndex.getTitle());
                        } catch(IndexOutOfBoundsException e){
                            System.out.println(e.getMessage());
                        }
                    }
                    // insert at specific position
                    else if(usersInput == 8) {
                        System.out.print("Enter position to insert: ");
                        int index = intValidation();
                        Song newSong = promptSong();
                        if (newSong == null) continue;
                        try {
                            dlList.addAt(index, newSong);
                            System.out.println("Song inserted successfully!");
                        } catch (IndexOutOfBoundsException e) {
                            System.out.println("Invalid index: " + e.getMessage());
                        }
                    }
                    // remove at specific position
                    else if(usersInput == 9) {
                        System.out.print("Enter position to remove: ");
                        int index = intValidation();
                        try{
                            Song removedSong = dlList.removeAt(index);
                            System.out.println("Song removed successfully!");
                            System.out.println(removedSong);
                        } catch(IndexOutOfBoundsException e) {
                            System.out.println("Invalid index: " + e.getMessage());
                        }
                    }
                    // check if song exists
                    else if(usersInput == 10) {
                        Song newSong = promptSong();
                        if (newSong == null) continue;
                        boolean ok = dlList.contains(newSong);
                        if(ok){
                            System.out.println("This song is in the playlist");
                        } else{
                            System.out.println("This song is not in the playlist");
                        }
                    }
                    // find index of a song
                    else if(usersInput == 11) {
                        Song newSong = promptSong();
                        if (newSong == null) continue;
                        int position = dlList.indexOf(newSong);
                        if(position != -1) {
                            System.out.println("This song is in the position number: " + position);
                        } else {
                            System.out.println("This song was not found in the playlist");
                        }
                    }
                    // clear the entire list
                    else if(usersInput == 12) {
                        dlList.clear();
                    }
                }
            }

            else if(choice == 3){   // Playlist wrapper operations
                while(true){
                    playListBasics();
                    int usersInput = intValidation();
                    if (usersInput < 1 || usersInput > 8) {
                        System.out.println("Please type numbers from 1 to 8");
                        continue;
                    } else if (usersInput == 8) {
                        System.out.println("Back to the main menu");
                        break;
                    }

                    if(usersInput == 1) {   // add song to playlist
                        Song song = promptSong();
                        if (song == null) continue;
                        playList.addLast(song);
                    }
                    else if(usersInput == 2) {   // display all songs
                        playList.displayPlayListDL();
                    }
                    else if(usersInput == 3) {   // show total duration
                        String totalDuration = playList.getTotalDuration();
                        System.out.println("The playlist lasts: " + totalDuration);
                    }
                    else if(usersInput == 4) {   // set current song
                        int index = intValidation();
                        try {
                            playList.jumpToSong(index, player);
                        }
                        catch(IndexOutOfBoundsException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    else if (usersInput == 5) {  // show current song
                        Song currentSong = playList.getCurrentSong();
                        if (currentSong == null) {
                            System.out.println("No song has been selected yet");
                        } else {
                            System.out.println("Your current song is: " 
                                + currentSong.getTitle() + " — " + currentSong.getArtist());
                        }
                    }
                    else if(usersInput == 6) {   // play next song
                        playList.nextSong(player);
                    }
                    else if(usersInput == 7) {   // check if playlist has songs
                        boolean hasSongs = playList.hasSongs();
                        if(hasSongs){
                            System.out.println("This playlist has at least one song");
                        } else{
                            System.out.println("This playlist is empty");
                        }
                    }
                }
            }

            else if(choice == 4) { // Audio controls submenu
                handleAudioControls(playList, player);
            }

            else if(choice == 5){  // Search & stats submenu
                while(true) {
                    playListSearchAndStats();
                    int usersInput = intValidation();
                    if(usersInput < 1 || usersInput > 4) {
                        System.out.println("Please type numbers from 1 to 4");
                        continue;
                    } else if (usersInput == 4) {
                        System.out.println("Back to the main menu");
                        break;
                    }
                    else if(usersInput == 1){  // search song by title
                        String titleSong = stringValidation();
                        Song foundSong = playList.findSongByTitle(titleSong);
                        if(foundSong != null){
                            System.out.println("Song Found");
                            System.out.println(foundSong);
                        } else {
                            System.out.println("Song not Found"); 
                        }
                    }
                    else if (usersInput == 2) { // search by artist
                        System.out.print("Artist: ");
                        String artistName = stringValidation();
                        PlayListDL playListByArtist = playList.findSongsByArtist(artistName);
                        if (playListByArtist != null && !playListByArtist.getPlayListDL().isEmpty()) {
                            System.out.println("Songs by " + artistName + ":");
                            playListByArtist.displayPlayListDL();
                        } else {
                            System.out.println("Artist not found");
                        }
                    }
                    else if(usersInput == 3) {  // playlist statistics
                        playList.getPlayListDLStatistics();
                    }
                }
            }

            else if (choice == 6) { // Save/export submenu
                saveAndExport();
                while(true) {
                    int usersInput = intValidation();
                    if(usersInput <1 || usersInput > 2) {
                        System.out.println("Please type numbers from 1 to 2");
                        continue;
                    } else if (usersInput == 2) {
                        System.out.println("Back to the main menu");
                        break;
                    }
                    else if(usersInput ==1) { // save as CSV
                        System.out.print("Saving file as: ");
                        String saveFileNAme = stringValidation();
                        FileUtils.savePlaylistToCSV(saveFileNAme, dlList);
                    }
                }
            }
        }
    }

    /**
     * Convert a DoublyLinkedSongList (singly) to a DoublyLinkedSongList
     * so the GUI can use bidirectional traversal.
     */
    private static DoublyLinkedSongList toDoublyLinked(DoublyLinkedSongList sll) {
        DoublyLinkedSongList dll = new DoublyLinkedSongList();
        // Walk the singly list via nodes to avoid extra get(i) calls
        DoublyLinkedSongNode cur = sll.getHeadNode();
        while (cur != null) {
            dll.addLast(cur.getSong());
            cur = cur.getNext();
        }
        return dll;
    }

    /**
     * Audio controls submenu loop.
     * Lets the user control playback using the shared AudioPlayer instance.
     */
    private static void handleAudioControls(PlayListDL playlist, AudioPlayer player) {
        while (true) {
            audioControls();              // show audio controls menu
            int choice = intValidation(); // read validated choice

            // guard invalid range
            if (choice < 1 || choice > 8) {
                System.out.println("Please type numbers from 1 to 8");
                continue;
            }
            // return to main menu
            if (choice == 8) { 
                System.out.println("Back to the main menu");
                break;
            }

            // dispatch actions
            switch (choice) {
                case 1: // play current song
                    playlist.playCurrentSong(player);
                    player.displayCurrentSongInfo();
                    break;

                case 2: // stop playback and reset position
                    playlist.stopPlayback(player);
                    break;

                case 3: // pause playback (remember position)
                    player.pauseAudio();
                    break;

                case 4: // resume from paused position
                    player.resumeAudio();
                    break;

                case 5: { // set volume from LOW/MED/HIGH
                    System.out.print("Volume (LOW | MED | HIGH): ");
                    String lvl = stringValidation().trim().toUpperCase();
                    int pct;
                    switch (lvl) {
                        case "LOW":  pct = 30; break;
                        case "MED":  pct = 60; break;
                        case "HIGH": pct = 90; break;
                        default:
                            System.out.println("Unknown level. Use LOW, MED, or HIGH.");
                            continue; // reprompt inside audio menu
                    }
                    try {
                        player.setVolumePercent(pct);
                        System.out.println("Volume set to " + lvl + " (" + pct + "%)");
                    } catch (IllegalStateException ex) {
                        // thrown if no clip is currently loaded
                        System.out.println("Cannot set volume: " + ex.getMessage());
                    }
                    break;
                }

                case 6: // play sequentially from the first song
                    playlist.playFromStart(player);
                    break;

                case 7: // pick and play a random song
                    playlist.playRandom(player);
                    break;
            }
        }
    }

    // ------------------------- Input helpers -------------------------

    /** Reads an integer from the shared Scanner, reprompting until valid. */
    public static int intValidation() {
        while (true) {
            try {
                int n = input.nextInt();
                input.nextLine(); // consume leftover newline
                return n;
            } catch (InputMismatchException e) {
                System.out.println("Must enter a number");
                input.next();      // discard invalid token
                System.out.print(">> ");
            }
        }
    }

    /** Reads a full line of text from the shared Scanner, reprompting if needed. */
    public static String stringValidation() {
        while (true) {
            try {
                String s = input.nextLine();
                return s;
            } catch (InputMismatchException e) {
                System.out.println("Must enter a String");
                input.next();      // discard invalid token
                System.out.print(">> ");
            }
        }
    }

    /**
     * Prompts the user for Song fields and constructs a Song.
     * Accepts duration as "mm:ss" or just seconds. Returns null if invalid format.
     */
    public static Song promptSong() {
        System.out.print("Title: ");
        String title = stringValidation();

        System.out.print("Artist: ");
        String artist = stringValidation();

        System.out.print("Duration (mm:ss or seconds): ");
        String d = input.nextLine().trim();
        int duration = parseFlexibleDuration(d);
        if (duration < 0) {
            System.out.println("Invalid duration format. Use mm:ss or seconds.");
            return null; // caller should handle null
        }

        System.out.print("Absolute file path: ");
        String path = stringValidation();

        return new Song(title, artist, duration, path);
    }

    /**
     * Parses a duration entered as "mm:ss" or plain seconds.
     * Returns -1 if the input format is invalid.
     */
    public static int parseFlexibleDuration(String s) {
        if (s.contains(":")) {
            String[] p = s.split(":");
            if (p.length == 2) {
                return Integer.parseInt(p[0].trim()) * 60 + Integer.parseInt(p[1].trim());
            } else {
                return -1;
            }
        }
        return Integer.parseInt(s.trim());
    }

    // ------------------------- Menus (printing only) -------------------------

    /** Prints the top-level menu options. */
    public static void printMenu() {
        System.out.println("Options:");
        System.out.println("[0] Launch GUI (use current playlist)         [C]");
        System.out.println("[1] File Operations");
        System.out.println("[2] DoublyLinkedSongList Operations");
        System.out.println("[3] Playlist Basics (uses DoublyLinkedSongList)");
        System.out.println("[4] Audio Controls");
        System.out.println("[5] Playlist Search & Stats");
        System.out.println("[6] Save / Export");
        System.out.println("[7] Quit");
        System.out.print(">> ");
    }

    /** Prints the file operations submenu. */
    public static void fileOperations() {
        System.out.println("Options:");
        System.out.println("[1] Load playlist from CSV (single file)       [C]");
        System.out.println("[2] Load multiple CSV files and combine        [A]");
        System.out.println("[3] Validate audio file existence for songs    [B]");
        System.out.println("[4] Handle/report malformed CSV rows           [B]");
        System.out.println("[5] Go back");
        System.out.print(">> ");
    }

    /** Prints the low-level DoublyLinkedSongList operations submenu. */
    public static void DoublyLinkedSongListOperation() {
        System.out.println("[1] addFirst(Song)                              [C]");
        System.out.println("[2] addLast(Song)                               [C]");
        System.out.println("[3] removeFirst()                               [C]");
        System.out.println("[4] removeLast()                                [C]");
        System.out.println("[5] size() / isEmpty()                          [C]");
        System.out.println("[6] display()                                   [C]");
        System.out.println("[7] get(index)                                  [B]");
        System.out.println("[8] addAt(index, Song)                          [B]");
        System.out.println("[9] removeAt(index)                             [B]");
        System.out.println("[10] contains(Song)                             [B]");
        System.out.println("[11] indexOf(Song)                              [B]");
        System.out.println("[12] clear()                                    [B]");
        System.out.println("[13] Go back");
        System.out.print(">> ");
    }

    /** Prints the playlist wrapper (high-level) operations submenu. */
    public static void playListBasics() {
        System.out.println("Options:");
        System.out.println("[1] addSong(Song)  (adds to end)                [C]");
        System.out.println("[2] displayPlayListDL()                           [C]");
        System.out.println("[3] getTotalDuration()  (mm:ss)                 [C]");
        System.out.println("[4] setCurrentSong(index)                       [C]");
        System.out.println("[5] getCurrentSong()                            [C]");
        System.out.println("[6] nextSong()                                  [C]");
        System.out.println("[7] hasSongs()");
        System.out.println("[8] Go back");
        System.out.print(">> ");
    }

    /** Prints the audio controls submenu. */
    public static void audioControls() {
        System.out.println("Options:");
        System.out.println("[1] playCurrentSong()                           [C]");
        System.out.println("[2] stopPlayback()                              [C]");
        System.out.println("[3] pause()                                     [B]");
        System.out.println("[4] resume()                                    [B]");
        System.out.println("[5] setVolume (LOW | MED | HIGH)                [B]");
        System.out.println("[6] playFromStart()                             [B]");
        System.out.println("[7] playRandom()                                [B]");
        System.out.println("[8] Go back");
        System.out.print(">> ");
    }

    /** Prints the search & statistics submenu for the playlist. */
    private static void playListSearchAndStats(){
        System.out.println("Options:");
        System.out.println("[1] findSongByTitle(title) → Song               [B]");
        System.out.println("[2] findSongsByArtist(artist) → new Playlist    [B]");
        System.out.println("[3] getPlaylistStatistics()                     [B]");
        System.out.println("[4] Go back");
        System.out.print(">> ");
    }

    /** Prints the save/export submenu. */
    public static void saveAndExport() {
        System.out.println("[1] savePlaylistToCSV(filename)                 [C]");
        System.out.println("[2] Go back");
        System.out.print(">> ");
    }
}