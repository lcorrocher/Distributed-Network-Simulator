package IPSwitch;

import DataStructures.AmGraph;
import DataStructures.HashTable;
import DataStructures.Node;
import utils.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static utils.CSVReader.cityCoordinates;
import static utils.LoadingBar.printDynamicLoadingBar;


/**
 * Class representing the network.
 * The network has a transfer rate, a Hash Table of switches, a table of routers, a map of the topology, and a number of files to transfer.
 */
public class Network {
    private final int TRANSFER_RATE = 0;

    /**
     * maps the city name to the index in the adjacency matrix.
     * used for graph traversal algorithms, primarily Dijkstra's algorithm.
     */
    private Map<String, Integer> cityToIndexMap;

    /**
     * Hash Table of router and switch nodes in the network. The key is the name of the node and the value is the node object. Used for graph traversal.
     */
    private final HashMap<String, Node> nodesByCityName;
    public HashTable<Integer, Switch> switches;
    //private HashTable<Integer, IPSwitch.Router> routers;
    // create HashTable of string, integer, IPSwitch.Router
    /**
     * Hash Table of routers, where the key is the name of the router and the value is a table entry of the router id and the router object.
     * E.g. "CASPER", 1, Router1
     * "TOPEKA", 2, Router2
     * "CHICAGO", 3, Router3
     */
    private HashTable<String, HashTable.Entry<Integer, Router>> routers;
    /**
     * HashTable of the topology of the network, netId -> (nextHopLengthList, routerList)
     * e.g. 1 -> List<Double> -> List<Router>
     */
    private HashTable<Integer, HashTable.Entry<List<Double>, List<Router>>> topology;
    private int filesToTransfer;
    private HashTable<String, Integer> netIdTable;

    private HashTable<String, ArrayList<Packet>> packetsToReassemble;

    // table of string sourcetoDest, integer netId
    public HashTable<String, Integer> createNetIdTable() {
        HashTable<String, Integer> netIdTable = new HashTable<>();
        netIdTable.put("fresnoToNyc", 1);
        netIdTable.put("fresnoToMinneapolis", 2);
        netIdTable.put("fresnoToAustin", 3);
        netIdTable.put("austinToFresno", 4);
        netIdTable.put("austinToMinneapolis", 5);
        netIdTable.put("austinToNyc", 6);
        netIdTable.put("minneapolisToFresno", 7);
        netIdTable.put("minneapolisToAustin", 8);
        netIdTable.put("minneapolisToNyc", 9);
        netIdTable.put("nycToFresno", 10);
        netIdTable.put("nycToAustin", 11);
        netIdTable.put("nycToMinneapolis", 12);
        return netIdTable;
    }

    /**
     * Map which generates the city to index map used for graph traversal algorithms adjacency matrix.
     */
    public void generateCityToIndexMap() {
        cityToIndexMap = new HashMap<>();
        int index = 0;
        for (String cityName : nodesByCityName.keySet()) {
            cityToIndexMap.put(cityName, index);
            index++;
        }
    }

    /**
     * gets the route name from the netId. e.g. 1 -> "fresnoToNyc"
     */
    private String getRouteNameFromNetId(int netId) {
        for (HashTable.Entry<String, Integer> entry : netIdTable.entrySet()) {
            if (entry.getValue() == netId) {
                return entry.getKey();
            }
        }
        return null;
    }

    int sourceSwitch;

/*
     * Method to show the topology of the network.
     * This method is used for debugging purposes.

    private void showTopology() {
        // Double check this
        StringBuilder sb = new StringBuilder();
        sb.append("=========================END SWITCHES==========================\n");
        for (Switch s: switches.values()){
            // TODO: Get rid of or refactor the getCityName method.
            sb.append(Main.getCityNameFromSwitchId(s.getName())).append(": ");
            sb.append(s.getQueue().size()).append(" Packets\n");
        }
        sb.append("============================ROUTERS============================\n");
        for (HashTable.Entry<Integer, Router> m: routers.values()){
            Router r = m.getValue();
            sb.append(r.toString().split(" ")[1]).append(": ");
            sb.append(r.getQueue().size()).append(" Packets\n");
        }
        System.out.println(sb);
    }
*/

