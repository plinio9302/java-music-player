import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.sound.sampled.*;   // AudioInputStream, AudioFormat, AudioSystem

/**
 * MusicPlayerGUI
 * Minimal Swing GUI I built for the C-level requirements.
 * It shows a JList of songs, a "Now Playing" label, and basic buttons:
 * Play, Stop, Add Song, Remove.
 *
 * I’m using a DoublyLinkedSongList (dll) as my backing data structure
 * to match the Week 2 spec. The JList is kept in sync with dll.
 */
public class MusicPlayerGUI extends JFrame {

    // --- UI state / components ---
    // Swing model that backs the JList visually (what the user sees)
    private DefaultListModel<Song> listModel = new DefaultListModel<>();
    // The list widget (uses listModel above)
    private JList<Song> playlistList = new JList<>(listModel);
    // Status label that shows the current track
    private JLabel nowPlaying = new JLabel("Now Playing: —");
    // Basic transport
    private JButton btnPlay = new JButton("Play");
    private JButton btnStop = new JButton("Stop");
    // Add/remove songs via chooser and selection
    private JButton btnAdd = new JButton("Add Song");
    private JButton btnRemove = new JButton("Remove");
    // My simple audio backend (plays WAV/AIFF via Java Sound)
    private AudioPlayer audio = new AudioPlayer();

    // Underlying doubly-linked playlist data structure (not null after setPlaylistData or first Add)
    private DoublyLinkedSongList dll;

    /** Constructor: sets up the window, wires the UI, and initializes control states. */
    public MusicPlayerGUI() {
        super("Music Playlist — List View Only");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);              // meets the C-level minimum size spec
        setLocationRelativeTo(null);    // center the window on screen

        buildListUI();                  // build only the list + top bar for now
        wireSelectionToNowPlaying();    // when I click a row, update "Now Playing"
        wireControls();                 // Play/Stop buttons
        wireRemoveSong();               // Remove button
        wireAddSong();                  // Add button
        // wireDoubleClickPlay();       // (optional) I can enable double-click-to-play here

