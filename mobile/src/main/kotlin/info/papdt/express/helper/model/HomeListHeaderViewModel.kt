package info.papdt.express.helper.model

data class HomeListHeaderViewModel(
        val lastUpdateTime: Long = 0,
        val filterKeyword: String? = null,
        val filterCompanyName: String? = null
)