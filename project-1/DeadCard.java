class DeadCard extends Card {
    final Card ref; // Reference to the original Card
    private int deathOrder;
    private int revivalProgress;

    DeadCard(Card ref, int deathOrder) {
        super(ref.getName(), ref.getAinit(), ref.getHinit(), ref.getEntryOrder());
        this.ref = ref;
        this.deathOrder = deathOrder;
        this.revivalProgress = 0;
    }

    // Returns how much health is missing until full revival
    public int getHmissing() {
        return ref.getHbase() - this.revivalProgress;
    }

    public int getRevivalProgress() {
        return revivalProgress;
    }

    public void setRevivalProgress(int revivalProgress) {
        this.revivalProgress = revivalProgress;
    }

    public int getDeathOrder() {
        return deathOrder;
    }

    public void setDeathOrder(int deathOrder) {
        this.deathOrder = deathOrder;
    }

    public Card getRef() {
        return ref;
    }

    // Applies partial revival: reduces base attack by 5% and increases progress
    public void applyPartial(int pts) {
        if (pts <= 0) return;
        ref.setAbase((int) Math.floor(ref.getAbase() * 0.95));
        int newProg = revivalProgress + pts;
        int cap = ref.getHbase();
        revivalProgress = (newProg > cap) ? cap : newProg;
        // Entry order will be updated when revived (not while in discard)
    }

    // Applies full revival: 10% attack penalty, restores full health and current attack
    public void applyFullRevive() {
        ref.setAbase((int) Math.floor(ref.getAbase() * 0.90)); // permanent penalty
        revivalProgress = 0;
        ref.setHcur(ref.getHbase());
        // Acur = max(1, Abase * Hcur / Hbase) = Abase when fully revived
        ref.setAcur(ref.getAbase());
    }
}