package cz.kelev.shihta.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveBottomSheet(
    vm: ShiftViewModel,
    userName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val entries by vm.entries.collectAsState()
    val selectedYear by vm.selectedYear.collectAsState()
    val selectedMonth by vm.selectedMonth.collectAsState()
    val months = listOf(
        "Leden", "Únor", "Březen", "Duben", "Květen", "Červen",
        "Červenec", "Srpen", "Září", "Říjen", "Listopad", "Prosinec"
    )

    var selectedFormat by remember { mutableStateOf("") }

    val folderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            ExportManager.saveToFolder(
                context = context,
                treeUri = it,
                entries = entries,
                userName = userName,
                monthName = months[selectedMonth - 1],
                year = selectedYear,
                daysInMonth = vm.daysInCurrentMonth,
                format = selectedFormat
            )
            onDismiss()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("Uložit na zařízení", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    selectedFormat = "pdf"
                    folderLauncher.launch(null)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorBrown)
            ) {
                Text("Uložit jako PDF")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    selectedFormat = "xlsx"
                    folderLauncher.launch(null)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorHeader)
            ) {
                Text("Uložit jako Excel")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}