    private void instantiateGraph() {

        System.out.println(Colour.yellow("Creating the usaGraph using adjacency matrix...\n"));
        AmGraph usaGraph = new AmGraph(nodesByCityName);

        usaGraph.addEdge("FRESNO", "CASPER");
        usaGraph.addEdge("FRESNO", "TOPEKA");
        usaGraph.addEdge("AUSTIN", "CASPER");
        usaGraph.addEdge("AUSTIN", "TOPEKA");
        usaGraph.addEdge("AUSTIN", "CHICAGO");
        usaGraph.addEdge("NEW YORK CITY", "TOPEKA");
        usaGraph.addEdge("NEW YORK CITY", "CHICAGO");
        usaGraph.addEdge("MINNEAPOLIS", "CASPER");
        usaGraph.addEdge("MINNEAPOLIS", "TOPEKA");
        usaGraph.addEdge("MINNEAPOLIS", "CHICAGO");
        usaGraph.addEdge("CASPER", "TOPEKA");
        usaGraph.addEdge("CASPER", "CHICAGO");
        usaGraph.addEdge("TOPEKA", "CHICAGO");

        usaGraph.printGraph();
    }


    /**
     * Method to run the network at a national scale.
     *
     * @throws InterruptedException If the thread is interrupted.
     * @throws IOException          If the file is not found or cannot be read.
     */
    public void runWAN() throws InterruptedException, IOException {
        System.out.println("\n \nDestination end host located in WIDER AREA NETWORK (WAN)...");
        Thread.sleep(2000);
        System.out.println("Packets will be routed through WAN...");
        Thread.sleep(2000);
        System.out.println("Sending packets to end host in 3 seconds...");
        Thread.sleep(3000);

        long start = System.currentTimeMillis();
        int counter = 0;
        while (filesToTransfer > 0) {
            System.out.println("=====================================");

//            if (counter % 10 == 0) {
//                showTopology();
//            }



            for (HashTable.Entry<Integer, Switch> entry : switches.entrySet()) {
                List<Double> nextHopsList = topology.get(entry.getKey()).getKey();
                List<Router> nextRouterList = topology.get(entry.getKey()).getValue();

                double currentNextHop = nextHopsList.get(counter % (nextHopsList.size()));
                Router currentRouter = nextRouterList.get(counter % (nextRouterList.size()));
                Router nextRouter = nextRouterList.get((counter + 1) % (nextRouterList.size()));


                double distanceToNextNode = currentRouter.getDistanceTo(nextRouter);

                try {
                    currentRouter.top();
                } catch (NoSuchElementException e) {
                    continue; // queue is empty
                }

                Packet p = currentRouter.top();

                // if the packet at the top of the current router queue is a packet
                // that is destined to this netid then we should process it
                if (p.getDestNetId() == entry.getKey()) {
                    p = currentRouter.dequeue();

                    // if it's the last router in our routing path
                    // we should enqueue it to the switch
                    if ((counter + 1) % (nextRouterList.size()) == 0) {
                        Switch destSwitch = switches.get(entry.getKey());

                        sendPacketsAndReassemble(p, destSwitch, start);
                    } else {
                        // if it's not the last router in our routing path
                        nextRouter.enqueue(p);
                    }
                }
            }
            //Thread.sleep(TRANSFER_RATE);
            counter++;
        }
        System.out.println(Colour.yellowBold("\nTransfer complete.\nPlease check the destination folder for the transferred file. The log file contains details on processing speed.\n"));
    }

