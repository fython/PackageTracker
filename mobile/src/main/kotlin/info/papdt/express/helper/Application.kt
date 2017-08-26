package info.papdt.express.helper

import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatDelegate
import com.google.firebase.iid.FirebaseInstanceId

import com.tencent.bugly.crashreport.CrashReport
import info.papdt.express.helper.api.PushApi

import info.papdt.express.helper.services.ClipboardDetectService
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.support.SettingsInstance
import moe.feng.kotlinyan.common.getSharedPreferencesProvider

class Application : android.app.Application() {

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

		if (mSettings.getBoolean(Settings.KEY_DETECT_FROM_CLIPBOARD, false)) {
			startService(Intent(applicationContext, ClipboardDetectService::class.java))
		}

		/** Init CrashReport  */
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

		SettingsInstance = getSharedPreferencesProvider()
	}

}
