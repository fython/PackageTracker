package info.papdt.express.helper.services;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import info.papdt.express.helper.R;
import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.ui.AddActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DetectNumberService extends AccessibilityService {

	private PackageDatabase mPackageDatabase;
	private String mShowingNumber = null;
	private NotificationManager mNotificationManager;
	private NotificationActionReceiver mActionReceiver;

	private String lastCompany = null, lastName = null;

	public static final int NOTIFICATION_ID_ASSIST = 100;

	private static final String PACKAGE_TAOBAO = "com.taobao.taobao", PACKAGE_JD = "com.jingdong.app.mall";

	private static final String TAOBAO_WAYBILL_VIEW_ID = "tv_logistic_waybill_number",
			TAOBAO_COMPANY_VIEW_ID = "tv_logistic_company",
			JD_TEXT_RIGHT_VIEW_ID = "text_right",
			JD_TEXT_LEFT_VIEW_ID = "text_left",
			JD_TEXT_MSG_ITEM_VIEW_ID = "item_msg",
			JD_ORDER_ID_CONTENT_VIEW_ID = "order_id_content",
			JD_DELIVERY_WAY_CONTENT_VIEW_ID = "delivery_way_content",
			JD_WARE_INFO_NAME_VIEW_ID = "ware_info_name";

	private static final String TAOBAO_CHECK_ACTIVITY = "com.taobao.cainiao.logistic.LogisticDetailActivity";
	private static final String JD_CHECK_ACTIVITY = "com.jd.lib.ordercenter.logisticstrack.LogisticsTrackActivity";
	private static final String JD_CHECK_ACTIVITY_2 = "com.jd.lib.ordercenter.neworderdetail.NewOrderDetailActivity";

	private static final String ACTION_DELETE_ASSIST_NOTI = "info.papdt.express.helper.ACTION_DELETE_ASSIST_NOTI";

	public static final String TAG = DetectNumberService.class.getSimpleName();

	@Override
	public void onServiceConnected() {
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mPackageDatabase = PackageDatabase.getInstance(this);
		getServiceInfo().packageNames = new String[] {
				PACKAGE_TAOBAO,
				PACKAGE_JD
		};
		setServiceInfo(getServiceInfo());

		try {
			if (mActionReceiver != null) {
				unregisterReceiver(mActionReceiver);
			}
			mActionReceiver = new NotificationActionReceiver();
			registerReceiver(mActionReceiver, new IntentFilter(ACTION_DELETE_ASSIST_NOTI));
		} catch (Exception e) {

		}
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		AccessibilityNodeInfo nodeInfo = event.getSource();
		switch (event.getEventType()) {
			case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
				Log.i(TAG, event.getClassName().toString());
				switch (event.getPackageName().toString()) {
					case PACKAGE_TAOBAO:
						if (!TAOBAO_CHECK_ACTIVITY.equals(event.getClassName().toString())) {
							mShowingNumber = null;
							lastCompany = null;
							lastName = null;
						}
						break;
					case PACKAGE_JD:
						if (!JD_CHECK_ACTIVITY.equals(event.getClassName().toString()) &&
								!JD_CHECK_ACTIVITY_2.equals(event.getClassName().toString())) {
							mShowingNumber = null;
							lastCompany = null;
							lastName = null;
						}
						break;
				}
			case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
			case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
				switch (event.getPackageName().toString()) {
					case PACKAGE_TAOBAO:
						try {
							String number = null, company = null;
							List<AccessibilityNodeInfo> nodes
									= search(0, nodeInfo, TAOBAO_WAYBILL_VIEW_ID);
							for (AccessibilityNodeInfo node : nodes) {
								number = getPackageNumber(node.getText().toString());
								break;
							}
							nodes = search(0, nodeInfo, TAOBAO_COMPANY_VIEW_ID);
							for (AccessibilityNodeInfo node : nodes) {
								company = getPackageCompany(node.getText().toString());
								break;
							}
							if (number != null && mPackageDatabase.indexOf(number) == -1
									&& !number.equals(mShowingNumber)) {
								mShowingNumber = number;
								sendNotification("淘宝", company, number, null, true);
							}
						} catch (Exception e) {

						}
						break;
					case PACKAGE_JD:
						try {
							List<AccessibilityNodeInfo> flatNodes = flatNodes(nodeInfo);
							String number = findNumberFromJd(flatNodes);
							String company = findCompanyFromJd(flatNodes);
							String name = findNameFromJd(flatNodes);
							if (number != null && mPackageDatabase.indexOf(number) == -1) {
								if (!number.equals(mShowingNumber)) {
									mShowingNumber = number;
									lastCompany = company;
									lastName = name;
									sendNotification("京东", company, number, name, true);
								} else if (lastCompany == null || lastName == null) {
									lastCompany = company;
									lastName = name;
									sendNotification("京东", company, number, name, false);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
				}
				break;
		}
	}

	@Override
	public void onInterrupt() {
		unregisterReceiver(mActionReceiver);
		mActionReceiver = null;
		mPackageDatabase = null;
	}

	private String findCompanyFromJd(List<AccessibilityNodeInfo> nodeInfos) {
		Iterator<AccessibilityNodeInfo> iterator = nodeInfos.iterator();
		while (iterator.hasNext()) {
			AccessibilityNodeInfo cur = iterator.next();
			if (cur.getViewIdResourceName() != null && cur.getViewIdResourceName().contains(JD_TEXT_LEFT_VIEW_ID)) {
				if (cur.getText().toString().contains("国内承运人")) {
					AccessibilityNodeInfo next = iterator.next();
					if (next != null
							&& next.getViewIdResourceName() != null
							&& next.getViewIdResourceName().contains(JD_TEXT_RIGHT_VIEW_ID)
							&& !TextUtils.isEmpty(next.getText())) {
						if (next.getText().toString().contains("京东")) {
							return "京东快递";
						} else {
							return next.getText().toString();
						}
					}
				}
			}
			if (cur.getViewIdResourceName() != null && cur.getViewIdResourceName().contains(JD_TEXT_MSG_ITEM_VIEW_ID)) {
				if (cur.getText() != null && cur.getText().toString().contains("待出库交付")) {
					String target = cur.getText().toString();
					int startIndex = target.indexOf("待出库交付") + 5;
					int endIndex = target.substring(startIndex).indexOf("，") + startIndex;
					if (endIndex == -1) endIndex = startIndex + 4;
					if (endIndex >= target.length()) endIndex = target.length() - 1;
					return target.substring(startIndex, endIndex);
				}
			}
			if (cur.getViewIdResourceName() != null && cur.getViewIdResourceName().contains(JD_DELIVERY_WAY_CONTENT_VIEW_ID)) {
				if (cur.getText().toString().contains("京东")) {
					return "京东快递";
				} else {
					return cur.getText().toString();
				}
			}
		}
		return null;
	}

	private String findNumberFromJd(List<AccessibilityNodeInfo> nodeInfos) {
		Iterator<AccessibilityNodeInfo> iterator = nodeInfos.iterator();
		String result = null;
		while (iterator.hasNext()) {
			AccessibilityNodeInfo cur = iterator.next();
			if (cur.getViewIdResourceName() != null && cur.getViewIdResourceName().contains(JD_TEXT_LEFT_VIEW_ID)) {
				if (cur.getText().toString().contains("订单编号")) {
					AccessibilityNodeInfo next = iterator.next();
					if (next != null
							&& next.getViewIdResourceName() != null
							&& next.getViewIdResourceName().contains(JD_TEXT_RIGHT_VIEW_ID)
							&& !TextUtils.isEmpty(next.getText())) {
						result = next.getText().toString();
					}
				}
			}
			if (cur.getViewIdResourceName() != null && cur.getViewIdResourceName().contains(JD_TEXT_MSG_ITEM_VIEW_ID)) {
				if (cur.getText() != null && cur.getText().toString().contains("运单号")) {
					String target = cur.getText().toString();
					int startIndex = target.indexOf("运单号");
					StringBuffer sb = new StringBuffer();
					String temp = target.substring(startIndex);
					for (int i = 0; i < temp.length(); i++) {
						int next = (i + 1) > temp.length() ? temp.length() - 1 : i + 1;
						String c = temp.substring(i, next);
						if (TextUtils.isDigitsOnly(c)) {
							sb.append(c);
							if (i == temp.length() - 1) {
								result = sb.toString();
							}
						} else if (sb.length() > 0) {
							result = sb.toString();
							sb = new StringBuffer();
						}
					}
				}
			}
			if (cur.getViewIdResourceName() != null && cur.getViewIdResourceName().contains(JD_ORDER_ID_CONTENT_VIEW_ID)) {
				return cur.getText().toString();
			}
		}
		return result;
	}

	private String findNameFromJd(List<AccessibilityNodeInfo> nodeInfos) {
		for (AccessibilityNodeInfo cur : nodeInfos) {
			if (cur.getViewIdResourceName() != null && cur.getViewIdResourceName().contains(JD_WARE_INFO_NAME_VIEW_ID)) {
				return cur.getText().toString();
			}
		}
		return null;
	}

	private List<AccessibilityNodeInfo> flatNodes(AccessibilityNodeInfo root) {
		if (root == null) return null;
		if (root.getChildCount() == 0) {
			List<AccessibilityNodeInfo> result = new ArrayList<>();
			result.add(root);
			return result;
		}
		ArrayList<AccessibilityNodeInfo> list = new ArrayList<>();
		for (int i = 0; i < root.getChildCount(); i++) {
			List<AccessibilityNodeInfo> result = flatNodes(root.getChild(i));
			if (result == null) continue;
			list.addAll(result);
		}
		return list;
	}

	private List<AccessibilityNodeInfo> search(int depth, AccessibilityNodeInfo root, String targetIdName) {
		if (root == null) return null;
		if (root.getChildCount() == 0) {
			//String space = "";
			//for (;depth > 0; depth--) space += " ";
			//Log.i(TAG, space + "id:" + root.getViewIdResourceName());
			//try { Log.i(TAG, space + "content:" + root.getText()); } catch (Exception e) {}
			if (root.getViewIdResourceName() != null && root.getViewIdResourceName().contains(targetIdName)) {
				List<AccessibilityNodeInfo> result = new ArrayList<>();
				result.add(root);
				return result;
			}
		}
		ArrayList<AccessibilityNodeInfo> list = new ArrayList<>();
		for (int i = 0; i < root.getChildCount(); i++) {
			List<AccessibilityNodeInfo> result = search(depth + 1, root.getChild(i), targetIdName);
			if (result == null) continue;
			list.addAll(result);
		}
		return list;
	}

	private String getPackageCompany(String source) {
		return source.substring(source.indexOf(" ") + 1);
	}

	public static String getPackageNumber(String source) {
		ArrayList<String> results = new ArrayList<>();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < source.length(); i++) {
			int next = (i + 1) > source.length() ? source.length() - 1 : i + 1;
			String c = source.substring(i, next);
			if (TextUtils.isDigitsOnly(c)) {
				sb.append(c);
				if (i == source.length() - 1) {
					results.add(sb.toString());
					Log.i(TAG, "getPackageNumber, found " + sb.toString());
				}
			} else if (sb.length() > 0) {
				results.add(sb.toString());
				Log.i(TAG, "getPackageNumber, found " + sb.toString());
				sb = new StringBuffer();
			}
		}
		if (results.isEmpty()) {
			return null;
		} else {
			String longest = "";
			for (String result : results) if (longest.length() < result.length()) longest = result;
			return longest;
		}
	}

	private void sendNotification(String appName, String company, String number, String name, boolean headsUp) {
		Intent addIntent = new Intent(getApplicationContext(), AddActivity.class);
		addIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		addIntent.putExtra(AddActivity.EXTRA_HAS_PREINFO, true);
		addIntent.putExtra(AddActivity.EXTRA_PRE_NUMBER, number);
		addIntent.putExtra(AddActivity.EXTRA_PRE_COMPANY, company);
		if (name != null) addIntent.putExtra(AddActivity.EXTRA_PRE_NAME, name);
		String notiTitle = (name != null) ? getString(R.string.auto_detect_noti_title_with_name, name)
				: getString(R.string.auto_detect_noti_title);
		String notiText = getString(R.string.auto_detect_noti_result, appName, company, number);
		Notification.Builder builder = new Notification.Builder(this)
				.setContentTitle(notiTitle)
				.setContentText(notiText)
				.setSmallIcon(R.drawable.ic_assistant_black_24dp)
				.setPriority(Notification.PRIORITY_HIGH)
				.setShowWhen(false)
				.setAutoCancel(true)
				.addAction(R.drawable.ic_add_black_24dp, getString(R.string.auto_detect_noti_action_add),
						PendingIntent.getActivity(
								this, 0, addIntent, PendingIntent.FLAG_CANCEL_CURRENT
						))
				.setDeleteIntent(PendingIntent.getBroadcast(
						this, 1002,
						new Intent(ACTION_DELETE_ASSIST_NOTI), PendingIntent.FLAG_CANCEL_CURRENT
				));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			builder.setColor(getResources().getColor(R.color.teal_500));
		}
		if (headsUp) {
			builder.setDefaults(Notification.DEFAULT_VIBRATE);
		}
		Notification mNotification = builder.build();
		mNotificationManager.notify(NOTIFICATION_ID_ASSIST, mNotification);
	}

	private class NotificationActionReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Received Action:" + intent.toString());
		}

	}

}
