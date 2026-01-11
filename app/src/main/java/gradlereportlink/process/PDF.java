/**
 * @author James Haro
 */
package gradlereportlink.process;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import gradlereportlink.datastores.Site;
import gradlereportlink.datastores.Visit;
import gradlereportlink.system.RegexFormatter;
import gradlereportlink.system.Storage;
public class PDF {

    protected static void main(Visit visit, Site site) { // Parse Renders by visitId
        getPDF(visit, site) // Call object by the parameter passed to main()
            .thenAccept(result -> { // When promise is fulfilled, assign pdfFileName
                System.out.println("      Render: " + result); // Log the successful file saving operation
        }).join(); // Return the final result
    }

    private static CompletableFuture<String> getPDF(Visit visit, Site site) { // Asynchronously render a visit as a PDF (promise)
        String filename = visit.toJSON().optString("visit_id") + "_" + visit.toJSON().optString("unique_id") + ".pdf"; // Assigned filename
        String reportFolder = site.toJSON().optString("unique_id") + "_" + site.toJSON().optString("name"); // Sane folder for the Site
        String safeFilename = RegexFormatter.sanitize(filename); // Sanitize/clean the filename for compliance in OS Filefolder structure
        String safePath = RegexFormatter.sanitize(reportFolder); // Sanitize the Path/Folder structure for compliance
        return Query.renderFile(renderPDF(visit), HttpResponse.BodyHandlers.ofByteArray()) // Build the query based on the visitId value
            .thenApply(HttpResponse::body) // Build the response to the query as the body
            .thenApply(bytes -> { // Build the bytes as a function of
                try {
                    boolean success = Storage.WriteToSMB(true, safePath, safeFilename, bytes); // Determine success
                    if (!success) {
                        return  safeFilename + " exists, skipping...";
                    }
                } catch (Exception e) { // Catch all Input/Output exceptions
                    e.printStackTrace(); // Print the exception
                    return "Error Writing to SMB...";
                }
                return safeFilename; // Log the successful file saving operation"; // Return the filename
            });
    }

    private static HttpRequest renderPDF(Visit visit) { // Render a PDF for a visit by its ID
        return Query.archQuery("visits/" + visit.toJSON().optInt("visit_id") + "/render_pdf?lang=en").build(); // Build the query
    }

}