package app.docreader.data

import app.docreader.domain.*
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption.*
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

internal object ImportCommit {
    suspend fun apply(repo: LocalRepository, staged: File, source: String, title: String,
        kind: Kind, hash: String, retainId: String?): DocumentInfo = repo.importLock.withLock {
        currentCoroutineContext().ensureActive()
        withContext(NonCancellable) {
            val prior = if (retainId==null) repo.source(source) else {
                val row=repo.document(retainId)
                require(row!=null && row.source==source) {
                    "The original library entry changed. Use Save a copy to keep your edits."
                }
                row
            }
            val info=DocumentInfo(prior?.id ?: UUID.randomUUID().toString(),title,kind,
                staged.length(),source,contentHash=hash)
            val target=repo.file(info.id)
            val existed=target.exists()
            val backup=File.createTempFile("backup-",".part",repo.files)
            if (existed) Files.move(target.toPath(),backup.toPath(),ATOMIC_MOVE,REPLACE_EXISTING)
            else backup.delete()
            try {
                Files.move(staged.toPath(),target.toPath(),ATOMIC_MOVE,REPLACE_EXISTING)
                repo.remember(info)
            } catch (failure: Throwable) {
                try {
                    if (existed) Files.move(backup.toPath(),target.toPath(),ATOMIC_MOVE,REPLACE_EXISTING)
                    else Files.deleteIfExists(target.toPath())
                } catch (restore: Throwable) { failure.addSuppressed(restore) }
                throw failure
            }
            backup.delete()
            info
        }
    }
}
