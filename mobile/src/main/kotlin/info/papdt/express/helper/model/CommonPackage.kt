package info.papdt.express.helper.model

import com.google.gson.Gson

class CommonPackage(
		val message: String?,
		val id: String,
		val company: String,
		val statusCode: String,
		val state: String,
		val isChecked: Boolean,
		val updateTime: String?
) {

	private var data: String = "[]"
	private var dataList: Array<CommonStatus>? = null

	fun getData(): Array<CommonStatus> {
		if (dataList == null) {
			dataList = GSON.fromJson(data, Array<CommonStatus>::class.java)
		}
		return dataList!!
	}

	companion object {

		val GSON = Gson()

		fun fromJson(json: String): CommonPackage = GSON.fromJson(json, CommonPackage::class.java)

		fun fromMap(map: Map<String, String>): CommonPackage
				= GSON.fromJson(GSON.toJson(map), CommonPackage::class.java)

        fun toCommonPackage(oldData: Kuaidi100Package): CommonPackage {
            val newData = CommonPackage(
                    oldData.message,
                    oldData.number ?: oldData.codeNumber!!,
                    oldData.companyType!!,
                    oldData.status ?: "",
                    oldData.getState().toString(),
                    false,
                    oldData.updateTime
            )
            oldData.data?.let {
                newData.dataList = CommonStatus.toNewPackageStatusList(it).toTypedArray()
            }
            return newData
        }

	}

}