public class DiscardAVL extends BaseAVL {

    @Override
    protected int compareCards(Card c1, Card c2) {
        DeadCard d1 = (DeadCard) c1;
        DeadCard d2 = (DeadCard) c2;

        // Compare by missing health (smaller Hmissing goes left)
        int a = Integer.compare(d1.getHmissing(), d2.getHmissing());
        if (a != 0) return a;

        // Then by death order (earlier discarded goes left)
        int b = Integer.compare(d1.getDeathOrder(), d2.getDeathOrder());
        if (b != 0) return b;

        // As fallback, compare by reference card entry order
        int c = Integer.compare(d1.getRef().getEntryOrder(), d2.getRef().getEntryOrder());
        if (c != 0) return c;

        // Final tie-breaker: compare by name (ensures determinism)
        return d1.getRef().getName().compareTo(d2.getRef().getName());
    }

    @Override
    public void insert(Card card) {
        // Insert a DeadCard node into the AVL tree
        DeadCard deadCard = (DeadCard) card;
        super.insert(deadCard);
    }

    @Override
    public void delete(Card card) {
        // Delete a DeadCard node from the AVL tree
        DeadCard deadCard = (DeadCard) card;
        super.delete(deadCard);
    }

    // Finds the card with the largest Hmissing <= heal (used in full revival)
    public Node bestFull(int heal) {
        Node cur = root;
        Node best = null;

        while (cur != null) {
            DeadCard deadCard = (DeadCard) cur.element;
            if (deadCard.getHmissing() <= heal) {
                best = cur;
                cur = cur.right; // search for a larger valid Hmissing
            } else {
                cur = cur.left;
            }
        }

        if (best == null) return null;

        // Move left to find the leftmost node with same Hmissing
        DeadCard bd = (DeadCard) best.element;
        int hm = bd.getHmissing();
        Node x = best;
        while (x.left != null) {
            DeadCard leftD = (DeadCard) x.left.element;
            if (leftD.getHmissing() == hm) x = x.left;
            else break;
        }

        return x;
    }

}