        updateControls();               // initialize enabled/disabled button states
    }

    /**
     * Builds the JList and the top control area (Now Playing + buttons).
     * I keep the layout simple: top bar + scrollable list.
     */
    private void buildListUI() {
        // Single selection is enough for this step
        playlistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom cell renderer so I control how each row looks without changing Song.toString()
        playlistList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                if (value instanceof Song s) {
                    // Show title — artist (duration in seconds)
                    label.setText(s.getTitle() + " — " + s.getArtist() + " (" + s.getDuration() + "s)");
                }
                return label;
            }
        });

        // Top bar has the Now Playing label on the left, buttons on the right
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(nowPlaying, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.add(btnPlay);
        controls.add(btnStop);
        controls.add(btnRemove);
        controls.add(btnAdd);
        topPanel.add(controls, BorderLayout.EAST);

        // The list itself goes inside a scroll pane
        JScrollPane scroll = new JScrollPane(playlistList);

        // Main layout: top bar + center list
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(scroll, BorderLayout.CENTER);
    }

    /**
     * When the user changes selection in the list (and the change is final),
     * update the Now Playing label to reflect the selected song (no auto-play).
     */
    private void wireSelectionToNowPlaying() {
        playlistList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateNowPlayingLabel(playlistList.getSelectedValue());
                updateControls(); // buttons may change state based on selection
            }
        });
    }

    /** Small helper that sets the text for the "Now Playing" label. */
    public void updateNowPlayingLabel(Song s) {
        if (s == null) {
            nowPlaying.setText("Now Playing: —");
        } else {
            nowPlaying.setText("Now Playing: " + s.getTitle() + " — " + s.getArtist());
        }
    }

    /**
     * Wires the Play and Stop buttons.
     * Play will use the selected song, or the first one if nothing is selected.
     * Stop tells the AudioPlayer to stop and resets UI state.
     */
    private void wireControls() {
        btnPlay.addActionListener(e -> {
            Song s = playlistList.getSelectedValue();
            if (s == null && !listModel.isEmpty()) {
                // If nothing is selected, default to the first list item
                playlistList.setSelectedIndex(0);
                s = playlistList.getSelectedValue();
            }
            if (s != null) {
                audio.playAudio(s);
                updateNowPlayingLabel(s);
            }
            updateControls();
        });

        btnStop.addActionListener(e -> {
            audio.stopAudio();
            updateControls();
        });
    }

    /**
     * Central place to enable/disable controls so the GUI gives visual feedback:
     * - Play enabled only when we have songs
     * - Stop reflects whether audio is actually playing
     * - Remove enabled only when there is a selection
     */
    private void updateControls() {
        boolean hasSongs = !listModel.isEmpty();
        btnPlay.setEnabled(hasSongs);
        btnStop.setEnabled(audio.isPlaying());
        btnRemove.setEnabled(playlistList.getSelectedIndex() >= 0);
    }

    /** Connects the Add button to the add-song flow below. */
    private void wireAddSong() {
        btnAdd.addActionListener(e -> onAddSong());
    }

    /**
     * Handles adding a song:
     * - Open a file chooser (wav/aiff)
     * - Build a Song object
     * - Ensure my DLL exists, append to it and to the list model
     * - Select the newly added row (I do NOT auto-play on add)
     */
    private void onAddSong() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Audio files (wav, aiff)", "wav", "aiff"));

        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File f = chooser.getSelectedFile();
        if (f == null || !f.isFile()) return;

        // Convert the file into a Song (title = filename without extension, artist = "Unknown")
        Song s = buildSongFromFile(f);

        // Ensure I have a backing DLL before adding the first song
        if (dll == null) dll = new DoublyLinkedSongList();

        // 1) Backing data structure
        dll.addLast(s);

        // 2) GUI list model
        listModel.addElement(s);

        // 3) Select the new row and refresh the label/state
        int newIndex = listModel.size() - 1;
        playlistList.setSelectedIndex(newIndex);
        updateNowPlayingLabel(s);
        updateControls();
    }

    /**
     * Build a Song from a file: I derive the title from the filename,
     * use "Unknown" for artist, and try to compute duration with Java Sound.
     */
    private Song buildSongFromFile(File f) {
        String name = f.getName();
        int dot = name.lastIndexOf('.');
        String title = (dot > 0 ? name.substring(0, dot) : name);
        String artist = "Unknown";
        int durationSec = computeDurationSeconds(f);
        return new Song(title, artist, durationSec, f.getAbsolutePath());
    }

    /**
     * Lightweight duration probe using AudioInputStream:
     * If this fails (unsupported or error), I return 0 seconds as a fallback.
     */
    private int computeDurationSeconds(File file) {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(file)) {
            AudioFormat fmt = ais.getFormat();
            long frames = ais.getFrameLength();
            if (fmt.getFrameRate() > 0 && frames > 0) {
                double seconds = frames / fmt.getFrameRate();
                return (int) Math.round(seconds);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    /** Hooks up the Remove button to remove the selected item (and from DLL if present). */
    private void wireRemoveSong() {
        btnRemove.addActionListener(e -> onRemoveSelected());
    }

    /**
     * Removes the currently selected song (if any).
     * Keeps the JList and the Now Playing label in a sane state after removal.
     * Also stops audio if the list becomes empty.
     */
    private void onRemoveSelected() {
        int idx = playlistList.getSelectedIndex();
        if (idx < 0) return; // nothing selected

        // Keep a reference to what I'm removing (useful for logging/debug)
        Song removed = listModel.get(idx);

        // Keep the backing structure in sync (if I’m maintaining dll here)
        if (dll != null) dll.removeAt(idx);

        // Remove from the visual list
        listModel.remove(idx);

        // If nothing left, clear selection/label and stop audio
        if (listModel.isEmpty()) {
            playlistList.clearSelection();
            updateNowPlayingLabel(null);
            if (audio.isPlaying()) audio.stopAudio();
            updateControls();
            return;
        }

        // Try to select the same index; if it was the last row, select the new last row
        int newIndex = Math.min(idx, listModel.size() - 1);
        playlistList.setSelectedIndex(newIndex);

        // Update the Now Playing label to reflect the new selection
        Song s = playlistList.getSelectedValue();
        updateNowPlayingLabel(s);

        updateControls();
    }

    /**
     * Optional convenience: double-clicking a list row immediately plays it,
     * updates selection and Now Playing label, and refreshes controls.
     */
    private void wireDoubleClickPlay() {
        playlistList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { // double click
                    int idx = playlistList.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        Song s = listModel.get(idx);
                        try {
                            audio.playAudio(s);
                            playlistList.setSelectedIndex(idx); // visually highlight the playing row
                            updateNowPlayingLabel(s);
                            updateControls();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(MusicPlayerGUI.this,
                                    "Could not play audio:\n" + ex.getMessage(),
                                    "Playback Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
    }

    /**
     * Loads the JList from a provided DoublyLinkedSongList (dll).
     * I clear the list model first, then push every Song in order.
     * If there are songs, I select the first one so the label shows something.
     */
    public void setPlaylistData(DoublyLinkedSongList dll) {
        this.dll = dll;
        listModel.clear();
        for (int i = 0; i < dll.size(); i++) {
            listModel.addElement(dll.get(i));
        }
        if (!listModel.isEmpty()) {
            playlistList.setSelectedIndex(0);
            updateNowPlayingLabel(listModel.get(0));
        } else {
            playlistList.clearSelection();
            updateNowPlayingLabel(null);
        }
        updateControls();
    }
}