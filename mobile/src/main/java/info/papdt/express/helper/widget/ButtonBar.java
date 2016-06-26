package info.papdt.express.helper.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import info.papdt.express.helper.R;

public class ButtonBar extends LinearLayout {

	private View mLeftButton, mRightButton;
	private AppCompatTextView mLeftText, mRightText;

	public ButtonBar(Context context) {
		this(context, null);
	}

	public ButtonBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ButtonBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.widget_button_bar, this);
		mLeftButton = findViewById(R.id.btn_left);
		mRightButton = findViewById(R.id.btn_right);
		mLeftText = (AppCompatTextView) findViewById(R.id.btn_left_title);
		mRightText = (AppCompatTextView) findViewById(R.id.btn_right_title);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ButtonBar, defStyle,
				R.style.Widget_ButtonBar);

		this.setLeftButtonText(a.getString(R.styleable.ButtonBar_leftText));
		this.setRightButtonText(a.getString(R.styleable.ButtonBar_rightText));
		this.setLeftButtonEnabled(a.getBoolean(R.styleable.ButtonBar_leftEnabled, true));
		this.setRightButtonEnabled(a.getBoolean(R.styleable.ButtonBar_rightEnabled, true));

		a.recycle();
	}

	public void setOnLeftButtonClickListener(OnClickListener listener) {
		mLeftButton.setOnClickListener(listener);
	}

	public void setOnRightButtonClickListener(OnClickListener listener) {
		mRightButton.setOnClickListener(listener);
	}

	public void setLeftButtonText(CharSequence text) {
		mLeftText.setText(text);
	}

	public void setRightButtonText(CharSequence text) {
		mRightText.setText(text);
	}

	public void setLeftButtonEnabled(boolean enabled) {
		mLeftButton.setVisibility(enabled ? VISIBLE : GONE);
	}

	public void setRightButtonEnabled(boolean enabled) {
		mRightButton.setVisibility(enabled ? VISIBLE : GONE);
	}

	public void onLeftButtonClick() {
		mLeftButton.callOnClick();
	}

	public void onRightButtonClick() {
		mRightButton.callOnClick();
	}

}
