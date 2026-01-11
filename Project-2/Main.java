import java.io.*;
import java.util.*;

/**
 * Main entry point for GigMatch Pro platform - HEAP INDEX OPTIMIZED VERSION
 */
public class Main {

    // Global user table and per-service freelancer lists
    private static MyHashTable<String, User> users = new MyHashTable<>(1000003);
    private static MyHashTable<String, ArrayList<Freelancer>> serviceToFreelancers = new MyHashTable<>(23);

    public static ArrayList<Freelancer> allFreelancers = new ArrayList<>();
    public static ArrayList<Customer> allCustomers = new ArrayList<>();

    // Queued service changes (to be applied at simulate_month)
    public static MyHashTable<String, ServiceChangeInfo> serviceChangeHashTable = new MyHashTable<>(100003);

    // Index of AVAILABLE freelancers: serviceName -> MaxHeap<Freelancer>
    // Heap internals keep an index map for O(1) access to positions.
    private static MyHashTable<String, MyMaxHeap<Freelancer>> availableByService = new MyHashTable<>(23);


    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        if (args.length != 2) {
            System.err.println("Usage: java Main <input_file> <output_file>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                processCommand(line, writer);
            }

        } catch (IOException e) {
            System.err.println("Error reading/writing files: " + e.getMessage());
            e.printStackTrace();
        }

