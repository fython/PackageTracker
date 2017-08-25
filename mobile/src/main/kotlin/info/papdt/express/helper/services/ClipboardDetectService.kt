package info.papdt.express.helper.services

import android.animation.Animator
import android.app.Service
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnticipateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import info.papdt.express.helper.R
import info.papdt.express.helper.support.ScreenUtils
import info.papdt.express.helper.ui.AddActivity
import moe.feng.kotlinyan.common.ServiceExtensions

class ClipboardDetectService : Service(), ClipboardManager.OnPrimaryClipChangedListener, ServiceExtensions {

	private var mLayoutParams: WindowManager.LayoutParams? = null

	private val mLayout: View by lazy { View.inflate(this, R.layout.popupwindow_app_entry, null) }
	private lateinit var mIconView: ImageView

	private val mHandler: Handler by lazy { UiHandler() }

	private var mLastNumber: String? = null

	private var mPositionY: Float = 0.toFloat()

	override fun onCreate() {
		super.onCreate()
		clipboardManager.addPrimaryClipChangedListener(this)
		mPositionY = ScreenUtils.dpToPx(this, 128f)
		initPopupView()
	}

	override fun onDestroy() {
		super.onDestroy()
		clipboardManager.removePrimaryClipChangedListener(this)
	}

	override fun onBind(intent: Intent): IBinder? {
		return null
	}

	override fun onPrimaryClipChanged() {
		mLastNumber = DetectNumberService.getPackageNumber(clipboardManager.primaryClip.toString())
		if (TextUtils.isEmpty(mLastNumber)) return
		try {
			windowManager.addView(mLayout, mLayoutParams)
			mIconView.scaleX = 0.5f
			mIconView.scaleY = 0.5f
			mIconView.animate()
					.scaleX(1f)
					.scaleY(1f)
					.setListener(null)
					.setDuration(500).setInterpolator(BounceInterpolator()).start()
			mHandler.sendEmptyMessageDelayed(0, 4500)
		} catch (e: Exception) {
			e.printStackTrace()
		}

	}

	private fun initPopupView() {
		mIconView = mLayout.findViewById(R.id.icon_view)
		mLayout.setOnClickListener { view ->
			AddActivity.launch(view.context, null, mLastNumber, null)
			mHandler.sendEmptyMessage(0)
		}
		mLayout.setOnTouchListener(object : View.OnTouchListener {
			private var lastY: Float = 0.toFloat()
			private var nowY: Float = 0.toFloat()
			private var tranY: Float = 0.toFloat()
			private var distance: Float = 0.toFloat()

			override fun onTouch(view: View, event: MotionEvent): Boolean {
				var ret = false
				when (event.action) {
					MotionEvent.ACTION_DOWN -> {
						distance = 0f
						lastY = event.rawY
						ret = true
						mHandler.removeMessages(0)
					}
					MotionEvent.ACTION_MOVE -> {
						// 获取移动时的X，Y坐标
						nowY = event.rawY
						// 计算XY坐标偏移量
						tranY = nowY - lastY
						distance += Math.abs(tranY)
						// 移动悬浮窗
						mLayoutParams!!.y += tranY.toInt()
						mPositionY = mLayoutParams!!.y.toFloat()
						//更新悬浮窗位置
						windowManager.updateViewLayout(mLayout, mLayoutParams)
						//记录当前坐标作为下一次计算的上一次移动的位置坐标
						lastY = nowY
					}
					MotionEvent.ACTION_UP -> if (distance < 5) {
						view.performClick()
					} else {
						mHandler.sendEmptyMessageDelayed(0, 3000)
					}
				}
				return ret
			}
		})

		mLayoutParams = WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT).apply {
			x = 0
			y = mPositionY.toInt()
			width = WindowManager.LayoutParams.WRAP_CONTENT
			height = WindowManager.LayoutParams.WRAP_CONTENT
			gravity = Gravity.RIGHT or Gravity.TOP
			flags = flags or (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
			format = PixelFormat.TRANSLUCENT
		}
	}

	internal inner class UiHandler : Handler() {

		override fun handleMessage(msg: Message) {
			when (msg.what) {
				0 -> mIconView.animate()
						.scaleX(0f)
						.scaleY(0f)
						.setListener(object : Animator.AnimatorListener {
							override fun onAnimationStart(animator: Animator) {}
							override fun onAnimationEnd(animator: Animator) {
								try {
									windowManager.removeViewImmediate(mLayout)
								} catch (e: Exception) {

								}
							}
							override fun onAnimationCancel(animator: Animator) {}
							override fun onAnimationRepeat(animator: Animator) {}
						})
						.setDuration(500).setInterpolator(AnticipateInterpolator()).start()
			}
		}

	}

}
