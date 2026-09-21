package app.docreader;

import org.junit.Test;
import static org.junit.Assert.*;

public class NavigationTest {
    @Test public void searchFindsNextAndWraps() {
        String text = "First document, second DOCUMENT.";
        int first = TextSearch.next(text, "document", 0);
        int second = TextSearch.next(text, "document", first+1);
        assertEquals(6, first);
        assertEquals(23, second);
        assertEquals(first, TextSearch.next(text, "document", second+1));
        assertEquals(-1, TextSearch.next(text, "missing", 0));
    }
    @Test public void typedPageNumbersStayInsideDocument() {
        assertEquals(0, PagePosition.parse("1", 12));
        assertEquals(11, PagePosition.parse("12", 12));
        assertEquals(-1, PagePosition.parse("13", 12));
        assertEquals(-1, PagePosition.parse("0", 12));
        assertEquals(-1, PagePosition.parse("", 12));
    }
    @Test public void restoresAvailablePageAfterDocumentChanges() {
        assertEquals(7, PagePosition.clamp(7, 20));
        assertEquals(2, PagePosition.clamp(7, 3));
    }
}
