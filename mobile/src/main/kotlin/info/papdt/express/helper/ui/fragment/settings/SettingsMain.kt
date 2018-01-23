package info.papdt.express.helper.ui.fragment.settings

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import com.google.firebase.iid.FirebaseInstanceId

import info.papdt.express.helper.R
import info.papdt.express.helper.api.PushApi
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.services.ClipboardDetectService
import info.papdt.express.helper.services.FCMService
import info.papdt.express.helper.support.*
import info.papdt.express.helper.ui.SettingsActivity
import moe.feng.alipay.zerosdk.AlipayZeroSdk
import moe.shizuku.preference.EditTextPreference
import moe.shizuku.preference.ListPreference
import moe.shizuku.preference.Preference
import moe.shizuku.preference.SwitchPreference
import moe.feng.kotlinyan.common.*

class SettingsMain : AbsPrefFragment(), Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

	// User interface preference
	private val mPrefNavigationTint: SwitchPreference by PreferenceProperty("navigation_tint")
	private val mPrefNightMode: ListPreference by PreferenceProperty("night_mode")
	private val mPrefShowTipsAgain: Preference by PreferenceProperty("show_tips_again")

	// Notification & push preference
	private val mPrefDontDisturb: SwitchPreference by PreferenceProperty("dont_disturb")
	private val mPrefIntervalTime: ListPreference by PreferenceProperty("interval")
	private val mPrefEnable: SwitchPreference by PreferenceProperty("enable_push")
	private val mPrefHttps: SwitchPreference by PreferenceProperty("enable_https")
	private val mPrefApiHost: EditTextPreference by PreferenceProperty("api_host")
	private val mPrefApiPort: EditTextPreference by PreferenceProperty("api_port")
	private val mPrefInstanceId: Preference by PreferenceProperty("firebase_instance_id")
	private val mPrefSync: Preference by PreferenceProperty("push_sync")
	private val mPrefReqPush: Preference by PreferenceProperty("request_push")
	private val mPrefWhatsThis: Preference by PreferenceProperty("push_intro")

    // Query api settings
    private val mPrefApiType: ListPreference by PreferenceProperty("api_type")

	// Auto detect
	private val mPrefFromClipboard: SwitchPreference by PreferenceProperty("from_clipboard")

	// About
	private val mPrefVersion: Preference by PreferenceProperty("version")
	private val mPrefSina: Preference by PreferenceProperty("sina")
	private val mPrefGithub: Preference by PreferenceProperty("github")
	private var mPrefAlipay: Preference? = null
	private val mPrefLicense: Preference by PreferenceProperty("license")
	private val mPrefGooglePlus: Preference by PreferenceProperty("googleplus")
	private val mPrefIconDesigner: Preference by PreferenceProperty("designer")
	private val mPrefContributors: Preference by PreferenceProperty("contributors")

	private var needRegister = false
	private var needFreeServer = false

	private val database by lazy { PackageDatabase.getInstance(activity!!) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setHasOptionsMenu(true)
	}

	override fun onCreatePreferences(bundle: Bundle?, s: String?) {
		addPreferencesFromResource(
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
					R.xml.settings_main_old else R.xml.settings_main
		)

		mPrefAlipay = findPreference("alipay")

		var versionName: String? = null
		var versionCode = 0
		try {
			activity?.packageManager?.getPackageInfo(activity?.packageName, 0)?.let {
				versionName = it.versionName
				versionCode = it.versionCode
			}
		} catch (e: PackageManager.NameNotFoundException) {
			e.printStackTrace()
		}

		mPrefVersion.summary = String.format(getString(R.string.app_version_format), versionName, versionCode)

		/** Default value  */
		mPrefNavigationTint.isEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
		mPrefNavigationTint.isChecked = settings.getBoolean(Settings.KEY_NAVIGATION_TINT, true)

		val target = settings.getInt(Settings.KEY_NIGHT_MODE, 0)
		if (mPrefNightMode.value == null) {
			mPrefNightMode.setValueIndex(target)
		}

		mPrefDontDisturb.isChecked = settings.getBoolean(Settings.KEY_NOTIFICATION_DO_NOT_DISTURB, true)

		val intervalTarget = settings.getInt(Settings.KEY_NOTIFICATION_INTERVAL, 1)
		if (mPrefIntervalTime.value == null) {
			mPrefIntervalTime.setValueIndex(intervalTarget)
		}

        mPrefApiType.value = SettingsInstance.packageApiType.toString()

		mPrefApiHost.text = SettingsInstance.pushApiHost
		mPrefApiPort.text = SettingsInstance.pushApiPort.toString()
		mPrefHttps.isChecked = SettingsInstance.enableHttps

		mPrefFromClipboard.isChecked = settings.getBoolean(Settings.KEY_DETECT_FROM_CLIPBOARD, false)

		/** Hide development items */
		mPrefInstanceId.isVisible = false
		mPrefReqPush.isVisible = false

		mPrefVersion.onPreferenceClickListener = this
		mPrefGithub.onPreferenceClickListener = this
		mPrefSina.onPreferenceClickListener = this
		mPrefAlipay?.onPreferenceClickListener = this
		mPrefLicense.onPreferenceClickListener = this
		mPrefIconDesigner.onPreferenceClickListener = this
		mPrefContributors.onPreferenceClickListener = this
		mPrefGooglePlus.onPreferenceClickListener = this

		// UI
		mPrefNavigationTint.onPreferenceChangeListener = this
		mPrefNightMode.onPreferenceChangeListener = this
		mPrefShowTipsAgain.onPreferenceClickListener = this

		// Notification & push
		mPrefDontDisturb.onPreferenceChangeListener = this
		mPrefIntervalTime.onPreferenceChangeListener = this
		mPrefInstanceId.onPreferenceClickListener = this
		mPrefSync.onPreferenceClickListener = this
		mPrefReqPush.onPreferenceClickListener = this
		mPrefEnable.onPreferenceChangeListener = this
		mPrefApiHost.onPreferenceChangeListener = this
		mPrefApiPort.onPreferenceChangeListener = this
		mPrefWhatsThis.onPreferenceClickListener = this
		mPrefHttps.onPreferenceChangeListener = this

        // Query api type
        mPrefApiType.onPreferenceChangeListener = this

		// Auto detect
		mPrefFromClipboard.onPreferenceChangeListener = this

		setEnablePush(SettingsInstance.enablePush)
	}

	override fun onStop() {
		super.onStop()
		if (needRegister) {
			PushApi.register().flatMap { PushApi.sync(database.getPackageIdList()) }.subscribe()
		}
	}

	private fun setEnablePush(b: Boolean) {
		if (b) {
			mPrefIntervalTime.setValueIndex(4)
			mPrefIntervalTime.onPreferenceChangeListener.onPreferenceChange(mPrefIntervalTime, "4")
		}
		SettingsInstance.enablePush = b
		mPrefSync.isEnabled = b
		mPrefReqPush.isEnabled = b
		mPrefIntervalTime.isEnabled = !b
		activity?.packageManager?.setComponentEnabledSetting(
				ComponentName(activity!!.applicationContext, FCMService::class.java),
				if (b) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP
		)
	}

	private fun setFreeApiServer() {
		mPrefApiHost.text = "pt.api.rabi.coffee"
		mPrefApiPort.text = "3001"
		mPrefHttps.isChecked = true
		SettingsInstance.pushApiHost = "pt.api.rabi.coffee"
		SettingsInstance.pushApiPort = 3001
		SettingsInstance.enableHttps = true
	}

	override fun onPreferenceClick(pref: Preference): Boolean {
		return when (pref) {
			// UI
			mPrefShowTipsAgain -> {
				SettingsInstance.shouldShowTips = true
				makeRestartTips()
				true
			}
			// Notification & push
			mPrefInstanceId -> {
				AlertDialog.Builder(activity).apply {
					titleRes = R.string.pref_firebase_instance_id
					message = FirebaseInstanceId.getInstance().token ?: "null"
					okButton()
					negativeButton(R.string.pref_copy_button) { _, _ ->
						ClipboardUtils.putString(activity, FirebaseInstanceId.getInstance().token)
						makeSnackbar(resources.string[R.string.toast_copied_successfully], Snackbar.LENGTH_LONG)?.show()
					}
					neutralButton(R.string.pref_register_button) { _, _ ->
						PushApi.register(FirebaseInstanceId.getInstance().token!!).subscribe {
							makeSnackbar(if (it.code >= 0) "Succeed" else "Failed", Snackbar.LENGTH_LONG)?.show()
						}
					}
				}.create().apply {
					setOnShowListener {
						findViewById<TextView>(android.R.id.message).setTextIsSelectable(true)
					}
				}.show()
				true
			}
			mPrefSync -> {
				if (needRegister) {
					PushApi.register().flatMap {
						PushApi.sync(database.getPackageIdList())
					}.subscribe {
						makeSnackbar(if (it.code >= 0) "Succeed" else "Failed", Snackbar.LENGTH_LONG)?.show()
					}
					needRegister = false
				} else PushApi.sync(database.getPackageIdList())
						.subscribe {
							makeSnackbar(if (it.code >= 0) "Succeed" else "Failed", Snackbar.LENGTH_LONG)?.show()
						}
				true
			}
			mPrefReqPush -> {
				if (needRegister) {
					PushApi.register().flatMap { PushApi.requestPush() }.subscribe {
						makeSnackbar(it.message, Snackbar.LENGTH_LONG)?.show()
					}
					needRegister = false
				} else PushApi.requestPush().subscribe { makeSnackbar(it.message, Snackbar.LENGTH_LONG)?.show() }
				true
			}
			mPrefWhatsThis -> {
				activity?.buildAlertDialog {
					titleRes = R.string.fcm_push_intro_title
					messageRes = R.string.fcm_push_intro_msg
					okButton()
					/*neutralButton(R.string.fcm_push_no_server_button) { _, _ ->
						if (SettingsInstance.clickedDonate) {
							setFreeApiServer()
						} else {
							needFreeServer = true
							activity.buildAlertDialog {
								messageRes = R.string.no_server_message
								okButton()
							}.show()
						}
					}*/
				}?.show()
				true
			}
			// About
			mPrefGithub -> {
				openWebsite(getString(R.string.github_repo_url))
				true
			}
			mPrefSina -> {
				openWebsite(getString(R.string.author_sina_url))
				true
			}
			mPrefAlipay -> {
				if (AlipayZeroSdk.hasInstalledAlipayClient(activity)) {
					AlipayZeroSdk.startAlipayClient(activity, "aehvyvf4taua18zo6e")
				} else {
					ClipboardUtils.putString(activity, getString(R.string.alipay_support_account))
					makeSnackbar(getString(R.string.toast_copied_successfully), Snackbar.LENGTH_SHORT)
							?.show()
				}
				SettingsInstance.clickedDonate = true
				if (needFreeServer) setFreeApiServer()
				true
			}
			mPrefLicense -> {
				parentActivity?.let {
					SettingsActivity.launch(it, SettingsActivity.FLAG_LICENSE)
				}
				true
			}
			mPrefIconDesigner -> {
				openWebsite(getString(R.string.icon_designer_url))
				true
			}
			mPrefContributors -> {
				parentActivity?.let {
					SettingsActivity.launch(it, SettingsActivity.FLAG_CONTRIBUTORS)
				}
				true
			}
			mPrefGooglePlus -> {
				openWebsite(getString(R.string.google_plus_url))
				true
			}
			else -> false
		}
	}


	override fun onPreferenceChange(pref: Preference, o: Any?): Boolean {
		return when (pref) {
			// UI
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
			// Query Api
			mPrefApiType -> {
				SettingsInstance.packageApiType = (o as String).toLong()
				true
			}
			// Notification & push
			mPrefDontDisturb -> {
				val b = o as Boolean
				settings.putBoolean(Settings.KEY_NOTIFICATION_DO_NOT_DISTURB, b)
				true
			}
			mPrefIntervalTime -> {
				val value = Integer.parseInt(o as String)
				settings.putInt(Settings.KEY_NOTIFICATION_INTERVAL, value)
				activity?.applicationContext?.let(PushUtils::restartServices)
				true
			}
			mPrefEnable -> {
				val b = o as Boolean
				setEnablePush(b)
				if (b) needRegister = true
				database.size()
				true
			}
			mPrefHttps -> {
				val b = o as Boolean
				SettingsInstance.enableHttps = b
				needRegister = true
				true
			}
			mPrefApiHost -> {
				SettingsInstance.pushApiHost = o as String
				needRegister = true
				database.size()
				true
			}
			mPrefApiPort -> {
				SettingsInstance.pushApiPort = (o as String).toInt()
				needRegister = true
				database.size()
				true
			}
			// Auto detect
			mPrefFromClipboard -> {
				val isOpen = o as Boolean
				if (isOpen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					if (!android.provider.Settings.canDrawOverlays(activity)) {
						val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
								Uri.parse("package:" + activity!!.packageName))
						startActivity(intent)
						return false
					}
				}
				settings.putBoolean(Settings.KEY_DETECT_FROM_CLIPBOARD, isOpen)
				val intent = Intent(activity?.applicationContext, ClipboardDetectService::class.java)
				intent.run(if (!isOpen) activity!!::stopService else activity!!::startService)
				return true
			}
			else -> false
		}
	}

	override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
		inflater?.inflate(R.menu.menu_settings_main, menu)
		menu?.tintItemsColor(resources.color[android.R.color.white])
		super.onCreateOptionsMenu(menu, inflater)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
		R.id.action_donate -> {
			listView.smoothScrollToPosition(listView.adapter.itemCount - 1)
			true
		}
		R.id.action_play_store -> {
			val intent = Intent(Intent.ACTION_VIEW)
			intent.data = Uri.parse("market://details?id=${activity!!.packageName}")
			startActivity(intent)
			true
		}
		else -> super.onOptionsItemSelected(item)
	}

}
