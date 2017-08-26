package info.papdt.express.helper.ui.fragment.settings

import android.app.AlertDialog
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.widget.TextView

import com.google.firebase.iid.FirebaseInstanceId
import info.papdt.express.helper.R
import info.papdt.express.helper.api.PushApi
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.services.FCMService
import info.papdt.express.helper.support.ClipboardUtils
import info.papdt.express.helper.support.PushUtils
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.support.SettingsInstance
import moe.shizuku.preference.EditTextPreference
import moe.shizuku.preference.ListPreference
import moe.shizuku.preference.Preference
import moe.shizuku.preference.SwitchPreference

class SettingsNetwork : AbsPrefFragment(), Preference.OnPreferenceChangeListener {

	private val mPrefDontDisturb: SwitchPreference by PreferenceProperty("dont_disturb")
	private val mPrefIntervalTime: ListPreference by PreferenceProperty("interval")
	private val mPrefEnable: SwitchPreference by PreferenceProperty("enable_push")
	private val mPrefApiHost: EditTextPreference by PreferenceProperty("api_host")
	private val mPrefApiPort: EditTextPreference by PreferenceProperty("api_port")
	private val mPrefInstanceId: Preference by PreferenceProperty("firebase_instance_id")
	private val mPrefSync: Preference by PreferenceProperty("push_sync")
	private val mPrefReqPush: Preference by PreferenceProperty("request_push")

	private var needRegister = false

	override fun onCreatePreferences(bundle: Bundle?, s: String?) {
		addPreferencesFromResource(R.xml.settings_network)

		/** Default value  */
		mPrefDontDisturb.isChecked = settings.getBoolean(Settings.KEY_NOTIFICATION_DO_NOT_DISTURB, true)

		val target = settings.getInt(Settings.KEY_NOTIFICATION_INTERVAL, 1)
		if (mPrefIntervalTime.value == null) {
			mPrefIntervalTime.setValueIndex(target)
		}

		setEnablePush(SettingsInstance.enablePush)
		mPrefApiHost.text = SettingsInstance.pushApiHost
		mPrefApiPort.text = SettingsInstance.pushApiPort.toString()

		/** Hide development items */
		mPrefInstanceId.isVisible = false

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
			if (needRegister) {
				PushApi.register().subscribe {
					PushApi.sync(PackageDatabase.getInstance(activity).data.map { "${it.number}+${it.companyType}" })
							.subscribe {
								makeSnackbar(if (it.code >= 0) "Succeed" else "Failed", Snackbar.LENGTH_LONG).show()
							}
				}
				needRegister = false
			} else PushApi.sync(PackageDatabase.getInstance(activity).data.map { "${it.number}+${it.companyType}" })
					.subscribe {
						makeSnackbar(if (it.code >= 0) "Succeed" else "Failed", Snackbar.LENGTH_LONG).show()
					}
			true
		}
		mPrefReqPush.setOnPreferenceClickListener {
			if (needRegister) {
				PushApi.register().subscribe {
					PushApi.requestPush().subscribe { makeSnackbar(it.message, Snackbar.LENGTH_LONG).show() }
				}
				needRegister = false
			} else PushApi.requestPush().subscribe { makeSnackbar(it.message, Snackbar.LENGTH_LONG).show() }
			true
		}
		mPrefEnable.onPreferenceChangeListener = this
		mPrefApiHost.onPreferenceChangeListener = this
		mPrefApiPort.onPreferenceChangeListener = this
	}

	override fun onStop() {
		super.onStop()
		if (needRegister) PushApi.register(FirebaseInstanceId.getInstance().token ?: "null").subscribe()
	}

	private fun setEnablePush(b: Boolean) {
		SettingsInstance.enablePush = b
		mPrefSync.isEnabled = b
		mPrefReqPush.isEnabled = b
		context.packageManager.setComponentEnabledSetting(
				ComponentName(context.applicationContext, FCMService::class.java),
				if (b) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP
		)
	}

	override fun onPreferenceChange(pref: Preference, o: Any): Boolean {
		return when (pref) {
			mPrefDontDisturb -> {
				val b = o as Boolean
				settings.putBoolean(Settings.KEY_NOTIFICATION_DO_NOT_DISTURB, b)
				true
			}
			mPrefIntervalTime -> {
				val value = Integer.parseInt(o as String)
				settings.putInt(Settings.KEY_NOTIFICATION_INTERVAL, value)
				PushUtils.restartServices(activity.applicationContext)
				true
			}
			mPrefEnable-> {
				val b = o as Boolean
				setEnablePush(b)
				if (b) needRegister = true
				true
			}
			mPrefApiHost -> {
				SettingsInstance.pushApiHost = o as String
				needRegister = true
				true
			}
			mPrefApiPort -> {
				SettingsInstance.pushApiPort = (o as String).toInt()
				needRegister = true
				true
			}
			else -> false
		}
	}

}
