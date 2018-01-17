package info.papdt.express.helper.api

import android.app.Activity
import cn.nekocode.rxlifecycle.RxLifecycle
import com.spreada.utils.chinese.ZHConverter
import info.papdt.express.helper.model.BaseMessage
import info.papdt.express.helper.model.Package
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.ArrayList

object RxPackageApi {

	fun getPackage(number: String, com: String? = null,
	               parentActivity: Activity? = null)
			: Observable<BaseMessage<out Package?>> {
		var observable = Observable.just("")
		parentActivity?.let { observable = observable.compose(RxLifecycle.bind(parentActivity).withObservable()) }
		return observable
				.map {
					if (com == null) Kuaidi100PackageApi.getPackageByNumber(number) else Kuaidi100PackageApi.getPackage(com, number)
				}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
	}

	fun filterCompany(k: String, parentActivity: Activity? = null)
			: Observable<ArrayList<Kuaidi100PackageApi.CompanyInfo.Company>> {
		var observable = Observable.just(k)
		parentActivity?.let { observable = observable.compose(RxLifecycle.bind(parentActivity).withObservable()) }
		return observable
				.map {
					val keyword = ZHConverter.convert(it, ZHConverter.SIMPLIFIED).replace("快递".toRegex(), "")
					val src = ArrayList<Kuaidi100PackageApi.CompanyInfo.Company>()
					if (keyword?.trim()?.isNotEmpty()) {
						(0 until Kuaidi100PackageApi.CompanyInfo.info!!.size)
								.filterNot { !Kuaidi100PackageApi.CompanyInfo.names[it].toLowerCase().contains(keyword.toLowerCase()) && !Kuaidi100PackageApi.CompanyInfo.pinyin[it].contains(keyword) }
								.mapTo(src) { Kuaidi100PackageApi.CompanyInfo.info!![it] }
						src
					} else {
						Kuaidi100PackageApi.CompanyInfo.info!!
					}
				}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
	}

	fun filterCompanySync(k: String): ArrayList<Kuaidi100PackageApi.CompanyInfo.Company> {
		return filterCompany(k).blockingFirst()
	}

	fun detectCompany(id: String, parentActivity: Activity? = null): Observable<String> {
		var observable = Observable.just(id)
		parentActivity?.let { observable = observable.compose(RxLifecycle.bind(parentActivity).withObservable()) }
		return observable
				.map { Kuaidi100PackageApi.detectCompanyByNumber(it) ?: "" }
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
	}

}