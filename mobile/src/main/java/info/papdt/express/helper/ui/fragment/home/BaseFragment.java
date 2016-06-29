package info.papdt.express.helper.ui.fragment.home;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import info.papdt.express.helper.R;
import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.ui.MainActivity;
import info.papdt.express.helper.ui.adapter.HomePackageListAdapter;
import info.papdt.express.helper.ui.callback.OnDataRemovedCallback;
import info.papdt.express.helper.ui.common.AbsFragment;

public abstract class BaseFragment extends AbsFragment implements SwipeRefreshLayout.OnRefreshListener {

	private SwipeRefreshLayout mRefreshLayout;
	private RecyclerView mRecyclerView;
	private LinearLayout mEmptyView;

	private RecyclerView.Adapter mAdapter;
	private RecyclerViewSwipeManager mSwipeManager;

	private PackageDatabase mDatabase;

	private final static int FLAG_REFRESH_LIST = 0, FLAG_UPDATE_ADAPTER_ONLY = 1;

	public BaseFragment(PackageDatabase database) {
		this.mDatabase = database;
	}

	public BaseFragment() {
		this.mDatabase = PackageDatabase.getInstance(getContext());
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.fragment_home;
	}

	@Override
	protected void doCreateView(View rootView) {
		mRefreshLayout = $(R.id.refresh_layout);
		mRecyclerView = $(R.id.recycler_view);
		mEmptyView = $(R.id.empty_view);

		// Set up mRecyclerView
		mSwipeManager = new RecyclerViewSwipeManager();
		mRecyclerView.setHasFixedSize(false);
		mRecyclerView.setLayoutManager(
				new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
		);

		// Set up mRefreshLayout
		mRefreshLayout.setColorSchemeResources(R.color.pink_700);
		mRefreshLayout.setOnRefreshListener(this);

		setUpAdapter();
		mEmptyView.setVisibility(mAdapter != null && mAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
	}

	protected abstract void setUpAdapter();
	public abstract int getFragmentId();

	@Override
	public void onRefresh() {
		mHandler.sendEmptyMessage(FLAG_REFRESH_LIST);
	}

	public void notifyDataSetChanged() {
		mHandler.sendEmptyMessage(FLAG_UPDATE_ADAPTER_ONLY);
	}

	public void scrollToTop() {
		if (mAdapter != null && mAdapter.getItemCount() > 0) {
			mRecyclerView.smoothScrollToPosition(0);
		}
	}

	protected PackageDatabase getDatabase() {
		return mDatabase;
	}

	protected void setAdapter(RecyclerView.Adapter adapter) {
		this.mAdapter = adapter;
		mRecyclerView.setAdapter(mSwipeManager.createWrappedAdapter(mAdapter));
		mSwipeManager.attachRecyclerView(mRecyclerView);
		mEmptyView.setVisibility(mAdapter != null && mAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);

		/** Set undo operation */
		if (adapter instanceof HomePackageListAdapter) {
			((HomePackageListAdapter) adapter).setOnDataRemovedCallback(new OnDataRemovedCallback() {
				@Override
				public void onDataRemoved(int pos, @Nullable String title) {
					Message msg = new Message();
					msg.what = MainActivity.MSG_NOTIFY_ITEM_REMOVE;
					msg.arg1 = getFragmentId();
					Bundle data = new Bundle();
					data.putString("title", title);
					msg.setData(data);

					getMainActivity().mHandler.sendMessage(msg);

					getMainActivity().notifyDataChanged(getFragmentId());
				}
			});
		}
	}

	protected MainActivity getMainActivity() {
		return (MainActivity) getActivity();
	}

	public void onUndoActionClicked() {
		int position = mDatabase.undoLastRemoval();
		if (position >= 0 && mAdapter != null) {
			mAdapter.notifyItemInserted(position);
			getMainActivity().notifyDataChanged(getFragmentId());
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case FLAG_REFRESH_LIST:
					if (mRefreshLayout != null && !mRefreshLayout.isRefreshing()) {
						mRefreshLayout.setRefreshing(true);
					}
					new RefreshTask().execute();
					break;
				case FLAG_UPDATE_ADAPTER_ONLY:
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
						mEmptyView.setVisibility(mAdapter != null && mAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
					}
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
