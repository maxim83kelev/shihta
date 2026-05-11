package cz.kelev.shihta.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.kelev.shihta.AlarmReceiver
import java.util.Calendar
import cz.kelev.shihta.SendReminderReceiver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendReminderBottomSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("shihta_prefs", Context.MODE_PRIVATE)

    var enabled by remember { mutableStateOf(prefs.getBoolean("send_reminder_enabled", false)) }
    var day by remember { mutableStateOf(prefs.getInt("send_reminder_day", 1)) }
    var hour by remember { mutableStateOf(prefs.getInt("send_reminder_hour", 9)) }
    var minute by remember { mutableStateOf(prefs.getInt("send_reminder_minute", 0)) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp)
        ) {
            Text("Připomínka odeslání", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Upozornění k odeslání výkazu zaměstnavateli", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            // Вкл/выкл
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Aktivní", fontSize = 15.sp)
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = ColorBrown)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // День месяца
            Text("Den v měsíci", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { day = if (day == 1) 28 else day - 1 }) {
                    Text("◀", fontSize = 20.sp, color = ColorBrown)
                }
                Text(
                    text = "$day.",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorBrown,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(onClick = { day = if (day == 28) 1 else day + 1 }) {
                    Text("▶", fontSize = 20.sp, color = ColorBrown)
                }
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    prefs.edit()
                        .putBoolean("send_reminder_enabled", enabled)
                        .putInt("send_reminder_day", day)
                        .putInt("send_reminder_hour", hour)
                        .putInt("send_reminder_minute", minute)
                        .apply()
                    if (enabled) scheduleSendReminder(context, day, hour, minute)
                    else cancelSendReminder(context)
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

fun scheduleSendReminder(context: Context, day: Int, hour: Int, minute: Int) {
    val cal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.MONTH, 1)
        }
    }

    val intent = Intent(context, SendReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context, 2000, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
}

fun cancelSendReminder(context: Context) {
    val intent = Intent(context, SendReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context, 2000, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    manager.cancel(pendingIntent)
}