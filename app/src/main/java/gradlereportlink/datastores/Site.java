/**
 * @author James Haro
 */
package gradlereportlink.datastores;

import org.json.JSONObject;
public class Site { // Custom extracted version of the Site object in the Summary of Sites Query
    private final int id; // Extracted site "id"
    private final String name; // Extracted site "name"
    private final String unique_id; // Extracted site "unique_id", not uuid
    private final String updated_at; // Extracted site's "updated_at" timestamp
    private final JSONObject siteJson; // Final JSON object for building a List<Site>
    
    private Site(JSONObject json) { // Builds and declares a JSON Object of type Site
        this.id = json.optInt("id"); // Returns the declaration of a parsed id key
        this.name = json.optString("name"); // Returns the declaration of a parsed name key
        this.unique_id = json.optString("unique_id"); // Returns the declaration of a parsed unique_id key
        this.updated_at = json.optString("updated_at"); // Returns the declaration of a parsed updated_at key
        this.siteJson = new JSONObject(); // Declares this JSON Object
        siteJson.put("id", id); // Builds the JSON object's id, by the key "id"
        siteJson.put("name", name); // Builds the JSON object's name, by the key "name"
        siteJson.put("unique_id", unique_id); // Builds the JSON object's unique_id, by the key "unique_id"
        siteJson.put("updated_at", updated_at); // Builds the JSON object's updated_at timestamp, by the key "updated_at"
    }

    public static Site fromJSON(JSONObject json) {
        return new Site(json);
    }

    public JSONObject toJSON() { // Allows getting the JSON object of the called Site object
        return siteJson;
    }

}