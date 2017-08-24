package info.papdt.express.helper.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import info.papdt.express.helper.R;
import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.services.DetectNumberService;
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
	private AppBarLayout mAppBarLayout;
	private View mAppBarBackground, mAppBarTitle, mAppBarSmallTitle;

	private Package pack;
	private String number;

	private String preName, preNumber, preCompany;

	private boolean isFromMainActivity = false;

	private int nowStep;

	public static final String EXTRA_PRE_NUMBER = "pre_number",
			EXTRA_PRE_COMPANY = "pre_company", EXTRA_PRE_NAME = "pre_name";

	public static final String RESULT_EXTRA_PACKAGE_JSON = "package_json";

	public static final String EXTRA_IS_FROM_MAIN_ACTIVITY = "is_from_main", EXTRA_HAS_PREINFO = "has_pre_info";

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

		Intent intent = getIntent();

		if (intent.hasExtra(EXTRA_IS_FROM_MAIN_ACTIVITY)
				&& intent.getBooleanExtra(EXTRA_IS_FROM_MAIN_ACTIVITY, false)) {
			isFromMainActivity = true;
		}

		if (intent.hasExtra(EXTRA_HAS_PREINFO)
				&& intent.getBooleanExtra(EXTRA_HAS_PREINFO, false)) {
			preNumber = intent.getStringExtra(EXTRA_PRE_NUMBER);
			preCompany = intent.getStringExtra(EXTRA_PRE_COMPANY);
			preName = intent.getStringExtra(EXTRA_PRE_NAME);
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(DetectNumberService.Companion.getNOTIFICATION_ID_ASSIST());
		}

		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);
		((ViewGroup.MarginLayoutParams) mToolbar.getLayoutParams()).topMargin +=
				ScreenUtils.getStatusBarHeight(this);

		addStep(STEP_INPUT);

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

		setExpanded(getResources().getConfiguration().screenHeightDp > 480);
	}

	@Override
	protected void setUpViews() {
		mProgressBar = $(R.id.progress_bar);
		mAppBarLayout = $(R.id.app_bar_layout);
		mAppBarBackground = $(R.id.parallax_background);
		mAppBarTitle = $(R.id.title_view);
		mAppBarSmallTitle = $(R.id.small_title_view);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setExpanded(newConfig.screenHeightDp > 480);
	}

	private void setExpanded(boolean shouldExpand) {
		mAppBarBackground.setVisibility(shouldExpand ? View.VISIBLE : View.GONE);
		mAppBarTitle.setVisibility(shouldExpand ? View.VISIBLE : View.INVISIBLE);
		mAppBarSmallTitle.setVisibility(shouldExpand ? View.INVISIBLE : View.VISIBLE);
	}

	public void addStep(int step) {
		nowStep = step;

		FragmentTransaction fm = getFragmentManager().beginTransaction();

		/** Set Animation */
		fm.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

		switch (step) {
			case STEP_INPUT:
				if (mStepInput == null) {
					mStepInput = new StepInput();
				}
				fm.replace(R.id.container, mStepInput).addToBackStack(null).commit();
				break;
			case STEP_NO_INTERNET_CONNECTION:
				if (mStepNoInternetConnection == null) {
					mStepNoInternetConnection = new StepNoInternetConnection();
				}
				fm.replace(R.id.container, mStepNoInternetConnection).addToBackStack(null).commit();
				break;
			case STEP_NO_FOUND:
				if (mStepNoFound == null) {
					mStepNoFound = new StepNoFound();
				}
				fm.replace(R.id.container, mStepNoFound).addToBackStack(null).commit();
				break;
			case STEP_SUCCESS:
				fm.replace(R.id.container, new StepSuccess()).addToBackStack(null).commit();
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
		if (getFragmentManager().getBackStackEntryCount() > 0) {
			getFragmentManager().popBackStackImmediate();
			if (getFragmentManager().getBackStackEntryCount() <= 0) {
				super.onBackPressed();
			}
		} else {
			super.onBackPressed();
		}
	}

	public static void launch(Context context, String company, String number, String name) {
		Intent intent = new Intent(context, AddActivity.class);
		intent.putExtra(EXTRA_HAS_PREINFO, true);
		intent.putExtra(EXTRA_PRE_NUMBER, number);
		intent.putExtra(EXTRA_PRE_COMPANY, company);
		if (name != null) {
			intent.putExtra(EXTRA_PRE_NAME, name);
		}
		context.startActivity(intent);
	}

	public String getPreName() {
		return preName;
	}

	public String getPreNumber() {
		return preNumber;
	}

	public String getPreCompany() {
		return preCompany;
	}

}
