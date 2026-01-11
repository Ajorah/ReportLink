/**
 * @author James Haro
 */

package gradlereportlink.process;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.concurrent.CompletableFuture;

final class Query { // Dynamic query handler
    private static final String URL = System.getenv("RPT_ARCHI_URL"); // BASE URL for ArchiSnapper
    private static final String TOKEN = System.getenv("RPT_ARCHI_TOKEN"); // Get auth_token query parameter
    private static final HttpClient archClient = HttpClient.newHttpClient(); // Instance of a new http client for archquery
    protected static HttpRequest.Builder archQuery(String path) { // Create a new HttpRequest.Builder with the base URL and headers
        String separator = path.contains("?") ? "&" : "?"; // Check if the query has parameters, appending an & if it does
        return HttpRequest.newBuilder() // Return the built query...
            .uri(URI.create(URL).resolve(path + separator + "auth_token=" + TOKEN)) // Parse the full URI with the given query path, with a ternary separator including the required auth_token
            .header("accept", "application/json") // As a JSON response
            .GET(); // Using the GET method with no body
    }

    protected static CompletableFuture<HttpResponse<String>> processResponse(HttpRequest request) { // Promise all normal String responses
        return archClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()); // use archClient for the thread-safe queries
    }

    protected static CompletableFuture<HttpResponse<byte[]>> renderFile(HttpRequest request, BodyHandler<byte[]> bodyHandler) { // Promise a byte handler to write PDF/Image files
        return archClient.sendAsync(request, bodyHandler); //  use archClient for the thread-safe queries
    }

}