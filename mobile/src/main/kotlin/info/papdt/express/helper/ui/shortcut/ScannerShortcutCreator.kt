package info.papdt.express.helper.ui.shortcut

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

import info.papdt.express.helper.R
import info.papdt.express.helper.ui.HomeActivity
import info.papdt.express.helper.ui.ScannerActivity

class ScannerShortcutCreator : Activity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (intent.action == Intent.ACTION_CREATE_SHORTCUT) {
			val icon = IconCompat.createWithResource(this, R.mipmap.ic_scanner_launcher)
			val intent = ShortcutManagerCompat.createShortcutResultIntent(
					this,
					ShortcutInfoCompat.Builder(this, "scanner")
							.setActivity(ComponentName(this, HomeActivity::class.java))
							.setIntent(Intent(ScannerActivity.ACTION_SCAN_TO_ADD)
									.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
									.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
							.setIcon(icon)
							.setShortLabel(getString(R.string.activity_scanner))
							.setLongLabel(getString(R.string.activity_scanner))
							.build()
			)
			setResult(Activity.RESULT_OK, intent)
		} else {
			setResult(Activity.RESULT_CANCELED)
		}
		finish()
	}

}
