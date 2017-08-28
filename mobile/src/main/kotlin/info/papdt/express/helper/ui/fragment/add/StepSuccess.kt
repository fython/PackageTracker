package info.papdt.express.helper.ui.fragment.add

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.AppCompatTextView
import android.text.TextUtils
import android.view.View
import android.widget.Toast

import com.rengwuxian.materialedittext.MaterialEditText

import info.papdt.express.helper.R
import info.papdt.express.helper.api.RxPackageApi
import info.papdt.express.helper.model.BaseMessage
import info.papdt.express.helper.model.Package
import info.papdt.express.helper.ui.AddActivity
import info.papdt.express.helper.ui.CompanyChooserActivity
import moe.feng.kotlinyan.common.findNonNullView

class StepSuccess : AbsStepFragment() {

	private val mMsgText: AppCompatTextView by findNonNullView(R.id.tv_message)
	private val mDescText: AppCompatTextView by findNonNullView(R.id.tv_desc)
	private val mNameEdit: MaterialEditText by findNonNullView(R.id.et_name)

	override fun getLayoutResId(): Int {
		return R.layout.fragment_add_step_success
	}

	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		mButtonBar.setOnLeftButtonClickListener(View.OnClickListener { addActivity.onBackPressed() })
		mButtonBar.setOnRightButtonClickListener(View.OnClickListener {
			if (!TextUtils.isEmpty(mNameEdit.text.toString())) {
				addActivity.`package`!!.name = mNameEdit.text.toString()
			} else {
				addActivity.`package`!!.name = String.format(getString(R.string.package_name_unnamed), addActivity.number!!.substring(0, 4))
			}
			addActivity.finishAdd()
		})
		`$`<View>(R.id.btn_choose_company)!!.setOnClickListener {
			val intent = Intent(activity, CompanyChooserActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
			startActivityForResult(intent, AbsStepFragment.Companion.REQUEST_CODE_CHOOSE_COMPANY)
		}

		val p = addActivity.`package`
		if (p != null) {
			updateUIContent(p)
		} else {
			addActivity.addStep(AddActivity.STEP_NO_FOUND)
		}
	}

	private fun updateUIContent(p: Package) {
		mMsgText.text = String.format(getString(R.string.message_successful_format), p.number, p.companyChineseName)
		if (p.data != null) {
			mDescText.text = if (p.data.size > 0) String.format(getString(R.string.description_successful_format), p.data[0].context, p.data[0].time) else p.message
		} else {
			mDescText.text = getString(R.string.message_failure_forced)
		}
		if (!TextUtils.isEmpty(addActivity.preName)) {
			mNameEdit.setText(addActivity.preName)
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
		if (requestCode == AbsStepFragment.Companion.REQUEST_CODE_CHOOSE_COMPANY) {
			if (resultCode == Activity.RESULT_OK) {
				val companyCode = intent?.getStringExtra(AbsStepFragment.Companion.RESULT_EXTRA_COMPANY_CODE)
				RxPackageApi.getPackage(number = addActivity.number!!, com = companyCode,  parentActivity = activity)
						.doOnSubscribe {
							addActivity.showProgressBar()
						}
						.subscribe { message ->
							addActivity.hideProgressBar()
							if (message.code == BaseMessage.CODE_OKAY) {
								val p = message.data
								addActivity.`package` = p
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
