package info.papdt.express.helper.ui.adapter

import android.content.Context
import android.widget.ArrayAdapter
import info.papdt.express.helper.R
import moe.feng.kotlinyan.common.stringArrays

class HomeToolbarSpinnerAdapter(context: Context)
    : ArrayAdapter<String>(
        context, R.layout.item_spinner_for_home_toolbar,
        android.R.id.text1,
        context.resources.stringArrays[R.array.package_status_filter_entries]
) {

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

}