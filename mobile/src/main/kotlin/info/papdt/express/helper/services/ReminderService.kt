package info.papdt.express.helper.services

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.util.Log
import info.papdt.express.helper.CHANNEL_ID_PACKAGE_STATUS

import java.text.ParseException
import java.text.SimpleDateFormat

import info.papdt.express.helper.R
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.model.Kuaidi100Package
import info.papdt.express.helper.support.PushUtils
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.support.SettingsInstance
import info.papdt.express.helper.ui.DetailsActivity
import info.papdt.express.helper.ui.launcher.AppWidgetProvider
import moe.feng.kotlinyan.common.*
import java.util.*

class ReminderService : IntentService(TAG) {

	override fun onBind(intent: Intent): IBinder? {
		return null
	}

	public override fun onHandleIntent(intent: Intent) {
		val isEnabledDontDisturbMode = Settings.getInstance(applicationContext)
				.getBoolean(Settings.KEY_NOTIFICATION_DO_NOT_DISTURB, true)
		if (isEnabledDontDisturbMode && PushUtils.isDisturbTime(Calendar.getInstance())) {
			Log.i(TAG, "现在是勿扰时间段，跳过检查。")
			return
		}
		Log.i(TAG, "开始检查快递包裹")

		val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		val db = PackageDatabase.getInstance(applicationContext)

		db.pullDataFromNetwork(SettingsInstance.forceUpdateAllPackages)
		db.save()

		AppWidgetProvider.updateManually(application)

		for (i in 0 until db.size()) {
			val p = db[i]
			if (p.getState() != Kuaidi100Package.STATUS_FAILED && p.shouldPush) {
				Log.i(TAG, "包裹 $i 需要产生通知")
				val n = produceNotifications(this, i, p)
				nm.notify(i + 20000, n)
				p.shouldPush = false
			}
		}

		db.save()
	}

	companion object {

		private val TAG = ReminderService::class.java.simpleName

		private val ID = 100000

		private fun parseDefaults(context: Context): Int {
			val settings = Settings.getInstance(context)

			return (if (settings.getBoolean(Settings.KEY_NOTIFICATION_SOUND, true)) Notification.DEFAULT_SOUND else 0) or
					(if (settings.getBoolean(Settings.KEY_NOTIFICATION_VIBRATE, true)) Notification.DEFAULT_VIBRATE else 0) or
					Notification.DEFAULT_LIGHTS
		}

		private fun buildNotification(context: Context, title: String, subject: String, longText: String, time: Long, icon: Int, color: Int,
		                              defaults: Int, contentIntent: PendingIntent, deleteIntent: PendingIntent?): Notification {
			val n: Notification
			val builder = NotificationCompat.Builder(context, CHANNEL_ID_PACKAGE_STATUS)
			builder.setContentTitle(title)
			builder.setContentText(subject)
			builder.priority = NotificationCompat.PRIORITY_MAX
			builder.setStyle(NotificationCompat.BigTextStyle(builder).bigText(longText))
			builder.setDefaults(defaults)
			builder.setSmallIcon(icon)
			builder.setContentIntent(contentIntent)
			if (time > 0) builder.setWhen(time)
			builder.setAutoCancel(true)

			if (Build.VERSION.SDK_INT >= 21) {
				builder.color = color
			}
			n = builder.build()

			return n
		}

		fun produceNotifications(context: Context, position: Int, exp: Kuaidi100Package?): Notification? {
			if (exp != null) {
				val defaults = parseDefaults(context.applicationContext)

				val pi = PendingIntent.getActivity(
						context.applicationContext,
						position,
						Intent(context.applicationContext, DetailsActivity::class.java).apply {
							flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
							this["extra_package_json"] = exp.toJsonString()
							this["extra_state"] = exp.getState()
						},
						PendingIntent.FLAG_UPDATE_CURRENT)

				val title = exp.name
				val subject: String = when (exp.getState()) {
					Kuaidi100Package.STATUS_DELIVERED -> R.string.notification_delivered
					Kuaidi100Package.STATUS_ON_THE_WAY -> R.string.notification_on_the_way
					else -> R.string.notification_new_message
				}.run(context::getString)

				val smallIcon = when (exp.getState()) {
					Kuaidi100Package.STATUS_DELIVERED -> R.drawable.ic_done_white_24dp
					Kuaidi100Package.STATUS_ON_THE_WAY -> R.drawable.ic_local_shipping_white_24dp
					else -> R.drawable.ic_assignment_returned_white_24dp
				}

				val myDate = exp.data!![0].time
				val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
				var millis: Long = 0
				try {
					val date = sdf.parse(myDate)
					millis = date.time
				} catch (e: ParseException) {
					e.printStackTrace()
				}

				val n = buildNotification(context.applicationContext,
						title!!,
						subject,
						exp.data!![0].context!!,
						millis,
						smallIcon,
						context.resources.getIntArray(R.array.statusColor)[exp.getState()],
						defaults,
						pi, null)

				n.tickerText = title

				return n
			}
			return null
		}
	}

}
