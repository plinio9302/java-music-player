import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Swing GUI for the Java Music Player.
 *
 * <p>Displays the current playlist in a {@link JList}, shows a
 * &quot;Now Playing&quot; label, and provides transport buttons
 * (Play, Stop), plus Add Song and Remove Song actions.</p>
 *
 * <p>Double-clicking a row immediately starts playback of that song.
 * The GUI can be pre-loaded with an existing {@link DoublyLinkedSongList}
 * via {@link #setPlaylistData(DoublyLinkedSongList)}.</p>
 *
 * @author Plinio Durango
 * @version 2.0
 */
public class MusicPlayerGUI extends JFrame {

    // ----------------------------------------------------------------
    // UI components
    // ----------------------------------------------------------------

    private final DefaultListModel<Song> listModel  = new DefaultListModel<>();
    private final JList<Song>            songList   = new JList<>(listModel);
    private final JLabel                 nowPlaying = new JLabel("Now Playing: —");
    private final JButton                btnPlay    = new JButton("Play");
    private final JButton                btnStop    = new JButton("Stop");
    private final JButton                btnAdd     = new JButton("Add Song");
    private final JButton                btnRemove  = new JButton("Remove");

    // ----------------------------------------------------------------
    // Backend
    // ----------------------------------------------------------------

    private final AudioPlayer        audio = new AudioPlayer();
    private       DoublyLinkedSongList dll;

    // ----------------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------------

    /**
     * Builds and wires the GUI window.
     * Window size is 800×400 pixels, centred on screen.
     */
    public MusicPlayerGUI() {
        super("Java Music Player");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null);

        buildUI();
        wireSelectionListener();
        wirePlayStop();
        wireAddSong();
        wireRemoveSong();
        wireDoubleClickPlay();   // double-click a row to play immediately
        refreshControls();
    }

    // ----------------------------------------------------------------
    // UI construction
    // ----------------------------------------------------------------

    /**
     * Assembles the layout: a top bar (Now Playing + buttons) and a
     * scrollable song list in the centre.
     */
    private void buildUI() {
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        songList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof Song s) {
                    label.setText(String.format("%s — %s (%ds)",
                            s.getTitle(), s.getArtist(), s.getDuration()));
                }
                return label;
            }
        });

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(nowPlaying, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(btnPlay);
        buttons.add(btnStop);
        buttons.add(btnRemove);
        buttons.add(btnAdd);
        topBar.add(buttons, BorderLayout.EAST);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topBar,                    BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(songList), BorderLayout.CENTER);
    }

    // ----------------------------------------------------------------
    // Event wiring
    // ----------------------------------------------------------------

    /** Updates Now Playing and button states whenever the selection changes. */
    private void wireSelectionListener() {
        songList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateNowPlaying(songList.getSelectedValue());
                refreshControls();
            }
        });
    }

    /** Wires Play and Stop buttons. */
    private void wirePlayStop() {
        btnPlay.addActionListener(e -> {
            Song s = songList.getSelectedValue();
            if (s == null && !listModel.isEmpty()) {
                songList.setSelectedIndex(0);
                s = songList.getSelectedValue();
            }
            if (s != null) {
                audio.playAudio(s);
                updateNowPlaying(s);
            }
            refreshControls();
        });

        btnStop.addActionListener(e -> {
            audio.stopAudio();
            refreshControls();
        });
    }

    /** Double-clicking a row immediately starts playing that song. */
    private void wireDoubleClickPlay() {
        songList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int idx = songList.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        Song s = listModel.get(idx);
                        songList.setSelectedIndex(idx);
                        audio.playAudio(s);
                        updateNowPlaying(s);
                        refreshControls();
                    }
                }
            }
        });
    }

    /** Wires the Add Song button to a file chooser flow. */
    private void wireAddSong() {
        btnAdd.addActionListener(e -> onAddSong());
    }

    /** Wires the Remove button to remove the currently selected song. */
    private void wireRemoveSong() {
        btnRemove.addActionListener(e -> onRemoveSelected());
    }

    // ----------------------------------------------------------------
    // Actions
    // ----------------------------------------------------------------

    /**
     * Opens a file chooser, builds a Song from the chosen WAV/AIFF file,
     * appends it to the backing list and the visual list model, then
     * selects it. Does NOT auto-play on add.
     */
    private void onAddSong() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Audio files (wav, aiff)", "wav", "aiff"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File f = chooser.getSelectedFile();
        if (f == null || !f.isFile()) return;

        Song s = buildSongFromFile(f);
        if (dll == null) dll = new DoublyLinkedSongList();
        dll.addLast(s);
        listModel.addElement(s);

        int newIdx = listModel.size() - 1;
        songList.setSelectedIndex(newIdx);
        updateNowPlaying(s);
        refreshControls();
    }

    /**
     * Removes the currently selected song from both the visual list model
     * and the backing {@link DoublyLinkedSongList}. Stops audio if the
     * playlist becomes empty.
     */
    private void onRemoveSelected() {
        int idx = songList.getSelectedIndex();
        if (idx < 0) return;

        if (dll != null) dll.removeAt(idx);
        listModel.remove(idx);

        if (listModel.isEmpty()) {
            songList.clearSelection();
            updateNowPlaying(null);
            if (audio.isPlaying()) audio.stopAudio();
            refreshControls();
            return;
        }

        int newIdx = Math.min(idx, listModel.size() - 1);
        songList.setSelectedIndex(newIdx);
        updateNowPlaying(songList.getSelectedValue());
        refreshControls();
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    /**
     * Derives a {@link Song} from a file: title from the filename (without
     * extension), artist set to {@code "Unknown"}, and duration computed
     * via {@link AudioSystem} (falls back to 0 on error).
     *
     * @param f the audio file
     * @return a populated {@link Song}
     */
    private Song buildSongFromFile(File f) {
        String name  = f.getName();
        int    dot   = name.lastIndexOf('.');
        String title = (dot > 0) ? name.substring(0, dot) : name;
        return new Song(title, "Unknown", computeDurationSeconds(f), f.getAbsolutePath());
    }

    /**
     * Probes the audio file for its duration in seconds.
     * Returns 0 if the format is unsupported or an error occurs.
     *
     * @param file the audio file to probe
     * @return duration in whole seconds, or 0 on failure
     */
    private int computeDurationSeconds(File file) {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(file)) {
            AudioFormat fmt    = ais.getFormat();
            long        frames = ais.getFrameLength();
            if (fmt.getFrameRate() > 0 && frames > 0) {
                return (int) Math.round(frames / fmt.getFrameRate());
            }
        } catch (Exception ignored) { }
        return 0;
    }

    /**
     * Updates the "Now Playing" label.
     *
     * @param s the song now selected/playing, or {@code null} to reset
     */
    public void updateNowPlaying(Song s) {
        nowPlaying.setText(s == null
            ? "Now Playing: —"
            : "Now Playing: " + s.getTitle() + " — " + s.getArtist());
    }

    /**
     * Enables or disables buttons based on current state:
     * <ul>
     *   <li>Play — enabled when the list is non-empty</li>
     *   <li>Stop — enabled when audio is currently playing</li>
     *   <li>Remove — enabled when a row is selected</li>
     * </ul>
     */
    private void refreshControls() {
        btnPlay.setEnabled(!listModel.isEmpty());
        btnStop.setEnabled(audio.isPlaying());
        btnRemove.setEnabled(songList.getSelectedIndex() >= 0);
    }

    /**
     * Populates the GUI with songs from an existing {@link DoublyLinkedSongList}.
     * Clears the current list model, then loads all songs in order.
     * Selects the first song if the list is non-empty.
     *
     * @param dll the playlist to display
     */
    public void setPlaylistData(DoublyLinkedSongList dll) {
        this.dll = dll;
        listModel.clear();
        for (int i = 0; i < dll.size(); i++) {
            listModel.addElement(dll.get(i));
        }
        if (!listModel.isEmpty()) {
            songList.setSelectedIndex(0);
            updateNowPlaying(listModel.get(0));
        } else {
            songList.clearSelection();
            updateNowPlaying(null);
        }
        refreshControls();
    }
}
