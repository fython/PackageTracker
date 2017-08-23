package info.papdt.express.helper.ui.fragment.settings

import android.os.Bundle

import com.google.firebase.iid.FirebaseInstanceId
import info.papdt.express.helper.R
import info.papdt.express.helper.support.PushUtils
import info.papdt.express.helper.support.Settings
import moe.shizuku.preference.ListPreference
import moe.shizuku.preference.Preference
import moe.shizuku.preference.SwitchPreference

class SettingsNetwork : AbsPrefFragment(), Preference.OnPreferenceChangeListener {

	private val mPrefDontDisturb: SwitchPreference by PreferenceProperty("dont_disturb")
	private val mPrefIntervalTime: ListPreference by PreferenceProperty("interval")
	private val mPrefInstanceId: Preference by PreferenceProperty("firebase_instance_id")

	override fun onCreatePreferences(bundle: Bundle?, s: String?) {
		addPreferencesFromResource(R.xml.settings_network)

		/** Default value  */
		mPrefDontDisturb.isChecked = settings.getBoolean(Settings.KEY_NOTIFICATION_DO_NOT_DISTURB, true)

		val target = settings.getInt(Settings.KEY_NOTIFICATION_INTERVAL, 1)
		if (mPrefIntervalTime.value == null) {
			mPrefIntervalTime.setValueIndex(target)
		}

		mPrefInstanceId.summary = FirebaseInstanceId.getInstance().token

		/** Set callback  */
		mPrefDontDisturb.onPreferenceChangeListener = this
		mPrefIntervalTime.onPreferenceChangeListener = this
	}

	override fun onPreferenceChange(pref: Preference, o: Any): Boolean {
		if (pref === mPrefDontDisturb) {
			val b = o as Boolean
			settings.putBoolean(Settings.KEY_NOTIFICATION_DO_NOT_DISTURB, b)
			return true
		}
		if (pref === mPrefIntervalTime) {
			val value = Integer.parseInt(o as String)
			settings.putInt(Settings.KEY_NOTIFICATION_INTERVAL, value)
			PushUtils.restartServices(activity.applicationContext)
			return true
		}
		return false
	}

}
