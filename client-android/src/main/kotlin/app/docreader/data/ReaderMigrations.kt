package app.docreader.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object ReaderMigrations {
    val V1_V2 = object: Migration(1,2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE documents ADD COLUMN contentHash TEXT NOT NULL DEFAULT ''")
            db.execSQL("UPDATE documents SET contentHash=lower(id) WHERE length(id)=64 AND id NOT GLOB '*[^0-9a-fA-F]*'")
        }
    }
}
