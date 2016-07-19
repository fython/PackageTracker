package info.papdt.express.helper.ui.launcher;

import android.app.Application;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import info.papdt.express.helper.R;
import info.papdt.express.helper.services.AppWidgetService;
import info.papdt.express.helper.ui.DetailsActivity;

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
		for (int appWidgetId : appWidgetIds) {
			RemoteViews remoteViews = updateWidgetListView(context, appWidgetId);
			manager.updateAppWidget(appWidgetId, remoteViews);
		}
		super.onUpdate(context, manager, appWidgetIds);
	}

	private RemoteViews updateWidgetListView(Context context, int id) {
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.launcher_widget_layout);

		Intent intent = new Intent(context, AppWidgetService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

		views.setRemoteAdapter(R.id.widget_list_view, intent);
		views.setEmptyView(R.id.widget_list_view, R.id.empty_view);

		Intent tempIntent = new Intent(context, DetailsActivity.class);
		tempIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		views.setPendingIntentTemplate(R.id.widget_list_view, PendingIntent.getActivity(context, 0, tempIntent, PendingIntent.FLAG_CANCEL_CURRENT));

		return views;
	}

	public static void updateManually(Application app) {
		int[] ids = AppWidgetManager.getInstance(app).getAppWidgetIds(new ComponentName(app, AppWidgetProvider.class));
		AppWidgetManager.getInstance(app).notifyAppWidgetViewDataChanged(ids, R.id.widget_list_view);
	}

}
