package info.papdt.express.helper.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import info.papdt.express.helper.api.Kuaidi100PackageApi
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.model.HomeListEmptyViewModel
import info.papdt.express.helper.model.HomeListHeaderViewModel
import info.papdt.express.helper.model.HomeListNoResultViewModel
import info.papdt.express.helper.model.Kuaidi100Package
import info.papdt.express.helper.support.DateHelper
import info.papdt.express.helper.ui.items.*
import me.drakeet.multitype.MultiTypeAdapter
import java.text.Collator

class NewHomePackageListAdapter : MultiTypeAdapter() {

    companion object {

        const val SORT_BY_UPDATE_TIME = 0
        const val SORT_BY_NAME = 1
        const val SORT_BY_CREATE_TIME = 2

        const val FILTER_ON_THE_WAY = 0
        const val FILTER_DELIVERED = 1
        const val FILTER_ALL = 2

    }

    private var rawData: List<Kuaidi100Package>? = null
    var sortType: Int = SORT_BY_UPDATE_TIME
        set(value) {
            if (field == value) {
                return
            }
            field = value
            updateItems()
        }
    var filter: Int = FILTER_ON_THE_WAY
        set(value) {
            if (field == value) {
                return
            }
            field = value
            updateItems()
        }
    var filterKeyword: String? = null
        set(value) {
            if (field == value) {
                return
            }
            field = value
            updateItems()
        }
    var filterCompany: String? = null
        set(value) {
            if (field == value) {
                return
            }
            field = value
            updateItems()
        }
    var lastUpdateTime: Long = 0
        set(value) {
            if (field == value) {
                return
            }
            field = value
            updateItems()
        }

    init {
        register(Long::class.javaObjectType, DateSubheadViewBinder)
        register(Kuaidi100Package::class.java, PackageItemViewBinder)
        register(HomeListEmptyViewModel::class.java, HomeListEmptyViewBinder)
        register(HomeListNoResultViewModel::class.java, HomeListNoResultViewBinder)
        register(HomeListHeaderViewModel::class.java, HomeListHeaderViewBinder)
    }

    fun setPackages(packages: List<Kuaidi100Package>, notify: Boolean = true) {
        rawData = packages

        updateItems(notify)
    }

    private fun updateItems(notify: Boolean = true) {
        rawData?.filter { item ->
            val state = item.getState()
            when (filter) {
                FILTER_ON_THE_WAY -> if (
                        state != Kuaidi100Package.STATUS_ON_THE_WAY &&
                        state != Kuaidi100Package.STATUS_NORMAL &&
                        state != Kuaidi100Package.STATUS_RETURNING
                ) {
                    return@filter false
                }
                FILTER_DELIVERED -> if (
                        state != Kuaidi100Package.STATUS_DELIVERED &&
                        state != Kuaidi100Package.STATUS_RETURNED
                ) {
                    return@filter false
                }
                FILTER_ALL -> {}
                else -> throw IllegalArgumentException("Unsupported filter = $filter")
            }
            filterKeyword?.let {
                if (it !in item.name!! && it !in item.number!!) {
                    return@filter false
                }
            }
            filterCompany?.let {
                if (it != item.companyType) {
                    return@filter false
                }
            }
            return@filter true
        }?.let { filteredData ->
            if (filteredData.isEmpty()) {
                val newList = mutableListOf<Any>()

                if (filterKeyword == null && filterCompany == null) {
                    newList += HomeListEmptyViewModel(filter)
                } else {
                    newList += HomeListNoResultViewModel()
                }

                if (notify) {
                    setupItemsWithDiffUtils(newList)
                } else {
                    items = newList
                }

                return@let null
            } else {
                return@let filteredData
            }
        }?.let { data ->
            val newList = mutableListOf<Any>()
            when (sortType) {
                SORT_BY_UPDATE_TIME -> {
                    val groups = data
                            .sortedByDescending { it.getFirstStatusTime() }
                            .groupBy { pack ->
                                pack.getFirstStatusTime().let { time ->
                                    val diffDays = DateHelper.getDifferenceDaysForGroup(time.time)
                                    // Fix unstable group
                                    if (diffDays < 0) -1 else diffDays
                                }
                            }
                    for ((groupDate, packagesInGroup) in groups) {
                        newList.add(groupDate)
                        newList.addAll(packagesInGroup)
                    }
                }
                SORT_BY_CREATE_TIME -> {
                    newList.addAll(data.reversed())
                }
                SORT_BY_NAME -> {
                    newList.addAll(data.sortedWith(Comparator { o1, o2 ->
                        Collator.getInstance().compare(o1.name, o2.name)
                    }))
                }
                else -> throw IllegalArgumentException("Unsupported sort type = $sortType")
            }

            newList
        }?.let { sortedData ->
            val newData = mutableListOf(HomeListHeaderViewModel(
                    lastUpdateTime = lastUpdateTime,
                    filterKeyword = filterKeyword,
                    filterCompanyName = Kuaidi100PackageApi.CompanyInfo.getNameByCode(filterCompany)
            )) + sortedData

            if (notify) {
                setupItemsWithDiffUtils(newData)
            } else {
                items = newData
            }
        }
    }

    fun refreshPackage(database: PackageDatabase, number: String?): Boolean {
        if (number == null) {
            return false
        }
        for ((index, item) in items.withIndex()) {
            if (item is Kuaidi100Package) {
                if (item.number == number) {
                    val newData = database.getByNumber(number)
                    (items as MutableList<Any>)[index] = newData as Any
                    notifyItemChanged(index)
                }
            }
        }
        return false
    }

    fun setupItemsWithDiffUtils(newItems: List<Any>) {
        if (items.isNotEmpty()) {
            val result = DiffUtil.calculateDiff(DiffCallback(items as List<Any>, newItems))
            items = newItems
            result.dispatchUpdatesTo(this)
        } else {
            items = newItems
            notifyItemRangeInserted(0, newItems.size)
        }
    }

    class DiffCallback(val oldList: List<Any>, val newList: List<Any>) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldIndex: Int, newIndex: Int): Boolean {
            // Kuaidi100Package & Calendar's equals have been implemented.
            return oldList[oldIndex] == newList[newIndex]
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldIndex: Int, newIndex: Int): Boolean {
            var res = areItemsTheSame(oldIndex, newIndex)
            if (!res) {
                if (oldList[oldIndex] is Kuaidi100Package
                        && newList[newIndex] is Kuaidi100Package) {
                    val oldPack = oldList[oldIndex] as Kuaidi100Package
                    val newPack = newList[oldIndex] as Kuaidi100Package
                    if (oldPack.number == newPack.number) {
                        when {
                            oldPack.status != newPack.status ->
                                res = false
                            oldPack.getFirstStatusTime() != newPack.getFirstStatusTime() ->
                                res = false
                            oldPack.unreadNew xor newPack.unreadNew ->
                                res = false
                        }
                    }
                }
            }
            return res
        }

    }

}