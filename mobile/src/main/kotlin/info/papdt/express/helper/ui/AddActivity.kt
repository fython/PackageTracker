package info.papdt.express.helper.ui

import android.app.Fragment
import android.app.FragmentTransaction
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast

import info.papdt.express.helper.R
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.model.Package
import info.papdt.express.helper.services.DetectNumberService
import info.papdt.express.helper.support.ScreenUtils
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.ui.common.AbsActivity
import info.papdt.express.helper.ui.fragment.add.StepInput
import info.papdt.express.helper.ui.fragment.add.StepNoFound
import info.papdt.express.helper.ui.fragment.add.StepNoInternetConnection
import info.papdt.express.helper.ui.fragment.add.StepSuccess
import moe.feng.kotlinyan.common.AndroidExtensions
import moe.feng.kotlinyan.common.lazyFindNonNullView

class AddActivity : AbsActivity() {

	private val mStepInput: Fragment by lazy { StepInput() }
	private val mStepNoInternetConnection: Fragment by lazy { StepNoInternetConnection() }
	private val mStepNoFound: Fragment by lazy { StepNoFound() }
	private val mProgressBar: ProgressBar by lazyFindNonNullView(R.id.progress_bar)
	private val mAppBarBackground: View by lazyFindNonNullView(R.id.parallax_background)
	private val mAppBarTitle: View by lazyFindNonNullView(R.id.title_view)
	private val mAppBarSmallTitle: View by lazyFindNonNullView(R.id.small_title_view)

	var `package`: Package? = null
	var number: String? = null

	var preName: String? = null
		private set
	var preNumber: String? = null
		private set
	var preCompany: String? = null
		private set

	private var isFromMainActivity = false

	private var nowStep: Int = 0

	override fun onCreate(savedInstanceState: Bundle?) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			window.statusBarColor = Color.TRANSPARENT
		}
		super.onCreate(savedInstanceState)
		if (settings.getBoolean(Settings.KEY_NAVIGATION_TINT, true)
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isNightMode) {
			window.navigationBarColor = resources.color[R.color.brown_500]
		}
		setContentView(R.layout.activity_add)

		if (intent.hasExtra(EXTRA_IS_FROM_MAIN_ACTIVITY) && intent[EXTRA_IS_FROM_MAIN_ACTIVITY]?.asBoolean() == true) {
			isFromMainActivity = true
		}

		if (intent.hasExtra(EXTRA_HAS_PREINFO) && intent[EXTRA_HAS_PREINFO]?.asBoolean() == true) {
			preNumber = intent[EXTRA_PRE_NUMBER]?.asString()
			preCompany = intent[EXTRA_PRE_COMPANY]?.asString()
			preName = intent[EXTRA_PRE_NAME]?.asString()
			notificationManager.cancel(DetectNumberService.NOTIFICATION_ID_ASSIST)
		}

		mActionBar?.setDisplayHomeAsUpEnabled(true)
		mActionBar?.setDisplayShowTitleEnabled(false)
		(mToolbar?.layoutParams as ViewGroup.MarginLayoutParams).topMargin += ScreenUtils.getStatusBarHeight(this)

		addStep(STEP_INPUT)

		if (ScannerActivity.ACTION_SCAN_TO_ADD == intent.action) {
			Handler().postDelayed({
				val intent = Intent(this@AddActivity, ScannerActivity::class.java)
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
				mStepInput.startActivityForResult(intent, ScannerActivity.REQUEST_CODE_SCAN)
			}, 600)
		}

		setExpanded(resources.configuration.screenHeightDp > 480)
	}

	override fun setUpViews() {
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)
		setExpanded(newConfig.screenHeightDp > 480)
	}

	private fun setExpanded(shouldExpand: Boolean) {
		mAppBarBackground.visibility = if (shouldExpand) View.VISIBLE else View.GONE
		mAppBarTitle.visibility = if (shouldExpand) View.VISIBLE else View.INVISIBLE
		mAppBarSmallTitle.visibility = if (shouldExpand) View.INVISIBLE else View.VISIBLE
	}

	fun addStep(step: Int) {
		nowStep = step

		val fm = fragmentManager.beginTransaction()

		/** Set Animation  */
		fm.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

		when (step) {
			STEP_INPUT -> {
				fm.replace(R.id.container, mStepInput).addToBackStack(null).commit()
			}
			STEP_NO_INTERNET_CONNECTION -> {
				fm.replace(R.id.container, mStepNoInternetConnection).addToBackStack(null).commit()
			}
			STEP_NO_FOUND -> {
				fm.replace(R.id.container, mStepNoFound).addToBackStack(null).commit()
			}
			STEP_SUCCESS -> fm.replace(R.id.container, StepSuccess()).addToBackStack(null).commit()
		}
	}

	fun showProgressBar() {
		mProgressBar.visibility = View.VISIBLE
	}

	fun hideProgressBar() {
		mProgressBar.visibility = View.INVISIBLE
	}

	fun finishAdd() {
		if (`package` == null) {
			Toast.makeText(this, R.string.toast_unknown_error, Toast.LENGTH_LONG).show()
			return
		}

		if (isFromMainActivity) {
			val intent = Intent()
			intent.putExtra(RESULT_EXTRA_PACKAGE_JSON, `package`!!.toJsonString())
			setResult(MainActivity.RESULT_NEW_PACKAGE, intent)
		} else {
			val database = PackageDatabase.getInstance(applicationContext)
			database.add(`package`!!)
			database.save()
		}
		finish()
	}

	override fun onBackPressed() {
		if (fragmentManager.backStackEntryCount > 0) {
			fragmentManager.popBackStackImmediate()
			if (fragmentManager.backStackEntryCount <= 0) {
				super.onBackPressed()
			}
		} else {
			super.onBackPressed()
		}
	}

	companion object: AndroidExtensions {

		const val STEP_INPUT = 0
		const val STEP_NO_INTERNET_CONNECTION = 1
		const val STEP_NO_FOUND = 2
		const val STEP_SUCCESS = 3

		const val EXTRA_PRE_NUMBER = "pre_number"
		const val EXTRA_PRE_COMPANY = "pre_company"
		const val EXTRA_PRE_NAME = "pre_name"

		const val RESULT_EXTRA_PACKAGE_JSON = "package_json"

		const val EXTRA_IS_FROM_MAIN_ACTIVITY = "is_from_main"
		const val EXTRA_HAS_PREINFO = "has_pre_info"

		fun launch(context: Context, company: String?, number: String?, name: String?) {
			val intent = Intent(context, AddActivity::class.java)
			intent[EXTRA_HAS_PREINFO] = true
			intent[EXTRA_PRE_NUMBER] = number
			intent[EXTRA_PRE_COMPANY] = company
			intent[EXTRA_PRE_NAME] = name
			context.startActivity(intent)
		}
	}

}
