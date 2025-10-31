package com.ediapp.MediRoutine

import android.icu.util.Calendar
import androidx.compose.foundation.layout.add
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")
val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())


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
