package rikka.materialpreference;

import android.content.Context;
import android.util.AttributeSet;

/**
 * A version of {@link ListPreference} that presents the options in a drop down menu rather than a dialog.
 */
public class FixedSimpleMenuPreference extends SimpleMenuPreference {

	public FixedSimpleMenuPreference(Context context) {
		this(context, null);
	}

	public FixedSimpleMenuPreference(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.simpleMenuPreferenceStyle);
	}

	public FixedSimpleMenuPreference(Context context, AttributeSet attrs, int defStyle) {
		this(context, attrs, defStyle, 0);
	}

	public FixedSimpleMenuPreference(Context context, AttributeSet attrs, int defStyleAttr,
	                            int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	public void setValueIndex(int index) {
		setValue(getEntryValues()[index].toString());
		callChangeListener(getValue());
	}

}