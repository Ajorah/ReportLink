/**
 * @author James Haro
 */
package gradlereportlink.process;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONObject;
import gradlereportlink.datastores.Site;
import gradlereportlink.datastores.Visit;

public class Start {

    public static CompletableFuture<String> main(String startdate) { // Entry point
        processSince(startdate).join(); // Begins
        System.out.println("Finished processing visits for all sites since " + startdate + "."); // Declare end of parsing
        return CompletableFuture.completedFuture("Complete"); // Placeholder
    }

    private static CompletableFuture<String> processSince(String startdate) { // Promise method to build a list of Sites
        return Query.processResponse(Query.archQuery("sites.json?updated_since=" + startdate).build()) // Build the query
            .thenApply(HttpResponse::body) // Generate the response as the body
            .thenApply(body -> { // Apply the following logic to the body
                JSONArray response = new JSONArray(body); // Convert the body to a JSON Array                
                for (int i = 0; i < response.length(); i++) { // Iterate the length of the Array
                    JSONObject siteObject = response.optJSONObject(i); // Assign the current iteration to a Site Object
                    Site currentSite = Site.fromJSON(siteObject);
                    JSONArray visitsResponse = siteObject.optJSONArray("visits"); // Generate visits into a separate array for the current Site
                    for (int j = 0; j < visitsResponse.length(); j++) { // Iterate the length fo the Array
                        JSONObject visitObject = visitsResponse.optJSONObject(j); // Current iterations object
                        Visit currentVisit = Visit.fromJSON(visitObject);
                        PDF.main(currentVisit, currentSite);
                        JPG.main(currentVisit, currentSite);
                    }
                }
                return "Completed"; // Response code finality.
            });
    }
    
}