package IPSwitch;

import DataStructures.AmGraph;
import DataStructures.HashTable;
import DataStructures.Node;
import utils.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static utils.CSVReader.cityCoordinates;
import static utils.LoadingBar.printDynamicLoadingBar;


/**
 * Class representing the network.
 * The network has a transfer rate, a Hash Table of switches, a table of routers, a map of the topology, and a number of files to transfer.
 */
public class Network {
    /**
     * The adjacency matrix graph of the network.
     */
    private AmGraph usaGraph;
    /**
     * algorithm choice for routing packets through the network. Can be A*, Dijkstra, or preconfigured routing table, based off the user's decision in the GUI controller.
     */
    private final String algorithm;
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

    /**
     * hashtable which is instantiated and maps the netId to the route name
     * @return the hashtable
     */
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

    /**
     * creates the graph of the network using an adjacency matrix and adds the edges between the switches to mimic the topology of the network.
     * edges are added using String city names
     * nodesByCityName is a map of all routers and switches the city name to their respective node object
     */
    private void instantiateGraph() {

        System.out.println(Colour.yellow("Creating the usaGraph using adjacency matrix...\n"));
        this.usaGraph = new AmGraph(nodesByCityName);

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
     * used to represent all dijkstra paths between all switches in the network so it can integrate with code.
     * added condition that it must pass through an active router.
     * @return list of all shortest paths between all switches in the network.
     */
    public List<List<Node>> calculateAllShortestPaths() {
        long startTime = System.currentTimeMillis();
        System.out.println(Colour.yellow("\n\nCalculating shortest paths for all LAN routes in the network...\n"));

        IPSwitch.Paths paths = new IPSwitch.Paths(cityToIndexMap, nodesByCityName);

        List<List<Node>> shortestPaths = new ArrayList<>();
        for (String sourceCity : nodesByCityName.keySet()) {
            if (!(nodesByCityName.get(sourceCity) instanceof Switch)) {
                continue;
            }
            for (String destinationCity : nodesByCityName.keySet()) {
                if (!(nodesByCityName.get(destinationCity) instanceof Switch)) {
                    continue;
                }
                if (!sourceCity.equals(destinationCity)) {
                    List<Node> shortestPath = paths.dijkstra(usaGraph.getAdjacencyMatrix(), sourceCity, destinationCity);
                    if (shortestPath.stream().anyMatch(node -> node instanceof Router && ((Router) node).isActive())) {
                        shortestPaths.add(shortestPath);
                    }
                }
            }
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        Logger.log("[Dijkstra's algorithm - all routes] Time taken to execute: " + timeTaken + "ms");
        System.out.println(Colour.yellow("\nTime has been logged\n"));
        return shortestPaths;
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


        long start = System.currentTimeMillis();
        List<List<Node>> shortestPathsList;

        switch (algorithm) {
            case "Dijkstra":

                System.out.println("Using Dijkstra's algorithm to route packets through the network...");
                generateCityToIndexMap();
                System.out.println("City to Index Map: " + cityToIndexMap);
                System.out.println("Adjacency matrix:\n" + usaGraph.getAdjacencyMatrix() + "\n");

                // pretty print the adjacency matrix
                for (Map.Entry<Node, Map<Node, Double>> row: usaGraph.getAdjacencyMatrix().entrySet()) {
                    for (Map.Entry<Node, Double> col: row.getValue().entrySet()) {
                        System.out.println(row.getKey() + " -> " + col.getKey() + " : " + col.getValue());
                    }
                }

                Thread.sleep(2000);

                shortestPathsList = calculateAllShortestPaths();
                System.out.println("Shortest paths list: \n");
                for (List<Node> path : shortestPathsList) {
                    System.out.println(path);
                }

                Scanner in = new Scanner(System.in);
                System.out.println((Colour.yellowBold("Would you like to deactivate a router link in the network? (Y/N)")));
                String response = in.nextLine();
                if (response.equalsIgnoreCase("Y"))  {
                    while(true) {
                        System.out.println((Colour.yellow("Choose a router from the following to deactivate:")));
                        List<String> routerNamesList = getRouters();
                        routerNamesList.forEach(System.out::println);
                        System.out.println(Colour.yellow("\nEnter the name of the router to deactivate:"));
                        String routerToDeactivate = in.nextLine();

                        routerToDeactivate = routerToDeactivate.toUpperCase();
                        System.out.println("selected router to deactivate: " + routerToDeactivate);
                        if(nodesByCityName.containsKey(routerToDeactivate)) {
                            Router router = (Router) nodesByCityName.get(routerToDeactivate);
                            deactivateRouter(router);
                            break;
                        } else {
                            System.out.println(Colour.red("Invalid router name. Please try again."));
                        }
                    }
                    System.out.println("\nRecalculating shortest paths...");
                    shortestPathsList = calculateAllShortestPaths();
                    System.out.println("\nRevised shortest paths list is seen below. It does not contain the deactivated router.\n");
                    for (List<Node> path : shortestPathsList) {
                        System.out.println(path);
                    }
                }

                Thread.sleep(2000);
                System.out.println("\nSending packets to end host in 3 seconds...");
                Thread.sleep(3000);

                int counter = 0;
                while (filesToTransfer > 0) {
                    for (List<Node> path : shortestPathsList) {
                        // using first node .get(0) in path as the algorithm reverse engineers the path
                        Switch destSwitchInPath = (Switch) path.get(0);
                        List<Router> routersInPath = new ArrayList<>();
                        for (int i = 1; i < path.size() - 1; i++) {
                            Node node = path.get(i);
                            if (node instanceof Router) {
                                routersInPath.add((Router) node);
                            }
                        }

                        for(int i = 0; i < topology.get(sourceSwitch).getValue().get(0).getQueue().size(); i++){
                            routersInPath.get(0).enqueue(topology.get(sourceSwitch).getValue().get(0).dequeue());
                        }

                        int networkId = getKeyOfSwitch(destSwitchInPath);
                        for (int i = 0; i < routersInPath.size(); i++) {
                            Router currentRouter = routersInPath.get(i % routersInPath.size());
                            Router nextRouter = routersInPath.get((i + 1) % routersInPath.size());

                            try {
                                currentRouter.top();
                            } catch (NoSuchElementException e) {
                                continue;
                            }

                            Packet p = currentRouter.top();

//                            System.out.println("Packet Destination Net ID: " + p.getDestNetId());
//                            System.out.println("Network ID: " + networkId);
                            if (p.getDestNetId() == networkId) {
                                p = currentRouter.dequeue();
                                // if it's the last router in our routing path
                                // we should enqueue it to the switch
                                if (i == routersInPath.size() - 1) {
                                    sendPacketsAndReassemble(p, destSwitchInPath, start);
                                } else {
                                    // if it's not the last router in our routing path
                                    nextRouter.enqueue(p);
                                }
                            }
                        }
                        counter++;
                    }
                }
                break;


            case "A*":
                // TODO: Implement A* algorithm
                System.out.println("Using A* algorithm to route packets through the network...");
                counter = 0;
                while (filesToTransfer > 0) {
                    System.out.println("=====================================");
                }
                break;

            case "Preconfigured routing table":

                System.out.println("Using preconfigured routing table (static) to route packets through the network...");
                counter = 0;
                while (filesToTransfer > 0) {
                    System.out.println("=====================================");

                    for (HashTable.Entry<Integer, Switch> entry : switches.entrySet()) {
                        List<Router> nextRouterList = topology.get(entry.getKey()).getValue();

                        Router currentRouter = nextRouterList.get(counter % (nextRouterList.size()));
                        Router nextRouter = nextRouterList.get((counter + 1) % (nextRouterList.size()));

                        try {
                            currentRouter.top();
                        } catch (NoSuchElementException e) {
                            continue; // queue is empty
                        }

                        Packet p = currentRouter.top();

                       /* // if the packet at the top of the current router queue is a packet
                        // that is destined to this netid then we should process it
                        System.out.println("Packet Destination Net ID: " + p.getDestNetId());
                        System.out.println("Entry Key: " + entry.getKey());
*/
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
                        counter++;
                    }
                }
                break;
        }
        System.out.println(Colour.yellowBold("\nTransfer complete.\nPlease check the destination folder for the transferred file. The log file contains details on processing speed.\n"));
    }

    /**
     * Deactivates the router by setting the active status to false.
     * @param router the Router router to deactivate
     */
    private void deactivateRouter(Router router) {
        router.setActive(false);
    }

    /**
     * routes the broadcast response packets through the Wan to the origin city and returns the end host
     * @param originCity the city that calls the initial broadcast request
     * @throws InterruptedException if the thread is interrupted
     */
    public void runResponseWAN(String originCity) throws InterruptedException {
        System.out.println("\nBroadcast response initiated for origin city: " + originCity + "...");
        System.out.println("Broadcast response packet will be sent to " + originCity + " in 3 seconds...\n");
        Thread.sleep(3000);

        int counter = 0;
        for (int i = 0; i < 7; i++) { // ramdom, idk how many times to run this. Run this as long as it takes to route to all edge routers
            // ===================================== Issue here.Loop is incorrect.

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

                Packet brp = currentRouter.top();

               /*  if the packet at the top of the current router queue is a packet
                 that is destined to this netid then we should process it
                 swap before dequeuing*/
                if (brp.getDestNetId() == entry.getKey()) {
                    brp = currentRouter.dequeue();

                    // if it's the last router in our routing path
                    // we should enqueue it to the switch
                    if ((counter + 1) % (nextRouterList.size()) == 0) {
                        Switch destSwitch = switches.get(entry.getKey());

                        // assemble the brp (broadcast response packet) at the destination switch
                        destSwitch.enqueue(brp);

                      /*   after we enqueue from the last router in the routing path
                         to the destination switch for this message
                         we check if it was the last packet in the for reassembling the message*/
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

    /**
     * routes the broadcast request packets through the Wan and returns the end host
     * @param destinationHostName destination host name
     * @return the end host
     * @throws InterruptedException if the thread is interrupted
     */
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

                    break innerLoop;
                }
            }
            counter++;
        }

        clearNetwork();
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


    // TODO: use this method to clear network instead. It deletes ALL packets from routers and switches, not just one.
    /*private void clearNetwork() {
        for (HashTable.Entry<Integer, Switch> switchEntry : switches.entrySet()) {
            Switch currentSwitch = switchEntry.getValue();
            while (!currentSwitch.getQueue().isEmpty()) {
                currentSwitch.dequeue();
            }

            for (Router router : topology.get(switchEntry.getKey()).getValue()) {
                while (!router.getQueue().isEmpty()) {
                    router.dequeue();
                }
            }
        }
    }*/

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

    /**
     * sends broadcast packets to all edge routers in the network and rebuilds the message at the destination switch.
     * @param bp broadcast packet
     * @param destSwitch destination switch
     * @param destinationHostName destination host name
     * @return the end host
     * @throws InterruptedException if the thread is interrupted
     */
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

        /*// debugging purposes
        System.out.println("Destination Switch ID: " + switchIdToFind);
        System.out.println("Current Switch ID: " + currentSwitch.getCityName());
        System.out.println("Current Switch ID: " + currentSwitch.getName());*/

        TreeNode switchNode = currentSwitch.getLanTree().findSwitchNodeByName(switchIdToFind);
        if (switchNode != null) {
            treeDestSwitch = switchNode.getSwitchNode();
        } else {
            System.out.println("EndHost with ID " + switchIdToFind + " not found.");
        }

        treeDestSwitch.enqueue(p);

        /* after we enqueue from the last router in the routing path
         to the destination switch for this message
         we check if it was the last packet in the for reassembling the message*/
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

        /* if it is (i.e. in the switch queue we have N packets with the same msgId as the current enqueued packet
         then we can copy them in the reassembly array that will need to be sorted
         before reassembling the payload, copying it to the filepath pointed by the Switch object
         and then removing the msgId key from the reassembly array since it will not be needed anymore
         the packets will still remain in the queue
         there when they are not needed anymore)
        */

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
                    "Number of Packets in file [" + p.getFileName() + "] (msgId: " + p.getMsgId() + "): " + numPacketsRecv + "packets";
            Logger.log(logMessage);
            System.out.println(Colour.yellow(logMessage));
            System.out.println(Colour.yellow("Time has been logged"));

            /*CHRIS: REMOVED THIS LINE, CAUSING ERRORS, INCOMPATIBLE WITH HASHTABLE
             packetsToReassemble.remove(msgId);*/
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

            System.out.print("[NETWORK] - Building Routing table for ");
            System.out.print(Colour.yellow(route));
            System.out.print(" with");
            System.out.println(Colour.yellow(" netID = " + netId));

            /* TODO: BUG here
             only creates routers it hasn't already created or seen before from topology.txt file
*/

            if (topology.containsKey(netId)) {
                for (int i = 1; i < entry.length; i++) {
                    String cityName = entry[i].split("\\|")[0];
                    int routerId = Integer.parseInt(entry[i].split("\\|")[1]);
                    double nextHopLength = Double.parseDouble(entry[i].split("\\|")[2]);
                    Coordinates coordinates = cityCoordinates.get(cityName);
                    Router r = new Router(routerId, cityName, coordinates);
                    nodesByCityName.put(cityName, r);

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

            // find route
        }
        System.out.println("\n" + netIdTable.entrySet().size() + " routes built.");

        System.out.println("[NETWORK] - Init static tTopology: " + topology);
        System.out.println(Colour.green("[NETWORK] - Static topology built successfully!\n"));

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
    public Network(HashTable<String, HashTable.Entry<Integer, String>> localAreaNetworks, int filesToTransfer, String algorithm) throws IOException, InterruptedException {
        // create n switches
        this.nodesByCityName = new HashMap<>();
        switches = new HashTable<>();
        routers = new HashTable<>();
        topology = new HashTable<>();
        packetsToReassemble = new HashTable<>();
        this.filesToTransfer = filesToTransfer;
        this.algorithm = algorithm;

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

        // print switches
        for (HashTable.Entry<Integer, Switch> entry : switches.entrySet()) {
            System.out.println(entry.getKey() + "," + entry.getValue());
        }

        Thread.sleep(8000);
        // create the topology and routers simulating a routing algorithm already in place
        this.route();
    }

    /**
     * Method to get the key of a switch in the network, which is the network id.
     * @param s The switch to get the key of.
     * @return The key of the switch in the network.
     */
    public Integer getKeyOfSwitch(Switch s) {
        for (HashTable.Entry<Integer, Switch> entry : switches.entrySet()) {
            if (entry.getValue().equals(s)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * creates new list of routers from nodesByCityName
     */
    public List<String> getRouters() {
        List<String> routers = new ArrayList<>();
        for (String cityName : nodesByCityName.keySet()) {
            if (nodesByCityName.get(cityName) instanceof Router) {
                routers.add(cityName);
            }
        }
        return routers;
    }


}
