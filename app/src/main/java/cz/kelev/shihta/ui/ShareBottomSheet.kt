package cz.kelev.shihta.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    vm: ShiftViewModel,
    userName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val entries by vm.entries.collectAsState()
    val selectedYear by vm.selectedYear.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("Сохранить и поделиться", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    ExportManager.exportExcel(
                        context = context,
                        entries = entries,
                        userName = userName.ifBlank { "Shihta" },
                        monthName = vm.currentMonthName,
                        year = selectedYear,
                        daysInMonth = vm.daysInCurrentMonth
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorBrown)
            ) {
                Text("Excel (.xlsx)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    ExportManager.exportPdf(
                        context = context,
                        entries = entries,
                        userName = userName.ifBlank { "Shihta" },
                        monthName = vm.currentMonthName,
                        year = selectedYear,
                        daysInMonth = vm.daysInCurrentMonth
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorHeader)
            ) {
                Text("PDF")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}