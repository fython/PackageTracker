package info.papdt.express.helper.ui;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import info.papdt.express.helper.ui.common.AbsActivity;
import info.papdt.express.helper.ui.fragment.home.BaseFragment;
import info.papdt.express.helper.ui.fragment.home.FragmentAll;

public class MainActivity extends AbsActivity implements OnMenuTabClickListener {

	private BottomBar mBottomBar;
	private BaseFragment[] fragments;

	private PackageDatabase mDatabase;

	public static final int REQUEST_ADD = 10001, RESULT_NEW_PACKAGE = 2000;

	public static final int MSG_NOTIFY_DATA_CHANGED = 1, MSG_NOTIFY_ITEM_REMOVE = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		FragmentManager fragmentManager = getFragmentManager();
		switch (menuItemId) {
			case R.id.menu_item_all:
				fragmentManager.beginTransaction().replace(R.id.container, fragments[0]).commit();
				break;
			case R.id.menu_item_delivered:
				fragmentManager.beginTransaction().replace(R.id.container, fragments[1]).commit();
				break;
			case R.id.menu_item_on_the_way:
				fragmentManager.beginTransaction().replace(R.id.container, fragments[2]).commit();
				break;
		}
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
	}

	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_NOTIFY_DATA_CHANGED:
					for (int i = 0; i < fragments.length; i++) {
						if (i == msg.arg1) continue; // Skip the fragment which sent message.
						fragments[i].notifyDataSetChanged();
					}
					break;
				case MSG_NOTIFY_ITEM_REMOVE:
					final int fragId = msg.arg1;
					Snackbar.make(
							$(R.id.coordinator_layout),
							String.format(getString(R.string.toast_item_removed), msg.getData().getString("title")),
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
	};

	public void notifyDataChanged(int fromFragId) {
		Message msg = new Message();
		msg.what = MSG_NOTIFY_DATA_CHANGED;
		msg.arg1 = fromFragId;
		mHandler.sendMessage(msg);
	}

}
