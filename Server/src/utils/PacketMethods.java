package utils;

import java.security.SecureRandom;
import java.util.*;

import DataStructures.HashTable;
import IPSwitch.*;



/**
 * Class containing methods for using packets.
 */
public class PacketMethods { /**
     * Takes a String and converts it into a List of properly-formatted packets.
     * @param src Where the packets are being sent from.
     * @param dest Where the packets are being sent to.
     * @param data The data that we want to packetise.
     * @param packetSize Size of each packets.
     * @param fileName The filename for when the packets are reconstructed.
     * @return List of IPSwitch.Packet objects.
     */
    public static List<Packet> writePackets(int src, int dest, String data, int packetSize, String fileName, MessageType messageType, String srcNetIdFull) {
        // Calculates how many packets are required in the list.
        int packetsRequired = (data.length() / packetSize) + 1;
        if (data.length() % packetSize == 0){
            // If the data fits exactly into a set of packets, there is one less packet required.
            packetsRequired--;
        }

        List<Packet> list = new ArrayList<>();
        // Token is used to group packets together, random, but must be the same for all packets in a group.
        String token = generateRandomToken();

        // IPSwitch.Main loop for splitting packets and adding to the List.
        for (int i = 0; i < packetsRequired; i++) {
            // Finds the substring indexes, and creates the substring.
            int substringStartIndex = i * packetSize;
            int substringEndIndex = Math.min(substringStartIndex + 16, data.length()); // Prevents overflow.
            String subString = data.substring(substringStartIndex, substringEndIndex);

            // Creates our packet, and adds it to the list.
            Packet packet = new Packet(i, subString, src, dest, token, packetsRequired, messageType, srcNetIdFull);
            packet.setFileName(fileName);
            list.add(packet);
        }

        return list;
    }

    /**
     * sends broadcast packets to all edge routers.
     * @param edgeRouterId Map of String to ip. e.g. "NEW YORK CITY" -> "172.21"
     * @param sourceER The source edge router. aka. source city e.g. "NEW YORK CITY"
     * @param net the network to send the packets to.
     */
    public static void sendMultiplePackets(HashTable<String, String> edgeRouterId, HashTable<String, HashTable.Entry<Integer, String>> LOCAL_AREA_NETWORKS, String sourceER, Network net) {
        HashMap<String, String> payloads = new HashMap<>();
        String token = "";
        for (String destinationCity : edgeRouterId.keySet()) {
            payloads.put(destinationCity, "BROADCAST " + destinationCity);
            token = generateRandomToken();
        }
        System.out.println("Creating packets for broadcast with following destinations and payloads:");
        System.out.println(payloads.toString());

        for (String destinationCity : edgeRouterId.keySet()) {
            List<Packet> broadcastPackets = new ArrayList<>();
            String payload = payloads.get(destinationCity);

            int srcNetId = LOCAL_AREA_NETWORKS.get(edgeRouterId.get(sourceER)).getKey(); // e.g. 1 for New York City
            int destNetId = LOCAL_AREA_NETWORKS.get(edgeRouterId.get(destinationCity)).getKey(); // e.g. 2 for Fresno

            // creates a packet for each destination edge router
            Packet broadcastPacket = new Packet(1, payload, srcNetId, destNetId, token, 1, MessageType.TEXT, "srcNetIdFull");
            // adds the packet to the list of broadcast packets
            broadcastPackets.add(broadcastPacket);
            // sends the packets to the destination switch.
            addPacketsToStartSwitch(net, broadcastPackets);
        }

    }

    public static void createBroadcastResponsePacket(HashTable<String, String> edgeRouterId, HashTable<String, HashTable.Entry<Integer, String>> LOCAL_AREA_NETWORKS, String srcCity, String originCity, Network net) {
        System.out.println("\nCreating broadcast response packet for " + originCity + " from " + srcCity);

        String token = generateRandomToken();

        String payload = "BROADCAST RESPONSE " + originCity;
        System.out.println("Broadcast response payload: " + payload);
        int srcNetId = LOCAL_AREA_NETWORKS.get(edgeRouterId.get(originCity)).getKey();
        int destNetId = LOCAL_AREA_NETWORKS.get(edgeRouterId.get(originCity)).getKey();

        Packet brp = new Packet(1, payload, srcNetId, destNetId, token, 1, MessageType.TEXT, "srcNetIdFull"); // broadcast response packet
        net.addToSwitch(brp.getDestNetId(), brp);
    }



    /**
     * Reads a list of packets into a string.
     * @param packets Packets to be read.
     * @return String of the data.
     */
    public static String readPackets(List<Packet> packets, boolean orderPackets){

        if (orderPackets) {
            // Orders the packets by their sequence number.
            orderPackets(packets);
        }

        // Reads the packets.
        StringBuilder output = new StringBuilder();
        for(Packet p: packets){
            output.append(p.getPayload());
        }

        return output.toString();
    }

    private static void orderPackets(List<Packet> packets){
        Collections.sort(packets, new Packet());
    }
    /**
     * Generates a random token for grouping packets together. aka. tag/message id.
     * @return Random token.
     */
    private static String generateRandomToken() {
        // length of token
        int length = 16;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }

        return sb.toString();
    }
    /**
     * Sends packets to the destination switch.
     * @param net The network to send the packets to.
     * @param packets The packets to send.
     */
    public static void addPacketsToStartSwitch(Network net, List<Packet> packets) {
        for (Packet p: packets) {
            net.addToSwitch(p.getDestNetId(), p);
        }
    }

    public static void main(String[] args) {
        String token = generateRandomToken();
        System.out.println(token);
    }
}
