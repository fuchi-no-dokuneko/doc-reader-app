package app.docreader;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

final class Library {
    final File directory;
    final SharedPreferences prefs;

    Library(Context context) {
        directory = new File(context.getFilesDir(), "documents");
        directory.mkdirs();
        prefs = context.getSharedPreferences("reading", Context.MODE_PRIVATE);
    }
    synchronized List<Document> list() {
        List<Document> docs = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString("library", "[]"));
            for (int i = 0; i < array.length(); i++) {
                Document doc = new Document(array.getJSONObject(i));
                if (file(doc).isFile()) docs.add(doc);
            }
        } catch (JSONException | IllegalArgumentException ignored) {}
        return docs;
    }
    synchronized void remember(Document doc) {
        List<Document> docs = list();
        docs.removeIf(item -> item.id.equals(doc.id));
        docs.add(0, doc);
        while (docs.size() > 12) erase(docs.remove(docs.size() - 1));
        write(docs);
    }
    synchronized void remove(Document doc) {
        List<Document> docs = list();
        docs.removeIf(item -> item.id.equals(doc.id));
        erase(doc);
        write(docs);
    }
    private void erase(Document doc) {
        file(doc).delete();
        prefs.edit().remove(doc.id + ".page").remove(doc.id + ".scroll").apply();
    }
    private void write(List<Document> docs) {
        JSONArray array = new JSONArray();
        try { for (Document doc : docs) array.put(doc.json()); }
        catch (JSONException error) { throw new IllegalStateException(error); }
        prefs.edit().putString("library", array.toString()).apply();
    }
    File file(Document doc) { return new File(directory, doc.id); }
    Document find(String id) {
        for (Document doc : list()) if (doc.id.equals(id)) return doc;
        return null;
    }
    int position(Document doc, String key) { return prefs.getInt(doc.id + key, 0); }
    void position(Document doc, String key, int value) {
        prefs.edit().putInt(doc.id + key, value).apply();
    }
}
