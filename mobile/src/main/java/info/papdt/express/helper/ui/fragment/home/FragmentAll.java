package info.papdt.express.helper.ui.fragment.home;

import android.annotation.SuppressLint;

import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.ui.adapter.HomePackageListAdapter;

@SuppressLint("ValidFragment")
public class FragmentAll extends BaseFragment {

	public FragmentAll(PackageDatabase database) {
		super(database);
	}

	public FragmentAll() {}

	@Override
	protected void setUpAdapter() {
		HomePackageListAdapter adapter = new HomePackageListAdapter(getDatabase());
		setAdapter(adapter);
	}

	@Override
	public int getFragmentId() {
		return 0;
	}

}
