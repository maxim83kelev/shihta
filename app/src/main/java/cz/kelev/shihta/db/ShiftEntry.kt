package cz.kelev.shihta.db

import androidx.room.Entity

@Entity(
    tableName = "shift_entries",
    primaryKeys = ["year", "month", "day"]
)
data class ShiftEntry(
    val year: Int,
    val month: Int,
    val day: Int,
    val stavba: String = "",
    val hodiny: String = "",
    val poznamky: String = ""
)