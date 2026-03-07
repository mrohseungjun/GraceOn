package com.graceon

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class DailyVerseNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val launchIntent = Intent(context, com.example.graceon.MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, DailyVerseNotificationScheduler.channelId())
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("오늘의 말씀")
            .setContentText("GraceOn과 함께 오늘 내게 주시는 말씀을 만나보세요.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("GraceOn과 함께 오늘 내게 주시는 말씀을 만나보세요.")
            )
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(4107, notification)
    }
}
