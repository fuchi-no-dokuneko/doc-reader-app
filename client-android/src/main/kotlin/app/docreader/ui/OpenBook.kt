package app.docreader.ui

import app.docreader.domain.*
import app.docreader.data.BlockIndex
import app.docreader.render.*
import java.io.File
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.withLock

internal object OpenBook {
    suspend fun load(model: ReaderModel,runtime: TabRuntime,id: String,document: DocumentInfo,
        encoding: String,password: String?): DocumentInfo {
        val (info,file)=model.repo.snapshot(document)
        runtime.mutex.withLock { runInterruptible {
            runtime.close()
            if (info.kind==Kind.PDF) runtime.pdf=PdfBook(file,password)
            else {
                val cache=model.repo.cache(info.id)
                val (blocks,detected)=BlockIndex.open(file,info,encoding,cache) {
                    model.assets.resolve(info.source,it)
                }
                runtime.text=TextBook(blocks,detected,File(cache,"layout-$id"),model.assets)
            }
        } }
        return info
    }
}
