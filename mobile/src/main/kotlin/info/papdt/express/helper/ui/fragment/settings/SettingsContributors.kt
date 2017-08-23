package info.papdt.express.helper.ui.fragment.settings

import android.os.Bundle

import info.papdt.express.helper.R
import moe.shizuku.preference.Preference

class SettingsContributors : AbsPrefFragment(), Preference.OnPreferenceClickListener {

	private val mPrefCoderFox: Preference by PreferenceProperty("coderfox")
	private val mPrefHearSilent: Preference by PreferenceProperty("hearslient")
	private val mPrefArchieMeng: Preference by PreferenceProperty("archiemeng")

	override fun onCreatePreferences(bundle: Bundle?, s: String?) {
		addPreferencesFromResource(R.xml.settings_contributors)

		mPrefCoderFox.onPreferenceClickListener = this
		mPrefHearSilent.onPreferenceClickListener = this
		mPrefArchieMeng.onPreferenceClickListener = this
	}

	override fun onPreferenceClick(pref: Preference): Boolean {
		return when (pref) {
			mPrefCoderFox -> {
				openWebsite(getString(R.string.contributors_coderfox_weibo_url))
				true
			}
			mPrefHearSilent -> {
				openWebsite("https://github.com/hearsilent")
				true
			}
			mPrefArchieMeng -> {
				openWebsite("https://github.com/ArchieMeng")
				true
			}
			else -> false
		}
	}
}
