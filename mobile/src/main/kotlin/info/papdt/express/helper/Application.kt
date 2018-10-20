package info.papdt.express.helper

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.multidex.MultiDexApplication
import android.support.v7.app.AppCompatDelegate

import com.tencent.bugly.crashreport.CrashReport
import info.papdt.express.helper.model.MaterialIcon

import info.papdt.express.helper.services.ClipboardDetectService
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.support.SettingsInstance
import io.alterac.blurkit.BlurKit
import moe.feng.kotlinyan.common.getSharedPreferencesProvider 
import moe.feng.kotlinyan.common.ifSupportSDK
import moe.feng.kotlinyan.common.notificationManager
import moe.feng.kotlinyan.common.string

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
		BlurKit.init(this)

		// Init CrashReport
		val strategy = CrashReport.UserStrategy(applicationContext)
		strategy.appPackageName = packageName

		var versionName: String? = null
		var versionCode = 0
		try {
			val packageInfo = packageManager.getPackageInfo(packageName, 0)
			versionName = packageInfo.versionName
			versionCode = packageInfo.versionCode
		} catch (e: PackageManager.NameNotFoundException) {
			e.printStackTrace()
		}

		strategy.appVersion = "$versionName($versionCode)"
		CrashReport.initCrashReport(applicationContext, BUGLY_APP_ID, BUGLY_ENABLE_DEBUG, strategy)

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
	}

}
