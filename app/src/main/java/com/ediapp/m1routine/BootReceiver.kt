
package com.ediapp.m1routine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("MediRoutine_prefs", Context.MODE_PRIVATE)
            val morningEnabled = prefs.getBoolean("daily_report_enabled", false)
            val selectedTime = prefs.getString("notification_time", "08:00")

            if (morningEnabled && selectedTime != null) {
                scheduleNotification(context, selectedTime)
            }
        }
    }
}
