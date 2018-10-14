package info.papdt.express.helper.ui.items

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import info.papdt.express.helper.R
import info.papdt.express.helper.support.DateHelper
import me.drakeet.multitype.ItemViewBinder

object DateSubheadViewBinder : ItemViewBinder<Long, DateSubheadViewBinder.ViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_home_date_subhead, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, item: Long) {
        holder.text.text = DateHelper.getDifferenceDaysTextForGroup(holder.text.context, item)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val text: TextView = itemView.findViewById(android.R.id.text1)

    }

}