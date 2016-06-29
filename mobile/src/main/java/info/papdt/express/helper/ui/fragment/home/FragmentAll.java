package info.papdt.express.helper.ui.fragment.home;

import android.annotation.SuppressLint;
import android.os.Bundle;

import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.ui.adapter.HomePackageListAdapter;

@SuppressLint("ValidFragment")
public class FragmentAll extends BaseFragment {

	private static final String ARG_TYPE = "arg_type";

	public static final int TYPE_ALL = 0, TYPE_DELIVERED = 1, TYPE_DELIVERING = 2;

	public static FragmentAll newInstance(PackageDatabase db, int type) {
		FragmentAll fragment = new FragmentAll(db);
		Bundle data = new Bundle();
		data.putInt(ARG_TYPE, type);
		fragment.setArguments(data);
		return fragment;
	}

	public FragmentAll(PackageDatabase database) {
		super(database);
	}

	public FragmentAll() {
		super();
	}

	@Override
	protected void setUpAdapter() {
		HomePackageListAdapter adapter = new HomePackageListAdapter(getDatabase(), getArguments().getInt(ARG_TYPE));
		setAdapter(adapter);
	}

	@Override
	public int getFragmentId() {
		return getArguments().getInt(ARG_TYPE);
	}

}
