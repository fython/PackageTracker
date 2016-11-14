package info.papdt.express.helper;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;

import com.tencent.bugly.crashreport.CrashReport;

import info.papdt.express.helper.support.Settings;
import info.papdt.express.helper.ui.MainActivity;
import info.papdt.express.helper.ui.SplashActivity;

public class EntryActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Settings mSettings = Settings.getInstance(getApplicationContext());
		int defaultNightMode;
		switch (mSettings.getInt(Settings.KEY_NIGHT_MODE, 0)) {
			case 1:
				defaultNightMode = AppCompatDelegate.MODE_NIGHT_AUTO;
				break;
			case 2:
				defaultNightMode = AppCompatDelegate.MODE_NIGHT_YES;
				break;
			case 3:
				defaultNightMode = AppCompatDelegate.MODE_NIGHT_NO;
				break;
			case 0:
			default:
				defaultNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
		}
		AppCompatDelegate.setDefaultNightMode(defaultNightMode);

		super.onCreate(savedInstanceState);

		/** Init CrashReport */
		CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
		strategy.setAppPackageName(getPackageName());

		String versionName = null;
		int versionCode = 0;
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionName = packageInfo.versionName;
			versionCode = packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		strategy.setAppVersion(versionName + "(" + versionCode + ")");
		CrashReport.initCrashReport(getApplicationContext(), Constants.BUGLY_APP_ID, Constants.BUGLY_ENABLE_DEBUG, strategy);

		/** Open activity */
		if (mSettings.getBoolean(Settings.KEY_FIRST_RUN, true)) {
			mSettings.putBoolean(Settings.KEY_FIRST_RUN, false);
			Intent intent = new Intent(this, SplashActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} else {
			MainActivity.launch(this);
		}

		finish();
	}

}
