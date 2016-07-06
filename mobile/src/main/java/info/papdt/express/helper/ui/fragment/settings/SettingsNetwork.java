package info.papdt.express.helper.ui.fragment.settings;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;

import info.papdt.express.helper.R;
import info.papdt.express.helper.support.PushUtils;
import info.papdt.express.helper.support.Settings;

public class SettingsNetwork extends AbsPrefFragment implements Preference.OnPreferenceChangeListener {

	private SwitchPreference mPrefDontDisturb;
	private ListPreference mPrefIntervalTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_network);

		/** findPreference */
		mPrefDontDisturb = (SwitchPreference) findPreference("dont_disturb");
		mPrefIntervalTime = (ListPreference) findPreference("interval");

		/** Default value */
		mPrefDontDisturb.setChecked(getSettings().getBoolean(Settings.KEY_NOTIFICATION_DO_NOT_DISTURB, true));

		String[] values = getResources().getStringArray(R.array.notification_interval_item);
		int index, target = getSettings().getInt(Settings.KEY_NOTIFICATION_INTERVAL, 0);
		for (index = 0; index < values.length; index++) {
			if (values[index].equals(String.valueOf(target))) break;
		}
		mPrefIntervalTime.setSummary(getResources().getStringArray(R.array.notification_interval)[index]);

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
			String[] values = getResources().getStringArray(R.array.notification_interval_item);
			int index;
			for (index = 0; index < values.length; index++) {
				if (values[index].equals(o)) break;
			}
			mPrefIntervalTime.setSummary(getResources().getStringArray(R.array.notification_interval)[index]);
			PushUtils.restartServices(getActivity().getApplicationContext());
			return true;
		}
		return false;
	}

}
