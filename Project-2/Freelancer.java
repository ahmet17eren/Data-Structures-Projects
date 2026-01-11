public class Freelancer extends User implements Comparable<Freelancer> {

    private String serviceName;
    private int price;
    private int T;
    private int C;
    private int R;
    private int E;
    private int A;

    private boolean available = true;
    private String activeCustomerID;

    private double avgRating;
    private int completedCount;
    private int cancelledCount;

    private int completedThisMonth;
    private int cancelledThisMonth;

    private boolean platformBanned;
    private boolean burnedOut;

    // Cached composite score (invalidated when attributes change)
    private int cachedCompositeScore = -1;
    private boolean scoreDirty = true; // true → composite score must be recalculated

    Freelancer(String userId, String serviceName, int price, int T, int C,
               int R, int E, int A) {
        super(userId);
        this.serviceName = serviceName;
        this.price = price;
        this.T = T;
        this.C = C;
        this.R = R;
        this.E = E;
        this.A = A;
        this.avgRating = 5.0;
        this.burnedOut = false;
    }

    // Getter-Setter Methods
    public void setServiceName(String serviceType) {
        this.serviceName = serviceType;
        this.scoreDirty = true;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setA(int a) { A = a; this.scoreDirty = true; }
    public void setC(int c) { C = c; this.scoreDirty = true; }
    public void setE(int e) { E = e; this.scoreDirty = true; }
    public void setR(int r) { R = r; this.scoreDirty = true; }
    public void setT(int t) { T = t; this.scoreDirty = true; }

    public String getServiceName() { return serviceName; }
    public int getA() { return A; }
    public int getC() { return C; }
    public int getE() { return E; }
    public int getPrice() { return price; }
    public int getR() { return R; }
    public int getT() { return T; }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getActiveCustomerID() {
        return activeCustomerID;
    }

    public void setActiveCustomerID(String activeCustomerID) {
        this.activeCustomerID = activeCustomerID;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
        this.scoreDirty = true;
    }

    public int getCancelledCount() {
        return cancelledCount;
    }

    public void setCancelledCount(int cancelledCount) {
        this.cancelledCount = cancelledCount;
        this.scoreDirty = true;
    }

    public int getCancelledThisMonth() {
        return cancelledThisMonth;
    }

    public void setCancelledThisMonth(int cancelledThisMonth) {
        this.cancelledThisMonth = cancelledThisMonth;
    }

    public int getCompletedThisMonth() {
        return completedThisMonth;
    }

    public void setCompletedThisMonth(int completedThisMonth) {
        this.completedThisMonth = completedThisMonth;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
        this.scoreDirty = true;
    }

    public void setPlatformBanned(boolean platformBanned) {
        this.platformBanned = platformBanned;
    }

    public boolean isPlatformBanned() {
        return platformBanned;
    }

    public boolean isBurnedOut() {
        return burnedOut;
    }

    public void setBurnedOut(boolean burnedOut) {
        this.burnedOut = burnedOut;
        this.scoreDirty = true;
    }

    // Cached composite score calculator
    public int computeCompositeScore() {
        // Return cached value if still valid
        if (!scoreDirty) {
            return cachedCompositeScore;
        }

        // Otherwise recalc
        cachedCompositeScore = computeCompositeScoreInternal();
        scoreDirty = false;
        return cachedCompositeScore;
    }

    // Actual composite score calculation
    private int computeCompositeScoreInternal() {

        // Service profile multipliers
        int[] s = ServiceProfiles.getSkillsFor(this.serviceName);
        int Ts = s[0], Cs = s[1], Rs = s[2], Es = s[3], As = s[4];

        int Tf = this.T;
        int Cf = this.C;
        int Rf = this.R;
        int Ef = this.E;
        int Af = this.A;

        // Skill relevance score (normalized dot product)
        int dot = Tf * Ts + Cf * Cs + Rf * Rs + Ef * Es + Af * As;
        int sumS = Ts + Cs + Rs + Es + As;

        double skillScore = (sumS == 0)
                ? 0.0
                : (double) dot / (100.0 * sumS);

        // Rating component
        double ratingScore = this.avgRating / 5.0;

        // Reliability component
        int completed = this.completedCount;
        int cancelled = this.cancelledCount;
        double reliabilityScore =
                (completed + cancelled == 0)
                        ? 1.0
                        : 1.0 - (double) cancelled / (completed + cancelled);

        // Burnout penalty
        double burnoutPenalty = this.burnedOut ? 0.45 : 0.0;

        // Weighted sum of components
        double ws = 0.55;
        double wr = 0.25;
        double wl = 0.20;

        double total = ws * skillScore
                + wr * ratingScore
                + wl * reliabilityScore
                - burnoutPenalty;

        // Convert to 0–10000 integer
        double raw = 10000.0 * total;
        int composite = (int) Math.floor(raw);

        if (composite < 0) composite = 0;
        if (composite > 10000) composite = 10000;

        return composite;
    }

    @Override
    public int compareTo(Freelancer other) {
        int myScore = this.computeCompositeScore();
        int otherScore = other.computeCompositeScore();

        if (myScore != otherScore) {
            return Integer.compare(myScore, otherScore);
        }

        // Tie-break: lexicographically smaller ID wins
        return other.getUserID().compareTo(this.getUserID());
    }
}