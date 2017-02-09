package info.papdt.express.helper.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import info.papdt.express.helper.R;
import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.support.ScreenUtils;
import info.papdt.express.helper.support.Settings;
import info.papdt.express.helper.ui.common.AbsActivity;
import info.papdt.express.helper.ui.fragment.add.StepInput;
import info.papdt.express.helper.ui.fragment.add.StepNoFound;
import info.papdt.express.helper.ui.fragment.add.StepNoInternetConnection;
import info.papdt.express.helper.ui.fragment.add.StepSuccess;

public class AddActivity extends AbsActivity{

	public static final int STEP_INPUT = 0, STEP_NO_INTERNET_CONNECTION = 1, STEP_NO_FOUND = 2,
			STEP_SUCCESS = 3;

	private Fragment mStepInput, mStepNoInternetConnection, mStepNoFound, mStepSuccess;
	private ProgressBar mProgressBar;

	private Package pack;
	private String number;

	private boolean isFromMainActivity = false;

	private int nowStep;

	public static final String RESULT_EXTRA_PACKAGE_JSON = "package_json";

	public static final String EXTRA_IS_FROM_MAIN_ACTIVITY = "is_from_main";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			);
			getWindow().setStatusBarColor(Color.TRANSPARENT);
		}
		super.onCreate(savedInstanceState);
		if (getSettings().getBoolean(Settings.KEY_NAVIGATION_TINT, true)
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isNightMode()) {
			getWindow().setNavigationBarColor(getResources().getColor(R.color.brown_500));
		}
		setContentView(R.layout.activity_add);

		if (getIntent().hasExtra(EXTRA_IS_FROM_MAIN_ACTIVITY)
				&& getIntent().getBooleanExtra(EXTRA_IS_FROM_MAIN_ACTIVITY, false)) {
			isFromMainActivity = true;
		}

		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);
		((ViewGroup.MarginLayoutParams) mToolbar.getLayoutParams()).topMargin +=
				ScreenUtils.getStatusBarHeight(this);

		step(STEP_INPUT);

		if (ScannerActivity.ACTION_SCAN_TO_ADD.equals(getIntent().getAction())) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent(AddActivity.this, ScannerActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					mStepInput.startActivityForResult(intent, ScannerActivity.REQUEST_CODE_SCAN);
				}
			}, 600);
		}
	}

	@Override
	protected void setUpViews() {
		mProgressBar = $(R.id.progress_bar);
	}

	public void step(int step) {
		nowStep = step;

		FragmentTransaction fm = getFragmentManager().beginTransaction();

		/** Set Animation */
		fm.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

		switch (step) {
			case STEP_INPUT:
				if (mStepInput == null) {
					mStepInput = new StepInput();
				}
				fm.replace(R.id.container, mStepInput).commit();
				break;
			case STEP_NO_INTERNET_CONNECTION:
				if (mStepNoInternetConnection == null) {
					mStepNoInternetConnection = new StepNoInternetConnection();
				}
				fm.replace(R.id.container, mStepNoInternetConnection).commit();
				break;
			case STEP_NO_FOUND:
				if (mStepNoFound == null) {
					mStepNoFound = new StepNoFound();
				}
				fm.replace(R.id.container, mStepNoFound).commit();
				break;
			case STEP_SUCCESS:
				fm.replace(R.id.container, new StepSuccess()).commit();
				break;
		}
	}

	public void showProgressBar() {
		mProgressBar.setVisibility(View.VISIBLE);
	}

	public void hideProgressBar() {
		mProgressBar.setVisibility(View.INVISIBLE);
	}

	public void setPackage(Package pack) {
		this.pack = pack;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Package getPackage() {
		return this.pack;
	}

	public String getNumber() {
		return this.number;
	}

	public void finishAdd() {
		if (getPackage() == null) {
			Toast.makeText(this, R.string.toast_unknown_error, Toast.LENGTH_LONG).show();
			return;
		}

		if (isFromMainActivity) {
			Intent intent = new Intent();
			intent.putExtra(RESULT_EXTRA_PACKAGE_JSON, getPackage().toJsonString());
			setResult(MainActivity.RESULT_NEW_PACKAGE, intent);
		} else {
			PackageDatabase database = PackageDatabase.getInstance(getApplicationContext());
			database.add(getPackage());
			database.save();
		}
		finish();
	}

	@Override
	public void onBackPressed() {
		if (nowStep == STEP_NO_FOUND || nowStep == STEP_NO_INTERNET_CONNECTION || nowStep == STEP_SUCCESS) {
			step(STEP_INPUT);
		} else {
			super.onBackPressed();
		}
	}

}
