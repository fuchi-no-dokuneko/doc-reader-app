package app.docreader;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.*;

final class PdfScreen implements ReaderScreen, PdfEngine.Listener {
    private final MainActivity activity;
    private final Document document;
    private final LinearLayout root;
    private final ZoomPage image;
    private final PdfControls controls;
    private final ProgressBar progress;
    private final PdfEngine engine;
    private int page, count;
    private boolean closed;

    PdfScreen(MainActivity activity, Document doc) {
        this.activity = activity; document = doc;
        Ui ui = activity.ui;
        root = ui.column();
        progress = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        root.addView(progress, new LinearLayout.LayoutParams(-1, ui.dp(3)));
        image = new ZoomPage(activity);
        image.setTooltipText(activity.getString(R.string.pdf_hint));
        root.addView(image, new LinearLayout.LayoutParams(-1, 0, 1));
        controls = new PdfControls(activity, image, this::render, () -> page, () -> count);
        root.addView(controls.view);
        engine = new PdfEngine(activity.library.file(doc), this);
        page = activity.library.position(doc, ".page");
        root.post(() -> { if (!closed) render(page); });
    }
    private void render(int index) {
        controls.busy(true); progress.setVisibility(View.VISIBLE);
        engine.render(index, image.getWidth());
    }
    @Override public void ready(Bitmap bitmap, int index, int total) {
        page = index; count = total;
        image.bitmap(bitmap);
        image.setContentDescription(activity.getString(R.string.page_count, page+1, count));
        controls.update(page, count); progress.setVisibility(View.INVISIBLE);
        save();
    }
    @Override public void failed() {
        root.removeAllViews(); root.addView(StatusScreen.error(activity, R.string.pdf_error),
            new LinearLayout.LayoutParams(-1, -1));
    }
    @Override public View view() { return root; }
    @Override public void save() {
        if (count > 0) activity.library.position(document, ".page", page);
    }
    @Override public void close() { closed = true; engine.close(); image.bitmap(null); }
}
