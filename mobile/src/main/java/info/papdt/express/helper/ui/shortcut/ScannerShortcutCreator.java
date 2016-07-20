package info.papdt.express.helper.ui.shortcut;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import info.papdt.express.helper.R;
import info.papdt.express.helper.ui.MainActivity;
import info.papdt.express.helper.ui.ScannerActivity;

public class ScannerShortcutCreator extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getIntent().getAction().equals(Intent.ACTION_CREATE_SHORTCUT)) {
			Intent intent = new Intent();
			Parcelable icon = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_scanner_launcher);

			intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.activity_scanner));
			intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

			Intent launchIntent = new Intent(getApplicationContext(), MainActivity.class);
			launchIntent.setAction(ScannerActivity.ACTION_SCAN_TO_ADD);
			launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

			intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);

			setResult(RESULT_OK, intent);
		} else {
			setResult(RESULT_CANCELED);
		}
		finish();
	}

}
