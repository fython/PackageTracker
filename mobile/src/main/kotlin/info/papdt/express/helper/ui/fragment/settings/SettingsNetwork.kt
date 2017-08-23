package info.papdt.express.helper.ui.fragment.settings

import android.app.AlertDialog
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.widget.TextView

import com.google.firebase.iid.FirebaseInstanceId
import info.papdt.express.helper.R
import info.papdt.express.helper.api.PushApi
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.support.ClipboardUtils
import info.papdt.express.helper.support.PushUtils
import info.papdt.express.helper.support.Settings
import moe.feng.kotlinyan.common.ActivityExtensions
import moe.shizuku.preference.ListPreference
import moe.shizuku.preference.Preference
import moe.shizuku.preference.SwitchPreference

class SettingsNetwork : AbsPrefFragment(), Preference.OnPreferenceChangeListener {

	private val mPrefDontDisturb: SwitchPreference by PreferenceProperty("dont_disturb")
	private val mPrefIntervalTime: ListPreference by PreferenceProperty("interval")
	private val mPrefInstanceId: Preference by PreferenceProperty("firebase_instance_id")
	private val mPrefSync: Preference by PreferenceProperty("push_sync")

	override fun onCreatePreferences(bundle: Bundle?, s: String?) {
		addPreferencesFromResource(R.xml.settings_network)

		/** Default value  */
		mPrefDontDisturb.isChecked = settings.getBoolean(Settings.KEY_NOTIFICATION_DO_NOT_DISTURB, true)

		val target = settings.getInt(Settings.KEY_NOTIFICATION_INTERVAL, 1)
		if (mPrefIntervalTime.value == null) {
			mPrefIntervalTime.setValueIndex(target)
		}

		/** Set callback  */
		mPrefDontDisturb.onPreferenceChangeListener = this
		mPrefIntervalTime.onPreferenceChangeListener = this
		mPrefInstanceId.setOnPreferenceClickListener {
			AlertDialog.Builder(activity).apply {
				titleRes = R.string.pref_firebase_instance_id
				message = FirebaseInstanceId.getInstance().token ?: "null"
				okButton()
				negativeButton(R.string.pref_copy_button) { _, _ ->
					ClipboardUtils.putString(activity, FirebaseInstanceId.getInstance().token)
					makeSnackbar(resources.string[R.string.toast_copied_successfully], Snackbar.LENGTH_LONG).show()
				}
				neutralButton(R.string.pref_register_button) { _, _ ->
					PushApi.register(FirebaseInstanceId.getInstance().token!!).subscribe {
						makeSnackbar(if (it.code >= 0) "Succeed" else "Failed", Snackbar.LENGTH_LONG).show()
					}
				}
			}.create().apply {
				setOnShowListener {
					findViewById<TextView>(android.R.id.message).setTextIsSelectable(true)
				}
			}.show()
			true
		}
		mPrefSync.setOnPreferenceClickListener {
			PushApi.sync(
					list = PackageDatabase.getInstance(activity).data.map { "${it.number}+${it.companyType}" },
					token = FirebaseInstanceId.getInstance().token
			).subscribe { makeSnackbar(if (it.code >= 0) "Succeed" else "Failed", Snackbar.LENGTH_LONG).show() }
			true
		}
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
