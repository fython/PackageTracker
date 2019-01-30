package info.papdt.express.helper.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import android.view.View

import info.papdt.express.helper.ui.common.AbsActivity
import me.dm7.barcodescanner.zxing.ZXingScannerView
import moe.feng.kotlinyan.common.*
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.widget.Toast
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import info.papdt.express.helper.R
import java.io.IOException
import java.lang.Exception
import java.util.*


class ScannerActivity : AbsActivity(), ZXingScannerView.ResultHandler, PermissionActivity {

	companion object {

		const val REQUEST_CODE_SCAN = 30001
		const val EXTRA_RESULT = "extra_result"

		private const val REQUEST_PERMISSION = 20001
		private const val REQUEST_CODE_GALLERY = 20002

		const val ACTION_SCAN_TO_ADD = "info.papdt.express.helper.ACTION_SCAN_TO_ADD"

	}

	private val mScannerView: ZXingScannerView by lazyFindNonNullView(R.id.scanner_view)
	private val errorView: View by lazyFindNonNullView(R.id.iv_error)

	private lateinit var multiFormatReader: MultiFormatReader

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
		setContentView(R.layout.activity_scanner)

		val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java)
		hints[DecodeHintType.POSSIBLE_FORMATS] = mScannerView.formats
		multiFormatReader = MultiFormatReader()
		multiFormatReader.setHints(hints)

		runWithPermission(Manifest.permission.CAMERA) { startScan() } ?: run {
			errorView.makeVisible()
			buildAlertDialog {
				titleRes = R.string.dialog_explanation_permission_title
				messageRes = R.string.dialog_explanation_permission_message
				positiveButton(R.string.dialog_explanation_permission_pos_btn)  { _, _ -> jumpToSettings() }
				cancelButton { _, _ ->
					finish()
				}
				setOnCancelListener {
					finish()
				}
			}.show()
		}
	}

	override fun setUpViews() {

	}

	override fun onResume() {
		super.onResume()
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
				== PackageManager.PERMISSION_GRANTED) {
			startScan()
		}
	}

	override fun onPause() {
		super.onPause()
		mScannerView.stopCamera()
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.menu_scanner, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
		R.id.action_choose_from_gallery -> {
			val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
			intent.addCategory(Intent.CATEGORY_OPENABLE)
			intent.type = "image/*"
			startActivityForResult(intent, REQUEST_CODE_GALLERY)
			true
		}
		else -> super.onOptionsItemSelected(item)
	}

	override fun onRequestPermissionsResult(requestCode: Int,
											permissions: Array<String>,
											grantResults: IntArray) {
		handleOnRequestPermissionsResult(requestCode, permissions, grantResults) {
			if (Manifest.permission.CAMERA == it) {
				Snackbar.make(
						mScannerView,
						R.string.toast_permission_denied,
						Snackbar.LENGTH_SHORT
				).setAction(R.string.toast_permission_denied_action) { jumpToSettings() }.show()
			}
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = ui {
		when (requestCode) {
			REQUEST_CODE_GALLERY -> {
				if (resultCode == RESULT_OK) {
					val uri = data?.data
					if (uri != null) {
						val result = asyncIO {
							val bitmap = getBitmapFromUri(uri)
							val pixels = IntArray(bitmap.width * bitmap.height) { 0 }
							bitmap.getPixels(
									pixels, 0,
									bitmap.width, 0, 0,
									bitmap.width, bitmap.height)
							bitmap.recycle()
							val src = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
							val binBmp = BinaryBitmap(HybridBinarizer(src))
							try {
								multiFormatReader.decodeWithState(binBmp)
							} catch (e: Exception) {
								null
							}
						}.await()
						if (result != null) {
							handleResult(result)
						} else {
							Toast.makeText(
									this@ScannerActivity,
									R.string.toast_cannot_find_any_barcode,
									Toast.LENGTH_SHORT
							).show()
						}
					}
				}
			}
		}
	}

	@Throws(IOException::class)
	private fun getBitmapFromUri(uri: Uri): Bitmap {
		val pfd = contentResolver.openFileDescriptor(uri, "r") ?: throw IOException()
		val fileDescriptor = pfd.fileDescriptor
		val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
		pfd.close()
		return image
	}

	private fun startScan() {
		errorView.makeInvisible()
		mScannerView.setResultHandler(this)
		mScannerView.startCamera()
	}

	private fun jumpToSettings() {
		val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
				Uri.parse("package:" + application.packageName))
		startActivity(intent)
	}

	override fun handleResult(result: Result) {
		val intent = Intent()
		intent[EXTRA_RESULT] = result.text
		setResult(RESULT_OK, intent)
		finish()
	}

}
