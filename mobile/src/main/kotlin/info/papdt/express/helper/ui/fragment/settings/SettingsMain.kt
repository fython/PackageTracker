package info.papdt.express.helper.ui.fragment.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

import info.papdt.express.helper.R
import info.papdt.express.helper.support.*
import moe.feng.alipay.zerosdk.AlipayZeroSdk
import moe.shizuku.preference.Preference
import moe.feng.kotlinyan.common.*

class SettingsMain : AbsPrefFragment(), Preference.OnPreferenceClickListener {

	// About
	private val mPrefVersion: Preference by PreferenceProperty("version")
	private val mPrefSina: Preference by PreferenceProperty("sina")
	private val mPrefGithub: Preference by PreferenceProperty("github")
	private var mPrefAlipay: Preference? = null
	private val mPrefTelegram: Preference by PreferenceProperty("telegram")

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setHasOptionsMenu(true)
	}

	override fun onCreatePreferences(bundle: Bundle?, s: String?) {
		addPreferencesFromResource(R.xml.settings_main)

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

		mPrefVersion.onPreferenceClickListener = this
		mPrefGithub.onPreferenceClickListener = this
		mPrefSina.onPreferenceClickListener = this
		mPrefAlipay?.onPreferenceClickListener = this
		mPrefTelegram.onPreferenceClickListener = this
	}

	override fun onPreferenceClick(pref: Preference): Boolean {
		return when (pref) {
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
				true
			}
			mPrefTelegram -> {
				openWebsite("https://t.me/gwo_apps")
				true
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
		R.id.action_play_store -> {
			val intent = Intent(Intent.ACTION_VIEW)
			intent.data = Uri.parse("market://details?id=${activity!!.packageName}")
			startActivity(intent)
			true
		}
		else -> super.onOptionsItemSelected(item)
	}

}
