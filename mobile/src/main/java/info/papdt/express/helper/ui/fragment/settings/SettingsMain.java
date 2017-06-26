package info.papdt.express.helper.ui.fragment.settings;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;

import info.papdt.express.helper.R;
import info.papdt.express.helper.support.ClipboardUtils;
import info.papdt.express.helper.ui.SettingsActivity;
import moe.feng.alipay.zerosdk.AlipayZeroSdk;
import rikka.materialpreference.Preference;

public class SettingsMain extends AbsPrefFragment implements Preference.OnPreferenceClickListener {

	private Preference mPrefUI, mPrefNetwork, mPrefVersion, mPrefSina,
			mPrefGithub, mPrefAlipay, mPrefLicense, mPrefGooglePlus;
	private Preference mPrefIconDesigner, mPrefContributors, mPrefAutoDetect;

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		addPreferencesFromResource(R.xml.settings_main);

		mPrefUI = findPreference("settings_ui");
		mPrefNetwork = findPreference("settings_network");
		mPrefAutoDetect = findPreference("settings_auto_detect");
		mPrefVersion = findPreference("version");
		mPrefGithub = findPreference("github");
		mPrefSina = findPreference("sina");
		mPrefAlipay = findPreference("alipay");
		mPrefLicense = findPreference("license");
		mPrefIconDesigner = findPreference("designer");
		mPrefContributors = findPreference("contributors");
		mPrefGooglePlus = findPreference("googleplus");

		String versionName = null;
		int versionCode = 0;
		try {
			versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
			versionCode = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		mPrefVersion.setSummary(String.format(getString(R.string.app_version_format), versionName, versionCode));

		mPrefUI.setOnPreferenceClickListener(this);
		mPrefNetwork.setOnPreferenceClickListener(this);
		mPrefAutoDetect.setOnPreferenceClickListener(this);
		mPrefVersion.setOnPreferenceClickListener(this);
		mPrefGithub.setOnPreferenceClickListener(this);
		mPrefSina.setOnPreferenceClickListener(this);
		if (mPrefAlipay != null) mPrefAlipay.setOnPreferenceClickListener(this);
		mPrefLicense.setOnPreferenceClickListener(this);
		mPrefIconDesigner.setOnPreferenceClickListener(this);
		mPrefContributors.setOnPreferenceClickListener(this);
		mPrefGooglePlus.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {
		if (pref == mPrefUI) {
			SettingsActivity.launch(getParentActivity(), SettingsActivity.FLAG_UI);
			return true;
		}
		if (pref == mPrefGithub) {
			openWebsite(getString(R.string.github_repo_url));
			return true;
		}
		if (pref == mPrefSina) {
			openWebsite(getString(R.string.author_sina_url));
			return true;
		}
		if (pref == mPrefAlipay) {
			if (AlipayZeroSdk.hasInstalledAlipayClient(getActivity())) {
				AlipayZeroSdk.startAlipayClient(getActivity(), "aehvyvf4taua18zo6e");
			} else {
				ClipboardUtils.putString(getActivity(), getString(R.string.alipay_support_account));
				makeSnackbar(getString(R.string.toast_copied_successfully), Snackbar.LENGTH_SHORT)
						.show();
			}
			return true;
		}
		if (pref == mPrefLicense) {
			SettingsActivity.launch(getParentActivity(), SettingsActivity.FLAG_LICENSE);
			return true;
		}
		if (pref == mPrefNetwork) {
			SettingsActivity.launch(getParentActivity(), SettingsActivity.FLAG_NETWORK);
			return true;
		}
		if (pref == mPrefIconDesigner) {
			openWebsite(getString(R.string.icon_designer_url));
			return true;
		}
		if (pref == mPrefContributors) {
			SettingsActivity.launch(getParentActivity(), SettingsActivity.FLAG_CONTRIBUTORS);
			return true;
		}
		if (pref == mPrefGooglePlus) {
			openWebsite(getString(R.string.google_plus_url));
			return true;
		}
		if (pref == mPrefAutoDetect) {
			SettingsActivity.launch(getParentActivity(), SettingsActivity.FLAG_AUTO_DETECT);
			return true;
		}
		return false;
	}

}
