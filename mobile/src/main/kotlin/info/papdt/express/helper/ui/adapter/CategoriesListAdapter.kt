package info.papdt.express.helper.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import info.papdt.express.helper.R
import info.papdt.express.helper.model.Category
import info.papdt.express.helper.ui.items.CategoryItemViewBinder
import me.drakeet.multitype.MultiTypeAdapter

class CategoriesListAdapter(layoutRes: Int = R.layout.item_category) : MultiTypeAdapter() {

    private var rawData: List<Category>? = null

    init {
        register(Category::class.java, CategoryItemViewBinder(layoutRes))
    }

    fun setCategories(categories: List<Category>, notify: Boolean = true) {
        rawData = categories

        updateItems(notify)
    }

    private fun updateItems(notify: Boolean = true) {
        rawData?.sorted()
                ?.let { data ->
                    if (notify) {
                        setupItemsWithDiffUtils(data)
                    } else {
                        items = data
                    }
                }
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
            return oldList[oldIndex] == newList[newIndex]
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldIndex: Int, newIndex: Int): Boolean {
            return areItemsTheSame(oldIndex, newIndex)
        }

    }

}