package info.papdt.express.helper.api

import com.spreada.utils.chinese.ZHConverter
import info.papdt.express.helper.model.BaseMessage
import info.papdt.express.helper.model.Kuaidi100Package
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.ArrayList

object KtPackageApi {

	suspend fun getPackage(
            number: String, com: String? = null
    ): BaseMessage<out Kuaidi100Package?> = withContext(Dispatchers.IO) {
        PackageApi.getPackage(number, com)
	}

	suspend fun filterCompany(k: String)
			: ArrayList<Kuaidi100PackageApi.CompanyInfo.Company> = withContext(Dispatchers.IO) {
        val keyword = ZHConverter.convert(k, ZHConverter.SIMPLIFIED)
                .replace("快递".toRegex(), "")
        val src = ArrayList<Kuaidi100PackageApi.CompanyInfo.Company>()
        if (keyword.trim().isNotEmpty()) {
            (0 until Kuaidi100PackageApi.CompanyInfo.info!!.size)
                    .filterNot {
                        !Kuaidi100PackageApi.CompanyInfo.names[it]
                                .toLowerCase().contains(keyword.toLowerCase()) &&
                                !Kuaidi100PackageApi.CompanyInfo.pinyin[it].contains(keyword)
                    }
                    .mapTo(src) { Kuaidi100PackageApi.CompanyInfo.info!![it] }
            src
        } else {
            Kuaidi100PackageApi.CompanyInfo.info!!
        }
	}

	suspend fun detectCompany(id: String): String = withContext(Dispatchers.IO) {
        Kuaidi100PackageApi.detectCompanyByNumber(id) ?: ""
	}

}