    public void runResponseWAN(String originCity) throws InterruptedException {
        System.out.println("\nBroadcast response initiated for origin city: " + originCity + "...");
        System.out.println("Broadcast response packet will be sent to " + originCity + " in 3 seconds...\n");
        Thread.sleep(3000);

        int counter = 0;
        for (int i = 0; i < 7; i++) { // ramdom, idk how many times to run this. Run this as long as it takes to route to all edge routers
            // ===================================== Issue here.Loop is incorrect.


            //showTopology();

            // for each switch in the network, we will route the packets
            for (HashTable.Entry<Integer, Switch> entry : switches.entrySet()) {
                List<Double> nextHopsList = topology.get(entry.getKey()).getKey();
                List<Router> nextRouterList = topology.get(entry.getKey()).getValue();

                double currentNextHop = nextHopsList.get(counter % (nextHopsList.size()));
                Router currentRouter = nextRouterList.get(counter % (nextRouterList.size()));
                Router nextRouter = nextRouterList.get((counter + 1) % (nextRouterList.size()));

                try {
                    currentRouter.top();
                } catch (NoSuchElementException e) {
                    continue; // queue is empty
                }


                // =========================================== Issue here=======================================

                // it's getting the top packet in the queue but it's not the brp because the others haven't been dequeued yet.
                Packet brp = currentRouter.top();


                // if the packet at the top of the current router queue is a packet
                // that is destined to this netid then we should process it
                // swap before dequeuing
                if (brp.getDestNetId() == entry.getKey()) {
                    brp = currentRouter.dequeue();

                    // if it's the last router in our routing path
                    // we should enqueue it to the switch
                    if ((counter + 1) % (nextRouterList.size()) == 0) {
                        Switch destSwitch = switches.get(entry.getKey());

                        // assemble the brp (broadcast response packet) at the destination switch
                        destSwitch.enqueue(brp);

                        // after we enqueue from the last router in the routing path
                        // to the destination switch for this message
                        // we check if it was the last packet in the for reassembling the message
                        int numPackets = brp.getNumPacketsInPayload();
                        String msgId = brp.getMsgId();
                        System.out.println("Packets being transmitted through the network...");

                        int numPacketsRecv = 0;
                        for (int element = destSwitch.getQueue().size() - 1; element >= 0; element--) {
                            Packet packetInQueue = destSwitch.getQueue().peek(element);
                            if (Objects.equals(packetInQueue.getMsgId(), msgId)) {
                                numPacketsRecv++;
                                printDynamicLoadingBar(numPacketsRecv, numPackets);
                            }
                        }

                        // if all packets have been received and payload is "BROADCAST" + destination switch name:
                        // execute method to return the endhost
                        String cityName = Main.getCityNameFromSwitchId(destSwitch.getName());
                        if (numPacketsRecv == numPackets) {
                            System.out.println("\nNumber of Packets Received: " + numPacketsRecv);

                            System.out.println("Broadcast packet " + brp + " received successfully: " + cityName);

                            // remove all packets in network
                            clearNetwork();
                            return;

                        } else {
                            // if it's not the last router in our routing path
                            nextRouter.enqueue(brp);
                        }
                    }
                }
                //Thread.sleep(TRANSFER_RATE);
                counter++;
            }
        }
    }

