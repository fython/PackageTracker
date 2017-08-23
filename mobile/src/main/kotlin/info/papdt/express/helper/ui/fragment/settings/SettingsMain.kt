package info.papdt.express.helper.ui.fragment.settings

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar

import info.papdt.express.helper.R
import info.papdt.express.helper.support.ClipboardUtils
import info.papdt.express.helper.ui.SettingsActivity
import moe.feng.alipay.zerosdk.AlipayZeroSdk
import moe.shizuku.preference.Preference

class SettingsMain : AbsPrefFragment(), Preference.OnPreferenceClickListener {

	private val mPrefUI: Preference by PreferenceProperty("settings_ui")
	private val mPrefNetwork: Preference by PreferenceProperty("settings_network")
	private val mPrefVersion: Preference by PreferenceProperty("version")
	private val mPrefSina: Preference by PreferenceProperty("sina")
	private val mPrefGithub: Preference by PreferenceProperty("github")
	private var mPrefAlipay: Preference? = null
	private val mPrefLicense: Preference by PreferenceProperty("license")
	private val mPrefGooglePlus: Preference by PreferenceProperty("googleplus")
	private val mPrefIconDesigner: Preference by PreferenceProperty("designer")
	private val mPrefContributors: Preference by PreferenceProperty("contributors")
	private val mPrefAutoDetect: Preference by PreferenceProperty("settings_auto_detect")

	override fun onCreatePreferences(bundle: Bundle?, s: String?) {
		addPreferencesFromResource(R.xml.settings_main)

		mPrefAlipay = findPreference("alipay")

		var versionName: String? = null
		var versionCode = 0
		try {
			activity.packageManager.getPackageInfo(activity.packageName, 0).let {
				versionName = it.versionName
				versionCode = it.versionCode
			}
		} catch (e: PackageManager.NameNotFoundException) {
			e.printStackTrace()
		}

		mPrefVersion.summary = String.format(getString(R.string.app_version_format), versionName, versionCode)

		mPrefUI.onPreferenceClickListener = this
		mPrefNetwork.onPreferenceClickListener = this
		mPrefAutoDetect.onPreferenceClickListener = this
		mPrefVersion.onPreferenceClickListener = this
		mPrefGithub.onPreferenceClickListener = this
		mPrefSina.onPreferenceClickListener = this
		mPrefAlipay?.onPreferenceClickListener = this
		mPrefLicense.onPreferenceClickListener = this
		mPrefIconDesigner.onPreferenceClickListener = this
		mPrefContributors.onPreferenceClickListener = this
		mPrefGooglePlus.onPreferenceClickListener = this
	}

	override fun onPreferenceClick(pref: Preference): Boolean {
		return when (pref) {
			mPrefUI -> {
				SettingsActivity.launch(parentActivity, SettingsActivity.FLAG_UI)
				true
			}
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
							.show()
				}
				true
			}
			mPrefLicense -> {
				SettingsActivity.launch(parentActivity, SettingsActivity.FLAG_LICENSE)
				true
			}
			mPrefNetwork -> {
				SettingsActivity.launch(parentActivity, SettingsActivity.FLAG_NETWORK)
				true
			}
			mPrefIconDesigner -> {
				openWebsite(getString(R.string.icon_designer_url))
				true
			}
			mPrefContributors -> {
				SettingsActivity.launch(parentActivity, SettingsActivity.FLAG_CONTRIBUTORS)
				true
			}
			mPrefGooglePlus -> {
				openWebsite(getString(R.string.google_plus_url))
				true
			}
			mPrefAutoDetect -> {
				SettingsActivity.launch(parentActivity, SettingsActivity.FLAG_AUTO_DETECT)
				true
			}
			else -> false
		}
	}

}
