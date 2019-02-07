package info.papdt.express.helper.ui.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.papdt.express.helper.R
import info.papdt.express.helper.model.HomeListNoResultViewModel
import info.papdt.express.helper.ui.adapter.ItemViewHolder
import me.drakeet.multitype.ItemViewBinder

object HomeListNoResultViewBinder
    : ItemViewBinder<HomeListNoResultViewModel, HomeListNoResultViewBinder.ViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.home_list_no_result_view_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, item: HomeListNoResultViewModel) {
        holder.bind(item)
    }

    class ViewHolder(itemView: View) : ItemViewHolder<HomeListNoResultViewModel>(itemView) {

        override fun onBind(item: HomeListNoResultViewModel) {

        }

    }

}