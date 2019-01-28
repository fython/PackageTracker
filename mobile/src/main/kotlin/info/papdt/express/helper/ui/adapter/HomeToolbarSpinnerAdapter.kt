package info.papdt.express.helper.ui.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import info.papdt.express.helper.R
import moe.feng.kotlinyan.common.stringArrays

class HomeToolbarSpinnerAdapter(
        context: Context,
        private val entries: Array<String> = context.resources
                .stringArrays[R.array.package_status_filter_entries]
)
    : ArrayAdapter<String>(
        context, R.layout.item_spinner_for_home_toolbar,
        android.R.id.text1,
        entries
) {

    init {
        setDropDownViewResource(R.layout.item_spinner_for_home_toolbar_dropdown)
    }

    private fun getDrawable(id: Int): Drawable? {
        return context.getDrawable(id)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return super.getView(position, convertView, parent).also {
            val textView = it.findViewById<TextView>(android.R.id.text1)
            when (textView.text) {
                entries[0] -> {
                    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            getDrawable(R.drawable.ic_local_shipping_text_primary_color_24dp),
                            null,
                            null,
                            null
                    )
                }
                entries[1] -> {
                    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            getDrawable(R.drawable.ic_check_circle_text_primary_color_24dp),
                            null,
                            null,
                            null
                    )
                }
                entries[2] -> {
                    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            getDrawable(R.drawable.ic_assignment_text_primary_color_24dp),
                            null,
                            null,
                            null
                    )
                }
            }
        }
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return super.getDropDownView(position, convertView, parent).also {
            val textView = it.findViewById<TextView>(android.R.id.text1)
        }
    }

}