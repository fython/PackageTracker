package info.papdt.express.helper.ui.adapter

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.google.gson.Gson

import java.util.ArrayList

import de.hdodenhof.circleimageview.CircleImageView
import info.papdt.express.helper.R
import info.papdt.express.helper.api.PackageApi
import info.papdt.express.helper.model.Package
import info.papdt.express.helper.support.ColorGenerator
import info.papdt.express.helper.support.ScreenUtils
import info.papdt.express.helper.support.Spanny
import info.papdt.express.helper.ui.DetailsActivity
import info.papdt.express.helper.ui.SearchActivity

class SearchResultAdapter(
        private val parentActivity: AppCompatActivity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var packages: ArrayList<Package>? = null
    private var companies: ArrayList<PackageApi.CompanyInfo.Company>? = null

    private var items: ArrayList<ItemType>? = null

    private var DP_16_TO_PX = -1f
    private var statusTitleColor: Int = 0
    private var statusSubtextColor = -1
    private var STATUS_STRING_ARRAY: Array<String>? = null

    fun setPackages(packages: ArrayList<Package>?) {
        this.packages = packages
    }

    fun setCompanies(companies: ArrayList<PackageApi.CompanyInfo.Company>?) {
        this.companies = companies
    }

    fun setItems(items: ArrayList<ItemType>?) {
        this.items = items
        Log.i("test", Gson().toJson(items))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        if (DP_16_TO_PX == -1f) DP_16_TO_PX = ScreenUtils.dpToPx(parent.context, 8f)
        if (STATUS_STRING_ARRAY == null) STATUS_STRING_ARRAY = parent.context.resources.getStringArray(R.array.item_status_description)
        if (statusSubtextColor == -1) {
            statusTitleColor = parent.context.resources.getColor(R.color.package_list_status_title_color)
            statusSubtextColor = parent.context.resources.getColor(R.color.package_list_status_subtext_color)
        }

        return when (viewType) {
            ItemType.TYPE_EMPTY -> EmptyHolder(
                    LayoutInflater.from(parent.context)
                            .inflate(R.layout.item_search_result_empty, parent, false)
            )
            ItemType.TYPE_SUBHEADER -> SubheaderItemHolder(
                    LayoutInflater.from(parent.context)
                            .inflate(R.layout.item_list_details_info_subheader, parent, false)
            )
            ItemType.TYPE_COMPANY -> CompanyHolder(
                    LayoutInflater.from(parent.context)
                            .inflate(R.layout.item_list_company, parent, false)
            )
            ItemType.TYPE_PACKAGE -> PackageHolder(
                    LayoutInflater.from(parent.context)
                            .inflate(R.layout.item_list_package_for_home, parent, false)
            )
            ItemType.TYPE_NEW_PACKAGE -> ClickableHolder(
                    LayoutInflater.from(parent.context)
                            .inflate(R.layout.item_search_result_new_package, parent, false)
            )
            else -> throw IllegalArgumentException("Unknown item type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemType = items!![position]
        when (items!![position].viewType) {
            ItemType.TYPE_EMPTY -> {
                val eh = holder as EmptyHolder
                eh.title.setText(
                        if (if (position == 1) packages == null else companies == null)
                            R.string.item_title_please_wait
                        else
                            R.string.search_no_result
                )
            }
            ItemType.TYPE_SUBHEADER -> {
                val h0 = holder as SubheaderItemHolder
                h0.title.setText(if (position > 0) R.string.subheader_company else R.string.subheader_package)
            }
            ItemType.TYPE_COMPANY -> {
                val h1 = holder as CompanyHolder
                h1.titleText.text = companies!![itemType.index].name
                h1.otherText.text = if (companies!![itemType.index].phone != null) companies!![itemType.index].phone else companies!![itemType.index].website
                h1.otherText.visibility = if (h1.otherText.text != null) View.VISIBLE else View.INVISIBLE

                /** Set up the logo  */
                h1.logoView.setImageDrawable(ColorDrawable(ColorGenerator.MATERIAL.getColor(companies!![itemType.index].name)))
                h1.firstCharText.text = companies!![itemType.index].name.substring(0, 1)

                h1.rootView.setOnClickListener {
                    val phone = companies!![itemType.index].phone
                    if (phone != null && !TextUtils.isEmpty(phone)) {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse("tel:" + phone)
                        parentActivity.startActivity(intent)
                    }
                }
            }
            ItemType.TYPE_PACKAGE -> {
                val h2 = holder as PackageHolder
                val p = packages!![itemType.index]

                h2.titleText.text = p.name
                if (p.data!!.size > 0) {
                    val status = p.data!![0]
                    val spanny = Spanny(STATUS_STRING_ARRAY!![p.getState()], ForegroundColorSpan(statusTitleColor))
                            .append(" - " + status.context!!, ForegroundColorSpan(statusSubtextColor))
                    h2.descText.text = spanny
                    h2.timeText.text = status.ftime
                    h2.timeText.visibility = View.VISIBLE
                } else {
                    /** Set placeholder when cannot get data  */
                    h2.descText.setText(R.string.item_text_cannot_get_package_status)
                    h2.timeText.visibility = View.GONE
                }

                /** Set CircleImageView  */
                h2.bigCharView.text = p.name!!.substring(0, 1)
                h2.logoView.setImageDrawable(ColorDrawable(ColorGenerator.MATERIAL.getColor(p.name!!)))

                /** Add paddingTop/Bottom to the first or last item  */
                if (position == 0) {
                    h2.containerView.setPadding(0, DP_16_TO_PX.toInt(), 0, 0)
                } else if (position == itemCount) {
                    h2.containerView.setPadding(0, 0, 0, DP_16_TO_PX.toInt())
                }

                h2.containerView.setOnClickListener { DetailsActivity.launch(parentActivity, p) }
            }
        }
    }

    override fun getItemCount(): Int = items?.size ?: 0

    override fun getItemViewType(index: Int): Int = items!![index].viewType

    class ItemType(var viewType: Int) {

        // Optional
        var index: Int = 0

        companion object {

            const val TYPE_PACKAGE = 0
            const val TYPE_COMPANY = 1
            const val TYPE_SUBHEADER = 2
            const val TYPE_EMPTY = 3
            const val TYPE_NEW_PACKAGE = 4

        }

    }

    private inner class ClickableHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener { view ->
                if (view.context is SearchActivity) {
                    (view.context as SearchActivity).onAddButtonClicked()
                }
            }
        }

    }

    private inner class PackageHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var logoView: CircleImageView = itemView.findViewById(R.id.iv_logo)
        internal var titleText: AppCompatTextView = itemView.findViewById(R.id.tv_title)
        internal var descText: AppCompatTextView = itemView.findViewById(R.id.tv_other)
        internal var timeText: AppCompatTextView = itemView.findViewById(R.id.tv_time)
        internal var bigCharView: TextView = itemView.findViewById(R.id.tv_first_char)

        internal val containerView: View = itemView.findViewById(R.id.item_container)

    }

    private inner class SubheaderItemHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var title: AppCompatTextView = itemView.findViewById(R.id.tv_title)

    }

    private inner class EmptyHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var title: AppCompatTextView = itemView.findViewById(R.id.tv_title)

    }

    private inner class CompanyHolder internal constructor(internal var rootView: View) : RecyclerView.ViewHolder(rootView) {

        internal var titleText: AppCompatTextView = rootView.findViewById(R.id.tv_title)
        internal var otherText: AppCompatTextView = rootView.findViewById(R.id.tv_other)
        internal var logoView: CircleImageView = rootView.findViewById(R.id.iv_logo)
        internal var firstCharText: TextView = rootView.findViewById(R.id.tv_first_char)

    }

}
