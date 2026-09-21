package app.docreader;

import org.junit.Test;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class ManifestTest {
    @Test public void fileManagerCanResolveViewForSupportedMimeTypes() throws Exception {
        Element filter = filter("android.intent.action.VIEW");
        Set<String> types = data(filter, "android:mimeType");
        assertTrue(types.contains("application/pdf"));
        assertTrue(types.contains("text/plain"));
        assertTrue(types.contains("text/markdown"));
        assertTrue(types.contains("application/octet-stream"));
        assertTrue(data(filter, "android:scheme").contains("content"));
        assertTrue(data(filter, "android:scheme").contains("file"));
        Element activity = (Element)filter.getParentNode();
        assertEquals("true", activity.getAttribute("android:exported"));
        assertEquals("singleTop", activity.getAttribute("android:launchMode"));
    }
    @Test public void acceptsSharedDocumentTypes() throws Exception {
        assertTrue(data(filter("android.intent.action.SEND"), "android:mimeType").contains("application/pdf"));
    }
    private Element filter(String action) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        org.w3c.dom.Document xml = factory.newDocumentBuilder().parse(new File("src/main/AndroidManifest.xml"));
        NodeList actions = xml.getElementsByTagName("action");
        for (int i = 0; i < actions.getLength(); i++) {
            Element element = (Element)actions.item(i);
            if (action.equals(element.getAttribute("android:name"))) return (Element)element.getParentNode();
        }
        throw new AssertionError("Missing intent action: " + action);
    }
    private Set<String> data(Element filter, String attribute) {
        Set<String> values = new HashSet<>();
        NodeList nodes = filter.getElementsByTagName("data");
        for (int i = 0; i < nodes.getLength(); i++) values.add(((Element)nodes.item(i)).getAttribute(attribute));
        return values;
    }
}
