package info.papdt.express.helper.ui.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import info.papdt.express.helper.R
import info.papdt.express.helper.event.EventIntents
import info.papdt.express.helper.model.Category
import info.papdt.express.helper.model.MaterialIcon
import info.papdt.express.helper.ui.adapter.ItemViewHolder
import me.drakeet.multitype.ItemViewBinder

class CategoryItemViewBinder(private val layoutResource: Int)
    : ItemViewBinder<Category, CategoryItemViewBinder.ViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(layoutResource, parent, false))
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
                LocalBroadcastManager.getInstance(it.context)
                        .sendBroadcast(EventIntents
                                .notifyItemOnClick(CategoryItemViewBinder::class, item))
            }
        }

        override fun onBind(item: Category) {
            if (item.title.isEmpty()) {
                titleView.setText(R.string.choose_category_dialog_unclassified)
                iconTextView.text = ""
            } else {
                titleView.text = item.title
                iconTextView.text = item.iconCode
            }
        }

    }

}