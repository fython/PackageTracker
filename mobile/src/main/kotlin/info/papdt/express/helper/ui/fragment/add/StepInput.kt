package info.papdt.express.helper.ui.fragment.add

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast

import com.rengwuxian.materialedittext.MaterialEditText

import info.papdt.express.helper.R
import info.papdt.express.helper.api.RxPackageApi
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.model.BaseMessage
import info.papdt.express.helper.model.Package
import info.papdt.express.helper.receiver.ConnectivityReceiver
import info.papdt.express.helper.ui.AddActivity
import info.papdt.express.helper.ui.ScannerActivity
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import moe.feng.kotlinyan.common.findNonNullView

class StepInput : AbsStepFragment() {

	private val mEditText: MaterialEditText by findNonNullView(R.id.et_number)

	private var number: String? = null

	override fun getLayoutResId(): Int {
		return R.layout.fragment_add_step_input
	}

	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		mEditText.setRawInputType(InputType.TYPE_CLASS_NUMBER)
		`$`<View>(R.id.btn_scan)!!.setOnClickListener {
			val intent = Intent(addActivity, ScannerActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
			startActivityForResult(intent, ScannerActivity.REQUEST_CODE_SCAN)
		}

		if (!TextUtils.isEmpty(addActivity.preNumber)) {
			mEditText.setText(addActivity.preNumber)
		}

		mEditText.setOnEditorActionListener { _, i, _ ->
			if (i == EditorInfo.IME_ACTION_DONE) {
				mButtonBar.onRightButtonClick()
			}
			false
		}
		mButtonBar.setOnRightButtonClickListener(View.OnClickListener {
			mEditText.setText(mEditText.text.toString().trim { it <= ' ' })
			if (!checkNumberInput()) {
				Toast.makeText(context, R.string.toast_number_wrong, Toast.LENGTH_SHORT).show()
				return@OnClickListener
			}

			if (checkExistance()) {
				Toast.makeText(context, R.string.toast_number_exist, Toast.LENGTH_SHORT).show()
				return@OnClickListener
			}

			// Pass check
			number = mEditText.text.toString()
			if (ConnectivityReceiver.readNetworkState(activity)) {
				if (TextUtils.isEmpty(addActivity.preCompany)) {
					RxPackageApi.getPackage(number = number!!, parentActivity = activity).start()
				} else {
					RxPackageApi.filterCompany(addActivity.preCompany!!).subscribe {
						if (it.size == 1) {
							RxPackageApi.getPackage(number = number!!, com = it[0].code, parentActivity = activity)
						} else {
							RxPackageApi.getPackage(number = number!!, parentActivity = activity)
						}.start()
					}
				}
			} else {
				addActivity.addStep(AddActivity.STEP_NO_INTERNET_CONNECTION)
			}
		})
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
		if (requestCode == ScannerActivity.REQUEST_CODE_SCAN) {
			if (resultCode == ScannerActivity.RESULT_GET_RESULT) {
				mEditText.setText(intent!![ScannerActivity.EXTRA_RESULT]?.asString())
				mButtonBar.onRightButtonClick()
			}
		}
	}

	private fun checkNumberInput(): Boolean {
		return mEditText.text.toString().trim { it <= ' ' }.length > 4
	}

	private fun checkExistance(): Boolean {
		return PackageDatabase.getInstance(context!!).indexOf(mEditText.text.toString().trim { it <= ' ' }) != -1
	}

	private fun Observable<BaseMessage<out Package?>>.start(): Disposable {
		return doOnSubscribe {
			addActivity.showProgressBar()
			mEditText.isEnabled = false
		}.subscribe { message ->
			addActivity.hideProgressBar()
			mEditText.isEnabled = true
			if (message.code == BaseMessage.CODE_OKAY) {
				val p = message.data
				addActivity.number = number
				addActivity.`package` = p
				if (p?.status == "200") {
					addActivity.addStep(AddActivity.STEP_SUCCESS)
				} else {
					Toast.makeText(context, p?.message, Toast.LENGTH_SHORT).show()
					addActivity.addStep(AddActivity.STEP_NO_FOUND)
				}
			} else {
				addActivity.addStep(AddActivity.STEP_NO_FOUND)
			}
		}
	}

}
