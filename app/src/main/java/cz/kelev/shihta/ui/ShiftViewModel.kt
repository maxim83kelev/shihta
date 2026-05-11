package cz.kelev.shihta.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cz.kelev.shihta.db.ShiftDatabase
import cz.kelev.shihta.db.ShiftEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class ShiftViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = ShiftDatabase.getInstance(app).shiftDao()
    val prefs = app.getSharedPreferences("shihta_prefs", Context.MODE_PRIVATE)

    private val now = Calendar.getInstance()
    val today = now.get(Calendar.DAY_OF_MONTH)
    val realMonth = now.get(Calendar.MONTH) + 1
    val realYear = now.get(Calendar.YEAR)

    // Выбранный месяц и год — берём из настроек, по умолчанию текущий
    private val _selectedYear = MutableStateFlow(prefs.getInt("selected_year", realYear))
    private val _selectedMonth = MutableStateFlow(prefs.getInt("selected_month", realMonth))

    val selectedYear: StateFlow<Int> = _selectedYear
    val selectedMonth: StateFlow<Int> = _selectedMonth

    private val monthNames = listOf(
    "Leden", "Únor", "Březen", "Duben", "Květen", "Červen",
    "Červenec", "Srpen", "Září", "Říjen", "Listopad", "Prosinec"
)

val currentMonthName: String
    get() = monthNames[_selectedMonth.value - 1]

    val daysInCurrentMonth: Int
        get() {
            val cal = Calendar.getInstance()
            cal.set(_selectedYear.value, _selectedMonth.value - 1, 1)
            return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        }

    val entries: StateFlow<List<ShiftEntry>> = combine(_selectedYear, _selectedMonth) { year, month ->
        Pair(year, month)
    }.flatMapLatest { (year, month) ->
        dao.getEntriesForMonth(year, month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedMonth(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
        prefs.edit().putInt("selected_year", year).putInt("selected_month", month).apply()
    }

    fun saveEntry(day: Int, stavba: String, hodiny: String, poznamky: String) {
        viewModelScope.launch {
            dao.insert(
                ShiftEntry(
                    year = _selectedYear.value,
                    month = _selectedMonth.value,
                    day = day,
                    stavba = stavba,
                    hodiny = hodiny,
                    poznamky = poznamky
                )
            )
        }
    }
    // Итого и заработок
    val showHours = MutableStateFlow(prefs.getBoolean("show_hours", false))
    val showEarnings = MutableStateFlow(prefs.getBoolean("show_earnings", false))
    val hourlyRate = MutableStateFlow(prefs.getFloat("hourly_rate", 0f))

    fun setShowHours(value: Boolean) {
        showHours.value = value
        prefs.edit().putBoolean("show_hours", value).apply()
    }

    fun setShowEarnings(value: Boolean) {
        showEarnings.value = value
        prefs.edit().putBoolean("show_earnings", value).apply()
    }

    fun setHourlyRate(value: Float) {
        hourlyRate.value = value
        prefs.edit().putFloat("hourly_rate", value).apply()
    }
}