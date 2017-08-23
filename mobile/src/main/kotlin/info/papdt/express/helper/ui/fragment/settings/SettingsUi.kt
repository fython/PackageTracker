package info.papdt.express.helper.ui.fragment.settings

import android.os.Build
import android.os.Bundle

import info.papdt.express.helper.R
import info.papdt.express.helper.support.Settings
import moe.shizuku.preference.ListPreference
import moe.shizuku.preference.Preference
import moe.shizuku.preference.SwitchPreference

class SettingsUi : AbsPrefFragment(), Preference.OnPreferenceChangeListener {

	private val mPrefNavigationTint: SwitchPreference by PreferenceProperty("navigation_tint")
	private val mPrefNightMode: ListPreference by PreferenceProperty("night_mode")

	override fun onCreatePreferences(bundle: Bundle?, s: String?) {
		addPreferencesFromResource(R.xml.settings_ui)

		/** Default value  */
		mPrefNavigationTint.isEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
		mPrefNavigationTint.isChecked = settings.getBoolean(Settings.KEY_NAVIGATION_TINT, true)

		val target = settings.getInt(Settings.KEY_NIGHT_MODE, 0)
		if (mPrefNightMode.value == null) {
			mPrefNightMode.setValueIndex(target)
		}

		/** Set callback  */
		mPrefNavigationTint.onPreferenceChangeListener = this
		mPrefNightMode.onPreferenceChangeListener = this
	}

	override fun onPreferenceChange(pref: Preference, o: Any): Boolean {
		return when (pref) {
			mPrefNavigationTint -> {
				val b = o as Boolean
				settings.putBoolean(Settings.KEY_NAVIGATION_TINT, b)
				makeRestartTips()
				true
			}
			mPrefNightMode -> {
				val value = Integer.parseInt(o as String)
				settings.putInt(Settings.KEY_NIGHT_MODE, value)
				makeRestartTips()
				true
			}
			else -> false
		}
	}

}
