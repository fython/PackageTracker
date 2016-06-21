package info.papdt.express.helper.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.view.ViewCompat;
import android.view.Menu;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import info.papdt.express.helper.R;
import info.papdt.express.helper.ui.common.AbsActivity;
import info.papdt.express.helper.ui.fragment.home.FragmentAll;

public class MainActivity extends AbsActivity implements OnMenuTabClickListener {

	private BottomBar mBottomBar;
	private Fragment[] fragments;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		fragments = new Fragment[]{new FragmentAll(), new FragmentAll(), new FragmentAll()};

		mBottomBar = BottomBar.attach(this, savedInstanceState);
		mBottomBar.setItems(R.menu.bottombar_menu_home);
		mBottomBar.setOnMenuTabClickListener(this);
	}

	@Override
	protected void setUpViews() {
		ViewCompat.setElevation(mToolbar, getResources().getDimension(R.dimen.app_bar_elevation));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mBottomBar.onSaveInstanceState(outState);
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

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_home, menu);

		menu.findItem(R.id.action_search).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

		return super.onCreateOptionsMenu(menu);
	}

}
