package com.ediapp.MediRoutine

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

    fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val today = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd(E)", Locale.KOREAN)
        val dateString = dateFormat.format(today)

        val dbHelper = DatabaseHelper(context)
        val drugActionCount = dbHelper.getDrugTodayCount()

        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.ediapp.MediRoutine.ACTION_TAKE_MEDICINE"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val deleteIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "com.ediapp.MediRoutine.ACTION_NOTIFICATION_DISMISSED"
        }
        val deletePendingIntent = PendingIntent.getBroadcast(context, 1, deleteIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            // Optional: Add flags if you want specific behavior when MainActivity is launched
            // For example, to clear the back stack and start MainActivity as a new task:
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

//        if (drugActionCount < 1) {
//            notificationManager.notify(1, notification)
//        } else {
//            notificationManager.notify(1, notification)
//        }


        notificationManager.notify(1, notification)
    }
}
