/**
 * Represents a single node in the SongLinkedList.
 * Each node stores a Song and a reference to the next node.
 */
public class DoublyLinkedSongNode {
    private Song song;          // The song stored in this node
    private DoublyLinkedSongNode next;      // Reference to the next node in the list
    private DoublyLinkedSongNode prev;
    /**
     * Constructor: creates a node with a song and no next link.
     */
    public DoublyLinkedSongNode(Song song){
        this.song = song;
        this.next = null;
        this.prev = null;
    }

    // ----------------------- Setters -----------------------

    /** Set or update the song stored in this node */
    public void setSong(Song song){
        this.song = song;
    }

    /** Set or update the reference to the next node */
    public void setNext(DoublyLinkedSongNode next) {
        this.next = next;
    }

    public void setPrev(DoublyLinkedSongNode prev) {
        this.prev = prev;
    }

    // ----------------------- Getters -----------------------

    /** Get the song stored in this node */
    public Song getSong(){
        return this.song;
    }

    /** Get the next node in the list */
    public DoublyLinkedSongNode getNext(){
        return this.next;
    }

    public DoublyLinkedSongNode getPrev(){
        return this.prev;
    }

    // ----------------------- List Utility -----------------------

    /**
     * Insert a new node directly after this one.
     * The new node’s next pointer is set to the original "next".
     */
    // public void insertAfter(DoublyLinkedSongNode newNode){
    //     DoublyLinkedSongNode temp = this.next;  // store current next
    //     this.next = newNode;        // link this -> newNode
    //     newNode.next = temp;        // link newNode -> old next
    // }

    // public void insertBefore(DoublyLinkedSongNode newNode) {
    //     DoublyLinkedSongNode temp = this.prev;
    //     this.prev = newNode;
        
    // }

    // ----------------------- Overrides -----------------------

    /** Return the string representation of the song in this node */
    @Override
    public String toString(){
        return song.toString();
    }
}