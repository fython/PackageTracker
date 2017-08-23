package info.papdt.express.helper.ui.fragment.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import info.papdt.express.helper.R
import info.papdt.express.helper.services.ClipboardDetectService
import info.papdt.express.helper.support.Settings
import moe.shizuku.preference.Preference
import moe.shizuku.preference.SwitchPreference

class SettingsAutoDetect : AbsPrefFragment(), Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

	private val mPrefFromClipboard: SwitchPreference by PreferenceProperty("from_clipboard")
	private val mPrefFromScreen: Preference by PreferenceProperty("from_screen")

	override fun onCreatePreferences(bundle: Bundle?, s: String?) {
		addPreferencesFromResource(R.xml.settings_auto_detect)

		/** Default value  */
		mPrefFromClipboard.isChecked = settings!!.getBoolean(Settings.KEY_DETECT_FROM_CLIPBOARD, false)

		/** Set callback  */
		mPrefFromClipboard.onPreferenceChangeListener = this
		mPrefFromScreen.onPreferenceClickListener = this
	}

	override fun onPreferenceChange(pref: Preference, o: Any): Boolean {
		if (pref === mPrefFromClipboard) {
			val isOpen = o as Boolean
			if (isOpen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (!android.provider.Settings.canDrawOverlays(activity)) {
					val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
							Uri.parse("package:" + activity.packageName))
					startActivity(intent)
					return false
				}
			}
			settings.putBoolean(Settings.KEY_DETECT_FROM_CLIPBOARD, isOpen)
			val intent = Intent(activity.applicationContext, ClipboardDetectService::class.java)
			intent.run(if (!isOpen) activity::stopService else activity::startService)
			return true
		}
		return false
	}

	override fun onPreferenceClick(pref: Preference): Boolean {
		if (pref === mPrefFromScreen) {
			startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
			return true
		}
		return false
	}
}
