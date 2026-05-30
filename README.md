# Java Music Player

A Java application that combines a **custom doubly-linked list** data structure with Java's built-in `javax.sound.sampled` audio API to deliver both a command-line interface and a Swing GUI for managing and playing music playlists.

---

## Features

- **Doubly-Linked Playlist** — Songs are stored in a custom doubly-linked list (`DoublyLinkedSongList`) that supports O(1) add/remove at both ends, indexed access, forward and backward traversal, and bidirectional navigation.
- **Audio Playback** — Plays WAV and AIFF files via `AudioPlayer`, with support for play, stop, pause, resume, and volume control (LOW / MED / HIGH).
- **Playlist Navigation** — Jump to any song by index, advance to the next track, go back to the previous one, or play a random song.
- **Search & Stats** — Find songs by title, filter by artist, and view total song count and playlist duration.
- **CSV Import / Export** — Load a playlist from a CSV file, merge multiple CSV files into one playlist, validate that audio files exist on disk, and save the current playlist back to CSV.
- **Swing GUI** — A 800×400 Swing window with a scrollable song list, “Now Playing” label, Play/Stop buttons, and Add/Remove song controls. Launched directly from the CLI menu.
- **Flexible Duration Input** — Song duration can be entered as `mm:ss` or as plain seconds in both the CLI and CSV files.

---

## Data Structure

The core data structure is `DoublyLinkedSongList`, a hand-written doubly-linked list where each node (`DoublyLinkedSongNode`) holds a `Song` and bidirectional `next`/`prev` pointers.

| Operation | Method | Complexity |
|---|---|---|
| Add to front / back | `addFirst` / `addLast` | O(1) |
| Remove from front / back | `removeFirst` / `removeLast` | O(1) |
| Access by index | `get(index)` | O(n) |
| Insert / remove at index | `addAt` / `removeAt` | O(n) |
| Forward / backward print | `displayForward` / `displayBackward` | O(n) |
| Search by value | `indexOf` / `contains` | O(n) |

---

## Project Structure

```
.
├── Song.java                  # Song model (title, artist, duration, file path)
├── DoublyLinkedSongNode.java  # Doubly-linked list node
├── DoublyLinkedSongList.java  # Custom doubly-linked list for Song objects
├── PlayListDL.java            # High-level playlist wrapper with navigation & search
├── AudioPlayer.java           # Audio playback (WAV/AIFF) via javax.sound.sampled
├── MusicPlayerGUI.java        # Swing GUI (JList, Now Playing, Play/Stop/Add/Remove)
├── FileUtils.java             # CSV import/export and audio file validation
├── Main.java                  # Entry point — CLI menu and GUI launcher
└── Songs.csv                  # Sample playlist dataset
```

---

## Getting Started

### Prerequisites

- Java 17 or higher (uses pattern matching in `instanceof`)
- A terminal / command prompt
- WAV or AIFF audio files for playback

### Compile

Place all `.java` files in the same directory, then run:

```bash
javac *.java
```

### Run

```bash
java Main
```

---

## CSV Format

Playlists can be loaded from and saved to CSV files in the following format:

```
Title,Artist,Duration,FilePath
Clair de Lune,Debussy,5:24,/music/clair_de_lune.wav
Take Five,Dave Brubeck,5:24,/music/take_five.wav
```

- **Duration**: `mm:ss` format
- **FilePath**: absolute path to the audio file
- Lines with fewer than 4 columns or empty required fields are skipped with a warning

---

## CLI Menu Overview

```
[0] Launch GUI
[1] File Operations         — load CSV, merge multiple CSVs, validate files, save
[2] List Operations         — addFirst, addLast, removeFirst, removeLast, get, insert, remove, clear
[3] Playlist Operations     — add song, display, total duration, jump to, current song, next
[4] Audio Controls          — play, stop, pause, resume, volume, play from start, random
[5] Search & Stats          — find by title, find by artist, playlist statistics
[6] Save / Export           — write current playlist to CSV
[7] Quit
```

---

## License

This project is open source and available under the [MIT License](LICENSE).
