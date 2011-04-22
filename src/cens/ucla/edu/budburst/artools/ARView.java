package cens.ucla.edu.budburst.artools;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/*
 * Not used class at this time
 */
public class ARView extends View {

	ARViewObject	 	arViewObject;

	public ARView(Context context) {
		super(context);
		initialize();
	}
	public ARView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}	
	public ARView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();	
	}
	public void initialize() {
	}

	public void setARViewObject(ARViewObject arViews) {
		this.arViewObject = arViews;		
		invalidate();
	}

	
	/*
	protected void onDraw(Canvas canvas) {

		canvas.save();		
		if(arViewObject.isVisible()) {
			canvas.drawBitmap(arViewObject.getIcon(), arViewObject.getPosition().getX(), arViewObject.getPosition().getY(), null);

		}
		canvas.restore();
		
		super.onDraw(canvas);		
	}
	*/

}
