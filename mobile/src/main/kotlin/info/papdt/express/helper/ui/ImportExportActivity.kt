package info.papdt.express.helper.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ShareCompat
import android.support.v4.content.IntentCompat
import android.view.View

import java.text.SimpleDateFormat
import java.util.Calendar

import info.papdt.express.helper.R
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.ui.common.AbsActivity

class ImportExportActivity : AbsActivity() {

	private val database: PackageDatabase by lazy { PackageDatabase.getInstance(applicationContext) }
	private val BackupApi by lazy { info.papdt.express.helper.api.BackupApi(this@ImportExportActivity) }

	private var progressDialog: ProgressDialog? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_import_export)

		mActionBar!!.setDisplayHomeAsUpEnabled(true)
	}

	override fun setUpViews() {
		findViewById<View>(R.id.action_backup_all_data).setOnClickListener {
			val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
			intent.addCategory(Intent.CATEGORY_OPENABLE)
			intent.type = "text/*"

			val date = Calendar.getInstance().time
			val format = SimpleDateFormat("yyyyMMdd_HHmmss")

			intent[Intent.EXTRA_TITLE] = String.format(BACKUP_FILE_NAME, format.format(date))
			startActivityForResult(intent, REQUEST_WRITE_BACKUP_FILE)
		}

		findViewById<View>(R.id.action_export_list).setOnClickListener {
			if (database.size() == 0) {
				Snackbar.make(`$`(R.id.container)!!, R.string.toast_list_empty, Snackbar.LENGTH_SHORT)
						.show()
			} else {
				BackupApi.share().subscribe { text ->
					val intent = Intent(Intent.ACTION_SEND)
					intent.type = "text/plain"
					intent.putExtra(Intent.EXTRA_TEXT, text)
					startActivity(Intent.createChooser(intent, getString(R.string.dialog_share_title)))
				}
			}
		}
		findViewById<View>(R.id.action_restore_all_data).setOnClickListener {
			val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
			intent.addCategory(Intent.CATEGORY_OPENABLE)
			intent.type = "*/*"
			startActivityForResult(intent, REQUEST_OPEN_FILE_RESTORE)
		}
	}

	public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == REQUEST_WRITE_BACKUP_FILE && resultCode == Activity.RESULT_OK && data != null) {
			BackupApi.backup(data.data)
					.doOnSubscribe {
						progressDialog = ProgressDialog(this@ImportExportActivity).apply {
							setMessage(getString(R.string.dialog_backup_title))
							setCancelable(false)
						}
						progressDialog?.show()
					}
					.subscribe { isSucceed ->
						progressDialog?.dismiss()
						if (isSucceed) {
							Snackbar.make(
									`$`(R.id.container)!!, R.string.toast_backup_succeed, Snackbar.LENGTH_LONG
							).setAction(R.string.toast_backup_send_action) {
								ShareCompat.IntentBuilder.from(this@ImportExportActivity)
										.addStream(data.data)
										.setType("text/*")
										.startChooser()
							}.show()
						} else {
							Snackbar.make(
									`$`(R.id.container)!!, R.string.toast_backup_failed, Snackbar.LENGTH_LONG
							).show()
						}
					}
		}
		if (requestCode == REQUEST_OPEN_FILE_RESTORE && resultCode == Activity.RESULT_OK && data != null) {
			BackupApi.restore(data.data)
					.doOnSubscribe {
						progressDialog = ProgressDialog(this@ImportExportActivity).apply {
							setMessage(getString(R.string.dialog_restoring_title))
							setCancelable(false)
						}
						progressDialog?.show()
					}
					.subscribe { isSucceed ->
						progressDialog?.dismiss()
						if (!isSucceed) {
							Snackbar.make(
									`$`(R.id.container)!!, R.string.toast_restore_failed, Snackbar.LENGTH_LONG
							).show()
						} else {
							buildAlertDialog {
								titleRes = R.string.dialog_restored_title
								messageRes = R.string.dialog_restored_message
								isCancelable = false
								positiveButton(R.string.dialog_restored_restart_button) { _, _ ->
									val intent = packageManager
											.getLaunchIntentForPackage(packageName)
									val componentName = intent.component
									val i = IntentCompat.makeRestartActivityTask(componentName)
									startActivity(i)

									System.exit(0)
								}
							}.show()
						}
					}
		}
	}

	companion object {

		private const val BACKUP_FILE_NAME = "PackageTrackerBackup_%s.json"

		const val REQUEST_WRITE_BACKUP_FILE = 10001
		const val REQUEST_OPEN_FILE_RESTORE = 10002

		fun launch(activity: Activity) {
			val intent = Intent(activity, ImportExportActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			activity.startActivity(intent)
		}

	}

}
