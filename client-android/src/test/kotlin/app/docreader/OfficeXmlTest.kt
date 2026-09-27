package app.docreader

import app.docreader.domain.*
import app.docreader.format.*
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder

class OfficeXmlTest {
    @get:Rule val temporary=TemporaryFolder()
    @Test fun wordParagraphsAndMultiParagraphTableCells() {
        val file=zipFixture(temporary.newFile(),mapOf("word/document.xml" to """
            <w:document xmlns:w="urn:word"><w:body><w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:t>Chapter</w:t></w:r></w:p>
            <w:tbl><w:tr><w:tc><w:p><w:r><w:t>First</w:t></w:r></w:p><w:p><w:r><w:t>Second</w:t></w:r></w:p></w:tc>
            <w:tc><w:p><w:r><w:t>42</w:t></w:r></w:p></w:tc></w:tr></w:tbl></w:body></w:document>"""))
        val out=Blocks(); DocxDocument.parse(file,out)
        assertEquals(BlockType.HEADING,out.values.first().type)
        val table=out.values.first { it.type==BlockType.TABLE }
        assertTrue(table.text.contains("First\nSecond")); assertTrue(table.html.contains("42"))
    }
    @Test fun spreadsheetSharedStringsFormulasAndDates() {
        val file=zipFixture(temporary.newFile(),mapOf(
            "xl/workbook.xml" to "<workbook xmlns:r='urn:rels'><sheets><sheet name='Data' r:id='r1'/></sheets></workbook>",
            "xl/_rels/workbook.xml.rels" to "<Relationships><Relationship Id='r1' Target='worksheets/sheet1.xml'/></Relationships>",
            "xl/sharedStrings.xml" to "<sst><si><r><t>Hello </t></r><r><t>world</t></r></si></sst>",
            "xl/styles.xml" to "<styleSheet><cellXfs><xf numFmtId='0'/><xf numFmtId='14'/></cellXfs></styleSheet>",
            "xl/worksheets/sheet1.xml" to "<worksheet><sheetData><row r='1'><c r='A1' t='s'><v>0</v></c><c r='B1'><f>1+2</f><v>3</v></c><c r='C1' s='1'><v>45292</v></c></row></sheetData></worksheet>"))
        val out=Blocks(); XlsxDocument.parse(file,out,temporary.newFolder())
        assertTrue(out.text.contains("Hello world")); assertTrue(out.text.contains("2024-01-01"))
        assertTrue(out.values.any { it.type==BlockType.TABLE && "<th>C</th>" in it.html })
    }
}
