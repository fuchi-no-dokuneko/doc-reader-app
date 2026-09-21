package app.docreader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

final class SharedText {
    static Document create(String text, Library library) throws IOException {
        if (text == null) throw new IllegalArgumentException("Missing text");
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > DocFormat.TEXT.limit()) throw new BoundedCopy.TooLarge();
        String id = UUID.randomUUID().toString();
        File file = new File(library.directory, id);
        try { Files.write(file.toPath(), bytes); }
        catch (IOException error) { file.delete(); throw error; }
        Document doc = new Document(id, "Shared text.txt", DocFormat.TEXT, bytes.length);
        library.remember(doc);
        return doc;
    }
}
