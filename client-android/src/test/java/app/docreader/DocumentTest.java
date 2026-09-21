package app.docreader;

import org.junit.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

public class DocumentTest {
    @Test public void recognizesFilesFromProviders() {
        assertEquals(DocFormat.PDF, DocFormat.detect("REPORT.PDF", "application/octet-stream"));
        assertEquals(DocFormat.MARKDOWN, DocFormat.detect("notes.md", "text/plain"));
        assertEquals(DocFormat.TEXT, DocFormat.detect("document", "text/plain; charset=UTF-8"));
        assertThrows(IllegalArgumentException.class, () -> DocFormat.detect("photo.jpg", "image/jpeg"));
    }
    @Test public void decodesChineseUtf8AndBom() throws Exception {
        String text = "閱讀文件 / 阅读文件";
        assertEquals(text, TextDecoder.decode(text.getBytes(StandardCharsets.UTF_8)));
        assertEquals(text, TextDecoder.decode(("\ufeff" + text).getBytes(StandardCharsets.UTF_8)));
    }
    @Test public void decodesTextSavedAsUtf16() throws Exception {
        String text = "第一頁\nSecond page";
        assertEquals(text, TextDecoder.decode(("\ufeff" + text).getBytes(StandardCharsets.UTF_16LE)));
        assertEquals(text, TextDecoder.decode(text.getBytes(StandardCharsets.UTF_16)));
    }
    @Test public void copiesTheActualDocumentBytes() throws Exception {
        byte[] bytes = "A short document\n文件".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertEquals(bytes.length, BoundedCopy.copy(new ByteArrayInputStream(bytes), output, DocFormat.TEXT.limit()));
        assertArrayEquals(bytes, output.toByteArray());
    }
    @Test public void rejectsTextBeyondTheDocumentLimit() {
        byte[] bytes = new byte[(int)DocFormat.TEXT.limit()+1];
        assertThrows(BoundedCopy.TooLarge.class, () -> BoundedCopy.copy(
            new ByteArrayInputStream(bytes), new ByteArrayOutputStream(), DocFormat.TEXT.limit()));
    }
}
