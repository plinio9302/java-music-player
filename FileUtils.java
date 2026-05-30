import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Utility helpers for reading and writing playlists to CSV, and for
 * validating that referenced audio files exist on disk.
 *
 * <p>Expected CSV format (comma-delimited, with a header row):</p>
 * <pre>
 * Title,Artist,Duration,FilePath
 * Clair de Lune,Debussy,5:24,/music/clair_de_lune.wav
 * </pre>
 *
 * <p>Duration may be in {@code mm:ss} format or as plain seconds.
 * Lines with fewer than 4 columns, empty required fields, or an
 * unparseable duration are skipped with a descriptive warning.</p>
 *
 * @author Plinio Durango
 * @version 2.0
 */
public class FileUtils {

    // ----------------------------------------------------------------
    // Save
    // ----------------------------------------------------------------

    /**
     * Saves all songs from a linked list to a CSV file, overwriting any
     * existing file. Writes a header row followed by one row per song.
     * Duration is written in {@code mm:ss} format.
     *
     * @param filename the output file name
     * @param dlList   the list of songs to save
     */
    public static void savePlaylistToCSV(String filename, DoublyLinkedSongList dlList) {
        try (PrintWriter out = new PrintWriter(new FileOutputStream(filename))) {
            out.println("Title,Artist,Duration,FilePath");

            DoublyLinkedSongNode cur = dlList.getHeadNode();
            while (cur != null) {
                Song s = cur.getSong();
                int   m = s.getDuration() / 60;
                int   sec = s.getDuration() % 60;
                out.printf("%s,%s,%d:%02d,%s%n",
                    s.getTitle(), s.getArtist(), m, sec, s.getAbsoluteFilePath());
                cur = cur.getNext();
            }

            System.out.println("Playlist saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving playlist: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Load
    // ----------------------------------------------------------------

    /**
     * Loads a playlist from a single CSV file into a new
     * {@link DoublyLinkedSongList}.
     *
     * <p>The first line is treated as a header and skipped. Malformed
     * rows are logged to {@code stderr} and skipped; parsing continues
     * with the remaining rows.</p>
     *
     * @param fileName path to the CSV file
     * @return a new {@link DoublyLinkedSongList} populated with valid songs
     */
    public static DoublyLinkedSongList loadPlayListDLFromCSV(String fileName) {
        DoublyLinkedSongList list = new DoublyLinkedSongList();

        try (Scanner sc = new Scanner(new FileReader(fileName))) {
            if (!sc.hasNextLine()) {
                System.err.println("Warning: file is empty: " + fileName);
                return list;
            }

            sc.nextLine(); // skip header
            int lineNumber = 1;

            while (sc.hasNextLine()) {
                lineNumber++;
                String line = sc.nextLine().trim();

                if (line.isEmpty()) {
                    System.err.println(warn(fileName, lineNumber, "empty line (skipped)"));
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 4) {
                    System.err.println(warn(fileName, lineNumber,
                        "expected 4 columns (Title,Artist,Duration,FilePath), found " + parts.length));
                    continue;
                }

                String title   = parts[0].trim();
                String artist  = parts[1].trim();
                String durStr  = parts[2].trim();
                String path    = parts[3].trim();

                if (title.isEmpty() || artist.isEmpty() || path.isEmpty()) {
                    System.err.println(warn(fileName, lineNumber, "empty required field"));
                    continue;
                }

                int duration;
                try {
                    duration = parseDurationToSeconds(durStr);
                } catch (NumberFormatException ex) {
                    System.err.println(warn(fileName, lineNumber,
                        "unparseable duration \"" + durStr + "\""));
                    continue;
                }

                list.addLast(new Song(title, artist, duration, path));
            }

        } catch (IOException e) {
            System.err.println("Error reading CSV: " + e.getMessage());
        }

        return list;
    }

    // ----------------------------------------------------------------
    // Multi-file load
    // ----------------------------------------------------------------

    /**
     * Prompts the user for CSV file paths (one per line) and appends all
     * songs found into the provided list. Enter {@code "Exit"} to stop.
     *
     * @param dlList the destination list
     * @param input  the Scanner to read paths from
     * @return the total number of songs appended
     */
    public static int loadMultipleCSVsAndCombine(DoublyLinkedSongList dlList, Scanner input) {
        System.out.println("Enter CSV paths one per line. Type 'Exit' to finish:");
        int added = 0;

        while (true) {
            String path = input.nextLine().trim();
            if (path.equalsIgnoreCase("Exit")) break;

            DoublyLinkedSongList loaded = loadPlayListDLFromCSV(path);
            DoublyLinkedSongNode cur = loaded.getHeadNode();
            while (cur != null) {
                dlList.addLast(cur.getSong());
                cur = cur.getNext();
                added++;
            }
        }
        return added;
    }

    // ----------------------------------------------------------------
    // Validation
    // ----------------------------------------------------------------

    /**
     * Checks that every song's file path points to a real file on disk.
     * Prints a line for each missing or invalid entry.
     *
     * @param dlList the list to validate
     * @return the number of songs with missing or invalid files
     */
    public static int validateAudioFiles(DoublyLinkedSongList dlList) {
        int missing = 0;
        DoublyLinkedSongNode cur = dlList.getHeadNode();
        while (cur != null) {
            Song s = cur.getSong();
            if (!new File(s.getAbsoluteFilePath()).isFile()) {
                System.err.println("Missing: " + s.getTitle() + " -> " + s.getAbsoluteFilePath());
                missing++;
            }
            cur = cur.getNext();
        }
        return missing;
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    /**
     * Parses a duration string into total seconds.
     *
     * <p>Accepts two formats:
     * <ul>
     *   <li>{@code mm:ss} — e.g. {@code "3:45"} → 225 seconds</li>
     *   <li>Plain integer — e.g. {@code "225"} → 225 seconds</li>
     * </ul></p>
     *
     * @param duration the duration string
     * @return total duration in seconds
     * @throws NumberFormatException if the string cannot be parsed
     */
    public static int parseDurationToSeconds(String duration) {
        duration = duration.trim();
        if (duration.contains(":")) {
            String[] parts = duration.split(":");
            if (parts.length != 2) throw new NumberFormatException("Expected mm:ss, got: " + duration);
            return Integer.parseInt(parts[0].trim()) * 60 + Integer.parseInt(parts[1].trim());
        }
        // Plain seconds fallback
        return Integer.parseInt(duration);
    }

    /** Formats a standardized warning with file name and line number. */
    private static String warn(String file, int line, String msg) {
        return "Warning (" + file + ":" + line + "): " + msg + ".";
    }
}
