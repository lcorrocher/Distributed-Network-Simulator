# WAN packet forwarding - Assignment 4 - HashTables used for Broadcast Protocol, Hashing, Encryption in Packet Transmission

## Build

* JDK 20.x.x.
* IDE: Intellij/Eclipse

# Running the Application

This repository contains the necessary files to run the application. Follow the instructions below based on your preferred method.

## Server 

run the server(network) first by running the main method in the Server class. The server will start listening on port 8080 and run the network.
Note: The client will not run if the server is not running.

## Client

The client takes in 3 arguments: the source endHost name, the destination endHost name, and the message file name. 
The client will read the message from the file and send it to the destination endHost through the network.

At this moment, to run the client without cleaning the cache, you can run the following command in another terminal:
```bash
java -jar "out/artifacts/IP_Client_jar/IP Client.jar" "a" "p" "beemovie.txt"
```

To run the client with cleaning the cache, you can run the following command in another terminal:
```bash
java -jar "out/artifacts/IP_Client_jar/IP Client.jar" "cleanCache" "a" "p" "beemovie.txt"
```
Note: cleanCache isn't implemented yet, so it doesn't do anything.


## Cloning the Repository

To get started, clone this repository to your local machine:

```bash
git clone https://github.com/lcorrocher/Packet-Forwarding-Network-Hashing.git
```

## Running in IntelliJ IDEA/Eclipse

If you prefer to run the application in IntelliJ IDEA, follow these steps:

1. Open IntelliJ IDEA/Eclipse and import the project. This project was created using IntelliJ IDEA, but you can smart import the project in Eclipse.

2. Make sure the project is configured properly and all dependencies are resolved.

3. Navigate to the main class or entry point of the application.

4. Right-click on the main class and select "Run" to execute the application.

## Running with Executable Scripts

You can also run the application using provided executable scripts for Windows and Unix-based systems.

### Windows

To run the application on Windows, follow these steps:

1. Open File Explorer and navigate to the root directory of this repository.

2. Double-click on the `run.bat` file. This will execute the script.

3. The application will start running.

### Unix/Linux/Mac

To run the application on Unix-based systems (Linux, macOS), follow these steps:

1. Open a terminal.

2. Navigate to the root directory of this repository using the `cd` command:
   ```bash
   cd /path/to/this/repository
   chmod +x run.sh # make the script executable
   ./run.sh # run the script which will execute the application
   ```



## Directory Structure

### Packages

- **Datastructures:** Custom data structures used in the project.

- **IPSwitch:** Classes implementing data structures for the networking application.

- **utils:** Utility static classes.

### Test

- Testing classes with JUnit 5 through IntelliJ/Eclipse.

# Overview

This application is designed to send a message through a network to the receiving endHost.

