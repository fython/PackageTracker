package info.papdt.express.helper.ui.launcher

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import android.text.style.ForegroundColorSpan
import android.widget.RemoteViews
import android.widget.RemoteViewsService

import info.papdt.express.helper.R
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.model.Kuaidi100Package
import info.papdt.express.helper.support.ScreenUtils
import info.papdt.express.helper.support.Spanny

class ListFactory(private val mContext: Context, intent: Intent) : RemoteViewsService.RemoteViewsFactory {

	private val mAppWidgetId: Int = intent.getIntExtra(
			AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
	)

	private val mDatabase: PackageDatabase = PackageDatabase.getInstance(mContext.applicationContext)

	private val DP_16_TO_PX: Float = ScreenUtils.dpToPx(mContext, 8f)
	private val statusTitleColor: Int = ContextCompat.getColor(mContext, R.color.package_list_status_title_color)
	private val statusSubtextColor: Int = ContextCompat.getColor(mContext, R.color.package_list_status_subtext_color)
	private val STATUS_STRING_ARRAY: Array<String> = mContext.resources.getStringArray(R.array.item_status_description)
	private val STATUS_ERROR: String = mContext.getString(R.string.item_text_cannot_get_package_status)

	override fun onCreate() {

	}

	override fun onDataSetChanged() {

	}

	override fun onDestroy() {

	}

	private fun getDeliveringData(): List<Kuaidi100Package> {
		return mDatabase.data.filter {
			it.getState() == Kuaidi100Package.STATUS_ON_THE_WAY ||
					it.getState() == Kuaidi100Package.STATUS_RETURNING ||
					it.getState() == Kuaidi100Package.STATUS_NORMAL
		}
	}

	override fun getCount(): Int {
		return getDeliveringData().size
	}

	override fun getViewAt(i: Int): RemoteViews {
		val views = RemoteViews(mContext.packageName, R.layout.item_list_package_for_widget)

		val p = getDeliveringData()[i]

		views.setTextViewText(R.id.tv_title, p.name)

		if (p.data != null && p.data!!.size > 0) {
			val status = p.data!![0]
			val spanny = Spanny(STATUS_STRING_ARRAY[p.getState()], ForegroundColorSpan(statusTitleColor))
					.append(" - " + status.context, ForegroundColorSpan(statusSubtextColor))
			views.setTextViewText(R.id.tv_other, spanny)
		} else {
			/** Set placeholder when cannot get data  */
			views.setTextViewText(R.id.tv_other, STATUS_ERROR)
		}

		/** Set CircleImageView  */
		views.setTextViewText(R.id.tv_first_char, p.getFirstChar())
		val packagePalette = p.getPaletteFromId()
		val b = ScreenUtils.drawableToBitmap(ColorDrawable(packagePalette["100"]))
		views.setImageViewBitmap(R.id.iv_logo, b)
        views.setTextColor(R.id.tv_first_char, packagePalette["800"])

		/** Add paddingTop/Bottom to the first or last item  */
		if (i == 0) {
			views.setViewPadding(R.id.item_container, 0, DP_16_TO_PX.toInt(), 0, 0)
		} else if (i == count) {
			views.setViewPadding(R.id.item_container, 0, 0, 0, DP_16_TO_PX.toInt())
		}

		val intent = Intent()
		intent.putExtra(EXTRA_PACKAGE_JSON, p.toJsonString())
		intent.putExtra(EXTRA_STATE, p.getState())
		views.setOnClickFillInIntent(R.id.item_container, intent)

		return views
	}

	override fun getLoadingView(): RemoteViews? {
		return null
	}

	override fun getViewTypeCount(): Int {
		return 1
	}

	override fun getItemId(i: Int): Long {
		return i.toLong()
	}

	override fun hasStableIds(): Boolean {
		return true
	}

	companion object {

		private const val EXTRA_PACKAGE_JSON = "extra_package_json"
		private const val EXTRA_STATE = "extra_state"

	}

}
