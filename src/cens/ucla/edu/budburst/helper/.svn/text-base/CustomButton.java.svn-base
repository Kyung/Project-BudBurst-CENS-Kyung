package cens.ucla.edu.budburst.helper;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;
import cens.ucla.edu.budburst.R;

public class CustomButton extends Button {
	private Drawable separator;
	private Drawable separator_bottom;

	private static final String TAG = "selectedButton";

	public CustomButton(Context context) {
		this(context, null, 0);
	}

	public CustomButton(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public CustomButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		separator = getResources().getDrawable(R.drawable.btn_strip_divider);
		
		separator_bottom = getResources().getDrawable(R.drawable.btn_strip_divider_bottom);
		setBackgroundDrawable(getResources().getDrawable(R.layout.button_bar_selector));

		setCompoundDrawablesWithIntrinsicBounds(null, null, separator, null);

		//will be centered automatically? so lets just default to the left. (saves some space)
		this.setGravity(Gravity.CENTER|Gravity.CENTER_VERTICAL);
		
	}
	
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		this.setSelected(focused);
	}

	/*
	protected Drawable getSelectedDrawable() {
		return getResources().getDrawable(isSelected()?R.drawable.btn_strip_mark_on : R.drawable.btn_strip_mark_off);
	}
	*/

	@Override
	protected void drawableStateChanged () {
		super.drawableStateChanged();

		//setCompoundDrawablesWithIntrinsicBounds(getSelectedDrawable(), null, separator, null);
		setCompoundDrawablesWithIntrinsicBounds(null, null, separator, null);
	}

}
