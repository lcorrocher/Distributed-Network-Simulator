package IPSwitch;

public class ARPPacket {
    private String targetMac;
    private String targetIp;
    private String targetName;
    private String destinationLan;


    // Updated constructor without srcMac and srcIp parameters
    public ARPPacket(String targetName, String targetIp, String targetMac, String destinationLan) {
        this.targetName = targetName;
        this.targetIp = targetIp;
        this.targetMac = targetMac;
        this.destinationLan = destinationLan;
    }

    // Getters for the fields
    public String getTargetMac() {
        return targetMac;
    }

    public String getTargetIp() {
        return targetIp;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getDestinationLan() {
        return destinationLan;
    }

    public String toString() {
        return "ARPPacket{" +
                "targetMac='" + targetMac + '\'' +
                ", targetIp='" + targetIp + '\'' +
                ", targetName='" + targetName + '\'' +
                ", destinationLan='" + destinationLan + '\'' +
                '}';
    }
}
