package info.papdt.express.helper.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

import info.papdt.express.helper.R
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.ui.common.AbsActivity
import info.papdt.express.helper.ui.fragment.settings.*
import moe.feng.kotlinyan.common.*

class SettingsActivity : AbsActivity() {

	private var flag: Int = 0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		if (settings.getBoolean(Settings.KEY_NAVIGATION_TINT, true)
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isNightMode) {
			window.navigationBarColor = resources.color[R.color.colorPrimaryDark]
		}

		flag = intent[EXTRA_SETTINGS_FLAG]?.asInt() ?: FLAG_MAIN

		setContentView(R.layout.activity_settings)
	}

	override fun setUpViews() {
		mActionBar!!.setDisplayHomeAsUpEnabled(true)

		val f: Fragment
		when (flag) {
			FLAG_MAIN -> {
				f = SettingsMain()
				mActionBar!!.setTitle(R.string.activity_settings)
			}
			FLAG_LICENSE -> {
				supportFragmentManager.beginTransaction().replace(R.id.container, SettingsLicense()).commit()
				mActionBar!!.setTitle(R.string.open_source_license)
				return
			}
			FLAG_CONTRIBUTORS -> {
				f = SettingsContributors()
				mActionBar!!.setTitle(R.string.category_contributors)
			}
			else -> throw RuntimeException("Please set flag when launching activity.")
		}
		supportFragmentManager.beginTransaction().replace(R.id.container, f).commit()
	}

	fun makeSnackbar(message: String, duration: Int): Snackbar {
		return Snackbar.make(findViewById(R.id.container), message, duration)
	}

	companion object {

		private const val EXTRA_SETTINGS_FLAG = "extra_flag"

		const val FLAG_MAIN = 0
		const val FLAG_LICENSE = 2
		const val FLAG_CONTRIBUTORS = 4

		fun launch(activity: AppCompatActivity, flag: Int) {
			val intent = Intent(activity, SettingsActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
			intent[EXTRA_SETTINGS_FLAG] = flag
			activity.startActivity(intent)
		}
	}

}
