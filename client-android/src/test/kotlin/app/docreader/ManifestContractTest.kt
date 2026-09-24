package app.docreader

import org.junit.Test
import org.junit.Assert.*
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class ManifestContractTest {
    @Test fun externalViewShareAndUntypedFilesResolveWithoutNetworkPermission() {
        val xml=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File("src/main/AndroidManifest.xml"))
        val filters=xml.getElementsByTagName("intent-filter")
        val views=mutableListOf<Set<String>>()
        var shares=false
        for (i in 0 until filters.length) {
            val filter=filters.item(i) as Element
            val action=(filter.getElementsByTagName("action").item(0) as Element).getAttribute("android:name")
            val data=filter.getElementsByTagName("data")
            val types=(0 until data.length).map { (data.item(it) as Element).getAttribute("android:mimeType") }.filter(String::isNotEmpty).toSet()
            if (action.endsWith(".VIEW")) views+=types
            if (action.endsWith(".SEND")) shares=types.containsAll(listOf("text/*","application/*"))
        }
        assertTrue(views.any { it.containsAll(listOf("text/*","application/*")) })
        assertTrue(views.any { it.isEmpty() }); assertTrue(shares)
        val activity=xml.getElementsByTagName("activity").item(0) as Element
        assertEquals("true",activity.getAttribute("android:exported"))
        assertEquals("singleTop",activity.getAttribute("android:launchMode"))
        assertEquals(0,xml.getElementsByTagName("uses-permission").length)
    }
}
