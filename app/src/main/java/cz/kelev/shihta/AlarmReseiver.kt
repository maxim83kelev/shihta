package cz.kelev.shihta

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            AlarmScheduler.reschedule(context)
            return
        }

        // Перезапускаем будильник на следующую неделю
        val dayOfWeek = intent.getIntExtra("day_of_week", -1)
        if (dayOfWeek != -1) {
            rescheduleForNextWeek(context, dayOfWeek)
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "shihta_reminder",
                "Připomínka výkazu",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        // Intent для открытия приложения при тапе
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "shihta_reminder")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Šichta")
            .setContentText("Nezapomeňte vyplnit výkaz!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
    }

    private fun rescheduleForNextWeek(context: Context, dayOfWeek: Int) {
        val prefs = context.getSharedPreferences("shihta_prefs", Context.MODE_PRIVATE)
        val hour = prefs.getInt("alarm_hour", 18)
        val minute = prefs.getInt("alarm_minute", 0)

        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            set(java.util.Calendar.DAY_OF_WEEK, dayOfWeek)
            add(java.util.Calendar.WEEK_OF_YEAR, 1)
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("day_of_week", dayOfWeek)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, dayOfWeek, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val manager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        manager.setExactAndAllowWhileIdle(
            android.app.AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            pendingIntent
        )
    }
}