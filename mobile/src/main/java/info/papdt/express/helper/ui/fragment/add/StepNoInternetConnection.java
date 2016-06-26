package info.papdt.express.helper.ui.fragment.add;

import android.view.View;

import info.papdt.express.helper.R;
import info.papdt.express.helper.ui.AddActivity;
import info.papdt.express.helper.widget.ButtonBar;

public class StepNoInternetConnection extends AbsStepFragment {

	@Override
	protected int getLayoutResId() {
		return R.layout.fragment_add_step_no_internet;
	}

	@Override
	protected void doCreateView(View rootView) {
		ButtonBar buttonBar = $(R.id.button_bar);
		buttonBar.setOnLeftButtonClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getAddActivity().step(AddActivity.STEP_INPUT);
			}
		});
	}

}
