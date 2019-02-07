package info.papdt.express.helper.ui.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import info.papdt.express.helper.R
import info.papdt.express.helper.model.HomeListEmptyViewModel
import info.papdt.express.helper.ui.adapter.ItemViewHolder
import info.papdt.express.helper.ui.adapter.NewHomePackageListAdapter
import me.drakeet.multitype.ItemViewBinder

object HomeListEmptyViewBinder
    : ItemViewBinder<HomeListEmptyViewModel, HomeListEmptyViewBinder.ViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.home_list_empty_view_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, item: HomeListEmptyViewModel) {
        holder.bind(item)
    }

    class ViewHolder(itemView: View) : ItemViewHolder<HomeListEmptyViewModel>(itemView) {

        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        override fun onBind(item: HomeListEmptyViewModel) {
            textView.setText(when (item.type) {
                NewHomePackageListAdapter.FILTER_ALL -> R.string.frame_empty_tip_all
                NewHomePackageListAdapter.FILTER_ON_THE_WAY -> R.string.frame_empty_tip_delivering
                NewHomePackageListAdapter.FILTER_DELIVERED -> R.string.frame_empty_tip_delivered
                else -> 0
            })
        }

    }

}