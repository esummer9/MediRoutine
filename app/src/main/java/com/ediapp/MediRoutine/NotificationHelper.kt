package com.ediapp.MediRoutine

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
        val drugActionCount = dbHelper.getDrugActionCount()

        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "com.ediapp.MediRoutine.ACTION_TAKE_MEDICINE"
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, "medi_routine_channel")
            .setSmallIcon(R.drawable.med_routine)
            .setContentTitle("$dateString 약 복용")
            .setContentText("약 복용 수: $drugActionCount")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .addAction(R.drawable.med_routine, "복용", pendingIntent)
            .build()

        notificationManager.notify(1, notification)
    }
}
