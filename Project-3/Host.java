import java.util.ArrayList;

public class Host {

    private String hostId;
    private int clearanceLevel;
    private ArrayList<Backdoor> neighbourBackdoors;

    public Host(String hostId, int clearanceLevel){
        this.hostId = hostId;
        this.clearanceLevel = clearanceLevel;
        this.neighbourBackdoors = new ArrayList<>();
    }

    // Accessor methods

    public ArrayList<Backdoor> getNeighbourBackdoors() {
        return neighbourBackdoors;
    }

    public int getClearanceLevel() {
        return clearanceLevel;
    }

    public String getHostId() {
        return hostId;
    }

    // Adds a new connection (edge) to the adjacency list
    public void addNeighbour(Backdoor backdoor){
        neighbourBackdoors.add(backdoor);
    }
}