Please refer to the [Broadcast Protocol](#broadcast-protocol) section for a detailed explanation of this assignment's focus protocol - using HashTables.

## Time Complexity

The application is designed to be scalable and efficient, with a time complexity of O(1) for message transmission between endHosts in the same LAN and O(n) for message transmission between endHosts in different LANs.

## Message Transmission


**What is a message?**  

It can be anything someone wants to send to someone else over a network. In our case, we have created .txt files which will be used as messages: beemovie.txt, motivation.txt.
We will be sending these messages from one endHost to another through the network.

**Here, we will attempt to route a message from endHost a in FRESNO to endHost p in NEW YORK CITY**

The network uses a US network architecture, which is configured to simulate a network of Local Area Networks (LANs) connected by a Wide Area Network (WAN). The network consists of four LANs: NEW YORK CITY, MINNEAPOLIS, AUSTIN, and FRESNO. Each LAN contains a number of endHosts, which are the devices that send and receive messages across the network. The endHosts are connected to a citySwitch, which is the device that routes messages within the LAN. The citySwitches are connected to the edge routers, which route messages between the LANs. The edge routers are connected to the WAN, which routes messages between the edge routers. The network is preconfigured with the LAN topologies and the WAN routing tables. The application uses HashTables to store the LAN topologies and the WAN routing tables. The application also uses a Broadcast Protocol to discover the IP address of a foreign endHost that is not in the local LAN. The Broadcast Protocol sends a broadcast request packet to each LAN in the WAN to find the endHost. Once the IP address of the endHost is discovered, the application uses the WAN routing tables to route messages between the endHosts.

![overview.png](media%2FMessage%20Transmission%2Fimages%2Foverview.png)

The initial step is to decompose the message into packets. The message is read from a file and split into payloads of a specific predefined size (in our case - 16 bits). Each payload is then encoded into binary, encrypted and then hashed to preserve integrity. These payloads are then encapsulated into packet instances. Each packet contains a sequence number, payload, srcNetId (representing source edge router of packet), destNetId (destination edge router for packet), numPacketsInPayload, message type and the destNetIdFull (endHost IP address). It obtains the destination endHost IP address from the local endHostTable or queries the Broadcast Protocol to find the IP address of the foreign endHost (see [Broadcast Protocol](#broadcast-protocol) for more details).


![packetize.png](media%2FMessage%20Transmission%2Fimages%2Fpacketize.png)

These packets are then added to a list of packets, which is then added to the edge router. The edge router then forwards the packets to the destination edge router based on predefined WAN routing tables. In this case, the packets are sent from Fresno to New York City, passing through router instances Topeka and Chicago. These routers implement a queueing functionality and forward the packets to the next router in the path. 
 

![addToSwitch.png](media%2FMessage%20Transmission%2Fimages%2FaddToSwitch.png)

As a security precaution, the packets are encrypted and hashed to ensure the distributes security and integrity property. AES encryption is used to encrypt the packet payload with key size 256 bits. HMAC-SHA256 is used to hash the encrypted payload with a key size of 256 bits.
Because of the packet payload's encrypted and hashed properties, the message is passed through the network securely and with integrity. In the case of a malicious attacker, Malice is unable to intercept the message and read its true contents as it is encrypted. The hash ensures that the packet is not tampered with during transmission.  
An important property of SHA-256 is that it is collision-resistant, meaning that it is computationally infeasible to find two different inputs that hash to the same output. Secondly, it distributes an avalanche effect, meaning that a small change in the input will produce a significantly different output. This ensures that the packet is not tampered with during transmission and if it is, the hash will not match the original hash, and the packet will be discarded. The packet is resistant to Malice's collision, preimage and man in the middle attacks.


![receive.png](media%2FMessage%20Transmission%2Fimages%2Freceive.png)

Once the destination edge router (NEW YORK CITY) receives the packets, it adds them to a list and arranges them in order of sequence number, the same order they were decomposed in. The payload is extracted from each packet and the ordered 16 byte payloads are verified for integrity before decrypting and decoding from binary back to text.
This process is repeated for each packet in the list until the message is fully reconstructed. The message is then written to the destination endHost path (p here, who resides in Boston), a subdirectory of the edge router (NEW YORK CITY). endHost p can now access the message and read the reconstructed message.

![build.png](media%2FMessage%20Transmission%2Fimages%2Fbuild.png)



## Load LAN Topology into HashTable

Two components that are required for the Broadcast Protocol are the localEndHostTables and how they are initially loaded from empty HashTables.
The localEndHostTables HashTable maps a key String citySwitchId to an endHostTable HashTable value. In this application, there are four entries in localEndHostTables used to represent each LAN topology: one for NEW YORK CITY, MINNEAPOLIS, AUSTIN and FRESNO. The local endHostTable for each LAN is a HashTable that maps a key String endHostName to an EndHost object value. The EndHost object contains the endHostName, ipAddr, and citySwitchId fields. The citySwitchId field is used to identify the citySwitchId of the LAN the endHost is located in. The ipAddr field is used to store the IP address of the endHost. The endHostName field is used to store the name of the endHost.

![localEndHostTables.png](media%2FBroadcast%20Protocol%2Fimages%2FlocalEndHostTables.png)

The EndHost class contains attributes for the endHostName, ipAddr, and citySwitchId. The endHostName field is used to store the name of the endHost. The ipAddr field is used to store the IP address of the endHost. The citySwitchId field is used to identify the citySwitchId of the LAN the endHost is located in.
The createCache method is used to load the localEndHostTables with the initial endHost objects. The method creates a new EndHost object for each endHost and adds it to the appropriate localEndHostTable. This loads the LAN topologies into the HashTables, which are needed for the application to run.

![createLocalCache.png](media%2FBroadcast%20Protocol%2Fimages%2FcreateLocalCache.png)


## Broadcast Protocol

In order to successfully transmit a packetized message to the destination endHost ([Message Transmission](#message-transmission)), the sending endHost ultimately requires the receiving endHost's IP address, which is an attribute of the EndHost object. But how can it get its IP address if the endHost is not local? The solution we have designed and implemented is the Broadcast Protocol. Its objective is to obtain a foreign endHost object by searching through the WAN This endHost entry will be cached locally to avoid subsequent unnecessary and time consuming broadcast protocols.
Note: the Broadcast Protocol is not used if the destination endHost resides in the same LAN as the sending endHost as it already exists in the local endHostTable.

Let's run through an example: transferring a message from endHost a to endHost p. Assume the localEndHostTables are pre populated with existing endHost objects from the ‘createLocalCache’.


Here's the full schematic of the Broadcast Protocol including all steps:

![BP Full Schematic.png](media%2FBroadcast%20Protocol%2Fimages%2FBP%20Full%20Schematic.png)

1. **Local Query** - a queries its endHostTable (FRESNO) for endHostName p. With only the local EndHost objects cached initially, a realises that endHost p doesn't exist in the endHostTable and therefore must exist in another Local Area Network (LAN) in the Wider Area Network (WAN). However, a doesn't know where endHost p is as it only has the scope of the FRESNO endHostTable.

![1.png](media%2FBroadcast%20Protocol%2Fimages%2F1.png)

2. **Packet Generation** - FRESNO generates one broadcast request packet to send to each LAN i.e. AUSTIN, NEW YORK CITY, MINNEAPOLIS with the aim to discover which LAN contains the information for endHost p.

3. **WAN Broadcast** - It sends these broadcast request packets (red, 3 in total), one to each LAN across the network, each broadcast packet following a specific predefined
route to reach the destination LAN city edge router.

![2_3.png](media%2FBroadcast%20Protocol%2Fimages%2F2_3.png)

4. **Local ARP Request** - Once the broadcast packet arrives at each edge router i.e. city, it executes a broadcast request to the city asking if the city contains the endHost p in its LAN. Each city queries its local endHostTable checking if endHost p exists. Since the network is preconfigured, there must be an endHost p, but we are not sure yet where it is located.

5. **Unsuccessful ARP Request** - AUSTIN, MINNEAPOLIS first execute a local ARP request, searching for an endHost p entry in their local endHostTable, respectively. The entry doesn't exist in either.

6. **Unsuccessful ARP Response** - If the local ARP response is unsuccessful in locating endHost p, the city must return a response packet back to the origin edge router (FRESNO) so FRESNO can send an ARP request to the next edge router in the WAN and keep searching. MINNEAPOLIS and AUSTIN generate an unsuccessful response packet.

7. **Negative Response Transmission** - MINNEAPOLIS and AUSTIN send the unsuccessful ARP response packet across the network to FRESNO, following a specific route.

![4_5_6_7.png](media%2FBroadcast%20Protocol%2Fimages%2F4_5_6_7.png)

8. **Successful ARP Request** - NEW YORK CITY now executes a local ARP request, and it locates an existing endHost p entry in its endHostTable. The local ARP request is successful. If, for another endHost query, an endHost is found, all remaining ARP requests are abolished. Note: If endHost p would not have existed in any of the LAN, it is not in the Global Network.

9. **Successful ARP Response** - Having a successful ARP request, NEW YORK CITY now obtains p's IP address and generates an arp response packet saying it is the LAN which contains the desired endHost p.

10. **Positive Response Transmission** - NEW YORK CITY returns this response packet back to FRESNO, along with the EndHost object for p.

11. **Cache endHost** - Having received the endHost p object, FRESNO can now get p's IP address from p's IP address field (ipAddr). FRESNO adds endHost p to its local endHostTable to store p's information so it doesn't need to execute another Broadcast Protocol the next time the same endHost is requested for the session.

![8_9_10_11.png](media%2FBroadcast%20Protocol%2Fimages%2F8_9_10_11.png)

12. **Message Transmission Through WAN** - Now that endHost a knows where to send its messages to, it can finally send it across the network to endHost p in NEW YORK CITY using the ‘runWAN’ method. Subsequent messages from any endHost in FRESNO to endHost p can access p from its local endHostTable, avoiding the need to broadcast again. The Broadcast Protocol is a one-to-many mapping. All of the endHosts at the origin LAN will have access to endHost p once it has been added to the endHostMap. Caching the endHost decreases the time complexity of additional requests to the same endHost.

![12.png](media%2FBroadcast%20Protocol%2Fimages%2F12.png)

## Experimental Results

As estimated, Broadcast Protocol runtime is a time consuming process. For a broadcast request, the message transmission runtime will always be significantly larger than the cached message transmission runtime. Storing the endHost object in the local endHostTable HashTable proves to be time efficient as it does not have to execute the Broadcast Protocol. Over time, the LAN begins to learn the whole WAN topology and the message runtimes will decrease after the first time an endHost is queried from a foreign LAN.

![Results.png](media%2FResults%2FResults.png)

## UML

### Class Diagram sources root

![src.png](media%2FUML%2Fsrc.png)

### Class Diagram Datastructures package

![data structures used.png](media%2FUML%2FData%20Structures%2Fdata%20structures%20used.png)

## JavaDoc

The generated Javadoc can be found in the ./JavaDocs folder.

Link: [JavaDocs](JavaDocs)