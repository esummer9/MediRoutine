package com.ediapp.MediRoutine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.ediapp.MediRoutine.ACTION_TAKE_MEDICINE" -> {
                val dbHelper = DatabaseHelper(context)
                val newId = dbHelper.addDrugAction()

                val prefs = context.getSharedPreferences("MediRoutine_prefs", Context.MODE_PRIVATE)

                val medNickName = prefs.getString("med_nick_name", "") ?: ""
                Toast
                    .makeText(context, "$medNickName 을 복용했습니다. (ID: $newId)", Toast.LENGTH_SHORT)
                    .show()


//                Toast.makeText(context, "복용했습니다. (ID: $newId)", Toast.LENGTH_SHORT).show()

                // Update the notification using the helper
                NotificationHelper.showNotification(context)

                val intent = Intent(context, MainActivity::class.java).apply {
                    // 이미 실행 중인 Activity를 새로 생성하지 않고 맨 위로 가져옵니다.
                    // ** FLAG_ACTIVITY_CLEAR_TOP과 함께 사용하면 **'리로드'와 비슷한 효과**를 냅니다.**
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(intent)

            }
            "com.ediapp.MediRoutine.ACTION_NOTIFICATION_DISMISSED" -> {
                val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(mainActivityIntent)
            }
        }
    }
}
