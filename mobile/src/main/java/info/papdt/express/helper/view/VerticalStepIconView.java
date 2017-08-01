package info.papdt.express.helper.view;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.View;
import info.papdt.express.helper.R;
import info.papdt.express.helper.support.ScreenUtils;

public class VerticalStepIconView extends View {

	private Paint mPaint, mCirclePaint;
	private RectF mBounds, mIconBounds;
	private float radius, lineWidth, pointOffsetY = 0, iconSize;

	@ColorInt private int pointColor = Color.BLUE, iconColor = Color.WHITE;
	private boolean isMini = false;
	private Drawable centerIcon;
	private Bitmap centerIconBitmap;

	public VerticalStepIconView(Context context) {
		this(context, null);
	}

	public VerticalStepIconView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VerticalStepIconView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		lineWidth = ScreenUtils.dpToPx(context, 2);
		iconSize = ScreenUtils.dpToPx(context, 16);

		init();

		pointColor = context.getResources().getColor(R.color.blue_500);
	}

	void init() {
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setStrokeWidth(lineWidth);

		mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mCirclePaint.setStrokeWidth(lineWidth);
		mCirclePaint.setColor(pointColor);
	}

	public void setPointColor(@ColorInt int color) {
		pointColor = color;
	}

	public void setPointColorResource(@ColorRes int resId) {
		pointColor = getResources().getColor(resId);
	}

	public void setIsMini(boolean isMini) {
		this.isMini = isMini;
	}

	public void setCenterIcon(Drawable drawable) {
		this.centerIcon = drawable;
		this.centerIconBitmap = applyBitmapFromDrawable(drawable);
	}

	public void setCenterIcon(@DrawableRes int resId) {
		setCenterIcon(getResources().getDrawable(resId));
	}

	public void setPointOffsetY(float pointOffsetY) {
		this.pointOffsetY = pointOffsetY;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldW, int oldH) {
		super.onSizeChanged(w, h, oldW, oldH);
		mBounds = new RectF(getLeft(), getTop(), getRight(), getBottom());
		mIconBounds = new RectF(mBounds.centerX() - iconSize / 2, mBounds.centerY() - iconSize / 2 + pointOffsetY,
				mBounds.centerX() + iconSize / 2, mBounds.centerY() + iconSize / 2 + pointOffsetY);
		radius = Math.min(w, h) / 4;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		float r = isMini ? radius / 5 * 3 : radius;

		super.onDraw(canvas);

		mCirclePaint.setColor(pointColor);
		canvas.drawCircle(mBounds.centerX(), mBounds.centerY() + pointOffsetY, r, mCirclePaint);

		if (centerIcon != null) {
			mPaint.setColor(iconColor);
			mIconBounds.top = mBounds.centerY() - iconSize / 2 + pointOffsetY;
			mIconBounds.bottom = mBounds.centerY() + iconSize / 2 + pointOffsetY;
			canvas.drawBitmap(centerIconBitmap, null, mIconBounds, mPaint);
		}
	}

	private Bitmap applyBitmapFromDrawable(Drawable d) {
		if (d == null) {
			return null;
		}

		if (d instanceof BitmapDrawable) {
			return ((BitmapDrawable) d).getBitmap();
		}

		try {
			Bitmap bitmap;
			if (d instanceof ColorDrawable) {
				bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
			} else {
				bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
			}

			Canvas canvas = new Canvas(bitmap);
			d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			d.draw(canvas);
			return bitmap;
		} catch (OutOfMemoryError e) {
			return null;
		}
	}

}
