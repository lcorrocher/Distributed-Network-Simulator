package IPSwitch;

import DataStructures.HashTable;
import org.junit.jupiter.api.Test;

import static IPSwitch.ARP.arpBroadcast;
import static IPSwitch.ARP.createLocalCache;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;
import java.util.Map;

class ARPTest {
    Map<String, EndHost> endHostMap;

    @BeforeEach
    void setUp() {
        endHostMap = new HashMap<>();
        createLocalCache();
    }


    @Test
    void ARPBroadcastTest() throws InterruptedException {
        // gets the ARP response for the destination host name, returning the destination host name, destination LAN, target IP and target MAC
        String destinationHostName = "m";
        ARPPacket arpResponse = arpBroadcast(destinationHostName);

        assertEquals("m", arpResponse.getTargetName());
        assertEquals("NEW YORK CITY", arpResponse.getDestinationLan());
        assertEquals("172.21.001.1", arpResponse.getTargetIp());
        assertEquals("00:0a:95:9d:68:16", arpResponse.getTargetMac());
    }


}