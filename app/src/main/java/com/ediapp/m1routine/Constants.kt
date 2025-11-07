package com.ediapp.m1routine

import android.Manifest
import android.icu.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

import android.provider.Settings
import com.ediapp.m1routine.model.SharedPreferenceData

val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")
val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

val SHARED_PREFERENCES_NAME = "my_prefs"


enum class MonthOrWeek {
    MONTH,
    WEEK
}

fun getFourteenDaysAgoDate(amount : Int): Date {
    // Calendar 인스턴스를 생성하고 현재 날짜와 시간으로 초기화합니다.
    val calendar = Calendar.getInstance()
    calendar.time = Date()

    // 현재 날짜에서 14일을 뺍니다.
    calendar.add(Calendar.DAY_OF_YEAR, amount)

    // 계산된 날짜를 Date 객체로 반환합니다.
    return calendar.time
}

fun hasUsim(context: Context): Boolean {
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    // 권한이 있는지 확인
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
        != PackageManager.PERMISSION_GRANTED) {
        // 권한이 없으면 SIM 상태를 확정할 수 없음
        return false
    }

    return telephonyManager.simState == TelephonyManager.SIM_STATE_READY
}


fun getAndroidId(context: Context): String {
    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
}


fun getSharedPref(prefs: SharedPreferences) : SharedPreferenceData {

    val data = SharedPreferenceData(
        medName = prefs.getString("med_name", "")?:"",
        medNickName = prefs.getString("med_nick_name", "")?:"" ,
        notificationEnabled = prefs.getBoolean("daily_report_enabled", false),
        notificationTime = prefs.getString("notification_time", "08:00")?:""
    )
    return data
}
