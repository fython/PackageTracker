package info.papdt.express.helper.ui.fragment.settings;

import android.os.Bundle;
import android.preference.Preference;

import info.papdt.express.helper.R;

public class SettingsContributors extends AbsPrefFragment implements Preference.OnPreferenceClickListener {

	private Preference mPrefCoderFox;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_contributors);

		mPrefCoderFox = findPreference("coderfox");
		mPrefCoderFox.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {
		if (pref == mPrefCoderFox) {
			openWebsite(getString(R.string.contributors_coderfox_weibo_url));
			return true;
		}
		return false;
	}
}
