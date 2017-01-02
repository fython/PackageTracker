package info.papdt.express.helper.ui;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.ImageView;

import info.papdt.express.helper.R;
import info.papdt.express.helper.support.MaterialImageLoader;
import info.papdt.express.helper.ui.common.AbsActivity;

public class SplashActivity extends AbsActivity {
	private ImageView mLogoView;
	private AppCompatTextView mTitleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().getDecorView().setSystemUiVisibility(
					Build.VERSION.SDK_INT < Build.VERSION_CODES.M
							? View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							: View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
			);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				getWindow().setStatusBarColor(Color.TRANSPARENT);
			} else {
				getWindow().setStatusBarColor(getResources().getColor(R.color.lollipop_status_bar_grey));
			}
			getWindow().setNavigationBarColor(getResources().getColor(R.color.lollipop_status_bar_grey));
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				MainActivity.launch(SplashActivity.this);
				finish();
			}
		}, 3000);
	}

	@Override
	protected void setUpViews() {
		mLogoView = $(R.id.iv_logo);
		mTitleView = $(R.id.tv_title);

		MaterialImageLoader.animate(mLogoView).setDuration(1000).start();
		mTitleView.animate().alpha(1f).setDuration(500).start();
	}

}
