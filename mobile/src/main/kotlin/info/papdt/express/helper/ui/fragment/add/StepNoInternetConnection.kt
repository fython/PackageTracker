package info.papdt.express.helper.ui.fragment.add

import android.os.Bundle
import android.view.View

import info.papdt.express.helper.R

class StepNoInternetConnection : AbsStepFragment() {

	override fun getLayoutResId(): Int {
		return R.layout.fragment_add_step_no_internet
	}

	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		mButtonBar.setOnLeftButtonClickListener(View.OnClickListener { addActivity.onBackPressed() })
	}

}
