public class Customer extends User {

    private MyHashTable<String, Boolean> activeFreelancers;

    private MyHashTable<String, Boolean> blacklistedFreelancers;
    private int cancelPenaltyCount;
    private int totalSpent;
    private int effectiveSpentForLoyalty;
    private int totalEmploymentCount;
    private String loyaltyTier;

    Customer(String userId) {
        super(userId);
        activeFreelancers = new MyHashTable<>(101);
        blacklistedFreelancers = new MyHashTable<>(101);
        cancelPenaltyCount = 0;
        totalSpent = 0;
        this.effectiveSpentForLoyalty = 0;
        this.totalEmploymentCount = 0;
        this.loyaltyTier = "BRONZE";
    }

    public void addActiveFreelancer(String freelancerID) {
        activeFreelancers.put(freelancerID, true);
    }

    public void removeActiveFreelancer(String freelancerID) {
        activeFreelancers.remove(freelancerID);
    }

    public void incrementCancelPenaltyCount() {
        cancelPenaltyCount++;
    }

    public int getTotalSpent() {
        return totalSpent;
    }

    public void addSpending(int amount) {
        totalSpent += amount;
        effectiveSpentForLoyalty += amount;
    }

    public void applyCancelPenalty() {
        effectiveSpentForLoyalty -= 250;
    }

    public int getEffectiveSpentForLoyalty() {
        return effectiveSpentForLoyalty;
    }

    // Calculate discount rate based on loyalty tier
    public int getSubsidyPercent() {
        if ("SILVER".equals(loyaltyTier)) {
            return 5;
        } else if ("GOLD".equals(loyaltyTier)) {
            return 10;
        } else if ("PLATINUM".equals(loyaltyTier)) {
            return 15;
        } else {
            return 0; // BRONZE or unknown
        }
    }

    public void addToBlacklist(String freelancerID) {
        blacklistedFreelancers.put(freelancerID, true);
    }

    public void removeFromBlacklist(String freelancerID) {
        blacklistedFreelancers.remove(freelancerID);
    }

    public boolean isBlacklisted(String freelancerID) {
        return blacklistedFreelancers.isContainKey(freelancerID);
    }

    public int getBlacklistedCount() {
        return blacklistedFreelancers.size();
    }

    public int getTotalEmploymentCount() {
        return totalEmploymentCount;
    }

    public void incrementTotalEmploymentCount() {
        totalEmploymentCount++;
    }

    public String getLoyaltyTier() {
        return loyaltyTier;
    }

    public void setLoyaltyTier(String loyaltyTier) {
        this.loyaltyTier = loyaltyTier;
    }
}