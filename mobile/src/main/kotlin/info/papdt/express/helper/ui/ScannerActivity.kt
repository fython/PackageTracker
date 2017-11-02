package info.papdt.express.helper.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.WindowManager

import com.google.zxing.Result

import info.papdt.express.helper.R
import info.papdt.express.helper.ui.common.AbsActivity
import me.dm7.barcodescanner.zxing.ZXingScannerView
import moe.feng.kotlinyan.common.*

class ScannerActivity : AbsActivity(), ZXingScannerView.ResultHandler, PermissionActivity {

	private val mScannerView: ZXingScannerView by lazyFindNonNullView(R.id.scanner_view)
	private val errorView: View by lazyFindNonNullView(R.id.iv_error)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		ifSupportSDK (Build.VERSION_CODES.LOLLIPOP) {
			window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
			window.statusBarColor = Color.TRANSPARENT
		}
		setContentView(R.layout.activity_scanner)

		runWithPermission(Manifest.permission.CAMERA) { startScan() } ?: run {
			errorView.makeVisible()
			buildAlertDialog {
				titleRes = R.string.dialog_explanation_permission_title
				messageRes = R.string.dialog_explanation_permission_message
				positiveButton(R.string.dialog_explanation_permission_pos_btn)  { _, _ -> jumpToSettings() }
				cancelButton()
			}.show()
		}
	}

	override fun setUpViews() {
		mActionBar?.setDisplayHomeAsUpEnabled(true)
	}

	override fun onResume() {
		super.onResume()
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
			startScan()
		}
	}

	public override fun onPause() {
		super.onPause()
		mScannerView.stopCamera()
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		handleOnRequestPermissionsResult(requestCode, permissions, grantResults) {
			if (Manifest.permission.CAMERA == it) {
				Snackbar.make(mScannerView, R.string.toast_permission_denied, Snackbar.LENGTH_INDEFINITE)
						.setAction(R.string.toast_permission_denied_action) { jumpToSettings() }
						.show()
			}
		}
	}

	private fun startScan() {
		errorView.makeInvisible()
		mScannerView.setResultHandler(this)
		mScannerView.startCamera()
	}

	private fun jumpToSettings() {
		val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + application.packageName))
		startActivity(intent)
	}

	override fun handleResult(result: Result) {
		val intent = Intent()
		intent[EXTRA_RESULT] = result.text
		setResult(RESULT_GET_RESULT, intent)
		finish()
	}

	companion object {

		const val REQUEST_CODE_SCAN = 30001
		const val RESULT_GET_RESULT = 3000
		const val EXTRA_RESULT = "extra_result"

		private const val REQUEST_PERMISSION = 20001

		const val ACTION_SCAN_TO_ADD = "info.papdt.express.helper.ACTION_SCAN_TO_ADD"

	}

}
