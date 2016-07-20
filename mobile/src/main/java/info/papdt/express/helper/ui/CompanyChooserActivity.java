package info.papdt.express.helper.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import java.util.ArrayList;

import info.papdt.express.helper.R;
import info.papdt.express.helper.api.PackageApi;
import info.papdt.express.helper.support.Settings;
import info.papdt.express.helper.ui.adapter.CompanyListAdapter;
import info.papdt.express.helper.ui.common.AbsActivity;
import info.papdt.express.helper.ui.common.SimpleRecyclerViewAdapter;

public class CompanyChooserActivity extends AbsActivity {

	private RecyclerView mList;
	private AppCompatEditText mSearchEdit;
	private View mEmptyView;

	private CompanyListAdapter mAdapter;
	private ArrayList<PackageApi.CompanyInfo.Company> data;

	public final static String RESULT_EXTRA_COMPANY_CODE = "company_code";

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
		}

		super.onCreate(savedInstanceState);

		if (getSettings().getBoolean(Settings.KEY_NAVIGATION_TINT, true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setNavigationBarColor(getResources().getColor(R.color.lollipop_status_bar_grey));
		}

		data = PackageApi.CompanyInfo.info;

		setContentView(R.layout.activity_choose_company);
	}

	@Override
	protected void setUpViews() {
		mList = $(R.id.recycler_view);
		mSearchEdit = new AppCompatEditText(this);
		mEmptyView = $(R.id.empty_view);

		/** Create search edit widget */
		mSearchEdit.setTextAppearance(this, R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);
		mSearchEdit.setSingleLine(true);
		mSearchEdit.setBackgroundColor(Color.TRANSPARENT);
		mSearchEdit.setHint(R.string.search_hint_company);
		mSearchEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
		mSearchEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				new CompanyFilterTask().execute(charSequence.toString());
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
		mAdapter = new CompanyListAdapter(mList, data);
		mAdapter.setOnItemClickListener(new SimpleRecyclerViewAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position, SimpleRecyclerViewAdapter.ClickableViewHolder holder) {
				Intent intent = new Intent();
				intent.putExtra(RESULT_EXTRA_COMPANY_CODE, mAdapter.getItem(position).code);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		mList.setAdapter(mAdapter);
	}

	class CompanyFilterTask extends info.papdt.express.helper.asynctask.CompanyFilterTask {

		@Override
		public void onPostExecute(ArrayList<PackageApi.CompanyInfo.Company> data) {
			if (data.size() == 0) {
				mEmptyView.setVisibility(View.VISIBLE);
				mList.setVisibility(View.GONE);
			} else {
				mEmptyView.setVisibility(View.GONE);
				mList.setVisibility(View.VISIBLE);
				mAdapter.setList(data);
				mAdapter.notifyDataSetChanged();
			}
		}

	}

}
