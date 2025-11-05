
package com.ediapp.m1routine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val dbHelper = DatabaseHelper(context)
        val today = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(today)
        val actKey = "drug-$dateString"

        if (!dbHelper.isDrugExists(actKey)) {
            NotificationHelper.showMedicationNotification(context)
        }
    }
}
