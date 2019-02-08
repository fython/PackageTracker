package info.papdt.express.helper.ui.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import info.papdt.express.helper.R
import info.papdt.express.helper.model.HomeListHeaderViewModel
import info.papdt.express.helper.ui.adapter.ItemViewHolder
import me.drakeet.multitype.ItemViewBinder
import moe.feng.kotlinyan.common.makeGone
import moe.feng.kotlinyan.common.makeVisible
import java.text.DateFormat
import java.util.*

object HomeListHeaderViewBinder
    : ItemViewBinder<HomeListHeaderViewModel, HomeListHeaderViewBinder.ViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.home_list_header_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, item: HomeListHeaderViewModel) {
        holder.bind(item)
    }

    class ViewHolder(itemView: View) : ItemViewHolder<HomeListHeaderViewModel>(itemView) {

        private val lastUpdateTime: TextView = itemView.findViewById(R.id.last_update_time_text)
        private val filterKeywordText: TextView = itemView.findViewById(R.id.filter_keyword_text)
        private val filterCompanyText: TextView = itemView.findViewById(R.id.filter_company_text)
        private val filterKeywordLayout: View = itemView.findViewById(R.id.filter_keyword_layout)
        private val filterCompanyLayout: View = itemView.findViewById(R.id.filter_company_layout)

        override fun onBind(item: HomeListHeaderViewModel) {
            if (item.lastUpdateTime <= 0L) {
                lastUpdateTime.setText(R.string.last_update_time_text_unknown)
            } else {
                lastUpdateTime.text = context.getString(
                        R.string.last_update_time_text_format,
                        DateFormat.getDateTimeInstance().format(Date(item.lastUpdateTime))
                )
            }

            if (item.filterKeyword == null) {
                filterKeywordLayout.makeGone()
            } else {
                filterKeywordLayout.makeVisible()
                filterKeywordText.text = context.getString(
                        R.string.filter_keyword_text_format,
                        item.filterKeyword
                )
            }

            if (item.filterCompanyName == null) {
                filterCompanyLayout.makeGone()
            } else {
                filterCompanyLayout.makeVisible()
                filterCompanyText.text = context.getString(
                        R.string.filter_company_text_format,
                        item.filterCompanyName
                )
            }
        }

    }

}