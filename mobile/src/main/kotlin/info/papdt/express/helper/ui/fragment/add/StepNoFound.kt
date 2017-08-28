package info.papdt.express.helper.ui.fragment.add

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast

import info.papdt.express.helper.R
import info.papdt.express.helper.api.RxPackageApi
import info.papdt.express.helper.model.BaseMessage
import info.papdt.express.helper.model.Package
import info.papdt.express.helper.ui.AddActivity
import info.papdt.express.helper.ui.CompanyChooserActivity
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import moe.feng.kotlinyan.common.findNonNullView

class StepNoFound : AbsStepFragment() {

	private val mForceBtn: Button by findNonNullView(R.id.btn_force_add)

	override fun getLayoutResId(): Int {
		return R.layout.fragment_add_step_no_found
	}

	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		updateForceButton()
		mButtonBar.setOnLeftButtonClickListener(View.OnClickListener { addActivity.onBackPressed() })
		`$`<View>(R.id.btn_choose_company)!!.setOnClickListener {
			val intent = Intent(activity, CompanyChooserActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
			startActivityForResult(intent, AbsStepFragment.Companion.REQUEST_CODE_CHOOSE_COMPANY)
		}
		`$`<View>(R.id.btn_force_add)!!.setOnClickListener { addActivity.addStep(AddActivity.STEP_SUCCESS) }
	}

	private fun updateForceButton() {
		val p = addActivity.`package`
		if (p?.companyChineseName != null) {
			mForceBtn.isEnabled = true
			mForceBtn.text = String.format(getString(R.string.operation_force_add_when_cannot_find), addActivity.`package`!!.companyChineseName)
			mForceBtn.isEnabled = true
		} else {
			mForceBtn.isEnabled = false
			mForceBtn.text = String.format(getString(R.string.operation_force_add_when_cannot_find), getString(R.string.message_invalid_company))
			mForceBtn.isEnabled = false
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
		if (requestCode == AbsStepFragment.Companion.REQUEST_CODE_CHOOSE_COMPANY) {
			if (resultCode == Activity.RESULT_OK) {
				val companyCode = intent?.getStringExtra(AbsStepFragment.Companion.RESULT_EXTRA_COMPANY_CODE)
				RxPackageApi.getPackage(number = addActivity.number!!, com = companyCode, parentActivity = activity)
						.doOnSubscribe {
							addActivity.showProgressBar()
						}
						.subscribe { message ->
							addActivity.hideProgressBar()
							if (message.code == BaseMessage.CODE_OKAY) {
								val p = message.data
								addActivity.`package` = p
								updateForceButton()
								if (p.status == "200") {
									addActivity.addStep(AddActivity.STEP_SUCCESS)
								} else {
									Toast.makeText(context, p.message, Toast.LENGTH_SHORT).show()
									addActivity.addStep(AddActivity.STEP_NO_FOUND)
								}
							} else {
								addActivity.addStep(AddActivity.STEP_NO_FOUND)
							}
						}
			}
		}
	}
}
