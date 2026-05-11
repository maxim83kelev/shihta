package cz.kelev.shihta

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object AlarmScheduler {

    fun schedule(context: Context) {
        val prefs = context.getSharedPreferences("shihta_prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("alarm_enabled", false)
        if (!enabled) return

        val hour = prefs.getInt("alarm_hour", 18)
        val minute = prefs.getInt("alarm_minute", 0)
        val days = prefs.getStringSet("alarm_days", setOf("2","3","4","5","6")) ?: return

        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        cancel(context)

        days.forEach { dayStr ->
            val dayOfWeek = dayStr.toInt()
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("day_of_week", dayOfWeek)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                dayOfWeek,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            manager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancel(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        (1..7).forEach { day ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, day, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            manager.cancel(pendingIntent)
        }
    }

    fun reschedule(context: Context) {
        schedule(context)
    }
}