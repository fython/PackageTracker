package info.papdt.express.helper.ui.fragment.settings;

import android.os.Bundle;

import info.papdt.express.helper.R;
import moe.shizuku.preference.Preference;

public class SettingsContributors extends AbsPrefFragment implements Preference.OnPreferenceClickListener {

	private Preference mPrefCoderFox, mPrefHearSilent, mPrefArchieMeng;

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		addPreferencesFromResource(R.xml.settings_contributors);

		mPrefCoderFox = findPreference("coderfox");
		mPrefCoderFox.setOnPreferenceClickListener(this);

		mPrefHearSilent = findPreference("hearsilent");
		mPrefHearSilent.setOnPreferenceClickListener(this);

		mPrefArchieMeng = findPreference("archiemeng");
		mPrefArchieMeng.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {
		if (pref == mPrefCoderFox) {
			openWebsite(getString(R.string.contributors_coderfox_weibo_url));
			return true;
		}
		if (pref == mPrefHearSilent) {
			openWebsite("https://github.com/hearsilent");
			return true;
		}
		if (pref == mPrefArchieMeng) {
			openWebsite("https://github.com/ArchieMeng");
			return true;
		}
		return false;
	}
}
