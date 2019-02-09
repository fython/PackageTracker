package info.papdt.express.helper.ui.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import info.papdt.express.helper.R
import info.papdt.express.helper.model.Category
import info.papdt.express.helper.model.MaterialIcon
import info.papdt.express.helper.ui.adapter.ItemViewHolder
import me.drakeet.multitype.ItemViewBinder

object CategoryItemViewBinder : ItemViewBinder<Category, CategoryItemViewBinder.ViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_category, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, item: Category) {
        holder.bind(item)
    }

    class ViewHolder(itemView: View) : ItemViewHolder<Category>(itemView) {

        private val iconTextView: TextView = itemView.findViewById(R.id.icon_text_view)
        private val titleView: TextView = itemView.findViewById(R.id.title_view)

        init {
            iconTextView.typeface = MaterialIcon.iconTypeface

            itemView.setOnClickListener {
                
            }
        }

        override fun onBind(item: Category) {
            titleView.text = item.title
            iconTextView.text = item.iconCode
        }

    }

}