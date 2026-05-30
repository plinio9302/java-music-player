[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/aiqxq8vk)

Plinio Duarango
ID: 20800410

How to Run the Program: 
1. Make sure all the .java files (Song.java, AudioPlayer.java, DoublyLinkedSongNode.java, DoublyLinkedSongList.java, PlayListDL.java, MusicPlayerGUI.java, Main.java, and FileUtils.java) are in the same src/ folder.

2. Run the main program

3. The program starts in CLI mode with a menu of options.
	•	Choose 0 from the menu to launch the GUI (MusicPlayerGUI).
	•	In the GUI you can add songs (WAV or AIFF), remove songs, play/stop audio, or double-click a song to start playback.

You can also run the program directly from your IDE (e.g., IntelliJ or VS Code) by running the Main class.

Self-assessment checklist

Specifications for a C (80)


DoublyLinkedSongNode Class

	Fields: song (Song), next (DoublyLinkedSongNode), previous (DoublyLinkedSongNode) ✅

	Constructor accepting Song parameter ✅

	Appropriate getter/setter methods ✅


DoublyLinkedSongList Class

	All methods from SongLinkedList (updated for a doubly-linked list) ✅

	addFirst(Song song) and addLast(Song song) with bidirectional linking ✅

	removeFirst() and removeLast() with proper link management ✅

	get(int index) to get the Song object at that index ✅

	displayForward() and displayBackward() methods ✅


Enhanced Playlist Class with Bidirectional Navigation

	Migrate from SongLinkedList to DoublyLinkedSongList ✅

	nextSong() and previousSong() methods ✅

	hasNext() and hasPrevious() methods ✅

	Track position in the list of the current song being played ✅

	jumpToSong(int index) with bounds checking and immediate playback ✅

Basic GUI Implementation (800x400 minimum)

	Main window components:

		JList displaying current playlist with song information ✅

		“Now Playing” label showing current song details ✅

		Basic control buttons: Play, Stop ✅

		Add Song button with file chooser to add a song to the playlist ✅

		Remove Song button for removing selected song from the playlist ✅


	Event handling:

		Button clicks trigger appropriate playlist/audio actions ✅

		Double-click on the playlist item starts playing that song ✅


GUI-Audio Integration

	Highlight currently playing song in JList ✅

	Update “Now Playing” label during playback ✅

	Enable/disable buttons based on playlist state✅

	Visual feedback for all playlist operations✅









