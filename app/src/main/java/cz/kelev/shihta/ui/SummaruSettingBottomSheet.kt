package cz.kelev.shihta.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummarySettingsBottomSheet(
    vm: ShiftViewModel,
    onDismiss: () -> Unit
) {
    val showHours by vm.showHours.collectAsState()
    val showEarnings by vm.showEarnings.collectAsState()
    val hourlyRate by vm.hourlyRate.collectAsState()
    var rateText by remember { mutableStateOf(if (hourlyRate > 0f) hourlyRate.toInt().toString() else "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp)
        ) {
            Text("Souhrn a výdělek", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Итого часов
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Zobrazit součet hodin", fontSize = 15.sp)
                    Text("Celkový počet hodin za měsíc", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = showHours,
                    onCheckedChange = { vm.setShowHours(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = ColorBrown)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Заработок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Zobrazit výdělek", fontSize = 15.sp)
                    Text("Hodiny × sazba (bez daní a bonusů)", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = showEarnings,
                    onCheckedChange = { vm.setShowEarnings(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = ColorBrown)
                )
            }

            // Ставка — только если включён заработок
            if (showEarnings) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Hodinová sazba (Kč)", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rateText,
                    onValueChange = {
                        rateText = it
                        it.toFloatOrNull()?.let { rate -> vm.setHourlyRate(rate) }
                    },
                    label = { Text("Kč / hodina") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}