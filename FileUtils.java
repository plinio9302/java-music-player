import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.io.File;

/**
 * Utility helpers for reading/writing playlists to CSV and for
 * validating referenced audio files on disk.
 *
 * CSV format expected (with header row):
 *   Title,Artist,Duration,FilePath
 *
 * Duration is parsed as mm:ss by parseDurationToSeconds(..).
 */
public class FileUtils {

    /**
     * Save all songs from a linked list to a CSV file (overwrites if exists).
     * Writes a header and then one row per song.
     */
    public static void savePlaylistToCSV(String filename, DoublyLinkedSongList dlList) {
        try (PrintWriter out = new PrintWriter(new FileOutputStream(filename))) {
            // CSV header
            out.println("Title,Artist,Duration,FilePath");

            // Walk the list and write each song
            DoublyLinkedSongNode current = dlList.getHeadNode();
            while (current != null) {
                Song s = current.getSong();

                // One CSV line: title, artist, duration(seconds), path
                out.printf("%s,%s,%d,%s%n",
                           s.getTitle(),
                           s.getArtist(),
                           s.getDuration(),
                           s.getAbsoluteFilePath());

                current = current.getNext(); // advance
            }

            System.out.println("Playlist data written to " + filename);
        } catch (IOException e) {
            System.out.println("Error writing Playlist data: " + e.getMessage());
        }
    }

    /**
     * Load a playlist from a CSV file into a new DoublyLinkedSongList.
     * Skips the header line, reports malformed rows with line numbers,
     * and continues parsing the rest.
     */
    public static DoublyLinkedSongList loadPlayListDLFromCSV(String fileName) {
        DoublyLinkedSongList list = new DoublyLinkedSongList();

        try (Scanner sc = new Scanner(new FileReader(fileName))) {
            // Empty file?
            if (!sc.hasNextLine()) {
                System.out.println("Warning: file is empty: " + fileName);
                return list;
            }

            // Read entire file for simple line-number tracking
            StringBuilder sb = new StringBuilder();
            while (sc.hasNextLine()) {
                sb.append(sc.nextLine()).append("\n");
            }

            String[] lines = sb.toString().split("\n");

            // Expect header on the first line and skip it (i = 1)
            for (int i = 1; i < lines.length; i++) {
                int lineNo = i + 1;               // human-friendly 1-based index
                String line = lines[i].trim();
                if (line.isEmpty()) {
                    System.out.println(warn(fileName, lineNo, "empty line (skipped)"));
                    continue;
                }

                // Split by comma; expect at least 4 fields
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    System.out.println(warn(fileName, lineNo,
                        "not enough columns (need 4: Title,Artist,Duration,FilePath)"));
                    continue;
                }

                String title  = parts[0].trim();
                String artist = parts[1].trim();
                String durStr = parts[2].trim();
                String path   = parts[3].trim();

                // Basic required-field checks
                if (title.isEmpty() || artist.isEmpty() || path.isEmpty()) {
                    System.out.println(warn(fileName, lineNo, "empty required field(s)"));
                    continue;
                }

                // Parse mm:ss -> seconds
                int durationSecs;
                try {
                    durationSecs = parseDurationToSeconds(durStr);
                } catch (NumberFormatException ex) {
                    System.out.println(warn(fileName, lineNo, "bad duration \"" + durStr + "\""));
                    continue;
                }

                // Build and append song
                Song song = new Song(title, artist, durationSecs, path);
                list.addLast(song);
            }

        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
        }

        return list;
    }

    /** Build a standardized warning message with file and line number. */
    private static String warn(String file, int lineNo, String msg) {
        return "Warning (" + file + ":" + lineNo + "): " + msg + ".";
    }

    /**
     * Parse "mm:ss" into total seconds.
     * @throws NumberFormatException if the format is not mm:ss or parts aren’t integers.
     */
    public static int parseDurationToSeconds(String duration) {
        String[] time = duration.split(":");
        int minutes = Integer.parseInt(time[0]);
        int seconds = Integer.parseInt(time[1]);
        return minutes * 60 + seconds;
    }

    /**
     * Read multiple CSV paths (typed by user) and append all found songs
     * into the provided linked list. Stops when the user types "Exit".
     * @return total number of appended songs.
     */
    public static int loadMultipleCSVsAndCombine(DoublyLinkedSongList dlList, Scanner input) {
        System.out.println("Enter CSV paths (one per line). Type 'Exit' to finish:");
        int addedCount = 0;

        while (true) {
            String path = input.nextLine().trim();
            if (path.equalsIgnoreCase("Exit")) break;

            // Load from one CSV and append to the existing list
            DoublyLinkedSongList fromCsv = FileUtils.loadPlayListDLFromCSV(path);
            DoublyLinkedSongNode cur = fromCsv.getHeadNode();
            while (cur != null) {
                dlList.addLast(cur.getSong());
                cur = cur.getNext();
                addedCount++;
            }
        }
        return addedCount;
    }

    /**
     * Checks that every song’s absolute file path points to a real file.
     * Prints a line for each missing/invalid file and returns how many were missing.
     */
    public static int validateAudioFiles(DoublyLinkedSongList dlList) {
        int missing = 0;                      // count how many are missing
        DoublyLinkedSongNode cur = dlList.getHeadNode();

        while (cur != null) {
            Song s = cur.getSong();
            File f = new File(s.getAbsoluteFilePath());

            // If the file doesn’t exist or isn’t a file, report it
            if (!f.isFile()) {
                System.out.println("Missing/invalid: " + s.getTitle() + " — " + s.getAbsoluteFilePath());
                missing++;                    // ✅ increment only when missing
            }

            cur = cur.getNext();
        }

        return missing;                       // ✅ returns number of missing files
    }
}