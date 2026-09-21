package app.docreader;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import java.io.*;
import java.util.UUID;

final class DocumentImport {
    static Document read(ContentResolver resolver, Uri uri, String hint, Library library) throws IOException {
        String name = uri.getLastPathSegment();
        String mime = resolver.getType(uri);
        try (Cursor cursor = resolver.query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) name = cursor.getString(0);
        } catch (RuntimeException ignored) {}
        if (mime == null || mime.equals("application/octet-stream")) mime = hint;
        if (name == null || name.trim().isEmpty()) name = "Document";
        DocFormat format = DocFormat.detect(name, mime);
        String id = UUID.randomUUID().toString();
        File copy = new File(library.directory, id);
        try {
            long bytes;
            try (InputStream input = resolver.openInputStream(uri);
                 OutputStream output = new FileOutputStream(copy)) {
                if (input == null) throw new IOException("No stream");
                bytes = BoundedCopy.copy(input, output, format.limit());
            }
            if (format != DocFormat.PDF) TextDecoder.decode(java.nio.file.Files.readAllBytes(copy.toPath()));
            if (Thread.currentThread().isInterrupted()) throw new IOException("Cancelled");
            Document doc = new Document(id, name, format, bytes);
            library.remember(doc);
            return doc;
        } catch (IOException | RuntimeException error) {
            copy.delete();
            throw error;
        }
    }
    private DocumentImport() {}
}
