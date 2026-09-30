package app.docreader.data

import app.docreader.domain.DocumentInfo
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption.*
import kotlinx.coroutines.sync.withLock

internal object RevisionSnapshot {
    suspend fun open(repo: LocalRepository, requested: DocumentInfo): Pair<DocumentInfo,File> =
        repo.importLock.withLock {
            val row=repo.document(requested.id)
            var info=row ?: requested
            val source=repo.file(info.id)
            if (info.contentHash.isEmpty()) {
                info=info.copy(contentHash=FileHash.of(source))
                if (row!=null) repo.remember(info)
            }
            val frozen=File(repo.cache(info.id),"snapshot-${info.contentHash}")
            if (!frozen.exists()) {
                val staged=File.createTempFile("snapshot-",".part",frozen.parentFile)
                try {
                    source.inputStream().use { input -> staged.outputStream().use { input.copyTo(it) } }
                    Files.move(staged.toPath(),frozen.toPath(),ATOMIC_MOVE,REPLACE_EXISTING)
                } finally { staged.delete() }
            }
            // Keep old revisions for live readers, including EPUB zip assets.
            info to frozen
        }
}