    public EndHost runBroadcastWAN(String destinationHostName) throws InterruptedException {
        System.out.println("\n\nBroadcasting request packets to all edge routers...");
        Thread.sleep(2000);
        System.out.println("Broadcast request packets will be routed through WAN...");
        Thread.sleep(2000);
        System.out.println("Sending broadcast request packets to edge routers in 3 seconds...");
        Thread.sleep(3000);

        long start = System.currentTimeMillis();
        int counter = 0;
        EndHost targetEndHost = null;


        outerLoop:
        for (int i = switches.entrySet().size(); i >= 0; i--) { // run maximum 4 times

            //showTopology();


            innerLoop:
            // entry.getKey() is the netId. e.g. 1 for route from Fresno to NYC, 2 for route from Fresno to Minneapolis
            // entry.getValue() is the switch

            // for each switch in the network, we will route the packets
            for (HashTable.Entry<Integer, Switch> entry : switches.entrySet()) {
                List<Double> nextHopsList = topology.get(entry.getKey()).getKey();
                List<Router> nextRouterList = topology.get(entry.getKey()).getValue();

                double currentNextHop = nextHopsList.get(counter % (nextHopsList.size()));
                Router currentRouter = nextRouterList.get(counter % (nextRouterList.size()));
                Router nextRouter = nextRouterList.get((counter + 1) % (nextRouterList.size()));

                try {
                    currentRouter.top();
                } catch (NoSuchElementException e) {
                    continue; // queue is empty
                }

                Packet p = currentRouter.top();

                if (p.getDestNetId() == entry.getKey()) {
                    p = currentRouter.dequeue();

                    // if targetEndHost has not already been found
                    if (targetEndHost == null) {

                        if ((counter + 1) % (nextRouterList.size()) == 0) {
                            Switch destSwitch = switches.get(entry.getKey());

                            targetEndHost = sendBroadcastPacketsAndReassemble(p, destSwitch, destinationHostName);
                        } else {
                            nextRouter.enqueue(p);
                        }
                    }

                    // remove all packets in network
                    // dequeue remaining packets from all the routers and switches in network
                    clearNetwork();
                    break innerLoop;
                }
            }
            counter++;
        }
        long timeTaken = System.currentTimeMillis() - start;

        String logMessage = "[Broadcast Request]  Time taken: " + timeTaken + "ms";
        Logger.log(logMessage);
        System.out.println(Colour.yellow(logMessage));
        System.out.println(Colour.yellow("Time has been logged"));

        Thread.sleep(2000);

        return targetEndHost;
    }



    // TODO: make msgId to remove packets which contain the same msgId
    /**
     * Method to clear the network of packets. Dequeues all packets from all routers and switches in the network.
     * If the queue is empty, it ignores it and continues to the next queue.
     */
    private void clearNetwork() {
        for (HashTable.Entry<Integer, Switch> switchEntry : switches.entrySet()) {
            Switch currentSwitch = switchEntry.getValue();
            try {
                currentSwitch.top();
            } catch (NoSuchElementException e) {
                continue; // queue in switch is empty -ignore
            }
            switchEntry.getValue().dequeue();

            for (Router router : topology.get(switchEntry.getKey()).getValue()) {
                try {
                    router.top();
                } catch (NoSuchElementException e) {
                    continue; // queue in router is empty - ignore
                }
                router.dequeue();
            }
        }
    }

    /**
     * Method to run the network at a local scale.
     *
     * @throws InterruptedException If the thread is interrupted.
     * @throws IOException          If the file is not found or cannot be read.
     */
    public void runLAN() throws InterruptedException, IOException {
        System.out.println("\n \nDestination end host located in LOCAL AREA NETWORK (LAN)...");
        Thread.sleep(2000);
        System.out.println("Packets will be routed through LAN...");
        Thread.sleep(2000);
        System.out.println("Sending packets to end host in 3 seconds...");
        Thread.sleep(3000);

        long start = System.currentTimeMillis();

        while (filesToTransfer > 0) {
            Switch firstSwitch = switches.get(sourceSwitch);

            if (firstSwitch.getQueue().size() > 2) {
                randomSwap(firstSwitch);
            }

            Packet p = firstSwitch.dequeue();

            sendPacketsAndReassemble(p, firstSwitch, start);
        }
    }

