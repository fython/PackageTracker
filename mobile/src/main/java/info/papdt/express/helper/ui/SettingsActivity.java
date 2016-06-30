package info.papdt.express.helper.ui;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import info.papdt.express.helper.R;
import info.papdt.express.helper.ui.common.AbsActivity;
import info.papdt.express.helper.ui.fragment.settings.SettingsMain;

public class SettingsActivity extends AbsActivity {

	private int flag;

	private static final String EXTRA_SETTINGS_FLAG = "extra_flag";

	public static final int FLAG_MAIN = 0, FLAG_UI = 1, FLAG_LICENSE = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			getWindow().setStatusBarColor(Color.TRANSPARENT);
		}

		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		flag = intent.getIntExtra(EXTRA_SETTINGS_FLAG, FLAG_MAIN);

		setContentView(R.layout.activity_settings);
	}

	@Override
	protected void setUpViews() {
		mActionBar.setDisplayHomeAsUpEnabled(true);

		Fragment f = null;
		switch (flag) {
			case FLAG_MAIN:
				f = new SettingsMain();
				break;
			case FLAG_UI:
				break;
			case FLAG_LICENSE:
				break;
			default:
				throw new RuntimeException("Please set flag when launching activity.");
		}
		getFragmentManager().beginTransaction().replace(R.id.container, f).commit();
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
