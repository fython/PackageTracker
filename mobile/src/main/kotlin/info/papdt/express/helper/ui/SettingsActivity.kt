package info.papdt.express.helper.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment

import info.papdt.express.helper.R
import info.papdt.express.helper.ui.common.AbsActivity
import info.papdt.express.helper.ui.fragment.settings.*

class SettingsActivity : AbsActivity() {

	private lateinit var action: String

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
	}

	fun makeSnackbar(message: String, duration: Int): Snackbar {
		return Snackbar.make(findViewById(R.id.container), message, duration)
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
