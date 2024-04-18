package IPSwitch;

import java.util.Comparator;

/**
 * Class representing a packet in the network.
 * Each packet has a sequence number, source and destination network id, message id, number of packets in the payload, and the payload itself.
 */
public class Packet implements Comparator<Packet> {
    /**
     * The sequence number of the packet.
     * First packet in the payload has sequence number 0, and the sequence number increases by 1 for each subsequent packet.
     * Packets will be the same size, except for the last packet in the payload, which may be smaller.
     */
    private int sqn;
    /**
     * The source network id of the packet.
     */
    private int srcNetId;
    /**
     * The destination network id of the packet.
     */
    private int destNetId;
    /**
     * The message id of the packet.
     * All packets in the same payload will have the same message id. This is used as a tag to identify packets that belong to the same payload.
     */
    private String msgId;
    /**
     * The number of packets in the payload.
     */
    private int numPacketsInPayload;
    /**
     * The payload of the packet. May be text for a message or a string of rgb values for an image.
     */
    private String payload;
    private MessageType messageType;
    private int imageWidth;
    private int imageHeight;
    /**
     * The filename to store on disk when reassembling the payload.
     */
    private String fileName;
    /**
     * The number of hops the packet has made.
     */
    int hopCount = 0;


    private String destNetIdFull;

    public Packet(){

    }

    /**
     * Constructor for the packet.
     * @param sqn The sequence number of the packet.
     * @param payload The payload of the packet.
     * @param srcNetId The source network id of the packet. e.g. 1 for New York City,
     * @param destNetId The destination network id of the packet. e.g. for fresno, 2
     * @param msgId The message id of the packet.
     * @param numPacketsInPayload The number of packets in the payload.
     */
    public Packet(int sqn , String payload, int srcNetId, int destNetId, String msgId, int numPacketsInPayload, MessageType messageType, String destNetIdFull) {
        this.sqn = sqn;
        this.payload = payload;
        this.srcNetId = srcNetId;
        this.destNetId = destNetId;
        this.msgId = msgId;
        this.numPacketsInPayload = numPacketsInPayload;
        this.messageType = messageType;
        this.destNetIdFull = destNetIdFull;
    }


    // accessor methods

    /**
     * Gets the sequence number of the packet.
     * @return The sequence number of the packet.
     */
    public int getSqn() {
        return sqn;
    }

    /**
     * Gets the payload of the packet.
     * @return The payload of the packet.
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Gets the hop count of the packet.
     * @return The hop count of the packet.
     */
    public int getHopCount() {
        return hopCount;
    }

    /**
     * Gets the source network id of the packet.
     * @return The source network id of the packet.
     */
    public int getSrcNetId() {
        return srcNetId;
    }

    /**
     * Gets the message id of the packet.
     * @return The message id of the packet, aka the token/tag
     */
    public String getMsgId() {
        return msgId;
    }

    /**
     * Gets the destination network id of the packet.
     * @return The destination network id of the packet.
     */
    public int getDestNetId() {
        return destNetId;
    }

    /**
     * Gets the number of packets in the payload.
     * @return The number of packets in the payload.
     */
    public int getNumPacketsInPayload() {
        return numPacketsInPayload;
    }

    /**
     * Gets the filename of the packet.
     * @return The filename of the packet.
     */
    public String getFileName() {
        return fileName;
    }

    // mutator methods

    /**
     * Sets the sequence number of the packet.
     * @param sqn The sequence number of the packet.
     */
    public void setSqn(int sqn) {
        this.sqn = sqn;
    }

    /**
     * Sets the source netId of the packet
     * @param netId The source network id of the packet
     */
    public void setSrcNetId(int netId) {
        this.srcNetId = netId;
    }

    /**
     * Sets the destination id of the packet for routing.
     * @param netId The destination network id of the packet
     */
    public void setDestNetId(int netId) {
        this.destNetId = netId;
    }

    /**
     * Sets the filename of where the packet will be stored on disk.
     * @param s The filename of the packet.
     */
    public void setFileName(String s) {
        fileName = s;
    }

    public MessageType getMessageType(){
        return messageType;
    }

    public void setImageWidth(int width){
        this.imageWidth = width;
    }

    public void setImageHeight(int height){
        this.imageHeight = height;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    /**
     * toString method for the packet.
     * @return A string representation of the packet.
     */
    @Override
    public String toString() {
        return "[IPSwitch.Packet" + sqn + ": srcNetId: " + srcNetId + " destNetId: " + destNetId + " msgId: " + msgId + " numPacketsInPayload: " + numPacketsInPayload + " payload: " + payload + "]";
    }

    /**
     * Compares two packets by their sequence number.
     * @param p1 The first packet to compare.
     * @param p2 The second packet to compare.
     * @return The difference between the sequence numbers of the two packets.
     * Used for sorting packets by their sequence number.
     */
    @Override
    public int compare(Packet p1, Packet p2) {
        return Integer.compare(p1.getSqn(), p2.getSqn());
    }

    public String getDestNetIdFull() {
        return destNetIdFull;
    }
}
