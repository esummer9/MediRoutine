package com.ediapp.MediRoutine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.ediapp.MediRoutine.ACTION_TAKE_MEDICINE") {
            val dbHelper = DatabaseHelper(context)
            val newId = dbHelper.addDoAction()
            Toast.makeText(context, "복용했습니다. (ID: $newId)", Toast.LENGTH_SHORT).show()

            // Update the notification using the helper
            NotificationHelper.showNotification(context)
        }
    }
}
