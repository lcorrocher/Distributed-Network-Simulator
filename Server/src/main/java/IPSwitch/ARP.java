package IPSwitch;

import DataStructures.HashTable;
import utils.Colour;
import utils.FileUtils;

import java.util.List;

public class ARP {

    private static final String IO_LANS_DIR = Main.getIO_DIR() + "lans/";

    /**
     * hashtable to store the end host table for each edge router. Edge router ID -> end host name -> end host object
     * e.g. "172.21" -> "m" -> EndHost object
     *      "172.22" -> "a" -> EndHost object
     */
    public static HashTable<String, HashTable<String, EndHost>> localEndHostTables = new HashTable<>();
    /**
     * Creates the ARP cache for each edge router ID.
     * Reads the edgeRouterId.txt file to get the edge router IDs.
     * Creates a new ARP cache for each edge router. e.g. "NEW YORK CITY" arpCache titled "NEW YORK CITY.txt"
     * Adds
     */
    public static void createLocalCache() {
        try {
            List<String[]> lines = FileUtils.readFile(IO_LANS_DIR + "edgeRouterId.txt");

            for (String[] line : lines) {
                String[] entries = line[0].split(";");
                for (String entry : entries) {
                    String[] col = entry.split("\\|");
                    String cityName = col[0];
                    String fileName = cityName + ".txt";


                    // maps endhost name to endhost object
                    // e.g. "a" -> EndHost object
                    //      "b" -> EndHost object
                    HashTable<String, EndHost> endHostTable = new HashTable<>();

                    // Read the file for this edge router ID from correct path e.g. austin/AUSTIN.txt
                    List<String[]> arpLines = FileUtils.readFile(IO_LANS_DIR + cityName.toLowerCase() + "/" + fileName);
                    int id = 1;
                    for (String[] arpLine : arpLines) {
                        String[] arpEntries = arpLine[0].split(";");
                        for (String arpEntry : arpEntries) {
                            String[] arpCol = arpEntry.split("\\|");
                            String name = arpCol[0];
                            String ipAddr = arpCol[1];
                            String macAddr = arpCol[2];
                            EndHost endHost = new EndHost(id++, name, ipAddr, macAddr);

                            // Table for name -> endHost
                            endHostTable.put(name, endHost);
                        }
                    }

                    // Add the ARP cache to the Table of local ARP caches
                    localEndHostTables.put(cityName, endHostTable);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getIoLansDir() {
        return IO_LANS_DIR;
    }

    public static void updateCache(String edgeRouterId, String endHostName, String endHostIp, String endHostMac) {
        localEndHostTables.get(edgeRouterId).put(endHostName, new EndHost(1, endHostName, endHostIp, endHostMac));
    }
    /**
     * Looks through all of the local end host tables to see if the end host is present and returns the edge router ID.
     *
     * @param endHostName the name of the end host to find e.g. "a"
     * @return the edge router ID where the end host is located e.g. "NEW YORK CITY"
     */
    public static String findEdgeRouterId(String endHostName) {
        for (HashTable.Entry<String, HashTable<String, EndHost>> entry : localEndHostTables.entrySet()) {
            for (HashTable.Entry<String, EndHost> endHostEntry : entry.getValue().entrySet()) {
                if (endHostEntry.getKey().equals(endHostName)) {
                    return entry.getKey(); // e.g. "NEW YORK CITY"
                }
            }
        }
        System.out.println("No such end host name in current network. Please choose a valid end host. Terminating...");
        System.exit(1);
        return null; // return null if the end host is not found
    }
    /**
     * Determines if the destination host is local to the source host. They have the same edge router ID.
     *
     * @param destinationHostName the destination host name e.g. "h"
     * @return true if the destination host is local to the source host, false otherwise
     */
    public static boolean isCached(String destinationHostName, String sourceEdgeRouterId) {
        String destinationEdgeRouterId = findEdgeRouterId(destinationHostName);
        assert sourceEdgeRouterId != null;
        return sourceEdgeRouterId.equals(destinationEdgeRouterId);
    }


    public static ARPPacket arpBroadcast(String targetHostName) throws InterruptedException {
        System.out.println("\n\nARP broadcast initiated for target host: " + targetHostName + "...");
        Thread.sleep(3000);
        // Find the edge router ID (destination LAN) for the target host e.g. "FRESNO"
        String destinationLan = findEdgeRouterId(targetHostName);

        // Get the end host table for the destination LAN
        HashTable<String, EndHost> endHostTable = localEndHostTables.get(destinationLan);

        // Get the end host from the end host table
        EndHost targetEndHost = endHostTable.get(targetHostName);

        // Get the target IP and MAC address from the end host
        String targetIp = targetEndHost.getIpAddr();
        String targetMac = targetEndHost.getMacAddr();

        // Create and return the ARP response
        return new ARPPacket(targetHostName, targetIp, targetMac, destinationLan);
    }

    // ======================================================================================
    public static EndHost localARPRequest(String cityName, String destinationHostName) throws InterruptedException {
        HashTable<String, EndHost> endHostTable = localEndHostTables.get(cityName);
        EndHost targetEndHost;
        System.out.println(Colour.green("ARP request received."));
        System.out.println("Searching entry in " +  cityName + "'s local cache...");
        Thread.sleep(3000);
        System.out.println(Colour.yellow("Checking ARP response..."));
        if ((targetEndHost = endHostTable.get(destinationHostName)) != null) {
            String targetIp = targetEndHost.getIpAddr();
            String targetMac = targetEndHost.getMacAddr();
            System.out.println(Colour.green("\nENTRY EXISTS! ARP response: END HOST " + destinationHostName + " FOUND!" +
                    "\nObtaining ARP information... " +
                    "\n" + destinationHostName + "'s IP address: " + targetIp +
                    "\n" + destinationHostName + "'s MAC address for " + targetMac));
            return targetEndHost;
        } else {
            System.out.println(Colour.red("ARP response: END HOST NOT FOUND\n"));
        }
        return null;
    }

    // method to get the target end host from localARPRequest
    public static EndHost getTargetEndHost(String cityName, String destinationHostName) {
        return localEndHostTables.get(cityName).get(destinationHostName);
    }


    //===================================================================================


    // test
    public static void main(String[] args) throws InterruptedException {
        createLocalCache();

        String sourceHostName = "a";
        String destinationHostName = "h";


        // print arpCache size
        System.out.println("End host Cache size: " + localEndHostTables.entrySet().size());


        String destEndHostName = "a";
        String city = findEdgeRouterId(destEndHostName);



        // print all key and values in all endhosttables
        for (HashTable.Entry<String, HashTable<String, EndHost>> entry : localEndHostTables.entrySet()) {
            System.out.println("\nEnd Host Table for " + entry.getKey() + ":");
            for (HashTable.Entry<String, EndHost> endHostEntry : entry.getValue().entrySet()) {
                System.out.println(endHostEntry.getKey() + " -> " + endHostEntry.getValue());
            }
        }

        ARPPacket arpResponse = arpBroadcast(destinationHostName);
        System.out.println("ARP broadcast completed.");
        System.out.println(Colour.green("ARP response received: " + arpResponse));
    }


}

