import java.util.ArrayList;
import java.util.LinkedList;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class MatrixNet {

    private MyHashTable<String, Host> hostIDTable;
    private ArrayList<Host> hosts;

    public MatrixNet() {
        // Initialize with a large prime capacity to minimize collisions
        this.hostIDTable = new MyHashTable<>(100003);
        this.hosts = new ArrayList<>();
    }

    public String spawnHost(String hostId, int clearanceLevel) {
        if (hostIDTable.isContainKey(hostId))
            return "Some error occurred in spawn_host.";

        // Validate host ID format
        if (!hostId.matches("[A-Z0-9_]+")) {
            return "Some error occurred in spawn_host.";
        }

        Host newHost = new Host(hostId, clearanceLevel);
        hostIDTable.put(hostId, newHost);
        hosts.add(newHost);

        return "Spawned host " + hostId + " with clearance level " + clearanceLevel + ".";
    }

    public String linkBackdoor(String id1, String id2, int latency, int bandwidth, int firewall) {
        if (id1.equals(id2)) {
            return "Some error occurred in link_backdoor.";
        }

        Host h1 = hostIDTable.get(id1);
        Host h2 = hostIDTable.get(id2);

        if (h2 == null || h1 == null) {
            return "Some error occurred in link_backdoor.";
        }

        // Check if connection already exists
        if (hasConnection(h1, h2)) {
            return "Some error occurred in link_backdoor.";
        }

        h1.addNeighbour(new Backdoor(h2, latency, bandwidth, firewall));
        h2.addNeighbour(new Backdoor(h1, latency, bandwidth, firewall));

        return "Linked " + id1 + " <-> " + id2 +
                " with latency " + latency + "ms, " +
                "bandwidth " + bandwidth + "Mbps, " +
                "firewall " + firewall + ".";
    }

    private boolean hasConnection(Host h1, Host h2) {
        for (Backdoor nb : h1.getNeighbourBackdoors()) {
            if (nb.neighbor == h2) {
                return true;
            }
        }
        return false;
    }

    public String sealBackdoor(String id1, String id2) {

        Host h1 = hostIDTable.get(id1);
        Host h2 = hostIDTable.get(id2);

        if (h1 == null || h2 == null) {
            return "Some error occurred in seal_backdoor.";
        }

        Backdoor backdoorInH1 = null;
        Backdoor backdoorInH2 = null;

        // Find backdoor in first host
        for (Backdoor nb : h1.getNeighbourBackdoors()) {
            if (nb.neighbor == h2) {
                backdoorInH1 = nb;
                break;
            }
        }

        // Find backdoor in second host
        for (Backdoor nb : h2.getNeighbourBackdoors()) {
            if (nb.neighbor == h1) {
                backdoorInH2 = nb;
                break;
            }
        }

        if (backdoorInH2 == null || backdoorInH1 == null) {
            return "Some error occurred in seal_backdoor.";
        }

        // Toggle sealed status
        boolean newState = !backdoorInH1.isSealed;

        backdoorInH1.isSealed = newState;
        backdoorInH2.isSealed = newState;

        if (newState) {
            return "Backdoor " + id1 + " <-> " + id2 + " sealed.";
        } else {
            return "Backdoor " + id1 + " <-> " + id2 + " unsealed.";
        }

    }

    public String traceRoute(String src, String dest, int minBandwidth, int lambda) {

        Host h1 = hostIDTable.get(src);
        Host h2 = hostIDTable.get(dest);

        if (h1 == null || h2 == null) {
            return "Some error occurred in trace_route.";
        }

        if (src.equals(dest)) {
            return "Optimal route " + src + " -> " + src + ": " + src + " (Latency = 0ms)";
        }

        String logMessage = myBestRouteAlgorithm(h1, h2, minBandwidth, lambda);

        return logMessage;
    }

    public String myBestRouteAlgorithm(Host h1, Host h2, int minBandwidth, int lambda) {

        MyHashTable<String, ArrayList<State>> bestStates = new MyHashTable<>(200003);

        MyHeap<RouteNode> findMinHeap = new MyHeap<>(200000);

        ArrayList<State> startList = new ArrayList<>();
        startList.add(new State(0, 0));
        bestStates.put(h1.getHostId(), startList);

        findMinHeap.insert(new RouteNode(h1, 0, 0, null));

        RouteNode finalNode = null;

        while (!findMinHeap.isEmpty()) {

            RouteNode current = findMinHeap.extractMin();

            // Target reached
            if (current.host.equals(h2)) {
                finalNode = current;
                break;
            }

            // Explore neighbors
            for (Backdoor edge : current.host.getNeighbourBackdoors()) {

                // Constraints: Check seal status, bandwidth, and clearance level
                if (edge.isSealed) continue;
                if (edge.bandwidth < minBandwidth) continue;
                if (current.host.getClearanceLevel() < edge.firewall) continue;

                Host nextHost = edge.neighbor;

                // Dynamic latency calculation
                int segmentCost = edge.latency + (lambda * current.hopCount);

                int newLatency = current.totalLatency + segmentCost;
                int newHop = current.hopCount + 1;

                State newState = new State(newLatency, newHop);

                String nextId = nextHost.getHostId();

                ArrayList<State> list = bestStates.get(nextId);
                if (list == null) list = new ArrayList<>();

                // Skip if dominated
                if (isDominated(newState, list)) {
                    continue;
                }

                // Prune dominated states
                pruneDominated(newState, list);

                list.add(newState);
                bestStates.put(nextId, list);

                findMinHeap.insert(new RouteNode(nextHost, newLatency, newHop, current));
            }
        }

        if (finalNode == null) {
            return "No route found from " + h1.getHostId() + " to " + h2.getHostId();
        }

        String path = buildPath(finalNode);

        return "Optimal route " + h1.getHostId() + " -> " + h2.getHostId()
                + ": " + path + " (Latency = " + finalNode.totalLatency + "ms)";
    }


    // UTILITIES

    boolean isDominated(State newS, ArrayList<State> list) {
        for (State s : list) {
            if (s.latency <= newS.latency && s.hop <= newS.hop) {
                return true;
            }
        }
        return false;
    }

    void pruneDominated(State newS, ArrayList<State> list) {
        list.removeIf(s ->
                newS.latency <= s.latency && newS.hop <= s.hop
        );
    }

    String buildPath(RouteNode node) {
        ArrayList<String> order = new ArrayList<>();
        RouteNode current = node;

        while (current != null) {
            order.add(current.host.getHostId());
            current = current.parent;
        }

        int left = 0;
        int right = order.size() - 1;
        while (left < right) {
            String temp = order.get(left);
            order.set(left, order.get(right));
            order.set(right, temp);
            left++;
            right--;
        }

        String result = "";
        for (int i = 0; i < order.size(); i++) {
            result += order.get(i);
            if (i != order.size() - 1) {
                result += " -> ";
            }
        }

        return result;
    }

    public String scanConnectivity() {
        if (hosts.isEmpty() || hosts.size() <= 1) {
            return "Network is fully connected.";
        }
        int componentCount = countComponent(null);
        if (componentCount == 1) {
            return "Network is fully connected.";
        } else {
            return "Network has " + componentCount + " disconnected components.";
        }
    }

    public int countComponent(String breachedHost) {
        MyHashTable<String, Boolean> visited = new MyHashTable<>(100003);
        int componentCount = 0;
        for (Host host : hosts) {
            if (breachedHost != null && breachedHost.equals(host.getHostId()))
                continue;
            if (visited.isContainKey(host.getHostId()))
                continue;
            componentCount++;
            bfs(host, visited, breachedHost);
        }
        return componentCount;
    }


    public void bfs(Host startHost, MyHashTable<String, Boolean> visited, String breachedHost) {
        LinkedList<Host> queue = new LinkedList<>();
        queue.add(startHost);
        visited.put(startHost.getHostId(), true);
        while (!queue.isEmpty()) {
            Host current = queue.poll();
            for (Backdoor backdoor : current.getNeighbourBackdoors()) {
                if (breachedHost != null && breachedHost.equals(backdoor.neighbor.getHostId()))
                    continue;
                if (backdoor.isSealed || visited.isContainKey(backdoor.neighbor.getHostId()))
                    continue;
                queue.add(backdoor.neighbor);
                visited.put(backdoor.neighbor.getHostId(), true);
            }
        }
    }

    public String simulateHostBreach(String breachHost) {

        if (hostIDTable.get(breachHost) == null) {
            return "Some error occurred in simulate_breach.";
        }

        int oldComponentCount = countComponent(null);
        int newComponentCount = countComponent(breachHost);

        if (newComponentCount > oldComponentCount)
            return "Host " + breachHost + " IS an articulation point.\n" + "Failure results in " + newComponentCount + " disconnected components.";
        else
            return "Host " + breachHost + " is NOT an articulation point. Network remains the same.";
    }


    public String simulateBackdoorBreach(String bH1, String bH2) {

        Host h1 = hostIDTable.get(bH1);
        Host h2 = hostIDTable.get(bH2);

        if (h1 == null || h2 == null) {
            return "Some error occurred in simulate_breach.";
        }


        Backdoor willBreachBackdoor1 = null;
        for (Backdoor backdoor : hostIDTable.get(bH1).getNeighbourBackdoors())
            if (backdoor.neighbor.getHostId().equals(bH2))
                willBreachBackdoor1 = backdoor;

        Backdoor willBreachBackdoor2 = null;
        for (Backdoor backdoor : hostIDTable.get(bH2).getNeighbourBackdoors())
            if (backdoor.neighbor.getHostId().equals(bH1))
                willBreachBackdoor2 = backdoor;


        if (willBreachBackdoor1 == null || willBreachBackdoor1.isSealed || willBreachBackdoor2 == null || willBreachBackdoor2.isSealed)
            return "Some error occurred in simulate_breach.";

        int oldComponentCount = countComponent(null);
        willBreachBackdoor1.isSealed = true;
        willBreachBackdoor2.isSealed = true;
        int newComponentCount = countComponent(null);
        willBreachBackdoor1.isSealed = false;
        willBreachBackdoor2.isSealed = false;


        if (newComponentCount > oldComponentCount)
            return "Backdoor " + bH1 + " <-> " + bH2 + " IS a bridge.\n" + "Failure results in " + newComponentCount + " disconnected components.";
        else
            return "Backdoor " + bH1 + " <-> " + bH2 + " is NOT a bridge. Network remains the same.";
    }


    private boolean DFSCycleCheck(Host startHost, MyHashTable<String,Boolean> visited){
        // Using Stack (LinkedList) to avoid recursion stack overflow
        LinkedList<StackFrame> stack = new LinkedList<>();
        stack.push(new StackFrame(startHost,null));

        while (!stack.isEmpty()){
            StackFrame frame = stack.pop();
            Host current = frame.current;
            Host parent = frame.parent;

            if (visited.isContainKey(current.getHostId())) {
                continue;
            }

            visited.put(current.getHostId(),true);

            for (Backdoor backdoor: current.getNeighbourBackdoors()){
                if (backdoor.isSealed)
                    continue;
                // Ignore the path we came from
                if (parent != null && backdoor.neighbor.getHostId().equals(parent.getHostId())) {
                    continue;
                }
                // Cycle detected
                if (visited.isContainKey(backdoor.neighbor.getHostId())) {
                    return true;
                }
                stack.push(new StackFrame(backdoor.neighbor,current));
            }
        }
        return false;
    }



    private boolean hasCycle() {
        MyHashTable<String, Boolean> visited = new MyHashTable<>(100003);

        for (Host h : hosts) {
            if (!visited.isContainKey(h.getHostId())) {
                if (DFSCycleCheck(h,visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String oracleReport() {
        int totalHosts = hosts.size();
        int totalUnsealedEdgesDoubleCount = 0;
        double totalBandwidthDoubleCount = 0;
        double totalClearance = 0;

        for (Host h : hosts) {
            totalClearance += h.getClearanceLevel();

            for (Backdoor bd : h.getNeighbourBackdoors()) {
                if (!bd.isSealed) {
                    totalUnsealedEdgesDoubleCount++;
                    totalBandwidthDoubleCount += bd.bandwidth;
                }
            }
        }


        int edgeCount = totalUnsealedEdgesDoubleCount / 2;

        int components = countComponent(null);
        boolean isConnected;

        if(components == 1)
            isConnected = true;
        else
            isConnected = false;

        boolean cycleExists = hasCycle();


        double avgBandwidthRaw = 0.0;
        double avgClearanceRaw = 0.0;

        // Calculate averages in double precision
        if (totalUnsealedEdgesDoubleCount > 0) {
            avgBandwidthRaw = totalBandwidthDoubleCount / (double) totalUnsealedEdgesDoubleCount;
        }

        if (totalHosts > 0) {
            avgClearanceRaw = totalClearance / (double) totalHosts;
        }

        // Apply formatting requirements
        BigDecimal avgBandwidthBD = new BigDecimal(Double.toString(avgBandwidthRaw))
                .setScale(1, RoundingMode.HALF_UP);

        BigDecimal avgClearanceBD = new BigDecimal(Double.toString(avgClearanceRaw))
                .setScale(1, RoundingMode.HALF_UP);


        StringBuilder sb = new StringBuilder();
        sb.append("--- Resistance Network Report ---\n");
        sb.append("Total Hosts: ").append(totalHosts).append("\n");
        sb.append("Total Unsealed Backdoors: ").append(edgeCount).append("\n");
        sb.append("Network Connectivity: ").append(isConnected ? "Connected" : "Disconnected").append("\n");
        sb.append("Connected Components: ").append(components).append("\n");
        sb.append("Contains Cycles: ").append(cycleExists ? "Yes" : "No").append("\n");
        sb.append("Average Bandwidth: ").append(avgBandwidthBD).append("Mbps\n");
        sb.append("Average Clearance Level: ").append(avgClearanceBD);

        return sb.toString();
    }
}