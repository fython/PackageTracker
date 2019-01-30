package info.papdt.express.helper.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment

import info.papdt.express.helper.R
import info.papdt.express.helper.support.ResourcesUtils
import info.papdt.express.helper.ui.common.AbsActivity
import info.papdt.express.helper.ui.fragment.settings.*
import info.papdt.express.helper.view.SwipeBackCoordinatorLayout
import moe.feng.kotlinyan.common.lazyFindNonNullView
import org.jetbrains.anko.withAlpha

class SettingsActivity : AbsActivity() {

	private lateinit var action: String

	private val coordinatorLayout: SwipeBackCoordinatorLayout
			by lazyFindNonNullView(R.id.swipe_back_coordinator_layout)
	private val rootLayout: View by lazyFindNonNullView(R.id.root_layout)
	private val rootViewBgColor by lazy {
		ResourcesUtils.getColorIntFromAttr(theme, R.attr.rootViewBackgroundColor)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		action = intent.action ?: ACTION_MAIN

		setContentView(R.layout.activity_settings)
	}

	override fun setUpViews() {
		val f: Fragment
        mActionBar!!.setTitle(R.string.activity_settings)
		when (action) {
			ACTION_MAIN -> {
				f = SettingsMain()
			}
			ACTION_UI -> {
				f = SettingsUi()
			}
			ACTION_PUSH -> {
				f = SettingsPush()
			}
			ACTION_API -> {
				f = SettingsApi()
			}
			ACTION_ASSISTANT -> {
				f = SettingsAssistant()
			}
			ACTION_LICENSES -> {
				supportFragmentManager.beginTransaction().replace(R.id.container, SettingsLicense()).commit()
				mActionBar!!.setTitle(R.string.open_source_license)
				return
			}
			ACTION_CONTRIBUTORS -> {
				f = SettingsContributors()
			}
			else -> throw RuntimeException("Please set a supported action.")
		}
		supportFragmentManager.beginTransaction().replace(R.id.container, f).commit()

		if (f is SwipeBackCoordinatorLayout.OnSwipeListener) {
			coordinatorLayout.setOnSwipeListener(OnSwipeListener(f))
		}
	}

	fun makeSnackbar(message: String, duration: Int): Snackbar {
		return Snackbar.make(findViewById(R.id.container), message, duration)
	}

	inner class OnSwipeListener(listener: SwipeBackCoordinatorLayout.OnSwipeListener)
		: SwipeBackCoordinatorLayout.OnSwipeListener by listener {

		override fun onSwipeProcess(percent: Float) {
			rootLayout.setBackgroundColor(
					rootViewBgColor.withAlpha(
							(SwipeBackCoordinatorLayout.getBackgroundAlpha(percent) * 255)
									.toInt()
					)
			)
		}

        override fun onSwipeFinish(dir: Int) {
            window.statusBarColor = Color.TRANSPARENT
            finish()
        }

	}

	companion object {

		const val ACTION_MAIN = Intent.ACTION_MAIN
		const val ACTION_UI = "info.papdt.express.helper.action.SETTINGS_UI"
		const val ACTION_PUSH = "info.papdt.express.helper.action.SETTINGS_PUSH"
		const val ACTION_API = "info.papdt.express.helper.action.SETTINGS_API"
		const val ACTION_ASSISTANT = "info.papdt.express.helper.action.SETTINGS_ASSISTANT"
		const val ACTION_CONTRIBUTORS = "info.papdt.express.helper.action.SETTINGS_CONTRIBUTORS"
		const val ACTION_LICENSES = "info.papdt.express.helper.action.SETTINGS_LICENSES"

		fun launch(activity: Activity, action: String = ACTION_MAIN) {
			val intent = Intent(activity, SettingsActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
			intent.action = action
			activity.startActivity(intent)
		}

	}

}
