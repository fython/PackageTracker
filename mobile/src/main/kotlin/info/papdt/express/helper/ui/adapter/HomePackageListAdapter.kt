package info.papdt.express.helper.ui.adapter

import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.text.style.ForegroundColorSpan
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import de.hdodenhof.circleimageview.CircleImageView
import info.papdt.express.helper.R
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.model.Kuaidi100Package
import info.papdt.express.helper.support.ColorGenerator
import info.papdt.express.helper.support.Spanny
import info.papdt.express.helper.support.isFontProviderEnabled
import info.papdt.express.helper.ui.DetailsActivity
import info.papdt.express.helper.ui.MainActivity
import moe.feng.kotlinyan.common.*

class HomePackageListAdapter(private var db: PackageDatabase?, private val type: Int, private val parentActivity: AppCompatActivity) : RecyclerView.Adapter<HomePackageListAdapter.MyViewHolder>() {

	private var statusTitleColor: Int = 0
	private var statusSubtextColor = -1
	private var STATUS_STRING_ARRAY: Array<String>? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
		if (STATUS_STRING_ARRAY == null) STATUS_STRING_ARRAY = parent.context.resources.getStringArray(R.array.item_status_description)
		if (statusSubtextColor == -1) {
			statusTitleColor = parent.context.resources.getColor(R.color.package_list_status_title_color)
			statusSubtextColor = parent.context.resources.getColor(R.color.package_list_status_subtext_color)
		}

		val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_list_package_for_home, parent, false)
		return MyViewHolder(itemView)
	}

	override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
		val p = getItemData(position)

		if (p.name?.isNotEmpty() == true) {
			holder.titleText.text = p.name
		} else {
			holder.titleText.text = p.companyChineseName
		}
		if (p.data != null && p.data!!.size > 0) {
			val status = p.data!![0]
			val spanny = Spanny(STATUS_STRING_ARRAY!![p.getState()], ForegroundColorSpan(statusTitleColor))
					.append(" - " + status.context, ForegroundColorSpan(statusSubtextColor))
			holder.descText.text = spanny
			holder.timeText.text = status.ftime
			holder.timeText.visibility = View.VISIBLE
		} else {
			/** Set placeholder when cannot get data  */
			holder.descText.setText(R.string.item_text_cannot_get_package_status)
			holder.timeText.visibility = View.GONE
		}

		/** Set bold text when unread  */
		holder.descText.paint.isFakeBoldText = p.unreadNew
		holder.titleText.paint.isFakeBoldText = p.unreadNew || !isFontProviderEnabled

		/** Set CircleImageView  */
		if (p.name?.isNotEmpty() == true) {
			holder.bigCharView.text = p.name!!.substring(0, 1).toUpperCase()
			holder.logoView.setImageDrawable(ColorDrawable(ColorGenerator.MATERIAL.getColor(p.name!!)))
		} else if (p.companyChineseName?.isNotEmpty() == true) {
			holder.bigCharView.text = p.companyChineseName!!.substring(0, 1).toUpperCase()
			holder.logoView.setImageDrawable(ColorDrawable(ColorGenerator.MATERIAL.getColor(p.companyChineseName!!)))
		}

		/** Add paddingTop/Bottom to the first or last item  */
		if (position == 0) {
			holder.containerView.setPadding(0, 8.dp.toInt(), 0, 0)
		} else if (position == itemCount) {
			holder.containerView.setPadding(0, 0, 0, 8.dp.toInt())
		}
	}

	fun getItemData(pos: Int): Kuaidi100Package = when (type) {
		TYPE_DELIVERED -> db!!.deliveredData[db!!.deliveredData.size - pos - 1]
		TYPE_DELIVERING -> db!!.deliveringData[db!!.deliveringData.size - pos - 1]
		TYPE_ALL -> db!![db!!.size() - pos - 1]
		else -> db!![db!!.size() - pos - 1]
	}


	fun setDatabase(db: PackageDatabase) {
		this.db = db
		notifyDataSetChanged()
	}

	override fun getItemCount(): Int = when (type) {
		TYPE_DELIVERED -> db?.deliveredData?.size
		TYPE_DELIVERING -> db?.deliveringData?.size
		TYPE_ALL -> db?.size()
		else -> db?.size()
	} ?: 0

	inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {

		internal var logoView: CircleImageView = itemView.findViewById(R.id.iv_logo)
		internal var titleText: AppCompatTextView = itemView.findViewById(R.id.tv_title)
		internal var descText: AppCompatTextView = itemView.findViewById(R.id.tv_other)
		internal var timeText: AppCompatTextView = itemView.findViewById(R.id.tv_time)
		internal var bigCharView: TextView = itemView.findViewById(R.id.tv_first_char)

		val containerView: View = itemView.findViewById(R.id.item_container)

		init {
			containerView.setOnCreateContextMenuListener(this)
			containerView.setOnClickListener { DetailsActivity.launch(parentActivity, getItemData(adapterPosition)) }
		}

		override fun onCreateContextMenu(menu: ContextMenu, v: View?, info: ContextMenu.ContextMenuInfo?) {
			(parentActivity as? MainActivity)?.onContextMenuCreate(getItemData(adapterPosition))

			menu.setHeaderTitle(getItemData(adapterPosition).name)
			if (!getItemData(adapterPosition).unreadNew) {
				menu.add(Menu.NONE, R.id.action_set_unread, 0, R.string.action_set_unread)
			}
			menu.add(Menu.NONE, R.id.action_share, 0, R.string.action_share)
			menu.add(Menu.NONE, R.id.action_delete, 0, R.string.action_remove)
		}
	}

	companion object {

		val TYPE_ALL = 0
		val TYPE_DELIVERED = 1
		val TYPE_DELIVERING = 2

	}

}
