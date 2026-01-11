package gradlereportlink.datastores;

import org.json.JSONObject;

/**
 * @author James Haro
 */
public class Image {

    private final int visit_id;
    private final String name;
    private final String category_name;
    private final String created_at;
    private final String url;
    private final int image_count;
    private final JSONObject imageJson;

    private Image(JSONObject json) {
        this.visit_id = json.optInt("visit_id");
        this.name = json.optString("name");
        this.category_name = json.optString("category_name");
        this.created_at = json.optString("created_at");
        this.url = json.optString("url");
        this.image_count = json.optInt("image_count");
        this.imageJson = new JSONObject();
        imageJson.put("visit_id", visit_id);
        imageJson.put("name", name);
        imageJson.put("category_name", category_name);
        imageJson.put("created_at", created_at);
        imageJson.put("url", url);
        imageJson.put("image_count", image_count);
    }

    public static Image fromJSON(JSONObject json) {
        return new Image(json);
    }

    public JSONObject toJSON() {
        return imageJson;
    }

}