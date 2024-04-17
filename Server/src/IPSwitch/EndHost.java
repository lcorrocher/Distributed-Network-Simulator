package IPSwitch;

import DataStructures.Node;
import DataStructures.NodeType;

public class EndHost extends Node {
    private String ipAddr;
    private String macAddr;

    public EndHost(int id, String name, String ipAddr, String macAddr) {
        super(id, name, NodeType.ENDHOST);
        this.ipAddr = ipAddr;
        this.macAddr = macAddr;
    }


    public String getIpAddr() {
        return ipAddr;
    }

    public String getMacAddr() {
        return macAddr;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", IP Address: %s, MAC Address: %s", ipAddr, macAddr);
    }
}