    private EndHost sendBroadcastPacketsAndReassemble(Packet bp, Switch destSwitch, String destinationHostName) throws InterruptedException {

        //Fix later
        destSwitch.enqueue(bp);

        // after we enqueue from the last router in the routing path
        // to the destination switch for this message
        // we check if it was the last packet in the for reassembling the message
        int numPackets = bp.getNumPacketsInPayload();
        String msgId = bp.getMsgId();
        System.out.println("Packets being transmitted through the network...");

        int numPacketsRecv = 0;
        for (int element = destSwitch.getQueue().size() - 1; element >= 0; element--) {
            Packet packetInQueue = destSwitch.getQueue().peek(element);
            if (Objects.equals(packetInQueue.getMsgId(), msgId)) {
                numPacketsRecv++;
                printDynamicLoadingBar(numPacketsRecv, numPackets);

            }
        }


        // if all packets have been received and payload is "BROADCAST" + destination switch name:
        // then we can start the local ARP request which searches the local cache for the destination end host
        String cityName = Main.getCityNameFromSwitchId(destSwitch.getName());

        if (numPacketsRecv == numPackets) {

            System.out.println("\nNumber of Packets Received: " + numPacketsRecv);
            System.out.println("Broadcast packet " + bp + " has reached destination switch: " + cityName);
            System.out.println("\nExecuting local arp request in " + cityName + "...");
            Thread.sleep(2000);

            EndHost targetEndHost = ARP.localARPRequest(cityName, destinationHostName);
            // if endhost is found
            if (targetEndHost != null) {
                System.out.println(Colour.yellowBold("\nLocal ARP request successful in " + cityName + "!"));
                System.out.println(Colour.yellow("Aborting broadcast to other edge routers...\n"));
                Thread.sleep(1000);
                return targetEndHost;
            } else {
                System.out.println(Colour.yellow("Local ARP request failed! Checking next edge router..."));
            }
        } else {
            System.out.println(Colour.red("Broadcast packet has failed to reach destination switch: " + cityName
                                        + ".\nExiting..."));
            System.exit(1);
        }
        return null;
    }

