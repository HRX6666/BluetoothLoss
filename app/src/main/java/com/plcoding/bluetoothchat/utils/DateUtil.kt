package com.plcoding.bluetoothchat.utils


import java.text.SimpleDateFormat
import java.util.*


fun Date.getFormatString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    val sdf = SimpleDateFormat(pattern, Locale.CHINA)
    return sdf.format(this)
}


fun Date.getYearr(): Int {
    val calendar = GregorianCalendar()
    calendar.time = this
    return calendar.get(Calendar.YEAR)
}

fun Date.getMonthh(): Int {
    val calendar = GregorianCalendar()
    calendar.time = this
    //  月份从0开始，需要手动 +1
    return calendar.get(Calendar.MONTH) + 1
}

fun Date.getDayOfMonth(): Int {
    val calendar = GregorianCalendar()
    calendar.time = this
    return calendar.get(Calendar.DAY_OF_MONTH)
}

fun Date.getHour(): Int {
    val calendar = GregorianCalendar()
    calendar.time = this
    return calendar.get(Calendar.HOUR_OF_DAY)
}

fun Date.getMinute(): Int {
    val calendar = GregorianCalendar()
    calendar.time = this
    return calendar.get(Calendar.MINUTE)
}

fun Date.getSecond(): Int {
    val calendar = GregorianCalendar()
    calendar.time = this
    return calendar.get(Calendar.SECOND)
}

fun Date.plusDays(days: Int = 1): Date {
    val calendar = GregorianCalendar()
    calendar.time = this
    calendar.add(Calendar.DATE, days)
    return calendar.time
}

fun Date.minusDays(days: Int = 1): Date {
    val calendar = GregorianCalendar()
    calendar.time = this
    calendar.add(Calendar.DATE, -days)
    return calendar.time
}

class DateUtil {
    companion object {

        fun getDayCountOfMonth(year: Int, month: Int): Int {

            if (month == 2) {
                //判断年是不是闰年
                if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
                    return 29
                } else {
                    return 28
                }
            } else if (month == 4 || month == 6 || month == 9 || month == 11) {
                return 30
            } else
                return 31
        }

    }
}
