package app.docreader;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

final class PdfEngine {
    interface Listener {
        void ready(Bitmap image, int page, int count);
        void failed();
    }
    private final File file;
    private final Listener listener;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private PdfRenderer renderer;
    private volatile boolean closed;
    private volatile int generation;

    PdfEngine(File file, Listener listener) { this.file = file; this.listener = listener; }
    void render(int requested, int width) {
        int ticket = ++generation;
        worker.submit(() -> {
            if (closed || ticket != generation) return;
            Bitmap bitmap = null;
            try {
                if (renderer == null) {
                    ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                    try { renderer = new PdfRenderer(fd); }
                    catch (Exception error) { fd.close(); throw error; }
                }
                int count = renderer.getPageCount();
                if (count == 0) throw new IOException("Empty PDF");
                int index = PagePosition.clamp(requested, count);
                try (PdfRenderer.Page page = renderer.openPage(index)) {
                    double scale = Math.min(Math.max(1, width) * 2.0 / page.getWidth(),
                        Math.sqrt(4000000.0 / ((double) page.getWidth() * page.getHeight())));
                    bitmap = Bitmap.createBitmap(Math.max(1, (int)(page.getWidth() * scale)),
                        Math.max(1, (int)(page.getHeight() * scale)), Bitmap.Config.ARGB_8888);
                    bitmap.eraseColor(Color.WHITE);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                }
                Bitmap result = bitmap;
                main.post(() -> {
                    if (closed || ticket != generation) result.recycle();
                    else listener.ready(result, index, count);
                });
            } catch (Exception | OutOfMemoryError error) {
                if (bitmap != null) bitmap.recycle();
                main.post(() -> { if (!closed && ticket == generation) listener.failed(); });
            }
        });
    }
    void close() {
        closed = true;
        worker.submit(() -> { if (renderer != null) renderer.close(); });
        worker.shutdown();
    }
}
