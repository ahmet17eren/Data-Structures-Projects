public class AttackGroupCard extends Card {

    public int AofGroup;          // Attack value representing the group key
    public HealthAVL healths;     // Inner AVL tree storing cards by health

    public AttackGroupCard(int Att) {
        // Empty name and dummy values; used only as a group container
        super("", Att, 0, 0);
        this.AofGroup = Att;
        this.healths = new HealthAVL();
    }
}