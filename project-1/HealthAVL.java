public class HealthAVL extends BaseAVL {

    @Override
    protected int compareCards(Card c1, Card c2) {
        // Compare primarily by current health (Hcur)
        if (c1.getHcur() < c2.getHcur()) {
            return -1;
        } else if (c1.getHcur() > c2.getHcur()) {
            return 1;
        } else {
            // Tie-breaker: by entry order (older first)
            if (c1.getEntryOrder() < c2.getEntryOrder()) {
                return -1;
            } else if (c1.getEntryOrder() > c2.getEntryOrder()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    // Checks if the tree is empty
    public boolean isEmpty() {
        return root == null;
    }

    // Returns the card with minimum health in this subtree
    public Card minNode() {
        if (root == null) return null;
        Node cur = root;
        Node best = null;
        while (cur != null) {
            best = cur;
            cur = cur.left;
        }
        return best.element;
    }

    // Finds the card with the smallest Hcur strictly greater than given 'att'
    public Card minStrictGreaterH(int att) {
        if (root == null) return null;

        // Create a key card representing the lower bound (H = att+1)
        Card key = new Card("", 0, 0, Integer.MIN_VALUE);
        key.setHcur(att + 1);

        Node cur = root;
        Node best = null;

        while (cur != null) {
            int cmp = compareCards(key, cur.element);
            if (cmp <= 0) {
                // Current node is a valid candidate, go left for smaller
                best = cur;
                cur = cur.left;
            } else {
                // Go right for larger values
                cur = cur.right;
            }
        }

        return (best == null) ? null : best.element;
    }

}