package info.papdt.express.helper

import android.app.Activity
import android.content.Intent
import android.os.Bundle

import info.papdt.express.helper.support.SettingsInstance
import info.papdt.express.helper.ui.HomeActivity
import info.papdt.express.helper.ui.SplashActivity

class EntryActivity : Activity() {

	public override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		/** Open activity  */
		if (SettingsInstance.firstRun) {
			SettingsInstance.firstRun = false
			val intent = Intent(this, SplashActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			startActivity(intent)
		} else {
			// MainActivity.launch(this)
			startActivity(Intent(this, HomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
		}

		finish()
	}

}
