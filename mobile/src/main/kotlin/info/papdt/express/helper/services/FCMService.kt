package info.papdt.express.helper.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.model.CommonPackage
import info.papdt.express.helper.model.CommonStatus
import info.papdt.express.helper.model.Package
import info.papdt.express.helper.support.PushUtils
import info.papdt.express.helper.support.Settings
import moe.feng.kotlinyan.common.*
import java.util.*
import kotlin.concurrent.thread

class FCMService : FirebaseMessagingService() {

	private val TAG = FCMService::class.java.simpleName

	override fun onMessageReceived(remoteMessage: RemoteMessage?) {
		val isEnabledDontDisturbMode = Settings.getInstance(applicationContext)
				.getBoolean(Settings.KEY_NOTIFICATION_DO_NOT_DISTURB, true)
		remoteMessage?.data?.let { CommonPackage.fromMap(it) }?.let { data ->
			val db = PackageDatabase.getInstance(this)

			val index = db.indexOf(data.id)
			if (index != -1) {
				Log.d(TAG, "当前主动推送包裹位置为 $index")
				if (data.getData().size > db[index].data.size) {
					Log.d(TAG, "包裹 $index 需要产生通知")
					val oldPackage = db[index]
					oldPackage.data = data.getData().map(CommonStatus::toOldPackageStatus).toMutableList() as ArrayList<Package.Status>
					thread { db.save() }
					if (isEnabledDontDisturbMode && PushUtils.isDisturbTime(Calendar.getInstance())) {
						Log.i(TAG, "现在是勿扰时间段，跳过通知。")
					} else {
						val n = ReminderService.produceNotifications(this, index, oldPackage)
						notificationManager.notify(index + 20000, n)
					}
				}
			}
		}
	}

}
