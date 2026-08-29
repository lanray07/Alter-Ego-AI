package com.alteregoai.app.core

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.alteregoai.app.MainActivity
import java.util.Calendar

class NotificationScheduler(private val context: Context) {
    fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Daily mission reminders", NotificationManager.IMPORTANCE_DEFAULT).apply { description = "A gentle reminder to complete today's first mission." }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun scheduleDailyReminder(hour: Int = 8, minute: Int = 30) {
        createChannel()
        val intent = Intent(context, NotificationReceiver::class.java)
        val pending = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0); if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1) }
        context.getSystemService(AlarmManager::class.java).setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pending)
    }

    fun cancelDailyReminder() {
        val pending = PendingIntent.getBroadcast(context, REQUEST_CODE, Intent(context, NotificationReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        context.getSystemService(AlarmManager::class.java).cancel(pending)
    }

    companion object { const val CHANNEL_ID = "daily-missions"; const val REQUEST_CODE = 830 }
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (android.os.Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val openApp = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Your future self is watching")
            .setContentText("Open Alter Ego AI and complete the first mission of the day.")
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NotificationScheduler.REQUEST_CODE, notification)
    }
}
