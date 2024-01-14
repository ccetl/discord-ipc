package ccetl.discordipc;

import com.google.gson.JsonObject;

@SuppressWarnings("unused")
public class RichPresence {
    private String details;
    private String state;

    private Assets assets;
    private Timestamps timestamps;

    public void setDetails(String details) {
        this.details = details;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setLargeImage(String key, String text) {
        if (assets == null) {
            assets = new Assets();
        }
        assets.large_image = key;
        assets.large_text = text;
    }

    public void setSmallImage(String key, String text) {
        if (assets == null) {
            assets = new Assets();
        }
        assets.small_image = key;
        assets.small_text = text;
    }

    public void setStart(long time) {
        if (timestamps == null) {
            timestamps = new Timestamps();
        }
        timestamps.start = time;
    }

    public void setEnd(long time) {
        if (timestamps == null) {
            timestamps = new Timestamps();
        }
        timestamps.end = time;
    }

    public JsonObject toJson() {
        // Main
        JsonObject jsonObject = new JsonObject();

        if (details != null) {
            jsonObject.addProperty("details", details);
        }
        if (state != null) {
            jsonObject.addProperty("state", state);
        }

        // Assets
        if (assets != null) {
            jsonObject.add("assets", getAssets());
        }

        // Timestamps
        if (timestamps != null) {
            jsonObject.add("timestamps", getTimeStamps());
        }

        return jsonObject;
    }

    private JsonObject getAssets() {
        JsonObject jsonObject = new JsonObject();

        if (assets.large_image != null) {
            jsonObject.addProperty("large_image", assets.large_image);
        }
        if (assets.large_text != null) {
            jsonObject.addProperty("large_text", assets.large_text);
        }
        if (assets.small_image != null) {
            jsonObject.addProperty("small_image", assets.small_image);
        }
        if (assets.small_text != null) {
            jsonObject.addProperty("small_text", assets.small_text);
        }
        return jsonObject;
    }

    private JsonObject getTimeStamps() {
        JsonObject jsonObject1 = new JsonObject();

        if (timestamps.start != null) {
            jsonObject1.addProperty("start", timestamps.start);
        }
        if (timestamps.end != null) {
            jsonObject1.addProperty("end", timestamps.end);
        }
        return jsonObject1;
    }

    public static class Assets {
        public String large_image, large_text;
        public String small_image, small_text;
    }

    public static class Timestamps {
        public Long start;
        public Long end;
    }
}
