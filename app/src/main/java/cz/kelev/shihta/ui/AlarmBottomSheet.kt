package cz.kelev.shihta.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.kelev.shihta.AlarmScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmBottomSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("shihta_prefs", Context.MODE_PRIVATE)

    var enabled by remember { mutableStateOf(prefs.getBoolean("alarm_enabled", false)) }
    var hour by remember { mutableStateOf(prefs.getInt("alarm_hour", 18)) }
    var minute by remember { mutableStateOf(prefs.getInt("alarm_minute", 0)) }
    val savedDays = prefs.getStringSet("alarm_days", setOf("2","3","4","5","6")) ?: setOf("2","3","4","5","6")
    val selectedDays = remember { mutableStateListOf<String>().apply { addAll(savedDays) } }

    val dayLabels = listOf(
        "2" to "Po",
        "3" to "Út",
        "4" to "St",
        "5" to "Čt",
        "6" to "Pá",
        "7" to "So",
        "1" to "Ne"
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp)
        ) {
            Text("Připomínka", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Вкл/выкл
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Připomínka aktivní", fontSize = 15.sp)
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = ColorBrown)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Время
            Text("Čas", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Часы
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { hour = (hour + 1) % 24 }) {
                        Text("▲", fontSize = 18.sp, color = ColorBrown)
                    }
                    Text(
                        text = hour.toString().padStart(2, '0'),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorBrown
                    )
                    IconButton(onClick = { hour = (hour - 1 + 24) % 24 }) {
                        Text("▼", fontSize = 18.sp, color = ColorBrown)
                    }
                }
                Text(":", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                // Минуты
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { minute = (minute + 1) % 60 }) {
                        Text("▲", fontSize = 18.sp, color = ColorBrown)
                    }
                    Text(
                        text = minute.toString().padStart(2, '0'),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorBrown
                    )
                    IconButton(onClick = { minute = (minute - 1 + 60) % 60 }) {
                        Text("▼", fontSize = 18.sp, color = ColorBrown)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Дни недели
            Text("Dny", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayLabels.forEach { (key, label) ->
                    val selected = selectedDays.contains(key)
                    Button(
                        onClick = {
                            if (selected) selectedDays.remove(key)
                            else selectedDays.add(key)
                        },
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) ColorBrown else Color.LightGray
                        )
                    ) {
                        Text(label, fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Сохранить
            Button(
                onClick = {
                    prefs.edit()
                        .putBoolean("alarm_enabled", enabled)
                        .putInt("alarm_hour", hour)
                        .putInt("alarm_minute", minute)
                        .putStringSet("alarm_days", selectedDays.toSet())
                        .apply()
                    if (enabled) AlarmScheduler.schedule(context)
                    else AlarmScheduler.cancel(context)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorBrown)
            ) {
                Text("Uložit")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}