
package com.ediapp.m1routine

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NotificationHelper {

    private const val MEDICATION_CHANNEL_ID = "medi_routine_channel"

    fun showDailyCheckNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, MEDICATION_CHANNEL_ID)
            .setContentTitle("약 복용 시간입니다")
            .setContentText("오늘 약을 아직 복용하지 않으셨습니다.\n잊지말고 복용하세요.")
            .setSmallIcon(R.drawable.med_routine)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.med_routine))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(2, notification)
    }

    fun showAlwaysNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val today = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd(E)", Locale.KOREAN)
        val dateString = dateFormat.format(today)

        val dbHelper = DatabaseHelper(context)
        val drugActionCount = dbHelper.getDrugTodayCount()

        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "com.ediapp.m1routine.ACTION_TAKE_MEDICINE"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val deleteIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "com.ediapp.m1routine.ACTION_NOTIFICATION_DISMISSED"
        }
        val deletePendingIntent = PendingIntent.getBroadcast(context, 1, deleteIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
             flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val mainActivityPendingIntent = PendingIntent.getActivity(context, 2, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        var notification = NotificationCompat.Builder(context, "medi_routine_channel")
            .setContentTitle("$dateString 약 복용")
            .setSmallIcon(R.drawable.med_routine)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.med_routine)) // Add this line
            .setContentText("약복용 : $drugActionCount")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .addAction(R.drawable.med_routine, "복용", pendingIntent)
            .setDeleteIntent(deletePendingIntent)
            .setContentIntent(mainActivityPendingIntent) // This sets the intent for tapping the notification itself
            .build()

        notificationManager.notify(1, notification)
    }
}
