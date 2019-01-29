package info.papdt.express.helper.ui.items

import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.papdt.express.helper.R
import me.drakeet.multitype.ItemViewBinder

class SubheaderItemBinder : ItemViewBinder<String, SubheaderItemBinder.ItemHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ItemHolder {
        return ItemHolder(inflater.inflate(R.layout.item_list_details_info_subheader, parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, item: String) {
        holder.title.text = item
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var title: AppCompatTextView = itemView.findViewById(R.id.tv_title)

    }

}
