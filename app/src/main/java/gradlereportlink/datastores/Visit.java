/**
 * @author jharo
 */
package gradlereportlink.datastores;

import org.json.JSONObject;
public class Visit { // Custom extracted version of the Visit object in the expanded Site Query
    private final int visit_id; // Extracted visit "visit_id"
    private final int site_id; // Extracted visit "site_id"
    private final String date; // Extracted visit "date" timestamp
    private final String unique_id; // Extracted visit "unique_id", not uuid
    private final String pdf_link; // Extracted visit "pdf_link", for debugging/reference; Render.java doesn't actually use this
    private final JSONObject visitJson; // Final JSON object for building a List<Visit>

    private Visit(JSONObject json) { // Builds and declares a JSON Object of type Visit
        this.visit_id = json.optInt("visit_id"); // Returns the declaration of a parsed visit_id key
        this.site_id = json.optInt("site_id"); // Returns the declaration of a parsed site_id key
        this.date = json.optString("date"); // Returns the declaration of a parsed date key
        this.unique_id = json.optString("unique_id"); // Returns the declaration of a parsed unique_id key
        this.pdf_link = json.optString("pdf_link"); // Returns the declaration of a parsed pdf_link key
        this.visitJson = new JSONObject(); // Declares this JSON Object
        visitJson.put("visit_id", visit_id);  // Builds the JSON object's visit_id, by the key "visit_id"
        visitJson.put("site_id", site_id); // Builds the JSON object's site_id, by the key "site_id"
        visitJson.put("date", date); // Builds the JSON object's date, by the key "date"
        visitJson.put("unique_id", unique_id); // Builds the JSON object's unique_id, by the key "unique_id"
        visitJson.put("pdf_link", pdf_link); // Builds the JSON object's pdf_link, by the key "pdf_link"
    }

    public static Visit fromJSON(JSONObject json) {
        return new Visit(json);
    }

    public JSONObject toJSON() { // Allows getting the JSON object of the called Visit object
        return visitJson;
    }
    
}
