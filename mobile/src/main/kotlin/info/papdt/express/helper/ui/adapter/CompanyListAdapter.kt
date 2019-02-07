package info.papdt.express.helper.ui.adapter

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.util.ArrayList

import de.hdodenhof.circleimageview.CircleImageView
import info.papdt.express.helper.R
import info.papdt.express.helper.api.Kuaidi100PackageApi
import info.papdt.express.helper.ui.common.SimpleRecyclerViewAdapter

class CompanyListAdapter(
        recyclerView: RecyclerView,
        private var list: ArrayList<Kuaidi100PackageApi.CompanyInfo.Company>?
) : SimpleRecyclerViewAdapter(recyclerView) {

    fun setList(list: ArrayList<Kuaidi100PackageApi.CompanyInfo.Company>) {
        this.list = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewAdapter.ClickableViewHolder {
        bindContext(parent.context)
        return ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_company, parent, false))
    }

    override fun onBindViewHolder(holder: SimpleRecyclerViewAdapter.ClickableViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = getItem(position)
        if (holder is ItemHolder) {
            holder.titleText.text = item.name
            holder.otherText.text = if (item.phone != null) item.phone else item.website
            holder.otherText.visibility = if (holder.otherText.text != null) View.VISIBLE else View.INVISIBLE

            /** Set up the logo  */
            holder.firstCharText.text = item.name.substring(0, 1)
            item.getPalette().let {
                holder.logoView.setImageDrawable(
                        ColorDrawable(it.getPackageIconBackground(context!!)))
                holder.firstCharText.setTextColor(
                        it.getPackageIconForeground(context!!))
            }
        }
    }

    override fun getItemCount(): Int = list?.size ?: 0

    fun getItem(pos: Int): Kuaidi100PackageApi.CompanyInfo.Company = list!![pos]

    inner class ItemHolder(itemView: View) : SimpleRecyclerViewAdapter.ClickableViewHolder(itemView) {

        internal var titleText: AppCompatTextView = itemView.findViewById(R.id.tv_title)
        internal var otherText: AppCompatTextView = itemView.findViewById(R.id.tv_other)
        internal var logoView: CircleImageView = itemView.findViewById(R.id.iv_logo)
        internal var firstCharText: TextView = itemView.findViewById(R.id.tv_first_char)

    }

}
