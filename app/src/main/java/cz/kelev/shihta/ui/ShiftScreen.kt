package cz.kelev.shihta.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.kelev.shihta.db.ShiftEntry

val ColorBrown = Color(0xFF6B4226)
val ColorHeader = Color(0xFF8B5E3C)
val ColorBackground = Color(0xFFEDE8DF)
val ColorPaper = Color(0xFFFAF7F2)
val ColorBorder = Color(0xFFD0C0A8)
val ColorToday = Color(0xFFFFF176)

@Composable
fun ShiftScreen(
    vm: ShiftViewModel = viewModel(),
    userName: String = "",
    onSettingsClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    var showShareSheet by remember { mutableStateOf(false) }
    val entries by vm.entries.collectAsState()
    val entryMap = entries.associateBy { it.day }
    val selectedYear by vm.selectedYear.collectAsState()
    val showHours by vm.showHours.collectAsState()
    val showEarnings by vm.showEarnings.collectAsState()
    val hourlyRate by vm.hourlyRate.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Хедер
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorBrown)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (userName.isBlank()) "Šichta" else userName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${vm.currentMonthName} $selectedYear",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .background(ColorPaper)
                    .border(1.dp, ColorBorder)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {

                    // Заголовок таблицы
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ColorHeader)
                            .padding(vertical = 6.dp)
                    ) {
                        HeaderCell("Datum", 0.13f)
                        HeaderCell("Stavba", if (!showHours && !showEarnings) 0.35f else 0.28f)
                        HeaderCell("Hodiny", if (!showHours && !showEarnings) 0.17f else 0.15f)
                        HeaderCell("Poznámky", if (!showHours && !showEarnings) 0.35f else 0.20f)
                        if (showEarnings) HeaderCell("Kč", 0.12f)
                    }

                    LazyColumn(contentPadding = PaddingValues(bottom = 400.dp)) {
                        items((1..vm.daysInCurrentMonth).toList()) { day ->
                            val isToday = day == vm.today &&
                                vm.selectedMonth.value == vm.realMonth &&
                                vm.selectedYear.value == vm.realYear
                            ShiftRow(
                                day = day,
                                entry = entryMap[day],
                                isToday = isToday,
                                showHours = showHours,
                                showEarnings = showEarnings,
                                hourlyRate = hourlyRate,
                                onSave = { stavba, hodiny, poznamky ->
                                    vm.saveEntry(day, stavba, hodiny, poznamky)
                                }
                            )
                            HorizontalDivider(color = ColorBorder, thickness = 0.5.dp)
                        }

                        if (showHours || showEarnings) {
                            item {
                                SummaryRow(
                                    entries = entries,
                                    showHours = showHours,
                                    showEarnings = showEarnings,
                                    hourlyRate = hourlyRate,
                                    selectedYear = vm.selectedYear.value,
                                    selectedMonth = vm.selectedMonth.value
                                )
                            }
                        }
                    }
                }
            }
        }

        FabButtons(
            onSettingsClick = onSettingsClick,
            onShareClick = { showShareSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )

        if (showShareSheet) {
            ShareBottomSheet(
                vm = vm,
                userName = userName,
                onDismiss = { showShareSheet = false }
            )
        }
    }
}

@Composable
fun ShiftRow(
    day: Int,
    entry: ShiftEntry?,
    isToday: Boolean,
    showHours: Boolean,
    showEarnings: Boolean,
    hourlyRate: Float,
    onSave: (String, String, String) -> Unit
) {
    var stavba by remember(entry) { mutableStateOf(entry?.stavba ?: "") }
    var hodiny by remember(entry) { mutableStateOf(entry?.hodiny ?: "") }
    var poznamky by remember(entry) { mutableStateOf(entry?.poznamky ?: "") }

    val bgColor = if (isToday) ColorToday else Color.Transparent
    val hours = hodiny.replace(",", ".").toDoubleOrNull() ?: 0.0
    val earnings = hours * hourlyRate

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(bgColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = day.toString(),
            modifier = Modifier
                .weight(0.13f)
                .fillMaxHeight()
                .wrapContentHeight(Alignment.CenterVertically)
                .padding(horizontal = 6.dp),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
        EditCell(stavba, if (!showHours && !showEarnings) 0.35f else 0.28f) {
            stavba = it; onSave(stavba, hodiny, poznamky)
        }
        EditCell(hodiny, if (!showHours && !showEarnings) 0.17f else 0.15f) {
            hodiny = it; onSave(stavba, hodiny, poznamky)
        }
        EditCell(poznamky, if (!showHours && !showEarnings) 0.35f else 0.20f) {
            poznamky = it; onSave(stavba, hodiny, poznamky)
        }
        if (showEarnings) {
            Text(
                text = if (earnings > 0) "%.0f".format(earnings) else "",
                modifier = Modifier
                    .weight(0.12f)
                    .fillMaxHeight()
                    .wrapContentHeight(Alignment.CenterVertically)
                    .padding(horizontal = 2.dp),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                color = ColorBrown,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SummaryRow(
    entries: List<ShiftEntry>,
    showHours: Boolean,
    showEarnings: Boolean,
    hourlyRate: Float,
    selectedYear: Int,
    selectedMonth: Int
) {
    val totalHours = entries
        .filter { it.year == selectedYear && it.month == selectedMonth }
        .sumOf { it.hodiny.replace(",", ".").toDoubleOrNull() ?: 0.0 }
    val totalEarnings = totalHours * hourlyRate

    HorizontalDivider(color = ColorBrown, thickness = 1.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorBrown)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Celkem:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (showHours) {
                Text("%.1f h".format(totalHours), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            if (showEarnings) {
                Text("%.0f Kč*".format(totalEarnings), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    if (showEarnings) {
        Text(
            "* bez daní, odvodů a bonusů",
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier.weight(weight).padding(horizontal = 6.dp),
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

@Composable
fun RowScope.EditCell(
    value: String,
    weight: Float,
    onValueChange: (String) -> Unit
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            fontSize = 13.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .border(0.5.dp, ColorBorder)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    )
}