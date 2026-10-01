package app.docreader

import android.content.Context
import android.net.Uri
import androidx.room.testing.MigrationTestHelper
import app.docreader.data.*
import java.io.File

object MigrationFixture {
    const val name="migration-uat.db"
    suspend fun create(helper: MigrationTestHelper,context: Context): Pair<String,File> {
        val source=File(context.filesDir,"migration-source.py").apply { writeText("print('legacy')") }
        val id=FileHash.of(source).uppercase()
        val files=File(context.filesDir,"documents").apply { mkdirs() }
        source.copyTo(File(files,id),true)
        File(files,"opaque-old").writeText("old opaque snapshot")
        helper.createDatabase(name,1).use { db ->
            db.execSQL("INSERT INTO documents VALUES(?,?,?,?,?,?)",
                arrayOf<Any>(id,"legacy.py","CODE",source.length(),Uri.fromFile(source).toString(),100L))
            db.execSQL("INSERT INTO documents VALUES('opaque-old','other.txt','TEXT',19,'old:other',0)")
            db.execSQL("INSERT INTO positions VALUES(?,'legacy-tab',3,27,4294967296)",arrayOf(id))
            db.execSQL("INSERT INTO marks VALUES(9,?,3,0,2,'retained','bookmark','',4294967296)",arrayOf(id))
        }
        val preferences=PreferenceStore(context)
        preferences.workspace("""{"tabs":[{"id":"legacy-tab","doc":"$id"}],"active":"legacy-tab"}""")
        preferences.encoding(id,"UTF-8")
        return id to source
    }
}
