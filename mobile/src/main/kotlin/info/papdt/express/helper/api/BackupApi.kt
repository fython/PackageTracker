package info.papdt.express.helper.api

import android.app.Activity
import android.content.Context
import android.net.Uri
import cn.nekocode.rxlifecycle.RxLifecycle
import info.papdt.express.helper.R
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.support.FileUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.IOException

class BackupApi(val context: Activity) {

	private val database = PackageDatabase.getInstance(context.applicationContext)

	fun restore(uri: Uri): Observable<Boolean> {
		return Observable.just(uri)
				.compose(RxLifecycle.bind(context).withObservable())
				.map {
					try {
						val fileData = FileUtils.readTextFromUri(context, it)
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
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
	}

	fun backup(uri: Uri): Observable<Boolean> {
		return Observable.just(uri)
				.compose(RxLifecycle.bind(context).withObservable())
				.map {
					try {
						FileUtils.writeTextToUri(context, it, database.backupData)
						true
					} catch (e: Exception) {
						e.printStackTrace()
						false
					}
				}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
	}

	fun share(): Observable<String> {
		return Observable.just("")
				.compose(RxLifecycle.bind(context).withObservable())
				.map {
					database.data.map { pack ->
						context.getString(
								R.string.export_list_item_format,
								pack.name,
								pack.number + " " + pack.companyChineseName,
								if ((pack.data?.size ?: 0) > 0) pack.data!![0].context
								else context.getString(R.string.item_text_cannot_get_package_status)
						)
					}.reduce { acc, s -> "$acc\n\n$s" } + "\n\n" + context.getString(R.string.export_list_end_text)
				}
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
	}

}