    /**
     * Method to find if the Switch is the final Switch in the global network, which is also the root node in our tree.
     * Calls our tree dfs algorithm to find the endhost in the tree.
     * Reassembles packets at this endhost and writes to our output folder.
     *
     * @throws IOException If the file is not found or cannot be read.
     */
    private void sendPacketsAndReassemble(Packet p, Switch currentSwitch, long start) throws IOException, InterruptedException {
        Switch treeDestSwitch = null;
        String switchIdToFind = p.getDestNetIdFull();

        TreeNode switchNode = currentSwitch.getLanTree().findSwitchNodeByName(switchIdToFind);
        if (switchNode != null) {
            treeDestSwitch = switchNode.getSwitchNode();
            //System.out.println("EndHost found: " + treeDestSwitch.getName() + " (ID: " + treeDestSwitch.getId() + ")");
        } else {
            System.out.println("EndHost with ID " + switchIdToFind + " not found.");
        }

        //Fix later
        treeDestSwitch.enqueue(p);

        // after we enqueue from the last router in the routing path
        // to the destination switch for this message
        // we check if it was the last packet in the for reassembling the message
        int numPackets = p.getNumPacketsInPayload();
        String msgId = p.getMsgId();
        System.out.println("Packets being transmitted through the network...");

        int numPacketsRecv = 0;
        for (int element = treeDestSwitch.getQueue().size() - 1; element >= 0; element--) {
            Packet packetInQueue = treeDestSwitch.getQueue().peek(element);
            if (Objects.equals(packetInQueue.getMsgId(), msgId)) {
                numPacketsRecv++;
                printDynamicLoadingBar(numPacketsRecv, numPackets);
            }
        }

        // if it is (i.e. in the switch queue we have N packets with the same msgId as the current enqueued packet
        // then we can copy them in the reassembly array that will need to be sorted
        // before reassembling the payload, copying it to the filepath pointed by the Switch object
        // and then removing the msgId key from the reassembly array since it will not be needed anymore
        // the packets will still remain in the queue
        // there when they are not needed anymore)

        // I've changed destSwitch.getQueue.peek() to destSwitch.getQueue.dequeue()
        // Functionality remained the same, not sure how this relates to the original
        // But the queue should be empty once the loop finishes now.
        // However, implemented a counter of number of files being transferred.
        // Not ideal, but will figure something more elegant out when we start doing multi-packet transfers.


        if (numPacketsRecv == numPackets) {
            System.out.println("\nNumber of Packets Received: " + numPacketsRecv);
            ArrayList<Packet> packets = new ArrayList<>();
            packetsToReassemble.put(msgId, packets);
            for (int element = treeDestSwitch.getQueue().size() - 1; element >= 0; element--) {
                Packet packetInQueue = treeDestSwitch.getQueue().peek(element);

                if (Objects.equals(packetInQueue.getMsgId(), msgId)) {
                    packets.add(packetInQueue);
                }
            }
            List<Packet> packets1 = packetsToReassemble.get(msgId);

            System.out.println("\n\nReceived all packets with msgId = " + p.getMsgId());
            String data = PacketMethods.readPackets(packets1, false);
            String text = BinaryToTxt.convertToText(data);
            System.out.println("Reassembling Message...\n");
            Thread.sleep(2000);
            data = PacketMethods.readPackets(packets1, true);

            // e.g. server/io/lans/austin/austinFolder

            String cityFolder = ARP.getIoLansDir() + File.separator + Main.getEndHostFoundCity().toLowerCase() + File.separator + treeDestSwitch.getFolderPath();
            String endHostFolder = cityFolder + File.separator + Main.getDestinationEndHostName();
            ARP.addToEndHostFolderPaths(endHostFolder);

            String goodPath = endHostFolder + "\\" + "good";
            String badPath = endHostFolder + "\\" + "bad";

            CreateOutputFile.createDirectory(goodPath);
            CreateOutputFile.createDirectory(badPath);

            CreateOutputFile.createDirectory(endHostFolder);
            CreateOutputFile.createTextFile(Paths.get(badPath, p.getFileName() + ".out.bad").toString(), text);
            text = BinaryToTxt.convertToText(data);
            CreateOutputFile.createTextFile(Paths.get(goodPath, p.getFileName() + ".out").toString(), text);

            long timeTaken = System.currentTimeMillis() - start;
            String logMessage = "[Message Transmission] Time taken for file: " + timeTaken + "ms. \n" +
                                "Number of Packets in file [" + p.getFileName() + "] (msgId: " + p.getMsgId() + "): " + numPacketsRecv;
            Logger.log(logMessage);
            System.out.println(Colour.yellow(logMessage));
            System.out.println(Colour.yellow("Time has been logged"));

            //CHRIS: REMOVED THIS LINE, CAUSING ERRORS, INCOMPATIBLE WITH HASHTABLE
            // packetsToReassemble.remove(msgId);
            filesToTransfer--;
        }
    }

    /**
     * Method to add a packet to a switch.
     *
     * @param switchId The id of the switch to add the packet to.
     * @param p        The packet to add to the switch.
     */
    public void addToSwitch(int switchId, Packet p) {
        //Save Source Switch within our Network to later reference
        sourceSwitch = switchId;

        //Check if source and destination .Switch are the same
        if (p.getSrcNetId() == p.getDestNetId()) {
            //If they are equal, we enqueue the packets to the root of our tree for use within our LAN network.
            switches.get(switchId).enqueue(p);
        } else {
            //If they are not equal, then we enqueue on the first Router in our topology of our global network.
            topology.get(switchId).getValue().get(0).enqueue(p);
        }
    }

