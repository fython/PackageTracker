package info.papdt.express.helper.ui.items

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import de.hdodenhof.circleimageview.CircleImageView
import info.papdt.express.helper.R
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.event.EventIntents
import info.papdt.express.helper.model.Kuaidi100Package
import info.papdt.express.helper.model.MaterialIcon
import info.papdt.express.helper.support.Spanny
import info.papdt.express.helper.support.isFontProviderEnabled
import info.papdt.express.helper.support.localBroadcastManager
import info.papdt.express.helper.ui.DetailsActivity
import me.drakeet.multitype.ItemViewBinder
import moe.feng.kotlinyan.common.set
import java.text.DateFormat

object PackageItemViewBinder
    : ItemViewBinder<Kuaidi100Package, PackageItemViewBinder.ViewHolder>() {

    private var statusTitleColor: Int = 0
    private var statusSubtextColor = -1
    private var STATUS_STRING_ARRAY: Array<String>? = null

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        if (STATUS_STRING_ARRAY == null) {
            STATUS_STRING_ARRAY = parent.context.resources
                    .getStringArray(R.array.item_status_description)
        }
        if (statusSubtextColor == -1) {
            statusTitleColor = ContextCompat.getColor(parent.context,
                    R.color.package_list_status_title_color)
            statusSubtextColor = ContextCompat.getColor(parent.context,
                    R.color.package_list_status_subtext_color)
        }

        return ViewHolder(inflater.inflate(R.layout.item_home_package, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, item: Kuaidi100Package) {
        val context = holder.itemView.context

        holder.itemData = item

        if (item.name?.isNotEmpty() == true) {
            holder.titleText.text = item.name
        } else {
            holder.titleText.text = item.companyChineseName
        }
        if (item.data != null && item.data!!.size > 0) {
            val status = item.data!![0]
            val spanny = Spanny(STATUS_STRING_ARRAY!![item.getState()], ForegroundColorSpan(statusTitleColor))
                    .append(" - " + status.context, ForegroundColorSpan(statusSubtextColor))
            holder.descText.text = spanny
            holder.timeText.text = status.getTimeDate()?.let {
                DateFormat.getDateTimeInstance().format(it)
            } ?: status.ftime
            holder.timeText.visibility = View.VISIBLE
        } else {
            /** Set placeholder when cannot get data  */
            holder.descText.setText(R.string.item_text_cannot_get_package_status)
            holder.timeText.visibility = View.GONE
        }

        /** Set bold text when unread  */
        holder.descText.paint.isFakeBoldText = item.unreadNew
        holder.titleText.paint.isFakeBoldText = item.unreadNew || !isFontProviderEnabled

        /** Set CircleImageView  */
        holder.bigCharView.apply {
            if (item.iconCode?.isNotEmpty() == true) {
                typeface = MaterialIcon.iconTypeface
                paint.isFakeBoldText = false
                text = item.iconCode
            } else {
                typeface = Typeface.DEFAULT
                paint.isFakeBoldText = true
                if (item.name?.isNotEmpty() == true) {
                    text = item.name!!.substring(0, 1).toUpperCase()
                } else if (item.companyChineseName?.isNotEmpty() == true) {
                    text = item.companyChineseName!!.substring(0, 1).toUpperCase()
                }
            }
        }

        item.getPaletteFromId().let {
            holder.logoView.setImageDrawable(ColorDrawable(it.getPackageIconBackground(context)))
            holder.bigCharView.setTextColor(it.getPackageIconForeground(context))
            holder.statusIcon.imageTintList = ColorStateList.valueOf(it.getStatusIconTint(context))
        }

        holder.statusIcon.setImageResource(when (item.getState()) {
            Kuaidi100Package.STATUS_FAILED -> R.drawable.ic_error_outline_black_24dp
            Kuaidi100Package.STATUS_DELIVERED -> R.drawable.ic_done_black_24dp
            Kuaidi100Package.STATUS_RETURNED -> R.drawable.ic_done_black_24dp
            Kuaidi100Package.STATUS_NORMAL -> R.drawable.ic_local_shipping_black_24dp
            Kuaidi100Package.STATUS_ON_THE_WAY -> R.drawable.ic_local_shipping_black_24dp
            Kuaidi100Package.STATUS_RETURNING -> R.drawable.ic_local_shipping_black_24dp
            Kuaidi100Package.STATUS_OTHER -> R.drawable.ic_help_outline_black_24dp
            else -> R.drawable.ic_help_outline_black_24dp
        })
    }

    class ViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        internal val logoView: CircleImageView = itemView.findViewById(R.id.iv_logo)
        internal val titleText: AppCompatTextView = itemView.findViewById(R.id.tv_title)
        internal val descText: AppCompatTextView = itemView.findViewById(R.id.tv_other)
        internal val timeText: AppCompatTextView = itemView.findViewById(R.id.tv_time)
        internal val bigCharView: TextView = itemView.findViewById(R.id.tv_first_char)
        internal val statusIcon: ImageView = itemView.findViewById(R.id.status_icon)

        val containerView: View = itemView.findViewById(R.id.item_container)

        var itemData: Kuaidi100Package? = null

        init {
            containerView.setOnCreateContextMenuListener(this)
            containerView.setOnClickListener {
                if (itemData!!.unreadNew) {
                    itemData!!.unreadNew = false
                    val database = PackageDatabase.getInstance(itemView.context)
                    val position = database.indexOf(itemData!!)
                    if (position != -1) {
                        database[position] = itemData!!
                        adapter.notifyItemChanged(adapterPosition)
                    }
                }
                DetailsActivity.launch(it.context as Activity, itemData!!)
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu,
                                         v: View, info: ContextMenu.ContextMenuInfo?) {
            menu.setHeaderTitle(itemData!!.name)
            val menuItems = mutableListOf<MenuItem>()
            if (!itemData!!.unreadNew) {
                menuItems += menu.add(Menu.NONE, R.id.action_set_unread, 0, R.string.action_set_unread)
            }
            menuItems += menu.add(Menu.NONE, R.id.action_share, 0, R.string.action_share)
            menuItems += menu.add(Menu.NONE, R.id.action_delete, 0, R.string.action_remove)
            menuItems.forEach { it.setOnMenuItemClickListener(this) }
        }

        override fun onMenuItemClick(item: MenuItem): Boolean = when (item.itemId) {
            R.id.action_set_unread -> {
                val database = PackageDatabase.getInstance(itemView.context)
                itemData!!.unreadNew = true
                val position = database.indexOf(itemData!!)
                if (position != -1) {
                    database[position] = itemData!!
                    adapter.notifyItemChanged(adapterPosition)
                }
                true
            }
            R.id.action_share -> {
                val text = itemView.context.getString(R.string.share_info_format,
                        itemData!!.name,
                        itemData!!.number,
                        itemData!!.companyChineseName,
                        if (itemData!!.data!!.size > 0) itemData!!.data!![0].context else "Unknown",
                        if (itemData!!.data!!.size > 0) itemData!!.data!![0].time else ""
                )

                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent[Intent.EXTRA_TEXT] = text
                itemView.context.startActivity(Intent.createChooser(
                        intent, itemView.context.getString(R.string.dialog_share_title)))
                true
            }
            R.id.action_delete -> {
                itemData?.let { EventIntents.requestDeletePackage(it) }
                        ?.let { itemView.context.localBroadcastManager.sendBroadcast(it) }
                true
            }
            else -> false
        }

    }

}