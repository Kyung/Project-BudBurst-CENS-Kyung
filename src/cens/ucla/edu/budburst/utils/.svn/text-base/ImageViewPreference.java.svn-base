package cens.ucla.edu.budburst.utils;

import cens.ucla.edu.budburst.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageViewPreference extends Preference{

	private Drawable mIcon;
	private String mText;
	
	public ImageViewPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}
	
	public ImageViewPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		setLayoutResource(R.layout.preference_icon);
		
		//TypedArray a = context.obtainStyledAttributes(attrs, 
		//		R.styleable.IconPreferenceScreen, defStyle, 0);
		//mIcon = a.getDrawable(R.styleable.IconPreferenceScreen_icon);
	}
	
	@Override
	public void onBindView(View view) {
		super.onBindView(view);
		
		ImageView imageView = (ImageView)view.findViewById(R.id.icon);
		TextView textView = (TextView)view.findViewById(R.id.custom_preference_text);
		
		imageView.setImageDrawable(mIcon);
		textView.setText(mText);
		
		if(imageView != null && mIcon != null) {
			imageView.setImageDrawable(mIcon);
		}
	}
	
	
	public void setComponent(Drawable icon, String text) {
		
		if((icon == null && mIcon != null) || (icon != null && !icon.equals(mIcon))) {
			mIcon = icon;
		}
		mText = text;
		notifyChanged();
	}
	
	public Drawable getIcon() {
		return mIcon;
	}
}
