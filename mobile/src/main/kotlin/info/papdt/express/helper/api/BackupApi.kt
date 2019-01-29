package info.papdt.express.helper.api

import android.app.Activity
import android.net.Uri
import info.papdt.express.helper.R
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.support.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class BackupApi(val context: Activity) {

	private val database = PackageDatabase.getInstance(context.applicationContext)

	suspend fun restore(uri: Uri): Boolean {
		return withContext(Dispatchers.IO) {
            try {
                val fileData = FileUtils.readTextFromUri(context, uri)
                try {
                    database.restoreData(fileData)
                    database.save()
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            } catch (e: IOException) {
                false
            }
        }
    }

	suspend fun backup(uri: Uri): Boolean {
		return withContext(Dispatchers.IO) {
            try {
                FileUtils.writeTextToUri(context, uri, database.backupData)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

	suspend fun share(): String {
		return withContext(Dispatchers.IO) {
            database.data.map { pack ->
                context.getString(
                        R.string.export_list_item_format,
                        pack.name,
                        pack.number + " " + pack.companyChineseName,
                        if ((pack.data?.size ?: 0) > 0) pack.data!![0].context
                        else context.getString(R.string.item_text_cannot_get_package_status)
                )
            }.reduce { acc, s -> "$acc\n\n$s" } +
                    "\n\n" +
                    context.getString(R.string.export_list_end_text)
        }
    }

}