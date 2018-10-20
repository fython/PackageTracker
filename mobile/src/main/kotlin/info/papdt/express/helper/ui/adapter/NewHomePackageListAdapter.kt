package info.papdt.express.helper.ui.adapter

import info.papdt.express.helper.model.Kuaidi100Package
import info.papdt.express.helper.support.DateHelper
import info.papdt.express.helper.ui.items.DateSubheadViewBinder
import info.papdt.express.helper.ui.items.PackageItemViewBinder
import me.drakeet.multitype.MultiTypeAdapter

class NewHomePackageListAdapter : MultiTypeAdapter() {

    init {
        register(Long::class.javaObjectType, DateSubheadViewBinder)
        register(Kuaidi100Package::class.java, PackageItemViewBinder)
    }

    fun setPackages(packages: List<Kuaidi100Package>, notify: Boolean = true) {
        val newList = mutableListOf<Any>()

        val groups = packages.sortedByDescending { it.getFirstStatusTime() }
                .groupBy { pack ->
                    pack.getFirstStatusTime().let { time ->
                        DateHelper.getDifferenceDaysForGroup(time.time)
                    }
                }
        for ((groupDate, packagesInGroup) in groups) {
            newList.add(groupDate)
            newList.addAll(packagesInGroup)
        }

        items = newList

        if (notify) {
            notifyDataSetChanged()
        }
    }

}