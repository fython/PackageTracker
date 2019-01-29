package info.papdt.express.helper.ui.items

import android.app.Activity
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.papdt.express.helper.R
import info.papdt.express.helper.support.CheatSheet
import info.papdt.express.helper.support.ClipboardUtils
import info.papdt.express.helper.ui.DetailsActivity
import me.drakeet.multitype.ItemViewBinder

class DetailsTwoLineItemBinder : ItemViewBinder<DetailsTwoLineItem, DetailsTwoLineItemBinder.ItemHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ItemHolder {
        return ItemHolder(inflater.inflate(R.layout.item_list_details_info_normal, parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, item: DetailsTwoLineItem) {
        holder.setData(item)
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var data: DetailsTwoLineItem? = null

        var title: AppCompatTextView = itemView.findViewById(R.id.tv_title)
        var summary: AppCompatTextView = itemView.findViewById(R.id.tv_summary)
        var button: AppCompatImageButton = itemView.findViewById(R.id.btn_action)

        fun setData(newData: DetailsTwoLineItem) {
            this.data = newData
            if (DetailsTwoLineItem.TYPE_NAME == data!!.type) {
                title.setText(R.string.list_package_name)
                summary.text = data!!.content
                button.visibility = View.GONE
                itemView.setOnLongClickListener { v ->
                    if (v.context is DetailsActivity) {
                        (v.context as DetailsActivity).showNameEditDialog()
                    }
                    true
                }
            } else if (DetailsTwoLineItem.TYPE_NUMBER == data!!.type) {
                title.setText(R.string.list_package_number)
                summary.text = String.format(
                        STRING_NUMBER_FORMAT,
                        data!!.content,
                        data!!.optional
                )
                if (button.tag != null && button.tag as Boolean) {
                    button.setImageResource(R.drawable.ic_visibility_off_black_24dp)
                    summary.text = String.format(STRING_NUMBER_FORMAT, data!!.content, data!!.optional)
                } else {
                    val length = data!!.content!!.length
                    var str = if (length >= 4) data!!.content!!.substring(0, 4) else data!!.content!!
                    for (i in 4 until length) str += "*"
                    summary.text = String.format(STRING_NUMBER_FORMAT, str, data!!.optional)
                    button.setImageResource(R.drawable.ic_visibility_black_24dp)
                    button.contentDescription = itemView.resources
                            .getString(R.string.list_package_show_toggle_desc)
                    CheatSheet.setup(button)
                }
                button.visibility = View.VISIBLE
                button.setOnClickListener {
                    if (button.tag != null && button.tag as Boolean) {
                        val length = data!!.content!!.length
                        var str = if (length >= 4) data!!.content!!.substring(0, 4) else data!!.content!!
                        for (i in 4 until length) str += "*"
                        summary.text = String.format(STRING_NUMBER_FORMAT, str, data!!.optional)
                        button.setImageResource(R.drawable.ic_visibility_black_24dp)
                        button.tag = false
                    } else {
                        button.setImageResource(R.drawable.ic_visibility_off_black_24dp)
                        summary.text = String.format(STRING_NUMBER_FORMAT, data!!.content, data!!.optional)
                        button.tag = true
                    }
                }
                itemView.setOnLongClickListener { v ->
                    ClipboardUtils.putString(v.context, data!!.content!!)
                    Snackbar.make((v.context as Activity).findViewById(R.id.coordinator_layout),
                            R.string.toast_copied_code,
                            Snackbar.LENGTH_LONG
                    ).show()
                    true
                }
            }
        }

    }

    companion object {

        private const val STRING_NUMBER_FORMAT = "%1\$s (%2\$s)"

    }

}
