package info.papdt.express.helper.api

import info.papdt.express.helper.support.PackageApiType
import info.papdt.express.helper.support.SettingsInstance

object PackageApi {

	@JvmStatic fun getPackage(number: String, company: String? = null) = when (SettingsInstance.packageApiTypeInt) {
		PackageApiType.KUAIDI100 -> {
			if (company == null) {
				Kuaidi100PackageApi.getPackageByNumber(number)
			} else {
				Kuaidi100PackageApi.getPackage(company, number)
			}
		}
		PackageApiType.BAIDU -> {
			BaiduPackageApi.getPackageByNumber(number)
		}
		else -> throw IllegalArgumentException("This api type is unsupported")
	}

}