import java.util.ArrayList;

// Represents a node state in the priority queue for Dijkstra's algorithm
public class RouteNode implements Comparable<RouteNode> {

    public Host host;
    public int totalLatency;
    public int hopCount;
    public RouteNode parent; // Pointer to reconstruct path

    public RouteNode(Host host, int totalLatency, int hopCount, RouteNode parent) {
        this.host = host;
        this.totalLatency = totalLatency;
        this.hopCount = hopCount;
        this.parent = parent;
    }

    @Override
    public int compareTo(RouteNode other) {
        // Priority 1: Minimize Total Latency
        int latCompare = Double.compare(this.totalLatency, other.totalLatency);
        if (latCompare != 0) return latCompare;

        // Priority 2: Minimize Hop Count
        int hopCompare = Integer.compare(this.hopCount, other.hopCount);
        if (hopCompare != 0) return hopCompare;

        // Priority 3: Lexicographically smaller host sequence
        return comparePathsElementWise(this, other);
    }

    // Compares paths element-by-element to determine lexicographical order
    private int comparePathsElementWise(RouteNode nodeA, RouteNode nodeB) {
        ArrayList<String> pathA = new ArrayList<>();
        ArrayList<String> pathB = new ArrayList<>();

        // Reconstruct paths by backtracking
        RouteNode tempA = nodeA;
        while (tempA != null) {
            pathA.add(tempA.host.getHostId());
            tempA = tempA.parent;
        }

        RouteNode tempB = nodeB;
        while (tempB != null) {
            pathB.add(tempB.host.getHostId());
            tempB = tempB.parent;
        }

        // Reverse to get Start -> End order
        reverseList(pathA);
        reverseList(pathB);

        // Compare element by element
        int size = Math.min(pathA.size(), pathB.size());
        for (int i = 0; i < size; i++) {
            String idA = pathA.get(i);
            String idB = pathB.get(i);
            int comparison = idA.compareTo(idB);
            if (comparison != 0) {
                return comparison;
            }
        }

        return Integer.compare(pathA.size(), pathB.size());
    }

    // Helper method to reverse list in-place
    private void reverseList(ArrayList<String> list) {
        int left = 0;
        int right = list.size() - 1;
        while (left < right) {
            String temp = list.get(left);
            list.set(left, list.get(right));
            list.set(right, temp);
            left++;
            right--;
        }
    }
}