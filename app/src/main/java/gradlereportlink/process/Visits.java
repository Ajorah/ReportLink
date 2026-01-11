/**
 * @author James Haro
 */
package gradlereportlink.process;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gradlereportlink.datastores.Site;
import gradlereportlink.datastores.Visit;
public class Visits {

    protected static void main(Site site, long interval) { // Main method takes in a Site object
        getVisits(site, interval);// getId of the Site and pass it tot he getVisits() method
    }

    private static void getVisits(Site site, long interval) { // Take in an integer value of the siteId
        JSONArray resultArray = new JSONArray(); // Create new array
        listVisits(site, interval) // Pass the siteId value to the listvisits() method
            .thenAcceptAsync(visits -> { // Parse the response by the visits body
                for (Visit visit : visits) { // Iterate through each visit in the visits reponse
                    System.out.println("Site ID: " + visit.toJSON().optInt("site_id") + " = Site Name: " + site.toJSON().optString("name") + "\n    Visit: " + visit.toJSON().optInt("visit_id")); // Log current state
                    resultArray.put(visit.toJSON()); // Put the Visit object generated into the new array
                    if (visit.toJSON().optInt("visit_id") > 0) { // If the visit "exists"
                        PDF.main(visit, site); // Render the PDF
                        JPG.main(visit, site); // Render the JPG's
                    } else {
                        System.out.println("visit_id is invalid or array is empty"); // Error handling
                    }
                }
            }).join(); // Wait for the promise to complete
    }

    private static CompletableFuture<List<Visit>> listVisits(Site site, long interval) { // Promise query to return the list of Visits for the site
        Instant now = Instant.now(); // Instantiate the current timestamp
        return Query.processResponse(Query.archQuery("sites.json?id=" + site.toJSON().optInt("id")).build()) // Return the query
            .thenApply(HttpResponse::body) // Build the respone body
            .thenApplyAsync(body -> { // Process the body
                JSONArray visits = new JSONArray(body).optJSONObject(0).optJSONArray("visits"); // Build an array of the response's visits array
                List<Visit> newVisits = new ArrayList<>(); // Create new array
                try { // Try catch block for error handling
                    for (int i = 0; i < visits.length(); i++) { // Iterate through the visits array
                        JSONObject visitObject = visits.optJSONObject(i); // Parse the current iteration as a JSON Object
                        String dateStr = visitObject.optString("date"); // Check the JSON Objects current date
                        if (!dateStr.isEmpty()) { // If the date is not empty
                            try { // Try catch block for error handling
                                Instant date = ZonedDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant(); // Parse the date as a timestamp
                                if (Duration.between(date, now).toMillis() <= interval) { // If the timestamp is within the interval threshold
                                    newVisits.add(Visit.fromJSON(visitObject)); // Add the Visit object to the array
                                }
                            } catch (Exception e) { // Catch all exceptions
                                System.err.println("Failed to parse date: " + dateStr); // Print the error
                            }
                        } else {
                            System.out.println("Visit (ID: " + visitObject.optInt("visit_id") + ", Site: " + visitObject.optInt("site_id") + ") has no date field."); // Print that the date is empty
                        }
                    }
                } catch (JSONException e) { // Catch all JSON exceptions
                    System.err.println("Failed to parse JSON response for Site ID: " + site.toJSON().optInt("id") + ". Body: " + body + " - " + e.getMessage()); // Print the error
                }
                return newVisits; // Return the array
            });
    }
}