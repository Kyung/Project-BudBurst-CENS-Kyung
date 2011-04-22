package cens.ucla.edu.budburst.artools;

import java.util.ArrayList;

import cens.ucla.edu.budburst.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class ARProjector extends View {

	ARObject[]	 	arObj;
	private boolean 	projectorSwitch;
	Bitmap sample;
	Context context;
	private Paint textPaint;
	private Paint rectPaint;

	public void setProjectorSwitch(boolean projectorSwitch) {
		this.projectorSwitch = projectorSwitch;
	}

	public ARProjector(Context context) {
		super(context);
		this.context = context;
		initialize();
	}
	public ARProjector(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		initialize();
	}	
	public ARProjector(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initialize();	
	}
	public void initialize() {
		projectorSwitch = false;
		sample = BitmapFactory.decodeResource(context.getResources(), R.drawable.pbb_icon_small);
		
		textPaint = new Paint();
		textPaint.setColor(Color.GREEN);
		textPaint.setTextSize(20);
		textPaint.setAntiAlias(true);
		
		rectPaint = new Paint();
		rectPaint.setColor(Color.LTGRAY);
		rectPaint.setStyle(Paint.Style.STROKE);
		rectPaint.setStrokeWidth(2);
	}

	public void setARView(ARObject[] arObj) {
		this.arObj = arObj;
		invalidate();
	}
	protected void onDraw(Canvas canvas) {

		canvas.save();		
		if(projectorSwitch) {
			for(int i = 0 ; i < arObj.length ; i++) {				
				if(arObj[i].getArViewObject().isVisible()) {
					if(arObj[i].getArLocation().distance <= 100) {
						int xPoint = arObj[i].getArViewObject().getPosition().getX();
						int yPoint = arObj[i].getArViewObject().getPosition().getY();
						int nameLength = arObj[i].getArName().length() * 10;
						
						Log.i("K", "length : " + nameLength);
						
						canvas.drawRect(xPoint - 3, yPoint - 3, 
								xPoint + nameLength, yPoint + 53, rectPaint);
						canvas.drawBitmap(arObj[i].getArViewObject().getIcon(), xPoint, yPoint, null);
						canvas.drawText(" " + arObj[i].getArName(), xPoint + 53, yPoint + 20, textPaint);
						
						canvas.drawText(String.format("%5.2fmi", arObj[i].getArLocation().distance), xPoint + 53, yPoint + 40, textPaint);
					}
				}
			}
		}
		
		canvas.drawBitmap(sample, 10, 10, null);
		canvas.restore();
		
		super.onDraw(canvas);		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		
		switch(action) {
		case (MotionEvent.ACTION_DOWN) :
			if(touchSpecies(event.getX(), event.getY())) {
				Toast.makeText(context, "DID IT!!", Toast.LENGTH_SHORT).show();
			}
			//Toast.makeText(context, "X: " + event.getX() + ",Y: " + event.getY(), Toast.LENGTH_SHORT).show();
			
			break;
		}
		
		
		return super.onTouchEvent(event);
	}
	
	private boolean touchSpecies(float x, float y) {
		
		for(int i = 0 ; i < arObj.length ; i++) {				
			if(arObj[i].getArViewObject().isVisible()) {
				if(arObj[i].getArLocation().distance <= 100) {
					int xPoint = arObj[i].getArViewObject().getPosition().getX();
					int yPoint = arObj[i].getArViewObject().getPosition().getY();
					
					if((x >= xPoint && x <= xPoint + 50) && (y >= yPoint && y <= yPoint + 50)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}

}
