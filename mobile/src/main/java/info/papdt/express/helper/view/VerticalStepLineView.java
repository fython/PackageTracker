package info.papdt.express.helper.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.view.View;

import info.papdt.express.helper.R;
import info.papdt.express.helper.support.ScreenUtils;

public class VerticalStepLineView extends View {

	private Paint mPaint;
	private RectF mBounds;
	private float lineWidth, pointOffsetY = 0;

	@ColorInt private int lineColor = Color.GRAY;
	private boolean shouldDrawTopLine = true, shouldDrawBottomLine = true;

	public VerticalStepLineView(Context context) {
		this(context, null);
	}

	public VerticalStepLineView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VerticalStepLineView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		lineWidth = ScreenUtils.dpToPx(context, 2);

		init();

		lineColor = context.getResources().getColor(R.color.blue_grey_500);
	}

	private void init() {
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setStrokeWidth(lineWidth);
	}

	public void setLineShouldDraw(boolean top, boolean bottom) {
		shouldDrawTopLine = top;
		shouldDrawBottomLine = bottom;
	}

	public void setLineColor(@ColorInt int color) {
		lineColor = color;
	}

	public void setLineColorResource(@ColorRes int resId) {
		lineColor = getResources().getColor(resId);
	}

	public void setPointOffsetY(float pointOffsetY) {
		this.pointOffsetY = pointOffsetY;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldW, int oldH) {
		super.onSizeChanged(w, h, oldW, oldH);
		mBounds = new RectF(getLeft(), getTop(), getRight(), getBottom());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (shouldDrawTopLine) {
			mPaint.setColor(lineColor);
			canvas.drawLine(mBounds.centerX(), mBounds.centerY() + pointOffsetY, mBounds.centerX(), mBounds.top, mPaint);
		}

		if (shouldDrawBottomLine) {
			mPaint.setColor(lineColor);
			canvas.drawLine(mBounds.centerX(), mBounds.centerY() + pointOffsetY, mBounds.centerX(), mBounds.bottom, mPaint);
		}
	}

}
