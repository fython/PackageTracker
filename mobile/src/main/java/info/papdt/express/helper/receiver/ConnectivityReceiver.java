package info.papdt.express.helper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import info.papdt.express.helper.support.PushUtils;

public class ConnectivityReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (readNetworkState(context)) {
			Log.i("ConnectivityReceiver", "Start ReminderService.");
			PushUtils.startServices(context);
		} else {
			Log.i("ConnectivityReceiver", "Stop ReminderService.");
			PushUtils.stopServices(context);
		}
	}

	public static boolean readNetworkState(Context context) {
		if (context == null) {
			return false;
		}

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
	}

}