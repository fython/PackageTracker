package info.papdt.express.helper.ui.fragment.settings;

import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;

import info.papdt.express.helper.R;
import info.papdt.express.helper.support.Settings;

public class SettingsUi extends AbsPrefFragment implements Preference.OnPreferenceChangeListener {

	private SwitchPreference mPrefNavigationTint;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_ui);

		/** findPreference */
		mPrefNavigationTint = (SwitchPreference) findPreference("navigation_tint");

		/** Default value */
		mPrefNavigationTint.setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
		mPrefNavigationTint.setChecked(getSettings().getBoolean(Settings.KEY_NAVIGATION_TINT, true));

		/** Set callback */
		mPrefNavigationTint.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object o) {
		if (pref == mPrefNavigationTint) {
			Boolean b = (Boolean) o;
			getSettings().putBoolean(Settings.KEY_NAVIGATION_TINT, b);
			makeRestartTips();
			return true;
		}
		return false;
	}

}
