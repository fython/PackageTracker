package info.papdt.express.helper.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import info.papdt.express.helper.R;
import info.papdt.express.helper.support.Settings;
import info.papdt.express.helper.ui.common.AbsActivity;
import info.papdt.express.helper.ui.fragment.settings.*;

public class SettingsActivity extends AbsActivity {

	private int flag;

	private static final String EXTRA_SETTINGS_FLAG = "extra_flag";

	public static final int FLAG_MAIN = 0, FLAG_UI = 1, FLAG_LICENSE = 2, FLAG_NETWORK = 3,
			FLAG_CONTRIBUTORS = 4, FLAG_AUTO_DETECT = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getSettings().getBoolean(Settings.KEY_NAVIGATION_TINT, true)
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isNightMode()) {
			getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
		}

		Intent intent = getIntent();
		flag = intent.getIntExtra(EXTRA_SETTINGS_FLAG, FLAG_MAIN);

		setContentView(R.layout.activity_settings);
	}

	@Override
	protected void setUpViews() {
		mActionBar.setDisplayHomeAsUpEnabled(true);

		Fragment f;
		switch (flag) {
			case FLAG_MAIN:
				f = new SettingsMain();
				mActionBar.setTitle(R.string.activity_settings);
				break;
			case FLAG_UI:
				f = new SettingsUi();
				mActionBar.setTitle(R.string.category_user_interface);
				break;
			case FLAG_LICENSE:
				getFragmentManager().beginTransaction().replace(R.id.container, new SettingsLicense())
						.commit();
				mActionBar.setTitle(R.string.open_source_license);
				return;
			case FLAG_NETWORK:
				f = new SettingsNetwork();
				mActionBar.setTitle(R.string.category_network);
				break;
			case FLAG_CONTRIBUTORS:
				f = new SettingsContributors();
				mActionBar.setTitle(R.string.category_contributors);
				break;
			case FLAG_AUTO_DETECT:
				f = new SettingsAutoDetect();
				mActionBar.setTitle(R.string.category_auto_detect_services);
				break;
			default:
				throw new RuntimeException("Please set flag when launching activity.");
		}
		getSupportFragmentManager().beginTransaction().replace(R.id.container, f).commit();
	}

	public static void launch(AppCompatActivity activity, int flag) {
		Intent intent = new Intent(activity, SettingsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(EXTRA_SETTINGS_FLAG, flag);
		activity.startActivity(intent);
	}

	public Snackbar makeSnackbar(String message, int duration) {
		return Snackbar.make($(R.id.container), message, duration);
	}

}
