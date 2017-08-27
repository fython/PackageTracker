package info.papdt.express.helper.ui.fragment.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import info.papdt.express.helper.R
import info.papdt.express.helper.ui.AddActivity
import info.papdt.express.helper.ui.common.AbsFragment
import info.papdt.express.helper.widget.ButtonBar

abstract class AbsStepFragment : AbsFragment() {

	protected lateinit var mButtonBar: ButtonBar

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		retainInstance = true
	}

	override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View? {
		rootView = inflater.inflate(this.getLayoutResId(), parent, false)
		mButtonBar = rootView!!.findViewById(R.id.button_bar)
		return rootView
	}

	val addActivity: AddActivity
		get() = super.getActivity() as AddActivity

	companion object {

		const val REQUEST_CODE_CHOOSE_COMPANY = 1001
		const val RESULT_EXTRA_COMPANY_CODE = "company_code"

	}

}
