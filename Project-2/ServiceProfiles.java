public class ServiceProfiles {

    // Static final arrays
    private static final int[] PAINT = {70, 60, 50, 85, 90};
    private static final int[] WEB_DEV = {95, 75, 85, 80, 90};
    private static final int[] GRAPHIC_DESIGN = {75, 85, 95, 70, 85};
    private static final int[] DATA_ENTRY = {50, 50, 30, 95, 95};
    private static final int[] TUTORING = {80, 95, 70, 90, 75};
    private static final int[] CLEANING = {40, 60, 40, 90, 85};
    private static final int[] WRITING = {70, 85, 90, 80, 95};
    private static final int[] PHOTOGRAPHY = {85, 80, 90, 75, 90};
    private static final int[] PLUMBING = {85, 65, 60, 90, 85};
    private static final int[] ELECTRICAL = {90, 65, 70, 95, 95};

    public static int[] getSkillsFor(String serviceName) {
        switch (serviceName) {
            case "paint": return PAINT;
            case "web_dev": return WEB_DEV;
            case "graphic_design": return GRAPHIC_DESIGN;
            case "data_entry": return DATA_ENTRY;
            case "tutoring": return TUTORING;
            case "cleaning": return CLEANING;
            case "writing": return WRITING;
            case "photography": return PHOTOGRAPHY;
            case "plumbing": return PLUMBING;
            case "electrical": return ELECTRICAL;
            default: return null;
        }
    }
}
