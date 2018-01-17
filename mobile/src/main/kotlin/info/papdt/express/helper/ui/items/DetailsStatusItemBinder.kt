package info.papdt.express.helper.ui.items

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import info.papdt.express.helper.R
import info.papdt.express.helper.drawable.TextDrawable
import info.papdt.express.helper.model.Package
import info.papdt.express.helper.view.VerticalStepIconView
import info.papdt.express.helper.view.VerticalStepLineView
import me.drakeet.multitype.ItemViewBinder

class DetailsStatusItemBinder : ItemViewBinder<Package.Status, DetailsStatusItemBinder.ItemHolder>() {

    private var mPackage: Package? = null
    private val isShowed = BooleanArray(1000)
    var showChiba = false // 是否为慢递

    fun setData(src: Package?) {
        mPackage = src
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ItemHolder {
        return ItemHolder(inflater.inflate(R.layout.item_list_details_info_status, parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, item: Package.Status) {
        holder.setData(item)
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var data: Package.Status? = null

        var title: AppCompatTextView = itemView.findViewById(R.id.tv_title)
        var time: AppCompatTextView = itemView.findViewById(R.id.tv_time)
        var stepIcon: VerticalStepIconView = itemView.findViewById(R.id.step_icon_view)
        var stepLine: VerticalStepLineView = itemView.findViewById(R.id.step_line_view)

        var contactCard: CardView = itemView.findViewById(R.id.contact_card)
        var phoneView: AppCompatTextView = itemView.findViewById(R.id.contact_number)

        init {
            val callPhone = View.OnClickListener { view ->
                if (!TextUtils.isEmpty(phoneView.text.toString())) {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:" + phoneView.text.toString())
                    view.context.startActivity(intent)
                }
            }
            contactCard.setOnClickListener(callPhone)
            itemView.findViewById<View>(R.id.btn_call_contact).setOnClickListener(callPhone)
        }

        fun setData(newData: Package.Status) {
            this.data = newData

            /** Show time and location (if available)  */
            var timeText = data!!.time
            val location = data!!.getLocation()
            if (location != null) {
                timeText += "  ·  " + location
            }
            time.text = timeText
            /** Show status context */
            val context = data!!.context
            title.text = context

            /** Show contact card if available  */
            val phone = data!!.getPhone()
            if (phone != null) phoneView.text = phone
            contactCard.visibility = if (phone != null) View.VISIBLE else View.GONE

            /** Set up step view style  */
            val indexInStatus = adapterPosition - 3
            if (indexInStatus == 0) {
                stepIcon.setIsMini(false)
                stepLine.setLineShouldDraw(false, mPackage!!.data!!.size > 1)
                if (mPackage!!.data!!.size > 1) {
                    stepIcon.setPointOffsetY(-time.textSize)
                    stepLine.setPointOffsetY(-time.textSize)
                }
                val pointColorResId: Int
                val pointIconResId: Int
                when (mPackage!!.getState()) {
                    Package.STATUS_DELIVERED -> {
                        pointColorResId = R.color.green_500
                        pointIconResId = R.drawable.ic_done_white_24dp
                    }
                    Package.STATUS_FAILED, Package.STATUS_OTHER -> {
                        pointColorResId = R.color.red_500
                        pointIconResId = R.drawable.ic_close_white_24dp
                    }
                    Package.STATUS_RETURNED -> {
                        pointColorResId = R.color.brown_500
                        pointIconResId = R.drawable.ic_assignment_return_white_24dp
                    }
                    Package.STATUS_ON_THE_WAY -> {
                        pointColorResId = R.color.blue_700
                        pointIconResId = R.drawable.ic_local_shipping_white_24dp
                    }
                    Package.STATUS_NORMAL -> {
                        pointColorResId = R.color.blue_500
                        pointIconResId = R.drawable.ic_flight_white_24dp
                    }
                    else -> {
                        pointColorResId = R.color.blue_500
                        pointIconResId = R.drawable.ic_flight_white_24dp
                    }
                }
                stepIcon.setPointColorResource(pointColorResId)
                stepIcon.setCenterIcon(pointIconResId)
                if (showChiba) {
                    stepIcon.setCenterIcon(TextDrawable.builder()
                            .beginConfig()
                            .fullSizeFont()
                            .endConfig()
                            .buildRound("\uD83D\uDC22", Color.TRANSPARENT))
                }
            } else {
                stepIcon.setIsMini(true)
                stepIcon.setPointColorResource(R.color.blue_grey_500)
                stepLine.setPointOffsetY(-time.textSize)
                stepIcon.setPointOffsetY(-time.textSize)
                stepIcon.setCenterIcon(null)
                stepLine.setLineShouldDraw(true, true)
            }
            if (indexInStatus == mPackage!!.data!!.size - 1 && mPackage!!.data!!.size > 1) {
                stepLine.setLineShouldDraw(true, false)
                stepLine.setPointOffsetY(0f)
                stepIcon.setPointOffsetY(0f)
                stepIcon.setCenterIcon(null)
            }
            synchronized(this) {
                if (!isShowed[indexInStatus]) {
                    synchronized(this) {
                        isShowed[indexInStatus] = true
                    }
                    stepIcon.scaleX = 0f
                    stepIcon.scaleY = 0f
                    stepIcon.animate().scaleX(1f).scaleY(1f)
                            .setStartDelay((100 * (indexInStatus + 1)).toLong())
                            .setDuration(400).setInterpolator(OvershootInterpolator()).start()
                }
            }
        }

    }

}
