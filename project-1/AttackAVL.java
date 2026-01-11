public class AttackAVL extends BaseAVL {

    @Override
    protected int compareCards(Card c1, Card c2) {
        AttackGroupCard g1 = (AttackGroupCard) c1;
        AttackGroupCard g2 = (AttackGroupCard) c2;
        return Integer.compare(g1.AofGroup, g2.AofGroup);
    }

    // First node with A >= given value
    private Node lowerBoundA(int A) {
        Node cur = root, ans = null;
        while (cur != null) {
            int key = ((AttackGroupCard) cur.element).AofGroup;
            if (key >= A) {
                ans = cur;
                cur = cur.left;
            } else cur = cur.right;
        }
        return ans;
    }

    // Last node with A < given value
    private Node strictlyLessA(int A) {
        Node cur = root, ans = null;
        while (cur != null) {
            int key = ((AttackGroupCard) cur.element).AofGroup;
            if (key < A) {
                ans = cur;
                cur = cur.right;
            } else cur = cur.left;
        }
        return ans;
    }

    // Maximum A band node
    private Node maxA() {
        Node cur = root;
        if (cur == null) return null;
        while (cur.right != null) cur = cur.right;
        return cur;
    }

    // Successor band with key > A
    private Node successorA(int A) {
        Node cur = root, ans = null;
        while (cur != null) {
            int key = ((AttackGroupCard) cur.element).AofGroup;
            if (A < key) {
                ans = cur;
                cur = cur.left;
            } else cur = cur.right;
        }
        return ans;
    }

    // Predecessor band with key < A
    private Node predecessorA(int A) {
        Node cur = root, ans = null;
        while (cur != null) {
            int key = ((AttackGroupCard) cur.element).AofGroup;
            if (A > key) {
                ans = cur;
                cur = cur.right;
            } else cur = cur.left;
        }
        return ans;
    }

    // Insert a single Card: find its attack band, create if missing
    public void insertCard(Card c) {
        int A = c.getAcur();
        Node band = lowerBoundA(A);
        if (band != null && ((AttackGroupCard) band.element).AofGroup == A) {
            ((AttackGroupCard) band.element).healths.insert(c);
            return;
        }
        // Band not found → create a new band node
        super.insert(new AttackGroupCard(A));
        // Locate the inserted band and add the card to its inner tree
        Node newly = lowerBoundA(A);
        ((AttackGroupCard) newly.element).healths.insert(c);
    }

    public void deleteCard(Card c) {
        int A = c.getAcur();
        Node band = lowerBoundA(A);
        if (band == null || ((AttackGroupCard) band.element).AofGroup != A) return;
        HealthAVL inner = ((AttackGroupCard) band.element).healths;
        inner.delete(c);
        // If inner tree becomes empty, remove the band node
        if (inner.isEmpty()) {
            super.delete((AttackGroupCard) band.element);
        }
    }

    // P1: A >= hp AND H > att → choose min A band; in that band pick min H with H > att
    public Card findForPriority1(int att, int hp) {
        Node cur = lowerBoundA(hp);
        while (cur != null) {
            HealthAVL inner = ((AttackGroupCard) cur.element).healths;
            Card pick = inner.minStrictGreaterH(att);
            if (pick != null) return pick;
            cur = successorA(((AttackGroupCard) cur.element).AofGroup);
        }
        return null;
    }

    // P2: A < hp AND H > att → iterate from max A band downward; in band pick min H > att
    public Card findForPriority2(int att, int hp) {
        Node cur = strictlyLessA(hp);
        while (cur != null) {
            HealthAVL inner = ((AttackGroupCard) cur.element).healths;
            Card pick = inner.minStrictGreaterH(att);
            if (pick != null) return pick;
            cur = predecessorA(((AttackGroupCard) cur.element).AofGroup);
        }
        return null;
    }

    // P3: A >= hp AND H <= att → choose min A band; if that band's min H <= att, take it
    public Card findForPriority3(int att, int hp) {
        Node cur = lowerBoundA(hp);
        while (cur != null) {
            HealthAVL inner = ((AttackGroupCard) cur.element).healths;
            Card m = inner.minNode();
            if (m != null && m.getHcur() <= att) return m;
            cur = successorA(((AttackGroupCard) cur.element).AofGroup);
        }
        return null;
    }

    // P4: Pick max A band; then take that band's minimum-H card
    public Card findForPriority4(int att, int hp) {
        Node mx = maxA();
        if (mx == null) return null;
        return ((AttackGroupCard) mx.element).healths.minNode();
    }

    @Override
    public void insert(Card c) {
        insertCard(c);
    }

    @Override
    public void delete(Card c) {
        deleteCard(c);
    }

    public Node firstBandGreaterThan(int a){   // First band with A > a
        return successorA(a);
    }
    public Node nextBand(Node band){           // Next band in ascending A
        if (band == null) return null;
        return successorA(((AttackGroupCard)band.element).AofGroup);
    }
    public HealthAVL innerOf(Node band){
        return (band==null) ? null : ((AttackGroupCard)band.element).healths;
    }
}