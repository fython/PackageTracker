package info.papdt.express.helper.ui;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;

import java.util.ArrayList;

import info.papdt.express.helper.R;
import info.papdt.express.helper.api.PackageApi;
import info.papdt.express.helper.asynctask.CompanyFilterTask;
import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.support.Settings;
import info.papdt.express.helper.ui.adapter.SearchResultAdapter;
import info.papdt.express.helper.ui.common.AbsActivity;

public class SearchActivity extends AbsActivity {

	private RecyclerView mList;
	private AppCompatEditText mSearchEdit;
	private View rootLayout;

	private SearchResultAdapter mAdapter;

	private ArrayList<PackageApi.CompanyInfo.Company> companies;
	private ArrayList<Package> packages;

	private PackageDatabase mDatabase;

	private static final String EXTRA_CX = "cx", EXTRA_CY = "cy";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getSettings().getBoolean(Settings.KEY_NAVIGATION_TINT, true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setNavigationBarColor(getResources().getColor(R.color.lollipop_status_bar_grey));
		}

		setContentView(R.layout.activity_search);

		if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			rootLayout.setVisibility(View.INVISIBLE);

			ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
			if (viewTreeObserver.isAlive()) {
				viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
									overridePendingTransition(R.anim.do_not_move, R.anim.do_not_move);
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
								}
								circularRevealActivity();
							}
						}, 50);
						rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					}
				});
			}
		}
	}

	@Override
	protected void setUpViews() {
		mList = $(R.id.recycler_view);
		mSearchEdit = new AppCompatEditText(this);
		rootLayout = $(R.id.root_layout);

		/** Create search edit widget */
		mSearchEdit.setTextAppearance(this, R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);
		mSearchEdit.setSingleLine(true);
		mSearchEdit.setBackgroundColor(Color.TRANSPARENT);
		mSearchEdit.setHint(R.string.search_hint_common);
		mSearchEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
		mSearchEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (charSequence.length() > 0) {
					companies = null;
					packages = null;
				} else {
					companies = new ArrayList<>();
					packages = new ArrayList<>();
				}
				mAdapter.setCompanies(companies);
				mAdapter.setPackages(packages);
				mAdapter.setItems(buildItems());
				mAdapter.notifyDataSetChanged();
				if (charSequence.length() > 0) {
					new CompanySearchTask().execute(charSequence.toString().trim());
					new PackageSearchTask().execute(charSequence.toString().trim());
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});

		/** Set up custom view on ActionBar */
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mSearchEdit.setLayoutParams(lp);

		ActionBar.LayoutParams lp2 = new ActionBar.LayoutParams(lp);
		mActionBar.setCustomView(mSearchEdit, lp2);

		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);

		/** Set up company list */
		mList.setHasFixedSize(true);
		mList.setLayoutManager(new LinearLayoutManager(this));
		mAdapter = new SearchResultAdapter(this);

		mAdapter.setCompanies(null);
		mAdapter.setPackages(null);

		mList.setAdapter(mAdapter);
	}

	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	private void circularRevealActivity() {
		Intent intent = getIntent();

		int cx = intent.getIntExtra(EXTRA_CX, rootLayout.getWidth() / 2);
		int cy = intent.getIntExtra(EXTRA_CY, rootLayout.getHeight() / 2);

		float finalRadius = Math.max(rootLayout.getWidth(), rootLayout.getHeight());

		// create the animator for this view (the start radius is zero)
		Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, 0, finalRadius);
		circularReveal.setDuration(300);

		// make the view visible and start the animation
		rootLayout.setVisibility(View.VISIBLE);
		circularReveal.start();
	}

	private synchronized ArrayList<SearchResultAdapter.ItemType> buildItems() {
		ArrayList<SearchResultAdapter.ItemType> items = new ArrayList<>();
		items.add(new SearchResultAdapter.ItemType(SearchResultAdapter.ItemType.TYPE_SUBHEADER));
		if (packages != null && packages.size() > 0) {
			for (int i = 0; i < packages.size(); i++) {
				SearchResultAdapter.ItemType item = new SearchResultAdapter.ItemType(SearchResultAdapter.ItemType.TYPE_PACKAGE);
				item.index = i;
				items.add(item);
			}
		} else {
			items.add(new SearchResultAdapter.ItemType(SearchResultAdapter.ItemType.TYPE_EMPTY));
		}
		items.add(new SearchResultAdapter.ItemType(SearchResultAdapter.ItemType.TYPE_SUBHEADER));
		if (companies != null && companies.size() > 0) {
			for (int i = 0; i < companies.size(); i++) {
				SearchResultAdapter.ItemType item = new SearchResultAdapter.ItemType(SearchResultAdapter.ItemType.TYPE_COMPANY);
				item.index = i;
				items.add(item);
			}
		} else {
			items.add(new SearchResultAdapter.ItemType(SearchResultAdapter.ItemType.TYPE_EMPTY));
		}
		return items;
	}

	public static void launch(AppCompatActivity activity, int cx, int cy) {
		Intent intent = new Intent(activity, SearchActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(EXTRA_CX, cx);
		intent.putExtra(EXTRA_CY, cy);
		activity.startActivity(intent);
	}

	private class CompanySearchTask extends CompanyFilterTask {

		@Override
		protected void onPostExecute(ArrayList<PackageApi.CompanyInfo.Company> companies) {
			SearchActivity.this.companies = companies;
			mAdapter.setCompanies(companies);
			mAdapter.setItems(buildItems());
			mAdapter.notifyDataSetChanged();
		}

	}

	private class PackageSearchTask extends AsyncTask<String, Void, ArrayList<Package>> {

		@Override
		protected ArrayList<Package> doInBackground(String... str) {
			if (mDatabase == null) {
				mDatabase = PackageDatabase.getInstance(getApplicationContext());
			}

			String keyword = str[0].trim().toLowerCase();

			ArrayList<Package> result = new ArrayList<>();
			for (int i = 0; i < mDatabase.size(); i++) {
				if (mDatabase.get(i).name.toLowerCase().contains(keyword)
						|| mDatabase.get(i).number.toLowerCase().contains(keyword)) {
					result.add(mDatabase.get(i));
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(ArrayList<Package> packages) {
			SearchActivity.this.packages = packages;
			mAdapter.setPackages(packages);
			mAdapter.setItems(buildItems());
			mAdapter.notifyDataSetChanged();
		}

	}

}
