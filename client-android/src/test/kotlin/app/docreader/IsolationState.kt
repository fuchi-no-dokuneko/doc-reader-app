package app.docreader

import app.docreader.ui.ReaderModel
import app.docreader.domain.Mark
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*

object IsolationState {
    fun seed(model: ReaderModel,first: String,second: String): List<String> = runBlocking {
        val ids=listOf(first,second).map { model.tab(it)!!.document.id }
        assertNotEquals(ids[0],ids[1]); assertEquals(2,model.state.value.library.size)
        ids.forEachIndexed { i,id -> model.repo.addMark(Mark(document=id,page=0,text="mark-$i")) }
        model.preferences.encoding(ids[0],"UTF-8")
        model.preferences.encoding(ids[1],"GB18030")
        ids
    }
    fun check(model: ReaderModel,ids: List<String>) = runBlocking {
        ids.forEachIndexed { i,id -> assertEquals("mark-$i",model.repo.marks(id).single().text) }
        assertEquals("UTF-8",model.preferences.encoding(ids[0]).first())
        assertEquals("GB18030",model.preferences.encoding(ids[1]).first())
        assertEquals(2,model.repo.documents().size)
        assertTrue(model.preferences.workspace.first().contains(ids[0]))
        assertTrue(model.preferences.workspace.first().contains(ids[1]))
    }
}
