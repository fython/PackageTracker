package info.papdt.express.helper.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

import info.papdt.express.helper.R;
import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.support.PushUtils;
import info.papdt.express.helper.support.Settings;
import info.papdt.express.helper.ui.DetailsActivity;

public class ReminderService extends IntentService {

	private static final String TAG = ReminderService.class.getSimpleName();

	private static final int ID = 100000;

	public ReminderService() {
		super(TAG);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onHandleIntent(Intent intent) {
		boolean isEnabledDontDisturbMode = Settings.getInstance(getApplicationContext())
				.getBoolean(Settings.KEY_NOTIFICATION_DO_NOT_DISTURB, true);
		if (isEnabledDontDisturbMode && PushUtils.isDisturbTime(Calendar.getInstance())) {
			Log.i(TAG, "现在是勿扰时间段，跳过检查。");
			return;
		}
		Log.i(TAG, "开始检查快递包裹");

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		PackageDatabase db = PackageDatabase.getInstance(getApplicationContext());

		db.pullDataFromNetwork(false);
		db.save();

		for (int i = 0; i < db.size(); i++) {
			Package p = db.get(i);
			if (p.getState() != Package.STATUS_FAILED && p.shouldPush) {
				Log.i(TAG, "包裹 " + i + " 需要产生通知");
				Notification n = produceNotifications(i, p);
				nm.notify(i + 20000, n);
				p.shouldPush = false;
			}
		}

		db.save();
	}

	private static int parseDefaults(Context context) {
		Settings settings = Settings.getInstance(context);

		return (settings.getBoolean(Settings.KEY_NOTIFICATION_SOUND, true) ? Notification.DEFAULT_SOUND : 0) |
				(settings.getBoolean(Settings.KEY_NOTIFICATION_VIBRATE, true) ? Notification.DEFAULT_VIBRATE : 0) |
				Notification.DEFAULT_LIGHTS;
	}

	@SuppressWarnings("getNotification")
	private static Notification buildNotification(Context context, String title, String text, int icon, int color,
	                                              int defaults, PendingIntent contentIntent, PendingIntent deleteIntent) {
		Notification n;
		Notification.Builder builder = new Notification.Builder(context)
				.setContentTitle(title)
				.setContentText(text)
				.setSmallIcon(icon)
				.setDefaults(defaults)
				.setAutoCancel(true)
				.setContentIntent(contentIntent);

		if (Build.VERSION.SDK_INT >= 16) {
			if (Build.VERSION.SDK_INT >= 21) {
				builder.setColor(color);
			}
			n = builder.build();
		} else {
			n = builder.getNotification();
		}

		return n;
	}

	private Notification produceNotifications(int position, Package exp) {
		if (exp != null) {
			int defaults = parseDefaults(getApplicationContext());

			PendingIntent pi;

			Intent i = new Intent(getApplicationContext(), DetailsActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			i.putExtra("extra_package_json", exp.toJsonString());
			i.putExtra("extra_state", exp.getState());

			pi = PendingIntent.getActivity(getApplicationContext(), position, i, PendingIntent.FLAG_UPDATE_CURRENT);

			String title = exp.name;
			if (exp.getState() == Package.STATUS_DELIVERED) {
				title += getString(R.string.notification_delivered);
			} else {
				if (exp.getState() == Package.STATUS_ON_THE_WAY) {
					title += getString(R.string.notification_on_the_way);
				} else {
					title += getString(R.string.notification_new_message);
				}
			}

			int smallIcon;
			switch (exp.getState()) {
				case Package.STATUS_DELIVERED:
					smallIcon = R.drawable.ic_done_white_24dp;
					break;
				case Package.STATUS_ON_THE_WAY:
					smallIcon = R.drawable.ic_assignment_turned_in_white_24dp;
					break;
				default:
					smallIcon = R.drawable.ic_assignment_returned_white_24dp;
			}

			Notification n = buildNotification(getApplicationContext(),
					title,
					exp.data.get(exp.data.size() - 1).context,
					smallIcon,
					getResources().getIntArray(R.array.statusColor) [exp.getState()],
					defaults,
					pi,
					null);

			n.tickerText = title;

			return n;
		}
		return null;
	}

}
