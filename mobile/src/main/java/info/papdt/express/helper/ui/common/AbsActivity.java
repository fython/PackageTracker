package info.papdt.express.helper.ui.common;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import info.papdt.express.helper.R;
import info.papdt.express.helper.support.Settings;

public abstract class AbsActivity extends AppCompatActivity {

	protected Toolbar mToolbar;
	protected ActionBar mActionBar;
	private Settings mSettings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSettings = Settings.getInstance(getApplicationContext());
	}

	@Override
	public void setContentView(@LayoutRes int layoutResId) {
		super.setContentView(layoutResId);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar != null) {
			setSupportActionBar(mToolbar);
			mActionBar = getSupportActionBar();
		}
		setUpViews();
	}

	public int getNightMode() {
		return getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
	}

	public boolean isNightMode() {
		return getNightMode() == Configuration.UI_MODE_NIGHT_YES;
	}

	protected abstract void setUpViews();

	@Nullable
	protected <T extends View> T $(int viewId) {
		return (T) findViewById(viewId);
	}

	protected Settings getSettings() {
		return this.mSettings;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			this.onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
