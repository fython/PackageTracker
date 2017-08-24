package info.papdt.express.helper.services

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.PendingIntent
import android.content.*
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import info.papdt.express.helper.R
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.ui.AddActivity
import moe.feng.kotlinyan.common.ServiceExtensions

import java.util.ArrayList

class DetectNumberService : AccessibilityService(), ServiceExtensions {

	private lateinit var mPackageDatabase: PackageDatabase
	private var mShowingNumber: String? = null
	private var mActionReceiver: NotificationActionReceiver? = null

	private var lastCompany: String? = null
	private var lastName: String? = null

	public override fun onServiceConnected() {
		mPackageDatabase = PackageDatabase.getInstance(this)
		serviceInfo.packageNames = arrayOf(PACKAGE_TAOBAO, PACKAGE_JD)
		serviceInfo = serviceInfo

		try {
			if (mActionReceiver != null) {
				unregisterReceiver(mActionReceiver)
			}
			mActionReceiver = NotificationActionReceiver()
			registerReceiver(mActionReceiver, IntentFilter(ACTION_DELETE_ASSIST_NOTI))
		} catch (e: Exception) {

		}

	}

	override fun onAccessibilityEvent(event: AccessibilityEvent) {
		val nodeInfo = event.source
		when (event.eventType) {
			AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
				Log.i(TAG, event.className.toString())
				when (event.packageName.toString()) {
					PACKAGE_TAOBAO -> if (TAOBAO_CHECK_ACTIVITY != event.className.toString()) {
						mShowingNumber = null
						lastCompany = null
						lastName = null
					}
					PACKAGE_JD -> if (JD_CHECK_ACTIVITY != event.className.toString() && JD_CHECK_ACTIVITY_2 != event.className.toString()) {
						mShowingNumber = null
						lastCompany = null
						lastName = null
					}
				}
				when (event.packageName.toString()) {
					PACKAGE_TAOBAO -> try {
						var nodes = search(0, nodeInfo, TAOBAO_WAYBILL_VIEW_ID)
						val number: String? = nodes!!
								.firstOrNull()
								?.let { getPackageNumber(it.text.toString()) }
						nodes = search(0, nodeInfo, TAOBAO_COMPANY_VIEW_ID)
						val company: String? = nodes!!
								.firstOrNull()
								?.let { getPackageCompany(it.text.toString()) }
						if (number != null && mPackageDatabase!!.indexOf(number) == -1
								&& number != mShowingNumber) {
							mShowingNumber = number
							sendNotification("淘宝", company, number, null, true)
						}
					} catch (e: Exception) {

					}

					PACKAGE_JD -> try {
						val flatNodes = flatNodes(nodeInfo)
						val number = findNumberFromJd(flatNodes)
						val company = findCompanyFromJd(flatNodes)
						val name = findNameFromJd(flatNodes)
						if (number != null && mPackageDatabase!!.indexOf(number) == -1) {
							if (number != mShowingNumber) {
								mShowingNumber = number
								lastCompany = company
								lastName = name
								sendNotification("京东", company, number, name, true)
							} else if (lastCompany == null || lastName == null) {
								lastCompany = company
								lastName = name
								sendNotification("京东", company, number, name, false)
							}
						}
					} catch (e: Exception) {
						e.printStackTrace()
					}

				}
			}
			AccessibilityEvent.TYPE_WINDOWS_CHANGED, AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> when (event.packageName.toString()) {
				PACKAGE_TAOBAO -> try {
					var nodes = search(0, nodeInfo, TAOBAO_WAYBILL_VIEW_ID)
					val number: String? = nodes!!
							.firstOrNull()
							?.let { getPackageNumber(it.text.toString()) }
					nodes = search(0, nodeInfo, TAOBAO_COMPANY_VIEW_ID)
					val company: String? = nodes!!
							.firstOrNull()
							?.let { getPackageCompany(it.text.toString()) }
					if (number != null && mPackageDatabase!!.indexOf(number) == -1 && number != mShowingNumber) {
						mShowingNumber = number
						sendNotification("淘宝", company, number, null, true)
					}
				} catch (e: Exception) {
				}

				PACKAGE_JD -> try {
					val flatNodes = flatNodes(nodeInfo)
					val number = findNumberFromJd(flatNodes)
					val company = findCompanyFromJd(flatNodes)
					val name = findNameFromJd(flatNodes)
					if (number != null && mPackageDatabase!!.indexOf(number) == -1) {
						if (number != mShowingNumber) {
							mShowingNumber = number
							lastCompany = company
							lastName = name
							sendNotification("京东", company, number, name, true)
						} else if (lastCompany == null || lastName == null) {
							lastCompany = company
							lastName = name
							sendNotification("京东", company, number, name, false)
						}
					}
				} catch (e: Exception) {
					e.printStackTrace()
				}

			}
		}
	}

	override fun onInterrupt() {
		unregisterReceiver(mActionReceiver)
		mActionReceiver = null
	}

	private fun findCompanyFromJd(nodeInfos: List<AccessibilityNodeInfo>?): String? {
		val iterator = nodeInfos!!.iterator()
		while (iterator.hasNext()) {
			val cur = iterator.next()
			if (cur.viewIdResourceName != null && cur.viewIdResourceName.contains(JD_TEXT_LEFT_VIEW_ID)) {
				if (cur.text.toString().contains("国内承运人")) {
					val next = iterator.next()
					if (next != null
							&& next.viewIdResourceName != null
							&& next.viewIdResourceName.contains(JD_TEXT_RIGHT_VIEW_ID)
							&& !TextUtils.isEmpty(next.text)) {
						return if (next.text.toString().contains("京东")) {
							"京东快递"
						} else {
							next.text.toString()
						}
					}
				}
			}
			if (cur.viewIdResourceName != null && cur.viewIdResourceName.contains(JD_TEXT_MSG_ITEM_VIEW_ID)) {
				if (cur.text != null && cur.text.toString().contains("待出库交付")) {
					val target = cur.text.toString()
					val startIndex = target.indexOf("待出库交付") + 5
					var endIndex = target.substring(startIndex).indexOf("，") + startIndex
					if (endIndex == -1) endIndex = startIndex + 4
					if (endIndex >= target.length) endIndex = target.length - 1
					return target.substring(startIndex, endIndex)
				}
			}
			if (cur.viewIdResourceName != null && cur.viewIdResourceName.contains(JD_DELIVERY_WAY_CONTENT_VIEW_ID)) {
				return if (cur.text.toString().contains("京东")) {
					"京东快递"
				} else {
					cur.text.toString()
				}
			}
		}
		return null
	}

	private fun findNumberFromJd(nodeInfos: List<AccessibilityNodeInfo>?): String? {
		val iterator = nodeInfos!!.iterator()
		var result: String? = null
		while (iterator.hasNext()) {
			val cur = iterator.next()
			if (cur.viewIdResourceName != null && cur.viewIdResourceName.contains(JD_TEXT_LEFT_VIEW_ID)) {
				if (cur.text.toString().contains("订单编号")) {
					val next = iterator.next()
					if (next != null
							&& next.viewIdResourceName != null
							&& next.viewIdResourceName.contains(JD_TEXT_RIGHT_VIEW_ID)
							&& !TextUtils.isEmpty(next.text)) {
						result = next.text.toString()
					}
				}
			}
			if (cur.viewIdResourceName != null && cur.viewIdResourceName.contains(JD_TEXT_MSG_ITEM_VIEW_ID)) {
				if (cur.text != null && cur.text.toString().contains("运单号")) {
					val target = cur.text.toString()
					val startIndex = target.indexOf("运单号")
					var sb = StringBuffer()
					val temp = target.substring(startIndex)
					for (i in 0 until temp.length) {
						val next = if (i + 1 > temp.length) temp.length - 1 else i + 1
						val c = temp.substring(i, next)
						if (TextUtils.isDigitsOnly(c)) {
							sb.append(c)
							if (i == temp.length - 1) {
								result = sb.toString()
							}
						} else if (sb.isNotEmpty()) {
							result = sb.toString()
							sb = StringBuffer()
						}
					}
				}
			}
			if (cur.viewIdResourceName != null && cur.viewIdResourceName.contains(JD_ORDER_ID_CONTENT_VIEW_ID)) {
				return cur.text.toString()
			}
		}
		return result
	}

	private fun findNameFromJd(nodeInfos: List<AccessibilityNodeInfo>?): String? {
		return nodeInfos!!
				.firstOrNull { it.viewIdResourceName != null && it.viewIdResourceName.contains(JD_WARE_INFO_NAME_VIEW_ID) }
				?.let { it.text.toString() }
	}

	private fun flatNodes(root: AccessibilityNodeInfo?): List<AccessibilityNodeInfo>? {
		if (root == null) return null
		if (root.childCount == 0) {
			val result = ArrayList<AccessibilityNodeInfo>()
			result.add(root)
			return result
		}
		val list = ArrayList<AccessibilityNodeInfo>()
		for (i in 0 until root.childCount) {
			val result = flatNodes(root.getChild(i)) ?: continue
			list.addAll(result)
		}
		return list
	}

	private fun search(depth: Int, root: AccessibilityNodeInfo?, targetIdName: String): List<AccessibilityNodeInfo>? {
		if (root == null) return null
		if (root.childCount == 0) {
			//String space = "";
			//for (;depth > 0; depth--) space += " ";
			//Log.i(TAG, space + "id:" + root.getViewIdResourceName());
			//try { Log.i(TAG, space + "content:" + root.getText()); } catch (Exception e) {}
			if (root.viewIdResourceName != null && root.viewIdResourceName.contains(targetIdName)) {
				val result = ArrayList<AccessibilityNodeInfo>()
				result.add(root)
				return result
			}
		}
		val list = ArrayList<AccessibilityNodeInfo>()
		for (i in 0 until root.childCount) {
			val result = search(depth + 1, root.getChild(i), targetIdName) ?: continue
			list.addAll(result)
		}
		return list
	}

	private fun getPackageCompany(source: String): String {
		return source.substring(source.indexOf(" ") + 1)
	}

	private fun sendNotification(appName: String, company: String?, number: String, name: String?, headsUp: Boolean) {
		val addIntent = Intent(applicationContext, AddActivity::class.java)
		addIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
		addIntent.putExtra(AddActivity.EXTRA_HAS_PREINFO, true)
		addIntent.putExtra(AddActivity.EXTRA_PRE_NUMBER, number)
		addIntent.putExtra(AddActivity.EXTRA_PRE_COMPANY, company)
		if (name != null) addIntent.putExtra(AddActivity.EXTRA_PRE_NAME, name)
		val notiTitle = if (name != null)
			getString(R.string.auto_detect_noti_title_with_name, name)
		else
			getString(R.string.auto_detect_noti_title)
		val notiText = getString(R.string.auto_detect_noti_result, appName, company, number)
		val builder = Notification.Builder(this)
				.setContentTitle(notiTitle)
				.setContentText(notiText)
				.setSmallIcon(R.drawable.ic_assistant_black_24dp)
				.setPriority(Notification.PRIORITY_HIGH)
				.setShowWhen(false)
				.setAutoCancel(true)
				.setContentIntent(PendingIntent.getActivity(
						this, 0, addIntent, PendingIntent.FLAG_UPDATE_CURRENT
				))
				.addAction(R.drawable.ic_add_black_24dp, getString(R.string.auto_detect_noti_action_add),
						PendingIntent.getActivity(
								this, 0, addIntent, PendingIntent.FLAG_UPDATE_CURRENT
						))
				.setDeleteIntent(PendingIntent.getBroadcast(
						this, 1002,
						Intent(ACTION_DELETE_ASSIST_NOTI), PendingIntent.FLAG_CANCEL_CURRENT
				))
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			builder.setColor(resources.getColor(R.color.teal_500))
		}
		if (headsUp) {
			builder.setDefaults(Notification.DEFAULT_VIBRATE)
		}
		val mNotification = builder.build()
		notificationManager.notify(NOTIFICATION_ID_ASSIST, mNotification)
	}

	private inner class NotificationActionReceiver : BroadcastReceiver() {

		override fun onReceive(context: Context, intent: Intent) {
			Log.i(TAG, "Received Action:" + intent.toString())
		}

	}

	companion object {

		val NOTIFICATION_ID_ASSIST = 100

		private val PACKAGE_TAOBAO = "com.taobao.taobao"
		private val PACKAGE_JD = "com.jingdong.app.mall"

		private val TAOBAO_WAYBILL_VIEW_ID = "tv_logistic_waybill_number"
		private val TAOBAO_COMPANY_VIEW_ID = "tv_logistic_company"
		private val JD_TEXT_RIGHT_VIEW_ID = "text_right"
		private val JD_TEXT_LEFT_VIEW_ID = "text_left"
		private val JD_TEXT_MSG_ITEM_VIEW_ID = "item_msg"
		private val JD_ORDER_ID_CONTENT_VIEW_ID = "order_id_content"
		private val JD_DELIVERY_WAY_CONTENT_VIEW_ID = "delivery_way_content"
		private val JD_WARE_INFO_NAME_VIEW_ID = "ware_info_name"

		private val TAOBAO_CHECK_ACTIVITY = "com.taobao.cainiao.logistic.LogisticDetailActivity"
		private val JD_CHECK_ACTIVITY = "com.jd.lib.ordercenter.logisticstrack.LogisticsTrackActivity"
		private val JD_CHECK_ACTIVITY_2 = "com.jd.lib.ordercenter.neworderdetail.NewOrderDetailActivity"

		private val ACTION_DELETE_ASSIST_NOTI = "info.papdt.express.helper.ACTION_DELETE_ASSIST_NOTI"

		val TAG = DetectNumberService::class.java.simpleName

		fun getPackageNumber(source: String): String? {
			val results = ArrayList<String>()
			var sb = StringBuffer()
			for (i in 0 until source.length) {
				val next = if (i + 1 > source.length) source.length - 1 else i + 1
				val c = source.substring(i, next)
				if (TextUtils.isDigitsOnly(c)) {
					sb.append(c)
					if (i == source.length - 1) {
						results.add(sb.toString())
						Log.i(TAG, "getPackageNumber, found " + sb.toString())
					}
				} else if (sb.isNotEmpty()) {
					results.add(sb.toString())
					Log.i(TAG, "getPackageNumber, found " + sb.toString())
					sb = StringBuffer()
				}
			}
			return if (results.isEmpty()) {
				null
			} else {
				var longest = ""
				results.asSequence().filter { longest.length < it.length }.forEach { longest = it }
				longest
			}
		}
	}

}
