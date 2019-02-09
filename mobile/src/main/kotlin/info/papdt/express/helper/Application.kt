package info.papdt.express.helper

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.multidex.MultiDexApplication
import androidx.appcompat.app.AppCompatDelegate
import com.crashlytics.android.Crashlytics
import info.papdt.express.helper.dao.SRDatabase

import info.papdt.express.helper.model.MaterialIcon

import info.papdt.express.helper.services.ClipboardDetectService
import info.papdt.express.helper.support.MaterialColorGenerator
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.support.SettingsInstance
import io.alterac.blurkit.BlurKit
import io.fabric.sdk.android.Fabric
import moe.feng.kotlinyan.common.*

class Application : MultiDexApplication() {

	@SuppressLint("NewApi")
	override fun onCreate() {
		val mSettings = Settings.getInstance(applicationContext)
		val defaultNightMode: Int = when (mSettings.getInt(Settings.KEY_NIGHT_MODE, 0)) {
			1 -> AppCompatDelegate.MODE_NIGHT_AUTO
			2 -> AppCompatDelegate.MODE_NIGHT_YES
			3 -> AppCompatDelegate.MODE_NIGHT_NO
			else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
		}
		AppCompatDelegate.setDefaultNightMode(defaultNightMode)

		super.onCreate()

		if (mSettings.isClipboardDetectServiceEnabled()) {
			startService(Intent(applicationContext, ClipboardDetectService::class.java))
		}

		// Init Widget components
		MaterialIcon.init(this)
		MaterialColorGenerator.init(this)
		BlurKit.init(this)

		// Init CrashReport
		Fabric.with(this, Crashlytics())

		// Init notification channel
		ifSupportSDK (Build.VERSION_CODES.O) {
			notificationManager.createNotificationChannel(
					NotificationChannel(
							CHANNEL_ID_PACKAGE_STATUS,
							resources.string[R.string.notification_channel_name_status],
							NotificationManager.IMPORTANCE_HIGH
					).apply { description = resources.string[R.string.notification_channel_summary_status] }
			)
		}

		// Init settings
		SettingsInstance = getSharedPreferencesProvider()
        SRDatabase.init(this)
	}

}
