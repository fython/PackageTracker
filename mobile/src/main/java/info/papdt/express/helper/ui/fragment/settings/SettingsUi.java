package info.papdt.express.helper.ui.fragment.settings;

import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;

import info.papdt.express.helper.R;
import info.papdt.express.helper.support.Settings;

public class SettingsUi extends AbsPrefFragment implements Preference.OnPreferenceChangeListener {

	private SwitchPreference mPrefNavigationTint;
	private ListPreference mPrefNightMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_ui);

		/** findPreference */
		mPrefNavigationTint = (SwitchPreference) findPreference("navigation_tint");
		mPrefNightMode = (ListPreference) findPreference("night_mode");

		/** Default value */
		mPrefNavigationTint.setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
		mPrefNavigationTint.setChecked(getSettings().getBoolean(Settings.KEY_NAVIGATION_TINT, true));

		String[] values = getResources().getStringArray(R.array.night_mode_choices_item);
		int index, target = getSettings().getInt(Settings.KEY_NIGHT_MODE, 0);
		for (index = 0; index < values.length; index++) {
			if (values[index].equals(String.valueOf(target))) break;
		}
		mPrefNightMode.setSummary(getResources().getStringArray(R.array.night_mode_choices)[index]);

		/** Set callback */
		mPrefNavigationTint.setOnPreferenceChangeListener(this);
		mPrefNightMode.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object o) {
		if (pref == mPrefNavigationTint) {
			Boolean b = (Boolean) o;
			getSettings().putBoolean(Settings.KEY_NAVIGATION_TINT, b);
			makeRestartTips();
			return true;
		}
		if (pref == mPrefNightMode) {
			int value = Integer.parseInt((String) o);
			getSettings().putInt(Settings.KEY_NIGHT_MODE, value);
			String[] values = getResources().getStringArray(R.array.night_mode_choices_item);
			int index;
			for (index = 0; index < values.length; index++) {
				if (values[index].equals(o)) break;
			}
			mPrefNightMode.setSummary(getResources().getStringArray(R.array.night_mode_choices)[index]);
			makeRestartTips();
			return true;
		}
		return false;
	}

}
