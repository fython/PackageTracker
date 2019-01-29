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
import info.papdt.express.helper.support.ColorGenerator
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

    override fun onBindViewHolder(holder: SimpleRecyclerViewAdapter.ClickableViewHolder, pos: Int) {
        super.onBindViewHolder(holder, pos)
        if (holder is ItemHolder) {
            holder.titleText.text = getItem(pos).name
            holder.otherText.text = if (getItem(pos).phone != null) getItem(pos).phone else getItem(pos).website
            holder.otherText.visibility = if (holder.otherText.text != null) View.VISIBLE else View.INVISIBLE

            /** Set up the logo  */
            holder.logoView.setImageDrawable(ColorDrawable(ColorGenerator.MATERIAL.getColor(getItem(pos).name)))
            holder.firstCharText.text = getItem(pos).name.substring(0, 1)
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
