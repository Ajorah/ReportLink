/**
 * @author James Haro
 */
package gradlereportlink.process;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import gradlereportlink.datastores.Site;

public class Summary {

    public static void main(long interval) { // Entry point]
        Instant begin = Instant.now();
        List<Site> sites = getSites(begin, interval); // Create a List of Site objects via the getSites() method
        for (Site site : sites) { // Iterate through the sites List
            Visits.main(site, interval); // Call Visits.main for each site
        }
        System.out.println("Finished processing visits for all sites between a " + (interval / 60000) + " minute timeframe. Took " + Duration.between(begin, Instant.now()).toMillis() + " milliseconds."); // Declare end of parsing
    }

    private static List<Site> getSites(Instant now, long interval) { // Return the list of Site objects
        JSONArray resultArray = new JSONArray(); // Initialize the new array
        return listSites(now, interval) // Return the promise of a list of Sites
            .thenApplyAsync(sites -> { // Apply the following logic by the sites array of Site objects
                System.out.println("Site Updates: " + sites.size()); // Log the number of new Site objects
                for (Site site : sites) { // Iterate through the Site objects
                    resultArray.put(site.toJSON()); // Append to the new array
                }
                return sites; // Return the fetched sites
            }).join(); // Join the current request
    }

    private static CompletableFuture<List<Site>> listSites(Instant when, long interval) { // Promise method to build a list of Sites
        List<Site> updatedSites = new ArrayList<>(); // Create a list of Site objects that is empty
        return Query.processResponse(Query.archQuery("sites/summary.json").build()) // Build the query
            .thenApply(HttpResponse::body) // Apply the following logic to the body
            .thenApply(body -> { // Apply the following logic to the body
                JSONArray sitesArray = new JSONArray(body); // Create an array of sites from the body
                for (int i = 0; i < sitesArray.length(); i++) { // Iterate through the sites array
                    JSONObject siteObject = sitesArray.optJSONObject(i); // Create a Site object, in the current iteration of the array
                    String updatedAtStr = siteObject.optString("updated_at"); // Define the updated_at timeStamp
                    if (!updatedAtStr.isEmpty()) { // If the timestamp exists and is not empty
                        try { // Try catch block for error handling
                            Instant updatedAt = ZonedDateTime.parse(updatedAtStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant(); // Define the updated_at timestamp
                            if (Duration.between(updatedAt, when).toMillis() <= interval) { // Check the threshold against NOW being less or equal.
                                updatedSites.add(Site.fromJSON(siteObject)); // Add object if within threshold
                            }
                        } catch (Exception e) { // Catch all exceptions
                            System.err.println("Error: " + e.getMessage()); // Print parsing error
                        }
                    }
                }
                return updatedSites; // Return array
            });
    }

}