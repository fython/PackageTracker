package info.papdt.express.helper.support

import android.content.Context
import android.text.format.DateUtils
import info.papdt.express.helper.R
import java.util.*
import java.util.concurrent.TimeUnit

object DateHelper {

    fun getRelativeTimeTextAboutDays(time: Long): CharSequence {
        return DateUtils.getRelativeTimeSpanString(
                time, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS)
    }

    fun isUnknownTimeForGroup(time: Date): Boolean {
        val now = Calendar.getInstance().time
        return now.year + 1 == time.year && now.month == time.month && now.date == time.date
    }

    fun getDifferenceDaysForGroup(time: Date): Long {
        val now = Calendar.getInstance()
        val nowDate = now.time

        val diffDays = getDifferenceDays(time, nowDate)

        return when {
            diffDays < 3 -> diffDays
            diffDays <= 7 -> 7
            diffDays <= 30 -> 30
            diffDays <= 30 * 6 -> (diffDays / 30) * 30
            diffDays <= 365 -> 365
            else -> (diffDays.toFloat() / 365f).toLong() * 365 + 1
        }
    }

    fun getDifferenceDaysTextForGroup(context: Context, diffDays: Long): String {
        return context.run {
            when {
                diffDays < 0 -> getString(R.string.diff_time_pending)
                diffDays == 0L -> getString(R.string.diff_time_today)
                diffDays == 1L -> getString(R.string.diff_time_yesterday)
                diffDays == 2L -> getString(R.string.diff_time_two_days_ago)
                diffDays == 3L -> getString(R.string.diff_time_three_days_ago)
                diffDays <= 7L -> getString(R.string.diff_time_in_this_week)
                diffDays <= 30L -> getString(R.string.diff_time_in_this_month)
                diffDays <= 30 * 6L -> getString(R.string.diff_time_some_months_ago, diffDays / 30)
                diffDays <= 365 -> getString(R.string.diff_time_in_this_year)
                else -> getString(R.string.diff_time_some_years_ago, diffDays / 365)
            }
        }
    }

    fun getDifferenceDays(d1: Date, d2: Date): Long {
        return TimeUnit.DAYS.convert(d2.time - d1.time, TimeUnit.MILLISECONDS)
    }

    fun dateToCalendar(date: Date): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }

    fun Calendar.getDaysOfMonth(): Int = get(Calendar.DAY_OF_MONTH)
    fun Calendar.getMonth(): Int = get(Calendar.MONTH)
    fun Calendar.getYear(): Int = get(Calendar.YEAR)
    fun Calendar.getDaysOfYear(): Int = get(Calendar.DAY_OF_YEAR)

}