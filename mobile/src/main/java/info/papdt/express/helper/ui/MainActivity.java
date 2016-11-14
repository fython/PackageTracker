package info.papdt.express.helper.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import info.papdt.express.helper.R;
import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.support.PushUtils;
import info.papdt.express.helper.support.Settings;
import info.papdt.express.helper.ui.common.AbsActivity;
import info.papdt.express.helper.ui.fragment.home.BaseFragment;
import info.papdt.express.helper.ui.fragment.home.FragmentAll;
import info.papdt.express.helper.ui.launcher.AppWidgetProvider;

public class MainActivity extends AbsActivity implements OnMenuTabClickListener {

	private BottomBar mBottomBar;
	private BaseFragment[] fragments;

	private PackageDatabase mDatabase;

	public static final int REQUEST_ADD = 10001, RESULT_NEW_PACKAGE = 2000, REQUEST_DETAILS = 10002, RESULT_DELETED = 2001, RESULT_RENAMED = 2002;

	public static final int MSG_NOTIFY_DATA_CHANGED = 1, MSG_NOTIFY_ITEM_REMOVE = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getSettings().getBoolean(Settings.KEY_NAVIGATION_TINT, true)
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isNightMode()) {
			getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
		}

		/** Dirty fix for N */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			PushUtils.startServices(getApplicationContext());
		}

		setContentView(R.layout.activity_main);

		mDatabase = PackageDatabase.getInstance(getApplicationContext());

		fragments = new BaseFragment[]{
				FragmentAll.newInstance(mDatabase, FragmentAll.TYPE_ALL),
				FragmentAll.newInstance(mDatabase, FragmentAll.TYPE_DELIVERED),
				FragmentAll.newInstance(mDatabase, FragmentAll.TYPE_DELIVERING)
		};

		mBottomBar = BottomBar.attach(this, savedInstanceState);
		mBottomBar.setItems(R.menu.bottombar_menu_home);
		mBottomBar.setOnMenuTabClickListener(this);

		if (ScannerActivity.ACTION_SCAN_TO_ADD.equals(getIntent().getAction())) {
			Intent intent = new Intent(MainActivity.this, AddActivity.class);
			intent.setAction(ScannerActivity.ACTION_SCAN_TO_ADD);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivityForResult(intent, REQUEST_ADD);
		}
	}

	@Override
	protected void setUpViews() {
		FloatingActionButton fab = $(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, AddActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivityForResult(intent, REQUEST_ADD);
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mBottomBar.onSaveInstanceState(outState);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mDatabase.save();
	}

	@Override
	public void onMenuTabSelected(@IdRes int menuItemId) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		switch (menuItemId) {
			case R.id.menu_item_all:
				ft.replace(R.id.container, fragments[0]);
				break;
			case R.id.menu_item_delivered:
				ft.replace(R.id.container, fragments[1]);
				break;
			case R.id.menu_item_on_the_way:
				ft.replace(R.id.container, fragments[2]);
				break;
		}
		ft.commit();
	}

	@Override
	public void onMenuTabReSelected(@IdRes int menuItemId) {
		switch (menuItemId) {
			case R.id.menu_item_all:
				fragments[0].scrollToTop();
				break;
			case R.id.menu_item_delivered:
				fragments[1].scrollToTop();
				break;
			case R.id.menu_item_on_the_way:
				fragments[2].scrollToTop();
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_home, menu);

		menu.findItem(R.id.action_search).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			SettingsActivity.launch(this, SettingsActivity.FLAG_MAIN);
			return true;
		} else if (id == R.id.action_read_all) {
			new ReadAllTask().execute();
			return true;
		} else if (id == R.id.action_search) {
			View menuButton = findViewById(id);
			int[] location = new int[2];
			menuButton.getLocationOnScreen(location);
			SearchActivity.launch(this, location[0] + menuButton.getHeight() / 2, location[1] + menuButton.getWidth() / 2);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("Main", "result received, requestCode=" + requestCode + ", resultCode=" + resultCode);
		if (requestCode == REQUEST_ADD) {
			if (resultCode == RESULT_NEW_PACKAGE) {
				String jsonData = data.getStringExtra(AddActivity.RESULT_EXTRA_PACKAGE_JSON);
				Package p = Package.buildFromJson(jsonData);
				if (p != null) {
					Log.i("Main", p.toJsonString());
					mDatabase.add(p);
					this.notifyDataChanged(-1);
				}
			}
		}
		if (requestCode == REQUEST_DETAILS) {
			switch (resultCode) {
				case RESULT_RENAMED:
					notifyDataChanged(-1);
					break;
				case RESULT_DELETED:
					notifyDataChanged(-1);
					final int fragId = mBottomBar.getCurrentTabPosition();
					Snackbar.make(
							$(R.id.coordinator_layout),
							String.format(getString(R.string.toast_item_removed), data.getStringExtra("title")),
							Snackbar.LENGTH_LONG
					)
							.setAction(R.string.toast_item_removed_action, new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									fragments[fragId].onUndoActionClicked();
								}
							})
							.show();
					break;
			}
		}
	}

	public static void launch(Activity activity) {
		Intent intent = new Intent(activity, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(intent);
	}

	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_NOTIFY_DATA_CHANGED:
					AppWidgetProvider.updateManually(getApplication());
					for (int i = 0; i < fragments.length; i++) {
						if (i == msg.arg1) continue; // Skip the fragment which sent message.
						fragments[i].notifyDataSetChanged();
					}
					break;
				case MSG_NOTIFY_ITEM_REMOVE:
					Snackbar.make(
							$(R.id.coordinator_layout),
							String.format(getString(R.string.toast_item_removed), msg.getData().getString("title")),
							Snackbar.LENGTH_LONG
					)
							.setAction(R.string.toast_item_removed_action, new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									fragments[mBottomBar.getCurrentTabPosition()].onUndoActionClicked();
								}
							})
							.show();
					break;
			}
		}
	};

	public void notifyDataChanged(int fromFragId) {
		Message msg = new Message();
		msg.what = MSG_NOTIFY_DATA_CHANGED;
		msg.arg1 = fromFragId;
		mHandler.sendMessage(msg);
	}

	private class ReadAllTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... voids) {
			int count = 0;
			for (int i = 0; i < mDatabase.size(); i++) {
				if (mDatabase.get(i).unreadNew) {
					count++;
					mDatabase.get(i).unreadNew = false;
				}
			}
			mDatabase.save();
			return count;
		}

		@Override
		protected void onPostExecute(Integer count) {
			notifyDataChanged(-1);
			Snackbar.make(
					$(R.id.coordinator_layout),
					getString(R.string.toast_all_read, count),
					Snackbar.LENGTH_LONG
			).show();
		}

	}

}
