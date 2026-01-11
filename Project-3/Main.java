import java.io.*;
import java.util.Locale;

public class Main {

    // Core system object managing the MatrixNet simulation logic
    private static MatrixNet matrixNet = new MatrixNet();

    public static void main(String[] args) {
        // Set locale to US to ensure dot separators are used for decimals (e.g., 2.5)
        Locale.setDefault(Locale.US);

        if (args.length != 2) {
            System.err.println("Usage: java Main <input_file> <output_file>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        // Use try-with-resources to handle file I/O safety
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                processCommand(line, writer);
            }

        } catch (IOException e) {
            System.err.println("Error reading/writing files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processCommand(String command, BufferedWriter writer) throws IOException {
        // Parse the command line by splitting on whitespace
        String[] parts = command.split("\\s+");
        String operation = parts[0];

        String logMessage = "";

        try {
            switch (operation) {
                case "spawn_host":
                    // Format: spawn_host <hostId> <clearanceLevel>
                    String hostId = parts[1];
                    int clearanceLevel = Integer.parseInt(parts[2]);
                    logMessage = matrixNet.spawnHost(hostId, clearanceLevel);
                    break;

                case "link_backdoor":
                    // Format: link_backdoor <h1> <h2> <latency> <bandwidth> <firewall>
                    String h1 = parts[1];
                    String h2 = parts[2];
                    int latency = Integer.parseInt(parts[3]);
                    int bandwidth = Integer.parseInt(parts[4]);
                    int firewall = Integer.parseInt(parts[5]);
                    logMessage = matrixNet.linkBackdoor(h1, h2, latency, bandwidth, firewall);
                    break;

                case "seal_backdoor":
                    // Format: seal_backdoor <h1> <h2>
                    // Toggles the status between sealed and unsealed
                    String sH1 = parts[1];
                    String sH2 = parts[2];
                    logMessage = matrixNet.sealBackdoor(sH1, sH2);
                    break;

                case "trace_route":
                    // Format: trace_route <src> <dest> <min_bw> <lambda>
                    String src = parts[1];
                    String dest = parts[2];
                    int minBandwidth = Integer.parseInt(parts[3]);
                    int lambda = Integer.parseInt(parts[4]); // Congestion factor
                    logMessage = matrixNet.traceRoute(src, dest, minBandwidth, lambda);
                    break;

                case "scan_connectivity":
                    // Analyze network connectivity (connected vs disconnected)
                    logMessage = matrixNet.scanConnectivity();
                    break;

                case "simulate_breach":
                    // Distinguish between host breach (1 arg) and backdoor breach (2 args)
                    if (parts.length == 2) {
                        String breachHost = parts[1];
                        logMessage = matrixNet.simulateHostBreach(breachHost);
                    } else {
                        String bH1 = parts[1];
                        String bH2 = parts[2];
                        logMessage = matrixNet.simulateBackdoorBreach(bH1, bH2);
                    }
                    break;

                case "oracle_report":
                    // Generates a comprehensive topology report
                    logMessage = matrixNet.oracleReport();
                    break;

                default:
                    logMessage = "Unknown command: " + operation;
            }

            // Write the result to the output file if a message was generated
            if (!logMessage.isEmpty()) {
                writer.write(logMessage);
                writer.newLine();
            }

        } catch (Exception e) {
            // Handle parsing errors or runtime exceptions
            e.printStackTrace();
        }
    }
}