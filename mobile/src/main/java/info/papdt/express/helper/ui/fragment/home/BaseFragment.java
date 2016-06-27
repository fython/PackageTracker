package info.papdt.express.helper.ui.fragment.home;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import info.papdt.express.helper.R;
import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.ui.common.AbsFragment;

public abstract class BaseFragment extends AbsFragment implements SwipeRefreshLayout.OnRefreshListener {

	private SwipeRefreshLayout mRefreshLayout;
	private RecyclerView mRecyclerView;
	private RecyclerView.Adapter mAdapter;

	private PackageDatabase mDatabase;

	private final static int FLAG_REFRESH_LIST = 0, FLAG_UPDATE_ADAPTER_ONLY = 1;

	public BaseFragment(PackageDatabase database) {
		this.mDatabase = database;
	}

	protected BaseFragment() {
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.fragment_home;
	}

	@Override
	protected void doCreateView(View rootView) {
		mRefreshLayout = $(R.id.refresh_layout);
		mRecyclerView = $(R.id.recycler_view);

		// Set up mRecyclerView
		mRecyclerView.setHasFixedSize(false);
		mRecyclerView.setLayoutManager(
				new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
		);

		// Set up mRefreshLayout
		mRefreshLayout.setColorSchemeResources(R.color.pink_700);
		mRefreshLayout.setOnRefreshListener(this);

		setUpAdapter();
	}

	protected abstract void setUpAdapter();

	@Override
	public void onRefresh() {
		mHandler.sendEmptyMessage(FLAG_REFRESH_LIST);
	}

	public void scrollToTop() {
		if (mAdapter != null && mAdapter.getItemCount() > 0) {
			mRecyclerView.smoothScrollToPosition(0);
		}
	}

	protected void setAdapter(RecyclerView.Adapter adapter) {
		this.mAdapter = adapter;
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case FLAG_REFRESH_LIST:
					if (!mRefreshLayout.isRefreshing()) {
						mRefreshLayout.setRefreshing(true);
					}
					new RefreshTask().execute();
					break;
				case FLAG_UPDATE_ADAPTER_ONLY:
					mAdapter.notifyDataSetChanged();
					break;
			}
		}
	};

	public class RefreshTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... voids) {
			mDatabase.pullDataFromNetwork(false);
			return null;
		}

		@Override
		protected void onPostExecute(Void msg) {
			mRefreshLayout.setRefreshing(false);
			mHandler.sendEmptyMessage(FLAG_UPDATE_ADAPTER_ONLY);
		}

	}

}
