package info.papdt.express.helper.ui.fragment.settings;

import android.os.Bundle;

import info.papdt.express.helper.R;
import info.papdt.express.helper.support.PushUtils;
import info.papdt.express.helper.support.Settings;
import rikka.materialpreference.ListPreference;
import rikka.materialpreference.Preference;
import rikka.materialpreference.SwitchPreference;

public class SettingsNetwork extends AbsPrefFragment implements Preference.OnPreferenceChangeListener {

	private SwitchPreference mPrefDontDisturb;
	private ListPreference mPrefIntervalTime;

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		addPreferencesFromResource(R.xml.settings_network);

		/** findPreference */
		mPrefDontDisturb = (SwitchPreference) findPreference("dont_disturb");
		mPrefIntervalTime = (ListPreference) findPreference("interval");

		/** Default value */
		mPrefDontDisturb.setChecked(getSettings().getBoolean(Settings.KEY_NOTIFICATION_DO_NOT_DISTURB, true));

		int target = getSettings().getInt(Settings.KEY_NOTIFICATION_INTERVAL, 1);
		if (mPrefIntervalTime.getValue() == null) {
			mPrefIntervalTime.setValueIndex(target);
		}

		/** Set callback */
		mPrefDontDisturb.setOnPreferenceChangeListener(this);
		mPrefIntervalTime.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object o) {
		if (pref == mPrefDontDisturb) {
			Boolean b = (Boolean) o;
			getSettings().putBoolean(Settings.KEY_NOTIFICATION_DO_NOT_DISTURB, b);
			return true;
		}
		if (pref == mPrefIntervalTime) {
			int value = Integer.parseInt((String) o);
			getSettings().putInt(Settings.KEY_NOTIFICATION_INTERVAL, value);
			PushUtils.restartServices(getActivity().getApplicationContext());
			return true;
		}
		return false;
	}

}
