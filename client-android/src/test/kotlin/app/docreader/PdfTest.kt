package app.docreader

import app.docreader.pdf.*
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder

class PdfTest {
    @get:Rule val temporary=TemporaryFolder()
    private fun fixture(name: String)=temporary.newFile(name).apply {
        PdfTest::class.java.getResourceAsStream("/pdfium/$name")!!.use { input -> outputStream().use { input.copyTo(it) } }
    }
    @Test fun pdfiumTextAndInheritedResources() {
        for (name in listOf("hello_world.pdf","hello_world_2_pages_split_streams.pdf")) {
            PortablePdf(fixture(name),"").use { pdf ->
                assertTrue(pdf.structure.pages.isNotEmpty())
                val content=pdf.page(0).text.joinToString("") { it.text }
                assertTrue("$name: $content; contents=${pdf.structure.page(0)["Contents"]}",content.contains("Hello, world!"))
                assertTrue(pdf.page(0).text.flatMap { it.bounds }.all { it.right>=it.left && it.bottom>=it.top })
            }
        }
    }
    @Test fun pdfiumEncryptedRevisionsUserAndOwnerPasswords() {
        for (revision in listOf(2,3,5,6)) for (password in listOf("âge","hôtel")) {
            val file=fixture("encrypted_hello_world_r$revision.pdf")
            PortablePdf(file,password).use { pdf ->
                assertTrue("R$revision $password",pdf.page(0).text.joinToString("") { it.text }.contains("Hello, world!"))
                val unlocked=PdfWrite.unlocked(pdf.source)
                try { PortablePdf(unlocked,"").use { clear ->
                    assertNull(clear.source.crypt)
                    assertTrue(clear.page(0).text.joinToString("") { it.text }.contains("Hello, world!"))
                } } finally { unlocked.delete() }
            }
            file.delete()
        }
    }
    @Test fun wrongPasswordRejected() {
        val file=fixture("encrypted_hello_world_r6.pdf")
        assertThrows(SecurityException::class.java) { PortablePdf(file,"wrong") }
    }
}
