package info.papdt.express.helper.widget

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout

import info.papdt.express.helper.R

class ButtonBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
	: LinearLayout(context, attrs, defStyle) {

	private val mLeftButton: View
	private val mRightButton: View
	private val mLeftText: AppCompatTextView
	private val mRightText: AppCompatTextView

	init {
		LayoutInflater.from(context).inflate(R.layout.widget_button_bar, this)
		mLeftButton = findViewById(R.id.btn_left)
		mRightButton = findViewById(R.id.btn_right)
		mLeftText = findViewById(R.id.btn_left_title)
		mRightText = findViewById(R.id.btn_right_title)

		val a = context.obtainStyledAttributes(attrs, R.styleable.ButtonBar, defStyle,
				R.style.Widget_ButtonBar)

		this.setLeftButtonText(a.getString(R.styleable.ButtonBar_leftText))
		this.setRightButtonText(a.getString(R.styleable.ButtonBar_rightText))
		this.setLeftButtonEnabled(a.getBoolean(R.styleable.ButtonBar_leftEnabled, true))
		this.setRightButtonEnabled(a.getBoolean(R.styleable.ButtonBar_rightEnabled, true))

		a.recycle()
	}

	fun setOnLeftButtonClickListener(listener: View.OnClickListener) {
		mLeftButton.setOnClickListener(listener)
	}

	fun setOnRightButtonClickListener(listener: View.OnClickListener) {
		mRightButton.setOnClickListener(listener)
	}

	fun setLeftButtonText(text: CharSequence) {
		mLeftText.text = text
	}

	fun setRightButtonText(text: CharSequence) {
		mRightText.text = text
	}

	fun setLeftButtonEnabled(enabled: Boolean) {
		mLeftButton.visibility = if (enabled) View.VISIBLE else View.GONE
	}

	fun setRightButtonEnabled(enabled: Boolean) {
		mRightButton.visibility = if (enabled) View.VISIBLE else View.GONE
	}

	fun onLeftButtonClick() {
		mLeftButton.callOnClick()
	}

	fun onRightButtonClick() {
		mRightButton.callOnClick()
	}

}
