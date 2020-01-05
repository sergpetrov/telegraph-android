package com.telex.utils

import android.content.Context
import android.text.format.DateUtils
import java.util.Calendar

/**
 * @author Sergey Petrov
 */
object DateUtils {
    const val MONTHS_IN_YEAR = 12

    fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getMonthName(context: Context, month: Int, short: Boolean): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DATE, 1)

        var flags = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_MONTH_DAY or DateUtils.FORMAT_NO_YEAR
        if (short) {
            flags = flags or DateUtils.FORMAT_ABBREV_MONTH
        }
        return DateUtils
                .formatDateTime(context, calendar.timeInMillis, flags)
                .capitalize()
    }

    fun getCurrentYear(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }

    fun getCurrentMonth(): Int {
        return Calendar.getInstance().get(Calendar.MONTH)
    }
}
