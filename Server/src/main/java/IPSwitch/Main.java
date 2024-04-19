package IPSwitch;

import DataStructures.HashTable;
import utils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static IPSwitch.ARP.*;

public class Main {
    /**
     * hashmap to store the local area networks. City -> switch id, folder name
     * E.g. "NEW YORK CITY", 1, "nycFolder"
     */
    private static HashTable<String, HashTable.Entry<Integer, String>> LOCAL_AREA_NETWORKS;

    /**
     * hashtable to store the edge router id. City -> switch id
     * E.g. "NEW YORK CITY", 172.21
     */
    public static HashTable<String, String> edgeRouterId;

    /**
     * The network object to run the simulation.
     */
    private static Network net;
    private static String sourceHostName;
    private static String destinationEndHostName;
    private static String filename;
    static List<String> sourceHostNames;
    static List<String> destinationEndHostNames;
    private static String endHostFoundCity;
    private static final String IO_DIR = "server/io/";



    public static void main(String[] args) throws IOException, InterruptedException {

        String filenamesFilePath = IO_DIR + "messages/";
        File directory = new File(filenamesFilePath);
        List<String> filenames = new ArrayList<>();

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        filenames.add(file.getName());
                    }
                }
            }

        } else {
            System.out.println("The specified directory does not exist or is not a directory.");
        }


        int numberOfTransfers = 1;
        // Introductory welcome messages and user input
        displayIntro();
        Thread.sleep(2000);
        runNetworkServer(filenamesFilePath, numberOfTransfers);
    }

    public static String getIO_DIR() {
        return IO_DIR;
    }

    private static void runNetworkServer(String filenamesFilePath, int numberOfTransfers) throws InterruptedException {

        int portNumber = 8080;
        boolean hasToBuildCache = true;
        int clientId = 1;

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            System.out.println("Session established. Network is running and waiting for connections...\n");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String line;
                String[] message = new String[3];
                int index = 0;
                while ((line = in.readLine()) != null) {
                    message[index] = line;
                    index++;
                }

                if (!hasToBuildCache && message[0].equals("wipe")) {
                    System.out.println("Received client message [command = " + message[0]+ "]\n");
                    ARP.wipeCache();
                    Thread.sleep(1000);
                    continue;
                } else {
                    System.out.println("Received client message [src = " + message[0] + ", dst = " + message[1] + ", filename = " + message[2] + "]\n");
                }

                if (hasToBuildCache) {
                    // TODO: setupNetwork in here I think but didn't run last time for second iteration

                    System.out.println(Colour.yellow("Building ARP cache..."));
                    ARP.createLocalCache();
                    Thread.sleep(1000);
                    System.out.println(Colour.yellow("Processing FIRST REQUEST...\n"));
                    Logger.log("Transfer 1...\n");
                } else {
                    System.out.println(Colour.yellowBold("\n\nENTERING NEXT TRANSFER REQUEST..."));
                    Thread.sleep(2000);
                    System.out.println(Colour.yellow("Processing REQUEST " + (clientId) + "...\n"));
                    Logger.log("\nTransfer " + (clientId) + "...\n");
                }

                sourceHostName = message[0];
                destinationEndHostName = message[1];
                filename = message[2];

                Thread.sleep(1000);
                setupNetwork(numberOfTransfers);
                Thread.sleep(2000);

                // Print file contents
                System.out.println("\nYou will be transferring the following file: " + filename);
                System.out.println("The first few lines of " + filename + " is: \n\n");

                // filepath for the file to be transferred e.g beemovie.txt is server/io/filenames/beemovie.txt
                String filePath = filenamesFilePath + filename;

                System.out.println(Colour.green(TxtToBinary.extractFromFile(filePath).substring(0, 500) + "...\n"));

                System.out.println("\n");

                // log source, destination end host names, filename
                Logger.log("Source: " + sourceHostName + ", Destination: " + destinationEndHostName + ", filename: " + filename);

                String destinationIP;

                String sourceER = ARP.findEdgeRouterId(sourceHostName); // source edge router e.g. "NEW YORK CITY"
                String sourceIP = localEndHostTables.get(sourceER).get(sourceHostName).getIpAddr();

                // Check if the destination is cached locally. If not, initiate an ARP broadcast, which sends an arp request to every end local cache in the network, asking if the destination is in that LAN/
                boolean cached = ARP.isCached(destinationEndHostName, sourceER);
                if (cached) {
                    System.out.println("\nDestination end host [" + destinationEndHostName + "] is cached locally. No ARP broadcast needed. Searching through local end host map...");
                    System.out.println("IP address acquired for [" + destinationEndHostName + "]: " + localEndHostTables.get(sourceER).get(destinationEndHostName).getIpAddr());
                    System.out.println("MAC address acquired for [" + destinationEndHostName + "]: " + localEndHostTables.get(sourceER).get(destinationEndHostName).getMacAddr() + "\n\n");
                    Thread.sleep(2000);

                    destinationIP = localEndHostTables.get(sourceER).get(destinationEndHostName).getIpAddr();

                } else {
                    System.out.println("\n\nDestination [" + destinationEndHostName + "] is not locally cached. ARP broadcast needed. Initiating ARP broadcast...");

                    // executing ARP broadcast query and response, returning the new end host
                    EndHost newEndHost = initiateBroadcastProtocol(sourceER, destinationEndHostName);

                    String newEndHostIp = newEndHost.getIpAddr();
                    String newEndHostMac = newEndHost.getMacAddr();

                    Thread.sleep(2000);

                    System.out.println("\n\nNew end host acquired: " + newEndHost);
                    System.out.println("Updating local end host cache with destination end host [" + destinationEndHostName + "] for future queries...\n\n");
                    Thread.sleep(2000);

                    ARP.updateCache(sourceER, destinationEndHostName, newEndHostIp, newEndHostMac);
                    destinationIP = newEndHostIp;
                }

                // Gets the routing number of each folder.
                int[] locations = getSrcDest(sourceIP, destinationIP);
                int src = locations[0];
                int dest = locations[1];
                System.out.println(sourceIP);
                System.out.println(src);
                String folder = LOCAL_AREA_NETWORKS.get(keepUpToSecondPeriod(sourceIP)).getValue();

                String data = TxtToBinary.extractFromFile(filePath).toString();
                String data1 = TxtToBinary.convertToBinary(data);

                // Creates our packets and places them in a List.
                List<Packet> packets = PacketMethods.writePackets(src, dest, data1, 16, filename, MessageType.TEXT, destinationIP);

                // Adds the packets to the first switch. All have same srcNetId and destNetId.
                PacketMethods.addPacketsToStartSwitch(net, packets);

                // if local, runLAN, else (not local) runWAN
                if (src == dest) {
                    // Starts the main loop.
                    net.runLAN();
                } else {
                    // Starts the main loop.
                    net.runWAN();
                }

                // Close connections
                in.close();
                clientSocket.close();
                hasToBuildCache = false;
                clientId++;
            }
        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
        }

        System.out.println(Colour.yellowBold("\nNetwork offline. Thank you for using the Distributed Network Simulator!"));
    }

    public static String getEndHostFoundCity() {
        return endHostFoundCity;
    }


    /**
     * Prints introductory messages and asks the user for inputs on the number of runs, source and destination end host names.
     */
    private static void displayIntro() throws IOException {
        sourceHostNames = new ArrayList<>();
        destinationEndHostNames = new ArrayList<>();

        System.out.println(Colour.yellowBold("\n\n========================================= WELCOME TO THE IP SWITCH NETWORK - FOCUSSING ON GRAPHS! ==========================================\n"));
    }


    private static List<String> getInputHostNames(Scanner input, String type, int runNumber) {
        List<String> hostNames = new ArrayList<>();
        String prompt = type.equals("source") ? "source" : "destination";

        System.out.println(Colour.yellowBold("\n                   =========================== INPUT " + type.toUpperCase() + " END HOST NAMES FOR RUN " + runNumber + " =========================\n"));
        System.out.println(Colour.yellow("Please choose the " + prompt + " end host according to the Network schematic ranging from a to p. " +
                "\nThe " + prompt + " host will determine whether the packets will be sent within the LAN or WAN." +
                "\nPlease enter only ONE LETTER for the " + prompt + " end host name.\n"));

        System.out.println("Enter the " + prompt + " host name for run " + runNumber + ":");
        hostNames.add(getValidSingleLetterInput(input));

        return hostNames;
    }

    /**
     * Gets a valid single letter input from the user.
     *
     * @param input the scanner object to get user input
     * @return a valid single letter input
     */
    private static String getValidSingleLetterInput(Scanner input) {
        String userInput;
        char inputChar;

        while (true) {
            System.out.println("Please enter a single letter between 'a' and 'p': ");
            userInput = input.nextLine().trim().toLowerCase();

            if (userInput.length() != 1) {
                System.out.println("Invalid input. Please enter only ONE letter.");
                continue;
            }

            inputChar = userInput.charAt(0);

            if (inputChar < 'a' || inputChar > 'p') {
                System.out.println("Invalid input. The letter must be between 'a' and 'p'.");
                continue;
            }

            break;
        }

        return String.valueOf(inputChar);
    }

    /**
     * Sends broadcast packets to all edge routers except the source edge router. It sends a packet to each edge router with the message "BROADCAST" + destination edge router.
     *
     * @param sourceER            the source edge router e.g. "NEW YORK CITY"
     * @param destinationHostName the destination host name e.g. "h". Must not be local to the source edge router.
     * @throws InterruptedException if the thread is interrupted.
     */
    private static EndHost initiateBroadcastProtocol(String sourceER, String destinationHostName) throws InterruptedException {
        long startBRQ = System.currentTimeMillis(); // start time for broadcast request
        PacketMethods.sendMultiplePackets(edgeRouterId, LOCAL_AREA_NETWORKS, sourceER, net);
        EndHost targetEndHost = net.runBroadcastWAN(destinationHostName, sourceER);
        long totalBRQ = System.currentTimeMillis() - startBRQ;

        endHostFoundCity = findEdgeRouterId(destinationHostName);
        long startBRS = System.currentTimeMillis(); // start time for broadcast response
        returnToOriginEdgeRouter(endHostFoundCity, sourceER);

        long totalBRS = System.currentTimeMillis() - startBRS;
        long broadcastTime = totalBRQ + totalBRS;

        String logMessage = "[Broadcast Protocol] Time taken for sourcing endHost " + destinationHostName + ": " + broadcastTime + "ms. LAN of end host: " + endHostFoundCity;
        Logger.log(logMessage);
        System.out.println(Colour.yellow(logMessage));
        System.out.println(Colour.yellow("Time has been logged"));

        return targetEndHost;
    }

    private static void returnToOriginEdgeRouter(String endHostFoundCity, String originCity) throws InterruptedException {
        PacketMethods.createBroadcastResponsePacket(edgeRouterId, LOCAL_AREA_NETWORKS, endHostFoundCity, originCity, net);
        net.runResponseWAN(originCity);
    }

    /**
     * Sets up the network with the given number of files to transfer.
     *
     * @param filesToTransfer the number of files to transfer from a source folder to a destination folder.
     * @throws IOException if the file cannot be read.
     */
    private static void setupNetwork(int filesToTransfer) throws IOException, InterruptedException {
        System.out.println("Building network and loading lan topology...");

        CSVReader.readCoordinatesCSV();
        Thread.sleep(2000);

        LOCAL_AREA_NETWORKS = new HashTable<>();

        // Create an entry to put into the inner map
        HashTable.Entry<Integer, String> switchEntry1 = new HashTable.Entry(1, "new york cityFolder"); // id of the switch + folder for the switch. e.g 1, "nycFolder"
        HashTable.Entry<Integer, String> switchEntry2 = new HashTable.Entry(2, "fresnoFolder");
        HashTable.Entry<Integer, String> switchEntry3 = new HashTable.Entry(3, "minneapolisFolder");
        HashTable.Entry<Integer, String> switchEntry4 = new HashTable.Entry(4, "austinFolder");

        // read from edgeRouter.txt and create a map of city -> switch id
        List<String[]> lines = FileUtils.readFile(getIoLansDir()+ "edgeRouterId.txt");
        edgeRouterId = new HashTable<>();
        for (String[] line : lines) {
            String[] entries = line[0].split(";");
            for (String entry : entries) {
                String[] col = entry.split("\\|");
                String city = col[0];
                String id = col[1];
                edgeRouterId.put(city, id);
            }
        }
        // Put the entry into the outer map
        // name of the LAN and map with id of the switch + folder for the switch. e.g. "172.23" -> 1, "nycFolder"
        // {NEW YORK CITY={1=nycFolder}, FRESNO={2=fresnoFolder}, MINNEAPOLIS={3=minneapolisFolder}, AUSTIN={4=austinFolder}}


        LOCAL_AREA_NETWORKS.put(edgeRouterId.get("NEW YORK CITY"), switchEntry1);
        LOCAL_AREA_NETWORKS.put(edgeRouterId.get("FRESNO"), switchEntry2);
        LOCAL_AREA_NETWORKS.put(edgeRouterId.get("MINNEAPOLIS"), switchEntry3);
        LOCAL_AREA_NETWORKS.put(edgeRouterId.get("AUSTIN"), switchEntry4);

// TODO: uncomment an run this instead of the above code. Don't know why it doesn't work.

//        // adds the name of the city and the switch id to the LOCAL_AREA_NETWORKS map. e.g.
//        for (HashTable.Entry<String, String> entry : edgeRouterId.entrySet()) {
//            String cityName = entry.getKey();
//            String switchId = entry.getValue();
//
//            HashTable.Entry<Integer, String> switchEntry;
//            switch (switchId) {
//                case "172.21":
//                    switchEntry = switchEntry1;
//                    break;
//                case "172.22":
//                    switchEntry = switchEntry2;
//                    break;
//                case "172.23":
//                    switchEntry = switchEntry3;
//                    break;
//                case "172.24":
//                    switchEntry = switchEntry4;
//                    break;
//                default:
//                    throw new IllegalStateException("Unexpected value:" + switchId + ". Incorrect entry in edgeRouterId.txt file.");
//            }
//            if (switchEntry != null) {
//                LOCAL_AREA_NETWORKS.put(cityName, switchEntry);
//                System.out.println("City: " + cityName + " Switch ID: " + switchEntry.getKey() + " Folder: " + switchEntry.getValue());
//
//            }
//        }


        net = new Network(LOCAL_AREA_NETWORKS, filesToTransfer);
    }

    /**
     * Gets the city from the switch id.
     *
     * @param switchId the switch id e.g. "172.23"
     * @return the city name e.g. "NEW YORK CITY"
     */
    public static String getCityNameFromSwitchId(String switchId) {
        for (HashTable.Entry<String, String> entry : edgeRouterId.entrySet()) {
            if (entry.getValue().equals(switchId)) {
                return entry.getKey();
            }
        }
        return null;
    }


    /**
     * Gets the source and destination switches from the LOCAL_AREA_NETWORKS map.
     *
     * @param sourceName      the name of the source switch e.g. "LUCA"
     * @param destinationName the name of the destination switch e.g. "MIGUEL"
     * @return an array of integers with the source and destination switch ids.
     */
    private static int[] getSrcDest(String sourceName, String destinationName) {
        int[] out = new int[2];

        out[0] = LOCAL_AREA_NETWORKS.get(keepUpToSecondPeriod(sourceName)).getKey();
        out[1] = LOCAL_AREA_NETWORKS.get(keepUpToSecondPeriod(destinationName)).getKey();

        return out;
    }

    /**
     * Removes end of IP Address for use in finding the top level IPSwitch.Switch in the IPSwitch.Network.
     *
     * @param text the ip address
     * @return returns the resulted "cut down" string
     */
    private static String keepUpToSecondPeriod(String text) {
        int periodCount = 0;
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == '.') {
                periodCount++;
                if (periodCount == 2) {
                    break;
                }
            }
            result.append(c);
        }
        return result.toString();
    }

    public static String getDestinationEndHostName() {
        return destinationEndHostName;
    }

}