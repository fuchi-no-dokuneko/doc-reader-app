package app.docreader;

import android.net.Uri;
import java.nio.file.Files;
import java.util.concurrent.*;

final class OpenController {
    private final MainActivity activity;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private ReaderScreen reader;
    private Future<?> pending;
    private int generation;
    private boolean busy;
    Document document;

    OpenController(MainActivity activity) { this.activity = activity; }
    void importUri(Uri uri, String mime) {
        load(() -> DocumentImport.read(activity.getContentResolver(), uri, mime, activity.library));
    }
    void importText(String text) { load(() -> SharedText.create(text, activity.library)); }
    void open(Document doc) { load(() -> doc); }
    private void load(Callable<Document> source) {
        clear(); busy = true;
        int request = generation;
        activity.shell.display(activity.getString(R.string.app_name), true, StatusScreen.loading(activity));
        pending = worker.submit(() -> {
            try {
                Document doc = source.call();
                String text = doc.format == DocFormat.PDF ? null
                    : TextDecoder.decode(Files.readAllBytes(activity.library.file(doc).toPath()));
                activity.runOnUiThread(() -> {
                    if (request != generation || activity.isDestroyed()) return;
                    document = doc; busy = false;
                    activity.library.remember(doc);
                    reader = doc.format == DocFormat.PDF ? new PdfScreen(activity, doc)
                        : new TextScreen(activity, doc, text);
                    activity.shell.display(doc.name, true, reader.view());
                });
            } catch (Exception error) {
                activity.runOnUiThread(() -> {
                    if (request != generation || activity.isDestroyed()) return;
                    busy = true;
                    activity.shell.display(activity.getString(R.string.open_failed), true,
                        StatusScreen.error(activity, StatusScreen.message(error)));
                });
            }
        });
    }
    boolean reading() { return busy || document != null; }
    void save() { if (reader != null) reader.save(); }
    void clear() {
        generation++; busy = false;
        if (pending != null) { pending.cancel(true); pending = null; }
        if (reader != null) { reader.save(); reader.close(); reader = null; }
        document = null;
    }
    void destroy() { clear(); worker.shutdownNow(); }
}
