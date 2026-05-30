import java.util.NoSuchElementException;

/**
 * DoublyLinkedSongList
 * My doubly-linked list specialized for Song objects.
 *
 * C-level requirements I cover here:
 *  - addFirst / addLast
 *  - removeFirst / removeLast
 *  - get(int index) → returns the Song at that index
 *  - displayForward() / displayBackward()
 *
 * Extra helpers I use for the GUI and playlist features:
 *  - size(), isEmpty(), clear()
 *  - nodeAt(int) for jumping directly to a node (efficient traversal)
 *  - removeAt(int) and addAt(int, Song) for indexed edits
 */
public class DoublyLinkedSongList {
    // Pointers to the first and last nodes in the list
    private DoublyLinkedSongNode head;
    private DoublyLinkedSongNode tail;

    // I keep track of the number of elements for bounds checks / convenience
    private int size;

    /** @return true if there are no songs in the list */
    public boolean isEmpty() {
        return size == 0;
    }

    /** @return how many songs are stored */
    public int size() {
        return size;
    }

    /** Remove all nodes (constant-time reset of the list). */
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    /** @return direct access to the head node (used by playlist wrapper / GUI) */
    public DoublyLinkedSongNode getHeadNode() {
        return head;
    }

    /** @return direct access to the tail node (used by playlist wrapper / GUI) */
    public DoublyLinkedSongNode getTailNode() {
        return tail;
    }

    // ------------------------------------------------
    // Core operations (insert/remove at ends)
    // ------------------------------------------------

    /**
     * Add a song at the front of the list.
     * Handles the empty-list case by making head and tail the same node.
     */
    public void addFirst(Song song) {
        DoublyLinkedSongNode n = new DoublyLinkedSongNode(song);
        if (isEmpty()) {
            head = n;
            tail = n;
        } else {
            n.setNext(head);
            head.setPrev(n);
            head = n;
        }
        size++;
    }

    /**
     * Add a song at the end of the list.
     * Handles the empty-list case by making head and tail the same node.
     */
    public void addLast(Song song) {
        DoublyLinkedSongNode n = new DoublyLinkedSongNode(song);
        if (isEmpty()) {
            head = n;
            tail = n;
        } else {
            n.setPrev(tail);
            tail.setNext(n);
            tail = n;
        }
        size++;
    }

    /**
     * Remove and return the first song.
     * @throws NoSuchElementException if the list is empty
     */
    public Song removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }

        Song s = head.getSong();
        if (head == tail) { // only one node in the list
            head = null;
            tail = null;
        } else {
            head = head.getNext();
            head.setPrev(null); // new head has no previous
        }
        size--;
        return s;
    }

    /**
     * Remove and return the last song.
     * @throws NoSuchElementException if the list is empty
     */
    public Song removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }

        Song s = tail.getSong();
        if (head == tail) { // only one node in the list
            head = null;
            tail = null;
        } else {
            tail = tail.getPrev();
            tail.setNext(null); // new tail has no next
        }
        size--;
        return s;
    }

    // ------------------------------------------------
    // Random access helpers
    // ------------------------------------------------

    /** Returns the Song at index (0-based). Throws if out of bounds. */
    public Song get(int index) {
        return nodeAt(index).getSong();
    }

    /**
     * Find the first index of a Song by equality.
     * @param song the Song to look for
     * @return index if found, -1 otherwise
     */
    public int indexOf(Song song) {
        DoublyLinkedSongNode current = head;
        int index = 0;
        while (current != null) {
            if (current.getSong().equals(song)) {
                return index;
            }
            index++;
            current = current.getNext();
        }
        return -1;
    }

    /** @return true if the list contains the given Song by equality. */
    public boolean contains(Song song) {
        DoublyLinkedSongNode current = head;
        while (current != null) {
            if (current.getSong().equals(song)) {
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    /**
     * Remove and return the Song at a specific index.
     * This is used by the GUI "Remove selected" action.
     */
    public Song removeAt(int index) {
        DoublyLinkedSongNode node = nodeAt(index);
        Song s = node.getSong();

        DoublyLinkedSongNode prev = node.getPrev();
        DoublyLinkedSongNode next = node.getNext();

        if (prev == null && next == null) {   // removing the only node
            head = null;
            tail = null;
        } else if (prev == null) {            // removing head
            head = next;
            next.setPrev(null);
        } else if (next == null) {            // removing tail
            tail = prev;
            prev.setNext(null);
        } else {                              // removing from the middle
            prev.setNext(next);
            next.setPrev(prev);
        }
        size--;
        return s;
    }

    /**
     * Insert a Song at a specific index.
     * - index == 0 → addFirst
     * - index == size → addLast (append)
     * - otherwise splice between prev and current
     */
    public void addAt(int index, Song s) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index " + index);
        }

        if (index == 0) {          // insert at head
            addFirst(s);
            return;
        }
        if (index == size) {       // append at tail
            addLast(s);
            return;
        }

        // Insert before the node currently at `index`
        DoublyLinkedSongNode node = nodeAt(index);
        DoublyLinkedSongNode prev = node.getPrev();
        DoublyLinkedSongNode newNode = new DoublyLinkedSongNode(s);

        prev.setNext(newNode);
        newNode.setPrev(prev);
        newNode.setNext(node);
        node.setPrev(newNode);

        size++;
    }

    /**
     * Return the internal node at index.
     * I traverse from the head here; if needed I could optimize to choose
     * head/tail based on which side is closer (bidirectional traversal).
     */
    public DoublyLinkedSongNode nodeAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index);
        }

        // Simple forward walk (good enough for C-level; can optimize later)
        DoublyLinkedSongNode cur = head;
        for (int i = 0; i < index; i++) {
            cur = cur.getNext();
        }
        return cur;
    }

    // ------------------------------------------------
    // Display helpers (C requirement)
    // ------------------------------------------------

    /** Print the list from head → tail with indices and basic song info. */
    public void displayForward() {
        System.out.println("Playlist (forward):");
        int i = 0;
        DoublyLinkedSongNode cur = head;
        while (cur != null) {
            Song s = cur.getSong();
            System.out.printf("[%d] %s - %s (%ds)%n", i, s.getTitle(), s.getArtist(), s.getDuration());
            cur = cur.getNext();
            i++;
        }
    }

    /** Print the list from tail → head with indices and basic song info. */
    public void displayBackward() {
        System.out.println("Playlist (backward):");
        int i = size - 1;
        DoublyLinkedSongNode cur = tail;
        while (cur != null) {
            Song s = cur.getSong();
            System.out.printf("[%d] %s - %s (%ds)%n", i, s.getTitle(), s.getArtist(), s.getDuration());
            cur = cur.getPrev();
            i--;
        }
    }
}