    /**
     * Method to swap packets in the network.
     *
     * @param n The node to swap packets in.
     *          The method should peek the first 3 packets with the SAME msgId from the router queue and then swap them based on a random probability.
     */
    private void randomSwap(Node n) {
        /* generate random number between 0 and 0
         p(1, 2) = 0.20
         p(1, 3) = 0.25
         p(2, 3) = 0.10
         p(no swap) = 0.05
         */

        int[] indexes = n.findIndexCommonMsgId(n.getQueue().top().getMsgId());
        int second = indexes[0];
        int third = indexes[1];

        double roll = Math.random();
        if (roll <= 0.20) {
            // swap packet 1 with packet 2
            n.swap(n.getQueue(), 0, second);
        } else if (roll <= 0.45) {
            ;
            // swap packet 2 with packet 3
            n.swap(n.getQueue(), second, third);
        } else if (roll <= 0.95) {
            n.swap(n.getQueue(), second, third);
        }
    }

    /**
     * the routing of the packets through each node in the network
     * This builds the topology of the network.
     *
     * @throws IOException If the file is not found or cannot be read.
     */
    private void route() throws IOException, InterruptedException {
        List<String[]> lines = FileUtils.readFile("server/topology.txt");
        for (String[] arr : lines) {
            System.out.println(arr[1].split("\\|")[1]);
        }

        this.netIdTable = createNetIdTable();

        String route;

        for (String[] entry : lines) {
            String netName = entry[0].split("\\|")[0];
            int netId = netIdTable.get(netName);

            route = getRouteNameFromNetId(netId);

            System.out.print("[NETWORK] - Building Routing table for " );
            System.out.print(Colour.yellow(route));
            System.out.print(" with");
            System.out.println(Colour.yellow(" netID = " + netId));

            // TODO: BUG here
            // only creates routers it hasn't already created or seen before from topology.txt file

            if (topology.containsKey(netId)) {
                for (int i = 1; i < entry.length; i++) {
                    String cityName = entry[i].split("\\|")[0];
                    int routerId = Integer.parseInt(entry[i].split("\\|")[1]);
                    double nextHopLength = Double.parseDouble(entry[i].split("\\|")[2]);
                    Coordinates coordinates = cityCoordinates.get(cityName);
                    Router r = new Router(routerId, cityName, coordinates);
                    nodesByCityName.put(cityName, r);

                    // TODO: REMOVE Thread and System.out.println
//                    System.out.println(r + routerId + coordinates);
                    System.out.println(r);
                    Thread.sleep(2000);

                    // if router HashTable already contains city entry
                    if (routers.containsKey(cityName)) {
                        HashTable.Entry<Integer, Router> routerEntry = routers.get(cityName);

                        if (routerEntry.getKey() == routerId) {
                            r = routerEntry.getValue();
                        }
                    }

                    HashTable.Entry<Integer, Router> cityRouterEntry = new HashTable.Entry<>(routerId, r);
                    routers.put(cityName, cityRouterEntry);
                    topology.get(netId).getKey().add(nextHopLength);
                    topology.get(netId).getValue().add(r);
                }
            }
        }
        System.out.println("\n" + netIdTable.entrySet().size() + " routes built.");

        System.out.println("[NETWORK] - Init Topology: " + topology);
        System.out.println(Colour.green("[NETWORK] - Topology built successfully!\n"));

        // TODO: NOT SURE WHAT NETID IS USED FOR HERE
        // print out the topology
        for (HashTable.Entry<Integer, HashTable.Entry<List<Double>, List<Router>>> entry : topology.entrySet()) {

            route = getRouteNameFromNetId(entry.getKey());

            System.out.println(Colour.yellow("route: " + route));
            System.out.println(Colour.yellow("NextHopLengthList: " + entry.getValue().getKey()));
            System.out.println(Colour.yellow("RouterList: " + entry.getValue().getValue()));
            System.out.println();
        }
        Thread.sleep(3000);

        instantiateGraph();
    }

