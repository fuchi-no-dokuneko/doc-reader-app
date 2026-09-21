package app.docreader;

import org.json.JSONException;
import org.json.JSONObject;

final class Document {
    final String id, name;
    final DocFormat format;
    final long size;

    Document(String id, String name, DocFormat format, long size) {
        this.id = id; this.name = name; this.format = format; this.size = size;
    }
    Document(JSONObject json) throws JSONException {
        this(json.getString("id"), json.getString("name"),
            DocFormat.valueOf(json.getString("format")), json.getLong("size"));
    }
    JSONObject json() throws JSONException {
        return new JSONObject().put("id", id).put("name", name)
            .put("format", format.name()).put("size", size);
    }
}
