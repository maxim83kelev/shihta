package cz.kelev.shihta.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ShiftEntry::class], version = 2)
abstract class ShiftDatabase : RoomDatabase() {

    abstract fun shiftDao(): ShiftDao

    companion object {
        @Volatile
        private var INSTANCE: ShiftDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE shift_entries_new (year INTEGER NOT NULL, month INTEGER NOT NULL, day INTEGER NOT NULL, stavba TEXT NOT NULL DEFAULT '', hodiny TEXT NOT NULL DEFAULT '', poznamky TEXT NOT NULL DEFAULT '', PRIMARY KEY(year, month, day))")
                database.execSQL("INSERT OR REPLACE INTO shift_entries_new (year, month, day, stavba, hodiny, poznamky) SELECT year, month, day, stavba, hodiny, poznamky FROM shift_entries GROUP BY year, month, day")
                database.execSQL("DROP TABLE shift_entries")
                database.execSQL("ALTER TABLE shift_entries_new RENAME TO shift_entries")
            }
        }

        fun getInstance(context: Context): ShiftDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ShiftDatabase::class.java,
                    "shihta.db"
                )
                .addMigrations(MIGRATION_1_2)
                .build().also { INSTANCE = it }
            }
        }
    }
}