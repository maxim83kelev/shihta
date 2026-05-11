package cz.kelev.shihta

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class SendReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "shihta_send_reminder",
                "Připomínka odeslání",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, "shihta_send_reminder")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Šichta")
            .setContentText("Nezapomeňte odeslat výkaz zaměstnavateli!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(1002, notification)

        // Перезапускаем на следующий месяц
        val prefs = context.getSharedPreferences("shihta_prefs", Context.MODE_PRIVATE)
        val day = prefs.getInt("send_reminder_day", 1)
        val hour = prefs.getInt("send_reminder_hour", 9)
        val minute = prefs.getInt("send_reminder_minute", 0)
        val enabled = prefs.getBoolean("send_reminder_enabled", false)
        if (enabled) {
            cz.kelev.shihta.ui.scheduleSendReminder(context, day, hour, minute)
        }
    }
}