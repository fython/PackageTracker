package info.papdt.express.helper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;

import info.papdt.express.helper.support.Settings;
import info.papdt.express.helper.ui.MainActivity;
import info.papdt.express.helper.ui.SplashActivity;

public class EntryActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Settings mSettings = Settings.getInstance(getApplicationContext());

		super.onCreate(savedInstanceState);

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
