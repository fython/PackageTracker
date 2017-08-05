package info.papdt.express.helper.ui.fragment.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.IntentCompat;
import android.view.View;

import info.papdt.express.helper.R;
import info.papdt.express.helper.support.Settings;
import info.papdt.express.helper.ui.SettingsActivity;
import moe.shizuku.preference.PreferenceFragment;

abstract class AbsPrefFragment extends PreferenceFragment {

	private Settings mSettings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mSettings = Settings.getInstance(getActivity().getApplicationContext());
		super.onCreate(savedInstanceState);
	}

	public Settings getSettings() {
		return mSettings;
	}

	public SettingsActivity getParentActivity() {
		return (SettingsActivity) getActivity();
	}

	public Snackbar makeSnackbar(String message, int duration) {
		return getParentActivity().makeSnackbar(message, duration);
	}

	public void makeRestartTips() {
		makeSnackbar(getString(R.string.toast_need_restart), Snackbar.LENGTH_LONG)
				.setAction(R.string.toast_need_restart_action, new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Context context = getParentActivity();
						PackageManager packageManager = context.getPackageManager();
						Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
						ComponentName componentName = intent.getComponent();
						Intent i = IntentCompat.makeRestartActivityTask(componentName);
						getParentActivity().startActivity(i);

						System.exit(0);
					}
				}).show();
	}

	public void openWebsite(String url) {
		CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
		builder.setToolbarColor(getActivity().getResources().getColor(R.color.pink_500));
		builder.build().launchUrl(getActivity(), Uri.parse(url));
	}

}
