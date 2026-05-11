package cz.kelev.shihta.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {

    @Query("SELECT * FROM shift_entries WHERE year = :year AND month = :month ORDER BY day ASC")
    fun getEntriesForMonth(year: Int, month: Int): Flow<List<ShiftEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ShiftEntry)

    @Query("SELECT * FROM shift_entries WHERE year = :year AND month = :month AND day = :day LIMIT 1")
    suspend fun getEntry(year: Int, month: Int, day: Int): ShiftEntry?
}
