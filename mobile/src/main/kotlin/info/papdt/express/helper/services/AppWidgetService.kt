package info.papdt.express.helper.services

import android.content.Intent
import android.widget.RemoteViewsService

import info.papdt.express.helper.ui.launcher.ListFactory

class AppWidgetService : RemoteViewsService() {

	override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory {
		return ListFactory(applicationContext, intent)
	}

}
