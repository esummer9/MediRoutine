package com.ediapp.MediRoutine

import android.app.AlertDialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.WindowManager
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

        notificationManager.notify(1, notification)
    }

    fun showMedicationDialog(context: Context) {
        val dbHelper = DatabaseHelper(context)
        val builder = AlertDialog.Builder(context)
        builder.setTitle("약 복용 확인")
        builder.setMessage("오늘 약을 복용하셨나요?")
        builder.setPositiveButton("예") { _, _ ->
            dbHelper.addDrugAction()
        }
        builder.setNegativeButton("아니오") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        dialog.show()
    }
}
