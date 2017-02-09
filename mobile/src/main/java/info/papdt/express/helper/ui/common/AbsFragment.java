package info.papdt.express.helper.ui.common;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import info.papdt.express.helper.support.Settings;

public abstract class AbsFragment extends Fragment {

	protected View rootView;
	private Settings mSettings;
	private Context mContext;

	protected abstract @LayoutRes int getLayoutResId();

	protected abstract void doCreateView(View rootView);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSettings = Settings.getInstance(getContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle state) {
		if (parent != null) {
			rootView = inflater.inflate(this.getLayoutResId(), parent, false);
			mContext = parent.getContext();
		} else {
			rootView = inflater.inflate(this.getLayoutResId(), null);
		}
		this.doCreateView(rootView);

		return rootView;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Use activity instead of getApplicationContext in order to prevent memory leak
		mContext = activity;
	}

	@Nullable
	protected <T extends View> T $(@IdRes int viewId) {
		return rootView != null ? (T) rootView.findViewById(viewId) : null;
	}

	@Override
	public Context getContext() {
		return mContext;
	}

	protected Settings getSettings() {
		return mSettings;
	}

}
