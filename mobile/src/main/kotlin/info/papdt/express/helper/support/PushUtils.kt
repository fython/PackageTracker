package info.papdt.express.helper.support

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log

import java.util.Calendar

import info.papdt.express.helper.services.ReminderService

object PushUtils {

    fun startServiceAlarm(context: Context, service: Class<*>, interval: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, service)
        val p = PendingIntent.getService(context, 10000, i, PendingIntent.FLAG_CANCEL_CURRENT)
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, interval, p)
    }

    fun stopServiceAlarm(context: Context, service: Class<*>) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, service)
        val p = PendingIntent.getService(context, 10000, i, PendingIntent.FLAG_CANCEL_CURRENT)
        am.cancel(p)
    }

    fun startServices(context: Context) {
        val settings = Settings.getInstance(context)
        val interval = getIntervalTime(settings.getInt(Settings.KEY_NOTIFICATION_INTERVAL, 1))

        if (interval > -1) {
            Log.i("Utils", "Interval : " + interval)
            startServiceAlarm(context, ReminderService::class.java, interval.toLong())
        }
    }

    fun stopServices(context: Context) {
        stopServiceAlarm(context, ReminderService::class.java)
    }

    fun restartServices(context: Context) {
        stopServices(context)

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected) {
            startServices(context)
        }
    }

    fun getIntervalTime(id: Int): Int = when (id) {
        0 -> 10 * 60 * 1000
        1 -> 30 * 60 * 1000
        2 -> 60 * 60 * 1000
        3 -> 90 * 60 * 1000
        4 -> -1
        else -> -1
    }

    fun isDisturbTime(c: Calendar): Boolean {
        val hours = c.get(Calendar.HOUR_OF_DAY)
        return (hours >= 23) or (hours < 6)
    }

}
