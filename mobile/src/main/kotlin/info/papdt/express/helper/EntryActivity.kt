package info.papdt.express.helper

import android.app.Activity
import android.content.Intent
import android.os.Bundle

import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.ui.MainActivity
import info.papdt.express.helper.ui.SplashActivity

class EntryActivity : Activity() {

	public override fun onCreate(savedInstanceState: Bundle?) {
		val mSettings = Settings.getInstance(applicationContext)

		super.onCreate(savedInstanceState)

		/** Open activity  */
		if (mSettings.getBoolean(Settings.KEY_FIRST_RUN, true)) {
			mSettings.putBoolean(Settings.KEY_FIRST_RUN, false)
			val intent = Intent(this, SplashActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			startActivity(intent)
		} else {
			MainActivity.launch(this)
		}

		finish()
	}

}
