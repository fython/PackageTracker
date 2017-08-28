package info.papdt.express.helper.ui.fragment.add

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import info.papdt.express.helper.R
import info.papdt.express.helper.api.PackageApi.CompanyInfo
import info.papdt.express.helper.api.RxPackageApi
import info.papdt.express.helper.model.BaseMessage
import info.papdt.express.helper.model.Package
import info.papdt.express.helper.receiver.ConnectivityReceiver
import info.papdt.express.helper.ui.CompanyChooserActivity
import moe.feng.common.stepperview.VerticalStepperItemView
import moe.feng.kotlinyan.common.AndroidExtensions
import moe.feng.kotlinyan.common.AppCompatExtensions

class AddDialogFragment: DialogFragment(), AndroidExtensions, AppCompatExtensions {

	private lateinit var step0: VerticalStepperItemView
	private lateinit var step1: VerticalStepperItemView
	private lateinit var step2: VerticalStepperItemView
	private lateinit var currentCompanyText: TextView
	private lateinit var step1NextButton: Button
	private lateinit var loadingLayout: View
	private lateinit var detectErrorView: View
	private lateinit var detectTryAgainButton: Button
	private val addErrorView by lazy { contentView.findViewById<View>(R.id.error_layout_add) }
	private val addErrorMsg by lazy { contentView.findViewById<TextView>(R.id.add_error_message_text) }
	private val addErrorDesc by lazy { contentView.findViewById<TextView>(R.id.add_error_desc_text) }
	private val addLoadingView by lazy { contentView.findViewById<View>(R.id.loading_layout_add) }
	private val addFinishLayout by lazy { contentView.findViewById<View>(R.id.set_name_layout) }

	private var currentStep = 0

	private var companyCode: String = ""

	private var result: Package? = null

	private val number get() = arguments.getString(ARG_NUMBER)

	private lateinit var contentView: View

	private fun createView(inflater: LayoutInflater): View {
		val view = inflater.inflate(R.layout.fragment_add_dialog, null)
		contentView = view

		step0 = view.findViewById(R.id.stepper_detect_company)
		step1 = view.findViewById(R.id.stepper_choose_company)
		step2 = view.findViewById(R.id.stepper_find_package)
		currentCompanyText = view.findViewById(R.id.tv_current_company)
		step1NextButton = view.findViewById(R.id.choose_company_next_btn)
		loadingLayout = view.findViewById(R.id.loading_layout)
		detectErrorView = view.findViewById(R.id.error_text)
		detectTryAgainButton = view.findViewById(R.id.stepper_try_again)

		VerticalStepperItemView.bindSteppers(step0, step1, step2)

		step0.summary = resources.string[R.string.stepper_detect_company_summary].format(number)

		step1NextButton.setOnClickListener { step1.nextStep() }
		view.findViewById<Button>(R.id.choose_company_change_btn).setOnClickListener {
			val intent = Intent(activity, CompanyChooserActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
			startActivityForResult(intent, AbsStepFragment.REQUEST_CODE_CHOOSE_COMPANY)
		}
		detectTryAgainButton.setOnClickListener { doStep() }
		view.findViewById<Button>(R.id.choose_company_next_btn).setOnClickListener {
			currentStep = 2
			step1.nextStep()
			doStep()
		}
		view.findViewById<Button>(R.id.try_again_btn_step_2).setOnClickListener { doStep() }
		view.findViewById<Button>(R.id.back_button_step_2).setOnClickListener { step2.prevStep() }
		view.findViewById<Button>(R.id.back_button_step_2_2).setOnClickListener { step2.prevStep() }

		doStep()

		return view
	}

	private fun doStep() {
		when (currentStep) {
			0 -> {
				if (ConnectivityReceiver.readNetworkState(activity)) {
					RxPackageApi.detectCompany(number, activity)
							.doOnSubscribe {
								detectErrorView.makeGone()
								detectTryAgainButton.makeGone()
								loadingLayout.makeVisible()
							}
							.subscribe {
								step0.setErrorText(0)
								currentStep = 1
								step0.nextStep()
								it?.let(this::setCompany)
							}
				} else {
					step0.setErrorText(R.string.message_no_internet_connection)
					detectErrorView.makeVisible()
					detectTryAgainButton.makeVisible()
					loadingLayout.makeGone()
				}
			}
			2 -> {
				if (ConnectivityReceiver.readNetworkState(activity)) {
					RxPackageApi.getPackage(number, companyCode, activity)
							.doOnSubscribe {
								addLoadingView.makeVisible()
								addErrorView.makeGone()
								addFinishLayout.makeGone()
								step2.setErrorText(0)
							}
							.subscribe {
								if (it.code == BaseMessage.CODE_OKAY && it.data?.state != Package.STATUS_FAILED) {
									addErrorView.makeGone()
									addLoadingView.makeGone()
									addFinishLayout.makeVisible()
									result = it.data
									contentView.findViewById<TextView>(R.id.add_message_text).text =
											resources.string[R.string.message_successful_format]
													.format(number, currentCompanyText.text)
								} else {
									addErrorView.makeVisible()
									addLoadingView.makeGone()
									addFinishLayout.makeGone()
									addErrorMsg.setText(R.string.message_no_found)
									addErrorDesc.setText(R.string.description_no_found)
									step2.setErrorText(R.string.message_no_found)
								}
							}
				} else {
					addLoadingView.makeGone()
					addErrorView.makeVisible()
					addFinishLayout.makeGone()
					addErrorMsg.setText(R.string.message_no_internet_connection)
					addErrorDesc.setText(R.string.description_no_internet_connection)
					step2.setErrorText(R.string.message_no_internet_connection)
				}
			}
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return AlertDialog.Builder(activity)
				.setTitle(R.string.activity_add)
				.setView(createView(activity.layoutInflater))
				.setNegativeButton(android.R.string.cancel) { _, _ -> }
				.create()
	}

	private fun setCompany(company: String) {
		this.companyCode = company
		currentCompanyText.text = if (company.isNotBlank())
			CompanyInfo.getNameByCode(company) else resources.string[R.string.stepper_company_cannot_detect]
		step1NextButton.isEnabled = company.isNotBlank()
		step1NextButton.backgroundTintList = ColorStateList.valueOf(
				if (company.isNotBlank())
					resources.color[R.color.colorAccent] else resources.color[R.color.grey_300]
		)
		step1.setErrorText(if (company.isBlank()) R.string.stepper_company_cannot_detect else 0)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
		if (requestCode == AbsStepFragment.Companion.REQUEST_CODE_CHOOSE_COMPANY) {
			if (resultCode == Activity.RESULT_OK) {
				intent!![AbsStepFragment.Companion.RESULT_EXTRA_COMPANY_CODE]?.asString()?.let(this::setCompany)
			}
		}
	}

	companion object {

		private const val ARG_NUMBER = "number"

		fun newInstance(number: String) = AddDialogFragment().apply {
			arguments = Bundle().apply { putString(ARG_NUMBER, number) }
		}

	}

}