        // Simple profiling output to stderr
        System.err.println("=== PERFORMANCE STATS ===");
        System.err.println("request_job called: " + requestJobCount + " times, total: " + (requestJobTime / 1_000_000) + "ms");
        System.err.println("simulate_month called: " + simulateMonthCount + " times, total: " + (simulateMonthTime / 1_000_000) + "ms");
        System.err.println("Total freelancers: " + allFreelancers.size());
        System.err.println("Total customers: " + allCustomers.size());
    }

    // Profiling counters
    private static int requestJobCount = 0;
    private static int simulateMonthCount = 0;
    private static long requestJobTime = 0;
    private static long simulateMonthTime = 0;

    /**
     * Parses a single command line and dispatches to the corresponding operation.
     */
    private static void processCommand(String commandLine, BufferedWriter writer) throws IOException {
        StringTokenizer st = new StringTokenizer(commandLine);
        if (!st.hasMoreTokens()) return;

        String operation = st.nextToken();
        String result = "";

        long startTime = System.nanoTime();

        try {
            switch (operation) {
                case "register_customer":
                    result = register_customer(st.nextToken());
                    break;

                case "register_freelancer":
                    result = register_freelancer(
                            st.nextToken(), st.nextToken(),
                            Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()),
                            Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()),
                            Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())
                    );
                    break;

                case "request_job":
                    requestJobCount++;
                    result = request_job(st.nextToken(), st.nextToken(), Integer.parseInt(st.nextToken()));
                    requestJobTime += (System.nanoTime() - startTime);
                    break;

                case "employ_freelancer":
                    result = employ_freelancer(st.nextToken(), st.nextToken());
                    break;

                case "complete_and_rate":
                    result = complete_and_rate(st.nextToken(), Integer.parseInt(st.nextToken()));
                    break;

                case "cancel_by_freelancer":
                    result = cancel_by_freelancer(st.nextToken());
                    break;

                case "cancel_by_customer":
                    result = cancel_by_customer(st.nextToken(), st.nextToken());
                    break;

                case "blacklist":
                    result = blacklist(st.nextToken(), st.nextToken());
                    break;

                case "unblacklist":
                    result = unblacklist(st.nextToken(), st.nextToken());
                    break;

                case "change_service":
                    result = change_service(st.nextToken(), st.nextToken(), Integer.parseInt(st.nextToken()));
                    break;

                case "simulate_month":
                    simulateMonthCount++;
                    result = simulate_month();
                    simulateMonthTime += (System.nanoTime() - startTime);
                    break;

                case "query_freelancer":
                    result = query_freelancer(st.nextToken());
                    break;

                case "query_customer":
                    result = query_customer(st.nextToken());
                    break;

                case "update_skill":
                    result = update_skill(
                            st.nextToken(),
                            Integer.parseInt(st.nextToken()),
                            Integer.parseInt(st.nextToken()),
                            Integer.parseInt(st.nextToken()),
                            Integer.parseInt(st.nextToken()),
                            Integer.parseInt(st.nextToken())
                    );
                    break;

                default:
                    result = "Unknown command: " + operation;
            }

            writer.write(result);
            writer.newLine();

        } catch (Exception e) {
            writer.write("Error processing command: " + commandLine);
            writer.newLine();
        }
    }

    // -------------------- HEAP INDEX HELPERS --------------------

    /**
     * Ensures the freelancer is present in the available-by-service heap
     * with up-to-date score and position. Only for available & not banned.
     */
    private static void addOrUpdateInAvailable(Freelancer f) {
        if (f == null) return;
        if (!f.isAvailable()) return;
        if (f.isPlatformBanned()) return;

        String service = f.getServiceName();
        MyMaxHeap<Freelancer> heap = availableByService.get(service);
        if (heap == null) {
            // First time we see this service, create a new heap
            heap = new MyMaxHeap<>(128);
            availableByService.put(service, heap);
        }

        // Remove old copy if any, then insert so index map and heap order are correct
        heap.remove(f);
        heap.insert(f);
    }

    /**
     * Removes the freelancer from the available index for its service, if present.
     */
    private static void removeFromAvailable(Freelancer f) {
        if (f == null) return;
        String service = f.getServiceName();
        MyMaxHeap<Freelancer> heap = availableByService.get(service);
        if (heap != null) {
            heap.remove(f);   // Uses index map for O(1) lookup + O(log n) fix
        }
    }

    // -------------------- REGISTER / QUERY --------------------

    public static String register_customer(String customerID) {
        if (users.isContainKey(customerID))
            return "Some error occurred in register customer.";
        User cus = new Customer(customerID);
        users.put(customerID, cus);
        Customer realCus = (Customer) cus;
        allCustomers.add(realCus);
        return "registered customer " + customerID;
    }

    public static String register_freelancer(String freelancerID, String serviceName, int basePrice, int T, int C, int R, int E, int A) {
        if (users.isContainKey(freelancerID))
            return "Some error occurred in register freelancer.";

        User free = new Freelancer(freelancerID, serviceName, basePrice, T, C, R, E, A);
        users.put(freelancerID, free);
        Freelancer realFree = (Freelancer) free;
        allFreelancers.add(realFree);

        ArrayList<Freelancer> list = serviceToFreelancers.get(serviceName);
        if (list == null) {
            list = new ArrayList<>();
            serviceToFreelancers.put(serviceName, list);
        }
        list.add(realFree);

        // Newly registered freelancer is available and not banned -> add to index
        addOrUpdateInAvailable(realFree);

        return "registered freelancer " + freelancerID;
    }

    public static String query_freelancer(String freelancerID) {
        User f1 = users.get(freelancerID);
        if (f1 != null) {
            if (f1 instanceof Freelancer) {
                Freelancer f = (Freelancer) f1;
                String availableStr = f.isAvailable() ? "yes" : "no";
                String burnoutStr = f.isBurnedOut() ? "yes" : "no";
                String ratingStr = String.format(Locale.US, "%.1f", f.getAvgRating());

                return freelancerID + ": "
                        + f.getServiceName()
                        + ", price: " + f.getPrice()
                        + ", rating: " + ratingStr
                        + ", completed: " + f.getCompletedCount()
                        + ", cancelled: " + f.getCancelledCount()
                        + ", skills: ("
                        + f.getT() + "," + f.getC() + "," + f.getR() + "," + f.getE() + "," + f.getA()
                        + "), available: " + availableStr
                        + ", burnout: " + burnoutStr;
            } else
                return "Some error occurred in query freelancer.";
        }
        return "Some error occurred in query freelancer.";
    }

    public static String query_customer(String customerID) {
        User u1 = users.get(customerID);
        if (u1 != null) {
            if (u1 instanceof Customer) {
                Customer c = (Customer) u1;
                int totalSpent = c.getTotalSpent();
                String tier = c.getLoyaltyTier();
                int blacklistedCount = c.getBlacklistedCount();
                int totalEmploymentCount = c.getTotalEmploymentCount();

                return customerID
                        + ": total spent: $" + totalSpent
                        + ", loyalty tier: " + tier
                        + ", blacklisted freelancer count: " + blacklistedCount
                        + ", total employment count: " + totalEmploymentCount;
            }
            return "Some error occurred in query customer.";
        }
        return "Some error occurred in query customer.";
    }

    // -------------------- EMPLOY / COMPLETE / CANCEL --------------------

    public static String employ_freelancer(String customerID, String freelancerID) {
        User usercus = users.get(customerID);
        User userfree = users.get(freelancerID);

        if (!(usercus instanceof Customer) || !(userfree instanceof Freelancer)) {
            return "Some error occurred in employ.";
        }

        Customer cus = (Customer) usercus;
        Freelancer free = (Freelancer) userfree;

        if (cus.isBlacklisted(freelancerID) || free.isPlatformBanned() || !free.isAvailable()) {
            return "Some error occurred in employ.";
        }

        // Once employed, freelancer is no longer available -> remove from heap index
        removeFromAvailable(free);

        free.setAvailable(false);
        free.setActiveCustomerID(customerID);
        cus.addActiveFreelancer(freelancerID);
        cus.incrementTotalEmploymentCount();

        return customerID + " employed " + freelancerID + " for " + free.getServiceName();
    }

    public static String complete_and_rate(String freelancerID, int rating) {
        if (rating < 0 || rating > 5) {
            return "Some error occurred in complete and rate.";
        }

        User userF = users.get(freelancerID);
        if (userF == null || !(userF instanceof Freelancer)) {
            return "Some error occurred in complete and rate.";
        }

        Freelancer freelancer = (Freelancer) userF;
        if (freelancer.isAvailable())
            return "Some error occurred in complete and rate.";

        String customerID = freelancer.getActiveCustomerID();
        if (customerID == null) {
            return "Some error occurred in complete and rate.";
        }

        User userC = users.get(customerID);
        if (userC == null || !(userC instanceof Customer)) {
            return "Some error occurred in complete and rate.";
        }

        Customer customer = (Customer) userC;
        customer.removeActiveFreelancer(freelancerID);

        int m = freelancer.getCompletedCount() + freelancer.getCancelledCount();
        double oldAvg = freelancer.getAvgRating();
        int effectiveCount = m + 1;
        double newAvg = ((oldAvg * effectiveCount) + rating) / (effectiveCount + 1);
        freelancer.setAvgRating(newAvg);

        freelancer.setAvailable(true);
        freelancer.setCompletedCount(freelancer.getCompletedCount() + 1);
        freelancer.setCompletedThisMonth(freelancer.getCompletedThisMonth() + 1);

        if (rating >= 4)
            applySkillGains(freelancer);

        freelancer.setActiveCustomerID(null);

        int price = freelancer.getPrice();
        int customerPayment = price * (100 - customer.getSubsidyPercent()) / 100;
        customer.addSpending(customerPayment);

        // Freelancer becomes available again -> reinsert into heap if not banned
        if (!freelancer.isPlatformBanned()) {
            addOrUpdateInAvailable(freelancer);
        }

        return freelancerID + " completed job for " + customerID + " with rating " + rating;
    }

    public static void applySkillGains(Freelancer freelancer) {
        int[] serviceSkills = ServiceProfiles.getSkillsFor(freelancer.getServiceName());
        if (serviceSkills == null || serviceSkills.length < 5) return;

        int[] idxForPriority = {0, 1, 2, 3, 4};

        // Sort indices by importance from service profile
        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 5; j++) {
                int vi = serviceSkills[idxForPriority[i]];
                int vj = serviceSkills[idxForPriority[j]];

                if (vj > vi || (vj == vi && idxForPriority[j] < idxForPriority[i])) {
                    int tmp = idxForPriority[i];
                    idxForPriority[i] = idxForPriority[j];
                    idxForPriority[j] = tmp;
                }
            }
        }

        int primary = idxForPriority[0];
        int secondary1 = idxForPriority[1];
        int secondary2 = idxForPriority[2];

        int T = freelancer.getT();
        int C = freelancer.getC();
        int R = freelancer.getR();
        int E = freelancer.getE();
        int A = freelancer.getA();

        int[] inc = new int[5];
        inc[primary] += 2;
        inc[secondary1] += 1;
        inc[secondary2] += 1;

        T = Math.min(100, T + inc[0]);
        C = Math.min(100, C + inc[1]);
        R = Math.min(100, R + inc[2]);
        E = Math.min(100, E + inc[3]);
        A = Math.min(100, A + inc[4]);

        freelancer.setT(T);
        freelancer.setC(C);
        freelancer.setR(R);
        freelancer.setE(E);
        freelancer.setA(A);
    }

    public static String cancel_by_freelancer(String freelancerID) {
        User fu = users.get(freelancerID);
        if (!(fu instanceof Freelancer)) {
            return "Some error occurred in cancel by freelancer.";
        }

        Freelancer freelancer = (Freelancer) fu;
        if (freelancer.isAvailable() || freelancer.getActiveCustomerID() == null) {
            return "Some error occurred in cancel by freelancer.";
        }

        String customerID = freelancer.getActiveCustomerID();

        // Job is cancelled, freelancer returns to available state
        freelancer.setAvailable(true);
        freelancer.setActiveCustomerID(null);

        User cu = users.get(customerID);
        if (cu instanceof Customer) {
            ((Customer) cu).removeActiveFreelancer(freelancerID);
        }

        double oldAvg = freelancer.getAvgRating();
        int completed = freelancer.getCompletedCount();
        int cancelled = freelancer.getCancelledCount();
        int effectiveCount = completed + cancelled + 1;
        double newAvg = ((oldAvg * effectiveCount) + 0) / (effectiveCount + 1);
        freelancer.setAvgRating(newAvg);

        freelancer.setCancelledCount(cancelled + 1);
        freelancer.setCancelledThisMonth(freelancer.getCancelledThisMonth() + 1);

        freelancer.setT(Math.max(0, freelancer.getT() - 3));
        freelancer.setC(Math.max(0, freelancer.getC() - 3));
        freelancer.setR(Math.max(0, freelancer.getR() - 3));
        freelancer.setE(Math.max(0, freelancer.getE() - 3));
        freelancer.setA(Math.max(0, freelancer.getA() - 3));

        String baseMessage = "cancelled by freelancer: " + freelancerID + " cancelled " + customerID;

        boolean bannedNow = false;
        if (freelancer.getCancelledThisMonth() >= 5) {
            freelancer.setPlatformBanned(true);
            bannedNow = true;
        }

        // If still allowed on platform, reinsert into heap
        if (!freelancer.isPlatformBanned()) {
            addOrUpdateInAvailable(freelancer);
        }

        if (bannedNow) {
            return baseMessage + System.lineSeparator()
                    + "platform banned freelancer: " + freelancerID;
        }

        return baseMessage;
    }

    public static String cancel_by_customer(String customerID, String freelancerID) {
        User cu = users.get(customerID);
        User fu = users.get(freelancerID);

        if (!(cu instanceof Customer) || !(fu instanceof Freelancer)) {
            return "Some error occurred in cancel by customer.";
        }

        Customer customer = (Customer) cu;
        Freelancer freelancer = (Freelancer) fu;

        if (freelancer.isAvailable()) {
            return "Some error occurred in cancel by customer.";
        }

        String currentCust = freelancer.getActiveCustomerID();
        if (currentCust == null || !currentCust.equals(customerID)) {
            return "Some error occurred in cancel by customer.";
        }

        customer.removeActiveFreelancer(freelancerID);
        freelancer.setAvailable(true);
        freelancer.setActiveCustomerID(null);

        customer.incrementCancelPenaltyCount();
        customer.applyCancelPenalty();

        // Customer cancelled, freelancer is free again (if not banned)
        if (!freelancer.isPlatformBanned()) {
            addOrUpdateInAvailable(freelancer);
        }

        return "cancelled by customer: " + customerID + " cancelled " + freelancerID;
    }

    // -------------------- BLACKLIST --------------------

    public static String blacklist(String customerID, String freelancerID) {
        User cu = users.get(customerID);
        User fu = users.get(freelancerID);

        if (!(cu instanceof Customer) || !(fu instanceof Freelancer)) {
            return "Some error occurred in blacklist.";
        }

        Customer customer = (Customer) cu;
        if (customer.isBlacklisted(freelancerID)) {
            return "Some error occurred in blacklist.";
        }

        customer.addToBlacklist(freelancerID);
        return customerID + " blacklisted " + freelancerID;
    }

    public static String unblacklist(String customerID, String freelancerID) {
        User cu = users.get(customerID);
        User fu = users.get(freelancerID);

        if (!(cu instanceof Customer) || !(fu instanceof Freelancer)) {
            return "Some error occurred in unblacklist.";
        }

        Customer customer = (Customer) cu;
        if (!customer.isBlacklisted(freelancerID)) {
            return "Some error occurred in unblacklist.";
        }

        customer.removeFromBlacklist(freelancerID);
        return customerID + " unblacklisted " + freelancerID;
    }

    // -------------------- request_job (HEAP + INDEX) --------------------

    /**
     * Uses a per-service max-heap with index map to select top-K freelancers
     * in O(k log n) time, then auto-employs the best one.
     */
    public static String request_job(String customerID, String serviceName, int topK) {
        User cu = users.get(customerID);
        if (!(cu instanceof Customer)) {
            return "Some error occurred in request job.";
        }
        Customer customer = (Customer) cu;

        MyMaxHeap<Freelancer> heap = availableByService.get(serviceName);
        if (heap == null || heap.isEmpty()) {
            return "no freelancers available";
        }

        // Temporarily popped freelancers (to restore heap later)
        ArrayList<Freelancer> popped = new ArrayList<>();
        ArrayList<Freelancer> selected = new ArrayList<>();

        while (!heap.isEmpty() && selected.size() < topK) {
            Freelancer f = heap.extractMax();  // O(log n) with index-map based heap
            if (f == null) break;
            popped.add(f);

            // Skip if customer has blacklisted this freelancer
            if (customer.isBlacklisted(f.getUserID())) {
                continue;
            }
            // Extra safety: skip if banned or unexpectedly unavailable
            if (f.isPlatformBanned() || !f.isAvailable()) {
                continue;
            }

            selected.add(f);
        }

        // Restore heap by re-inserting all popped freelancers
        for (Freelancer f : popped) {
            heap.insert(f);
        }

        if (selected.isEmpty()) {
            return "no freelancers available";
        }

        int count = Math.min(topK, selected.size());
        StringBuilder sb = new StringBuilder();
        sb.append("available freelancers for ")
                .append(serviceName)
                .append(" (top ")
                .append(count)
                .append("):")
                .append(System.lineSeparator());

        for (int i = 0; i < count; i++) {
            Freelancer f = selected.get(i);
            String ratingStr = String.format(Locale.US, "%.1f", f.getAvgRating());
            int compScore = f.computeCompositeScore(); // uses internal cache

            sb.append(f.getUserID())
                    .append(" - composite: ")
                    .append(compScore)
                    .append(", price: ")
                    .append(f.getPrice())
                    .append(", rating: ")
                    .append(ratingStr);

            if (i < count - 1) {
                sb.append(System.lineSeparator());
            }
        }

        Freelancer best = selected.get(0);
        sb.append(System.lineSeparator());
        sb.append("auto-employed best freelancer: ")
                .append(best.getUserID())
                .append(" for customer ")
                .append(customerID);

        // This will also remove the freelancer from the heap index internally
        employ_freelancer(customerID, best.getUserID());

        return sb.toString();
    }

    // -------------------- simulate_month (HEAP AWARE) --------------------

    /**
     * Applies queued service changes, updates burnout flags and loyalty tiers,
     * and refreshes heap positions where needed.
     */
    public static String simulate_month() {
        // 1. Apply queued service changes (serviceChangeMap)
        if (!serviceChangeHashTable.isEmpty()) {

            // iterate over internal buckets of MyHashTable
            Node<String, ServiceChangeInfo>[] buckets = (Node<String, ServiceChangeInfo>[]) serviceChangeHashTable.getTable();
            int cap = serviceChangeHashTable.getCapacity();

            for (int i = 0; i < cap; i++) {
                Node<String, ServiceChangeInfo> node = buckets[i];
                while (node != null) {
                    String freelancerID = node.key;
                    ServiceChangeInfo info = node.value;

                    User u = users.get(freelancerID);
                    if (u instanceof Freelancer) {
                        Freelancer f = (Freelancer) u;
                        String oldService = f.getServiceName();

                        // Remove from old per-service list
                        ArrayList<Freelancer> oldList = serviceToFreelancers.get(oldService);
                        if (oldList != null) {
                            for (int idx = 0; idx < oldList.size(); idx++) {
                                if (oldList.get(idx) == f) {
                                    int lastIdx = oldList.size() - 1;
                                    if (idx != lastIdx) {
                                        oldList.set(idx, oldList.get(lastIdx));
                                    }
                                    oldList.remove(lastIdx);
                                    break;
                                }
                            }
                        }

                        // Remove from old heap if still available and not banned
                        if (f.isAvailable() && !f.isPlatformBanned()) {
                            removeFromAvailable(f);
                        }

                        // Apply new service and price
                        f.setServiceName(info.newService);
                        f.setPrice(info.newPrice);

                        // Add to new per-service list
                        ArrayList<Freelancer> newList = serviceToFreelancers.get(info.newService);
                        if (newList == null) {
                            newList = new ArrayList<>();
                            serviceToFreelancers.put(info.newService, newList);
                        }
                        newList.add(f);

                        // Insert into new heap if available and allowed
                        if (f.isAvailable() && !f.isPlatformBanned()) {
                            addOrUpdateInAvailable(f);
                        }
                    }

                    node = node.next;
                }
            }

            serviceChangeHashTable.clear();
        }

        // 2. Update burnout flags and reset monthly counters
        for (int i = 0; i < allFreelancers.size(); i++) {
            Freelancer f = allFreelancers.get(i);
            int completedThisMonth = f.getCompletedThisMonth();

            if (!f.isBurnedOut()) {
                if (completedThisMonth >= 5) f.setBurnedOut(true);
            } else {
                if (completedThisMonth <= 2) f.setBurnedOut(false);
            }

            f.setCompletedThisMonth(0);
            f.setCancelledThisMonth(0);

            // Burnout affects composite score, so refresh heap position if applicable
            if (f.isAvailable() && !f.isPlatformBanned()) {
                addOrUpdateInAvailable(f);
            }
        }

        // 3. Recompute loyalty tier for all customers
        for (int i = 0; i < allCustomers.size(); i++) {
            Customer c = allCustomers.get(i);
            int eff = c.getEffectiveSpentForLoyalty();
            String tier;
            if (eff < 500) tier = "BRONZE";
            else if (eff < 2000) tier = "SILVER";
            else if (eff < 5000) tier = "GOLD";
            else tier = "PLATINUM";
            c.setLoyaltyTier(tier);
        }

        return "month complete";
    }

    // -------------------- change_service --------------------

    /**
     * Queues a service and price change for a freelancer to be applied
     * at the next simulate_month call.
     */
    public static String change_service(String freelancerID, String newService, int newPrice) {
        User u = users.get(freelancerID);
        if (!(u instanceof Freelancer)) {
            return "Some error occurred in change service.";
        }
        Freelancer f = (Freelancer) u;

        if (newPrice <= 0) {
            return "Some error occurred in change service.";
        }

        int[] skillsForNew = ServiceProfiles.getSkillsFor(newService);
        if (skillsForNew == null) {
            return "Some error occurred in change service.";
        }

        String oldService = f.getServiceName();

        // Override any previously queued change for this freelancer
        serviceChangeHashTable.put(freelancerID, new ServiceChangeInfo(newService, newPrice));

        return "service change for " + freelancerID
                + " queued from " + oldService
                + " to " + newService;
    }

    /**
     * Type3: directly updates a freelancer's skill profile.
     * Validates range and refreshes heap if the freelancer is available.
     */
    public static String update_skill(String freelancerID, int T, int C, int R, int E, int A) {
        if (T < 0 || T > 100 ||
                C < 0 || C > 100 ||
                R < 0 || R > 100 ||
                E < 0 || E > 100 ||
                A < 0 || A > 100) {
            return "Some error occurred in update skill.";
        }

        User u = users.get(freelancerID);
        if (!(u instanceof Freelancer)) {
            return "Some error occurred in update skill.";
        }

        Freelancer f = (Freelancer) u;

        f.setT(T);
        f.setC(C);
        f.setR(R);
        f.setE(E);
        f.setA(A);

        // Skill changes affect composite score; refresh heap if relevant
        if (f.isAvailable() && !f.isPlatformBanned()) {
            addOrUpdateInAvailable(f);
        }

        return "updated skills of " + freelancerID + " for " + f.getServiceName();
    }
}