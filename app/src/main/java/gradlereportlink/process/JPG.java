package gradlereportlink.process;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gradlereportlink.datastores.Image;
import gradlereportlink.datastores.Site;
import gradlereportlink.datastores.Visit;
import gradlereportlink.system.RegexFormatter;
import gradlereportlink.system.Storage;

/**
 * @author James Haro
 */
public class JPG {

    protected static void main(Visit visit, Site site) { // Parse Renders by visitId
        listImages(visit) // Generate a list of images based on the current Visit objects Query
            .thenAccept(images -> { // Assign the result a function named images
                System.out.println("      Images: " + images.size()); // Display how many images pinged
                for (int i = 0; i < images.size(); i++) { // Iterate through the list
                    Image image = images.get(i); // Get the current iteration
                    getImage(site, image) // Download the image based on the site's id
                        .thenAccept(result -> { // Dispplay the result of the promise
                            System.out.println("      JPG: " + result); // As a result
                        });
                }
            }).join(); // Wait for completion in this thread.
    }

    private static CompletableFuture<List<Image>> listImages(Visit currentVisit) { // Pass the currentVisit Visit object to the function for a List of Image objects
        return Query.processResponse(Query.archQuery("visits/" + currentVisit.toJSON().optInt("visit_id")).build()) // Parse the query
            .thenApply(HttpResponse::body) // Assign the response as a body
            .thenApply(body -> { // Function body parses:
                List<Image> imageList = new ArrayList<>(); // Initialization of Image array
                try { 
                    JSONObject response = new JSONObject(body); // Turn the body into a JSON Object for manipulation
                    JSONArray checklistItems = response.optJSONArray("checklist_items"); // Create an array from the object
                    if (checklistItems != null) { // Check for null entries
                        for (int i = 0; i < checklistItems.length(); i++) { // Iterate through the array
                            JSONArray pictures = checklistItems.getJSONObject(i).optJSONArray("checklist_item_pictures"); // Iterate the second array within, the pictures
                            if (pictures != null && pictures.length() > 0) { // Check for null entries/# of images
                                for (int j = 0, c = 1; j < pictures.length(); j++, c++) { // Second loop w/ log-friendly variable (human readable)
                                    JSONObject picture = pictures.getJSONObject(j); // Get the current images data and assign as picture object
                                    JSONObject image = new JSONObject(); // Initialize a new object and assign it's properties below
                                    image.put("visit_id", response.optInt("visit_id"));
                                    image.put("name", checklistItems.getJSONObject(i).optString("name"));
                                    image.put("category_name", checklistItems.getJSONObject(i).optString("category_name"));
                                    image.put("created_at", OffsetDateTime.parse(picture.optString("created_at")).toLocalDate().toString());
                                    image.put("url", picture.optString("url"));
                                    image.put("image_count", c);
                                    imageList.add(Image.fromJSON(image)); // Add the image object to the list from the properties of the temporary object
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return imageList; // Return the promise
        });
    }

    private static CompletableFuture<String> getImage(Site site, Image current) { // Asynchronously render a visit's Image(s) (promise)
        String filename = current.toJSON().optInt("visit_id") + "_"
            + current.toJSON().optString("name") + "_"
            + current.toJSON().optString("category_name") + "_"
            + current.toJSON().optString("created_at") + "_"
            + current.toJSON().optInt("image_count") + ".jpg"; // Assigned filename
        String imageFolder = site.toJSON().optString("unique_id")
            + "_" 
            + site.toJSON().optString("name"); // Assigned folder name
        String safeFilename = RegexFormatter.sanitize(filename); // Ensure compliance in filefolder structures
        String safePath = RegexFormatter.sanitize(imageFolder); // Ensure compliance in filefolder structures
        return Query.renderFile(downloadJPG(current), HttpResponse.BodyHandlers.ofByteArray()) // Otherwise download the file
            .thenApply(HttpResponse::body) // Build body
            .thenApply(bytes -> { // Apply byte[]
                try {
                    boolean success = Storage.WriteToSMB(false, safePath, safeFilename, bytes); // Try to write byte[]
                    if (!success) { // If fail
                        return safeFilename + " exists, skipping..."; // Return
                    }
                } catch (Exception e) { // Catch all Input/Output exceptions
                    e.printStackTrace(); // Print the exception
                    return "Error Writing to SMB...";
                }
                return safeFilename; // Produce the filename for the result.
            });
    }

    private static HttpRequest downloadJPG(Image current) { // Render an image for a checklist item by its URL
        return HttpRequest.newBuilder()
            .uri(URI.create(current.toJSON().optString("url")))
            .build();
        }

}