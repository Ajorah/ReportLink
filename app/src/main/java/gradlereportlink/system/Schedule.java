/**
 * SCHEDULER
 * @author jharo
 */
package gradlereportlink.system;

import gradlereportlink.process.Start;
import gradlereportlink.process.Summary;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Schedule {
    private static final long INTERVAL = Long.parseLong(System.getenv("RPT_INTERVAL_MINS")) * 60L * 1000L; // Minutes in Milliseconds
    private static final String STARTDATE = System.getenv("RPT_START_DATE"); // Date in ISO format including Timezone
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(); // This thread is just a scheduler. Async is across the rest of the project

    public static void main() { // Can take arguments, need?
        System.out.println("Running fetch since " + STARTDATE + " System Variable."); // Print the timestamp to fetch from
        Start.main(STARTDATE).join(); // Wait for the START to finish
        Runnable task = () -> { // Task for running every interval of scheduler
            Instant now = Instant.now(); // Mark this millisecond
            System.out.println("Fetching summary at " + now); // Print to console
            System.out.println("Processing data since: " + now.minusMillis(INTERVAL)); // Tell me that time milliseconds as a differential.
            Summary.main(INTERVAL); // Continue every INTERVAL
        };

        // Run every INTERVAL, E.G every 30 minutes (30 * 60 * 1000)
        scheduler.scheduleAtFixedRate(task, INTERVAL, INTERVAL, TimeUnit.MILLISECONDS);
        
        // Shutdown "Gracefully"
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { // Hook a shutdown management protocol into the Runtime
            scheduler.shutdown(); // Hook it into th scheduler
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) { // Allows the termination period of 60 seconds
                    scheduler.shutdownNow(); // Shut down
                }
            } catch (InterruptedException ie) { // Interrupted?
                scheduler.shutdownNow(); // If I must
                Thread.currentThread().interrupt(); // System trap.
            }
        }));
    }
}