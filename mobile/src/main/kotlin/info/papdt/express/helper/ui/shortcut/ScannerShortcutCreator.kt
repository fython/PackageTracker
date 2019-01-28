package info.papdt.express.helper.ui.shortcut

import android.app.Activity
import android.content.Intent
import android.os.Bundle

import info.papdt.express.helper.R
import info.papdt.express.helper.ui.HomeActivity
import info.papdt.express.helper.ui.ScannerActivity

class ScannerShortcutCreator : Activity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (intent.action == Intent.ACTION_CREATE_SHORTCUT) {
			val intent = Intent()
			val icon = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_scanner_launcher)

			intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.activity_scanner))
			intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon)

			// TODO New Home
			val launchIntent = Intent(applicationContext, HomeActivity::class.java)
			launchIntent.action = ScannerActivity.ACTION_SCAN_TO_ADD
			launchIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

			intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)

			setResult(Activity.RESULT_OK, intent)
		} else {
			setResult(Activity.RESULT_CANCELED)
		}
		finish()
	}

}
