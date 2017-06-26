package info.papdt.express.helper.ui.fragment.settings;

import android.content.Intent;
import android.os.Bundle;
import info.papdt.express.helper.R;
import info.papdt.express.helper.services.ClipboardDetectService;
import info.papdt.express.helper.support.Settings;
import rikka.materialpreference.Preference;
import rikka.materialpreference.SwitchPreference;

public class SettingsAutoDetect extends AbsPrefFragment
		implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

	private SwitchPreference mPrefFromClipboard;
	private Preference mPrefFromScreen;

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		addPreferencesFromResource(R.xml.settings_auto_detect);

		/** findPreference */
		mPrefFromClipboard = (SwitchPreference) findPreference("from_clipboard");
		mPrefFromScreen = findPreference("from_screen");

		/** Default value */
		mPrefFromClipboard.setChecked(getSettings().getBoolean(Settings.KEY_DETECT_FROM_CLIPBOARD, false));

		/** Set callback */
		mPrefFromClipboard.setOnPreferenceChangeListener(this);
		mPrefFromScreen.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object o) {
		if (pref == mPrefFromClipboard) {
			Boolean b = (Boolean) o;
			getSettings().putBoolean(Settings.KEY_DETECT_FROM_CLIPBOARD, b);
			Intent intent = new Intent(getActivity().getApplicationContext(), ClipboardDetectService.class);
			if (!b) {
				getActivity().stopService(intent);
			} else {
				getActivity().startService(intent);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {
		if (pref == mPrefFromScreen) {
			Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
			startActivity(intent);
			return true;
		}
		return false;
	}
}
