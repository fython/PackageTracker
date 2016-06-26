package info.papdt.express.helper.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import info.papdt.express.helper.R;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.ui.common.AbsActivity;
import info.papdt.express.helper.ui.fragment.add.StepInput;
import info.papdt.express.helper.ui.fragment.add.StepNoInternetConnection;

public class AddActivity extends AbsActivity{

	public static final int STEP_INPUT = 0, STEP_NO_INTERNET_CONNECTION = 1, STEP_NO_FOUND = 2,
			STEP_SUCCESS = 3;

	private Fragment mStepInput, mStepNoInternetConnection;
	private ProgressBar mProgressBar;

	private Package pack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			);
			getWindow().setStatusBarColor(Color.TRANSPARENT);
		}
		super.onCreate(savedInstanceState);
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
				break;
			case STEP_SUCCESS:
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

}
