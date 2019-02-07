package info.papdt.express.helper.ui.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class ItemViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val context: Context get() = itemView.context

    var item: T? = null

    fun bind(item: T) {
        this.item = item
        onBind(item)
    }

    protected abstract fun onBind(item: T)

}