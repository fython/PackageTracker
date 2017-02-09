package info.papdt.express.helper.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import info.papdt.express.helper.R;
import info.papdt.express.helper.ui.common.AbsActivity;

public class ImportExportActivity extends AbsActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import_export);

		mActionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void setUpViews() {

	}

	public static void launch(Activity activity) {
		Intent intent = new Intent(activity, ImportExportActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(intent);
	}

}
