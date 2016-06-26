package info.papdt.express.helper.ui.fragment.add;

import info.papdt.express.helper.ui.AddActivity;
import info.papdt.express.helper.ui.common.AbsFragment;

public abstract class AbsStepFragment extends AbsFragment {

	public AddActivity getAddActivity() {
		return (AddActivity) super.getActivity();
	}

}
