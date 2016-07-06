package info.papdt.express.helper.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import info.papdt.express.helper.R;
import info.papdt.express.helper.model.Package;
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

	private int nowStep;

	public static final String RESULT_EXTRA_PACKAGE_JSON = "package_json";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			);
			getWindow().setStatusBarColor(Color.TRANSPARENT);
		}
		super.onCreate(savedInstanceState);
		if (getSettings().getBoolean(Settings.KEY_NAVIGATION_TINT, true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setNavigationBarColor(getResources().getColor(R.color.brown_500));
		}
		setContentView(R.layout.activity_add);

		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);

		step(STEP_INPUT);
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
		if (getPackage() == null || !getPackage().status.equals("200")) {
			Toast.makeText(this, R.string.toast_unknown_error, Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent();
		intent.putExtra(RESULT_EXTRA_PACKAGE_JSON, getPackage().toJsonString());
		setResult(MainActivity.RESULT_NEW_PACKAGE, intent);
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
