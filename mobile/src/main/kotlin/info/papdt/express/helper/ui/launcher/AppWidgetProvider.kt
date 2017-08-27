package info.papdt.express.helper.ui.launcher

import android.app.Application
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

import info.papdt.express.helper.R
import info.papdt.express.helper.services.AppWidgetService
import info.papdt.express.helper.ui.DetailsActivity

class AppWidgetProvider : android.appwidget.AppWidgetProvider() {

	override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
		for (appWidgetId in appWidgetIds) {
			val remoteViews = updateWidgetListView(context, appWidgetId)
			manager.updateAppWidget(appWidgetId, remoteViews)
		}
		super.onUpdate(context, manager, appWidgetIds)
	}

	private fun updateWidgetListView(context: Context, id: Int): RemoteViews {
		val views = RemoteViews(context.packageName, R.layout.launcher_widget_layout)

		val intent = Intent(context, AppWidgetService::class.java)
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
		intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

		views.setRemoteAdapter(R.id.widget_list_view, intent)
		views.setEmptyView(R.id.widget_list_view, R.id.empty_view)

		val tempIntent = Intent(context, DetailsActivity::class.java)
		tempIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		views.setPendingIntentTemplate(R.id.widget_list_view, PendingIntent.getActivity(context, 0, tempIntent, PendingIntent.FLAG_CANCEL_CURRENT))

		return views
	}

	companion object {

		fun updateManually(app: Application) {
			val ids = AppWidgetManager.getInstance(app).getAppWidgetIds(ComponentName(app, AppWidgetProvider::class.java))
			AppWidgetManager.getInstance(app).notifyAppWidgetViewDataChanged(ids, R.id.widget_list_view)
		}

	}

}
