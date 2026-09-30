package app.docreader;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.DocumentsProvider;
import java.io.*;

public class UatDocumentsProvider extends DocumentsProvider {
    private File directory;
    private static final String[] COLUMNS = {Document.COLUMN_DOCUMENT_ID,
        Document.COLUMN_DISPLAY_NAME, Document.COLUMN_MIME_TYPE, Document.COLUMN_FLAGS, Document.COLUMN_SIZE};
    @Override public boolean onCreate() {
        directory = new File(getContext().getFilesDir(), "fixtures"); directory.mkdirs();
        try {
            for (String name : new String[]{"styled.epub", "empty.epub"}) {
                try (InputStream in = getContext().getAssets().open(name);
                     OutputStream out = new FileOutputStream(new File(directory, name))) {
                    byte[] buffer = new byte[4096]; int count;
                    while ((count = in.read(buffer)) >= 0) out.write(buffer, 0, count);
                }
            }
            return true;
        } catch (IOException e) { throw new IllegalStateException(e); }
    }
    @Override public Cursor queryRoots(String[] projection) {
        MatrixCursor out = new MatrixCursor(new String[]{Root.COLUMN_ROOT_ID, Root.COLUMN_TITLE,
            Root.COLUMN_DOCUMENT_ID, Root.COLUMN_FLAGS, Root.COLUMN_MIME_TYPES});
        out.addRow(new Object[]{"root", "Doc Reader fixtures", "root", 0, "application/epub+zip"}); return out;
    }
    private void row(MatrixCursor out, String id) {
        File file = new File(directory, id);
        out.addRow(new Object[]{id, id.equals("root") ? "Doc Reader fixtures" : id,
            id.equals("root") ? Document.MIME_TYPE_DIR : "application/epub+zip", 0,
            id.equals("root") ? 0 : file.length()});
    }
    @Override public Cursor queryDocument(String id, String[] projection) {
        MatrixCursor out = new MatrixCursor(COLUMNS); row(out, id); return out;
    }
    @Override public Cursor queryChildDocuments(String id, String[] projection, String sort) {
        MatrixCursor out = new MatrixCursor(COLUMNS);
        row(out, "empty.epub"); row(out, "styled.epub"); return out;
    }
    @Override public ParcelFileDescriptor openDocument(String id, String mode, CancellationSignal signal)
        throws FileNotFoundException {
        if (!id.equals("empty.epub") && !id.equals("styled.epub")) throw new FileNotFoundException(id);
        return ParcelFileDescriptor.open(new File(directory, id), ParcelFileDescriptor.MODE_READ_ONLY);
    }
}
