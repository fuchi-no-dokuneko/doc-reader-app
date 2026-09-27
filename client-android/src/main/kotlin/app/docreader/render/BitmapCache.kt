package app.docreader.render

import android.graphics.Bitmap

class BitmapCache(private val capacity: Long = 32L*1024*1024) {
    private val entries = LinkedHashMap<String,Bitmap>(8,.75f,true)
    private var bytes = 0L
    @Synchronized fun get(key: String) = entries[key]
    @Synchronized fun put(key: String, bitmap: Bitmap) {
        entries.remove(key)?.let { bytes -= it.allocationByteCount }
        if (bitmap.allocationByteCount > capacity) return
        entries[key] = bitmap; bytes += bitmap.allocationByteCount
        val iterator = entries.entries.iterator()
        while (bytes > capacity && iterator.hasNext()) {
            bytes -= iterator.next().value.allocationByteCount; iterator.remove()
        }
    }
    @Synchronized fun retain(keys: Set<String>) {
        val iterator = entries.entries.iterator()
        while (iterator.hasNext()) { val entry = iterator.next()
            if (entry.key !in keys) { bytes -= entry.value.allocationByteCount; iterator.remove() }
        }
    }
    @Synchronized fun clear() { entries.clear(); bytes = 0 }
}
