package info.papdt.express.helper.services;

import android.animation.Animator;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import info.papdt.express.helper.R;
import info.papdt.express.helper.support.ScreenUtils;
import info.papdt.express.helper.ui.AddActivity;

public class ClipboardDetectService extends Service implements ClipboardManager.OnPrimaryClipChangedListener {

	private ClipboardManager mClipboardManager;
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mLayoutParams;

	private View mLayout;
	private ImageView mIconView;

	private Handler mHandler;

	private String mLastNumber;

	private float mPositionY;

	@Override
	public void onCreate() {
		super.onCreate();
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		mClipboardManager.addPrimaryClipChangedListener(this);
		mHandler = new UiHandler();
		mPositionY = ScreenUtils.dpToPx(this, 128);
		initPopupView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mClipboardManager.removePrimaryClipChangedListener(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onPrimaryClipChanged() {
		mLastNumber = DetectNumberService.getPackageNumber(mClipboardManager.getPrimaryClip().toString());
		if (TextUtils.isEmpty(mLastNumber)) return;
		try {
			mWindowManager.addView(mLayout, mLayoutParams);
			mIconView.setScaleX(0.5f);
			mIconView.setScaleY(0.5f);
			mIconView.animate()
					.scaleX(1f)
					.scaleY(1f)
					.setListener(null)
					.setDuration(500).setInterpolator(new BounceInterpolator()).start();
			mHandler.sendEmptyMessageDelayed(0, 4500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initPopupView() {
		mLayout = View.inflate(this, R.layout.popupwindow_app_entry, null);
		mIconView = mLayout.findViewById(R.id.icon_view);
		mLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AddActivity.launch(view.getContext(), null, mLastNumber, null);
				mHandler.sendEmptyMessage(0);
			}
		});
		mLayout.setOnTouchListener(new View.OnTouchListener() {
			private float lastY;
			private float nowY;
			private float tranY;
			private float distance;

			@Override
			public boolean onTouch(View view, MotionEvent event) {
				boolean ret = false;
				switch (event.getAction()){
					case MotionEvent.ACTION_DOWN:
						distance = 0;
						lastY = event.getRawY();
						ret = true;
						mHandler.removeMessages(0);
						break;
					case MotionEvent.ACTION_MOVE:
						// 获取移动时的X，Y坐标
						nowY = event.getRawY();
						// 计算XY坐标偏移量
						tranY = nowY - lastY;
						distance += Math.abs(tranY);
						// 移动悬浮窗
						mPositionY = mLayoutParams.y += tranY;
						//更新悬浮窗位置
						mWindowManager.updateViewLayout(mLayout, mLayoutParams);
						//记录当前坐标作为下一次计算的上一次移动的位置坐标
						lastY = nowY;
						break;
					case MotionEvent.ACTION_UP:
						if (distance < 5) {
							view.performClick();
						} else {
							mHandler.sendEmptyMessageDelayed(0, 3000);
						}
						break;
				}
				return ret;
			}
		});

		mLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		mLayoutParams.x = 0;
		mLayoutParams.y = (int) mPositionY;
		mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mLayoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
		mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		mLayoutParams.format = PixelFormat.TRANSLUCENT;
	}

	class UiHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0:
					mIconView.animate()
							.scaleX(0f)
							.scaleY(0f)
							.setListener(new Animator.AnimatorListener() {
								@Override public void onAnimationStart(Animator animator) {}
								@Override
								public void onAnimationEnd(Animator animator) {
									try {mWindowManager.removeViewImmediate(mLayout);} catch (Exception e) {}
								}
								@Override public void onAnimationCancel(Animator animator) {}
								@Override public void onAnimationRepeat(Animator animator) {}
							})
							.setDuration(500).setInterpolator(new AnticipateInterpolator()).start();
					break;
			}
		}

	}

}
