package info.papdt.express.helper.ui.fragment.add;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import info.papdt.express.helper.R;
import info.papdt.express.helper.ui.AddActivity;
import info.papdt.express.helper.ui.common.AbsFragment;
import info.papdt.express.helper.widget.ButtonBar;

abstract class AbsStepFragment extends AbsFragment {

	public final static int REQUEST_CODE_CHOOSE_COMPANY = 1001;
	public final static String RESULT_EXTRA_COMPANY_CODE = "company_code";

	protected ButtonBar mButtonBar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle state) {
		rootView = inflater.inflate(this.getLayoutResId(), group, false);
		mButtonBar = (ButtonBar) rootView.findViewById(R.id.button_bar);
		this.doCreateView(rootView);
		return rootView;
	}

	public AddActivity getAddActivity() {
		return (AddActivity) super.getActivity();
	}

}
