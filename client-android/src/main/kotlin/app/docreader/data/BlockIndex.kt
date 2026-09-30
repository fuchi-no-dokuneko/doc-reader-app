package app.docreader.data

import app.docreader.domain.*
import app.docreader.format.DocumentParser
import java.io.File

object BlockIndex {
    @Synchronized fun open(file: File, info: DocumentInfo, encoding: String, cache: File,
        resolve: (String) -> String): Pair<DiskBlocks,String> {
        val revision=info.contentHash.ifEmpty { FileHash.of(file) }
        val charset=java.net.URLEncoder.encode(encoding,"UTF-8")
        val directory = File(cache, "text-v3-$revision-${info.kind.name}-$charset")
        val marker = File(directory,"ready")
        if (!marker.exists()) {
            directory.deleteRecursively(); directory.mkdirs()
            try {
                val detected = DiskBlocks(directory,true).use {
                    DocumentParser.parse(file,info,encoding,directory,it,resolve)
                }
                marker.writeText(detected)
            } catch (e: Exception) { directory.deleteRecursively(); throw e }
        }
        return DiskBlocks(directory) to marker.readText()
    }
}
