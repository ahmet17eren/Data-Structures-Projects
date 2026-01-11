public class Backdoor {
    public Host neighbor; // The host connected at the other end of this link
    public int latency;
    public int firewall;
    public int bandwidth;
    public boolean isSealed;

    public Backdoor(Host neighbor, int latency, int bandwidth, int firewall) {
        this.neighbor = neighbor;
        this.latency = latency;
        this.bandwidth = bandwidth;
        this.firewall = firewall;
        this.isSealed = false;
    }
}