    /**
     * Constructor for the network.
     *
     * @param localAreaNetworks A map of local area networks, where the key is the name of the network and the value is a map entry of the network id and the folder path.
     * @param filesToTransfer   The number of files to transfer from a source folder to a destination folder.
     * @throws IOException If the file is not found or cannot be read.
     */
    public Network(HashTable<String, HashTable.Entry<Integer, String>> localAreaNetworks,int filesToTransfer) throws IOException, InterruptedException {
        // create n switches
        this.nodesByCityName = new HashTable<>();
        switches = new HashTable<>();
        routers = new HashTable<>();
        topology = new HashTable<>();
        packetsToReassemble = new HashTable<>();
        this.filesToTransfer = filesToTransfer;


        System.out.println("BUILDING SWITCHES...");
        Thread.sleep(2000);

        for (HashTable.Entry<String, HashTable.Entry<Integer, String>> entry : localAreaNetworks.entrySet()) {
            String networkIP = entry.getKey();
            String cityName = Main.getCityNameFromSwitchId(networkIP);
            Integer networkId = entry.getValue().getKey();
            String folderPath = entry.getValue().getValue();
            Coordinates coordinates = cityCoordinates.get(cityName);
            Switch s = new Switch(networkId, networkIP, folderPath, coordinates);
            nodesByCityName.put(cityName, s);

            // TODO: REMOVE Thread and System.out.println
            System.out.println(s);
            Thread.sleep(2000);

            switches.put(networkId, s);

            ArrayList<Double> nextHopLengthList = new ArrayList<>();
            ArrayList<Router> routerList = new ArrayList<>();
            HashTable.Entry<List<Double>, List<Router>> TableEntry = new HashTable.Entry<>(nextHopLengthList, routerList);
            topology.put(networkId, TableEntry);
            System.out.println("[NETWORK] - Created IPSwitch.Switch " + s.getName() + " with netID = " + s.getId());



            //Create our LAN Network. Each Switch is the root node of a Tree.
            Switch childNodeSwitchA = new Switch(1, networkIP + ".001", entry.getValue().getValue(), null);
            Switch childNodeSwitchB = new Switch(2, networkIP + ".002", entry.getValue().getValue(), null);
            TreeNode childNodeSwitchATreeNode = new TreeNode(childNodeSwitchA);
            TreeNode childNodeSwitchBTreeNode = new TreeNode(childNodeSwitchB);
            s.getLanTree().getRoot().addChild(childNodeSwitchATreeNode);
            s.getLanTree().getRoot().addChild(childNodeSwitchBTreeNode);

            //Create the child nodes of our tree.
            Switch childNodeEndpointA = new Switch(4, childNodeSwitchA.getName() + ".1", entry.getValue().getValue(), null);
            Switch childNodeEndpointB = new Switch(5, childNodeSwitchA.getName() + ".2", entry.getValue().getValue(), null);
            Switch childNodeEndpointC = new Switch(6, childNodeSwitchB.getName() + ".3", entry.getValue().getValue(), null);
            Switch childNodeEndpointD = new Switch(7, childNodeSwitchB.getName() + ".4", entry.getValue().getValue(), null);
            TreeNode childNodeEndpointATreeNode = new TreeNode(childNodeEndpointA);
            TreeNode childNodeEndpointBTreeNode = new TreeNode(childNodeEndpointB);
            TreeNode childNodeEndpointCTreeNode = new TreeNode(childNodeEndpointC);
            TreeNode childNodeEndpointDTreeNode = new TreeNode(childNodeEndpointD);
            childNodeSwitchATreeNode.addChild(childNodeEndpointATreeNode);
            childNodeSwitchATreeNode.addChild(childNodeEndpointBTreeNode);
            childNodeSwitchBTreeNode.addChild(childNodeEndpointCTreeNode);
            childNodeSwitchBTreeNode.addChild(childNodeEndpointDTreeNode);

            System.out.println("[NETWORK] - Created LAN " + s.getLanTree().toString());
        }

        System.out.println("[NETWORK] - Empty Topology: " + topology);

        // create the topology and routers simulating a routing algorithm already in place
        this.route();
    }

}
