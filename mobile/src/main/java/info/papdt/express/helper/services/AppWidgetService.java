package info.papdt.express.helper.services;

import android.content.Intent;
import android.widget.RemoteViewsService;

import info.papdt.express.helper.ui.launcher.ListFactory;

public class AppWidgetService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return (new ListFactory(this.getApplicationContext(), intent));
	}

}
