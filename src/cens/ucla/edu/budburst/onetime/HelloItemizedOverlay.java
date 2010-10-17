package cens.ucla.edu.budburst.onetime;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class HelloItemizedOverlay extends ItemizedOverlay {

    private Context mContext;
    private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
    private String getStrValue = null;
    private String pheno;
    private String common_name;
    private String science_name;
    private String date_taken;
    private String num_of_comments;
    private String latitude;
    private String longitude;
    private SharedPreferences pref;
    private Paint innerPaint, borderPaint, textPaint;
    private Canvas canvas;
    private PopupPanel panel = null;
    private MapView map=null;
    private Drawable marker=null;
 
    
    public HelloItemizedOverlay(Drawable defaultMarker, Context context, MapView mapView) {
        super(boundCenterBottom(defaultMarker));
        // TODO Auto-generated constructor stub
        mContext = context;
        this.marker = defaultMarker;
        this.map = mapView;
		pref = mContext.getSharedPreferences("userinfo", 0);
		panel = new PopupPanel(R.layout.popup);
		
    }

    @Override
    protected OverlayItem createItem(int i) {
        // TODO Auto-generated method stub
        return mOverlays.get(i);
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return mOverlays.size();
    }

    public void addOverlay(OverlayItem overlay)
    {
        mOverlays.add(overlay);
        populate();
    }
    
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		this.canvas = canvas;
		this.map = mapView;
		super.draw(canvas, map, shadow);
		
		boundCenterBottom(marker);
	}
		
    @Override
    protected boolean onTap(int index) {
        OverlayItem item = mOverlays.get(index);
        //final LinearLayout linear = (LinearLayout) View.inflate(mContext, R.layout.popup, null);

        //TextView sname = (TextView) linear.findViewById(R.id.sname);
        //TextView cname = (TextView) linear.findViewById(R.id.cname);
        //TextView dt_taken = (TextView) linear.findViewById(R.id.dt_taken);
        //TextView n_comments = (TextView) linear.findViewById(R.id.ncomments);
        //ImageView pheno_image = (ImageView) linear.findViewById(R.id.pheno_image);
        //ImageView phone_image = (ImageView) linear.findViewById(R.id.phone_image);
        
		getStrValue = item.getSnippet();
		GeoPoint geo = item.getPoint();
		
		Point pt = map.getProjection().toPixels(geo, null);
		
		//drawInfoWindow(canvas, map, false, geo);
		
		View view = panel.getView();
		
		String []eachValue = getStrValue.split(";;");
		
		pheno = eachValue[0];
		science_name = eachValue[1];
		common_name = eachValue[2];
		latitude = eachValue[3];
		longitude = eachValue[4];
		date_taken = eachValue[5];
		num_of_comments = eachValue[6];
		
		((TextView)view.findViewById(R.id.latitude)).setText(
				String.valueOf(geo.getLatitudeE6() / 1000000.0));
		((TextView)view.findViewById(R.id.longitude)).setText(
				String.valueOf(geo.getLongitudeE6() / 1000000.0));
		
		map.getController().setCenter(geo);
		panel.show();
		 
		return true;
		/*
		SharedPreferences.Editor edit = pref.edit();				
		edit.putString("lat", eachValue[3]);
		edit.putString("lng", eachValue[4]);
		edit.commit();
		
		cname.setText(" Science name : " + eachValue[1]);
        sname.setText(" Common name : " + eachValue[2]);
        dt_taken.setText(" Date&Time : " + eachValue[5]);
        n_comments.setText(" " + eachValue[6] + " comment(s)");
        
        //Bitmap bit = BitmapFactory.decodeResource(getContext()., id)
        
        phone_image.setBackgroundResource(R.drawable.shapedrawable);
        
        
	    if(eachValue[0] == null) {
	    	pheno_image.setImageResource(mContext.getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null));
	    }
	    else {
	    	pheno_image.setImageResource(mContext.getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + eachValue[0], null, null));
	    }
		*/
        /*
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        
        dialog.setTitle(" " + item.getTitle());
        //dialog.setMessage("Num of Comments: " + item.getSnippet());
        dialog.setView(linear);
        dialog.setPositiveButton("See Detail", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(mContext, WPinfo.class);

					intent.putExtra("common_name", common_name);
					intent.putExtra("science_name", science_name);
					intent.putExtra("comment", num_of_comments);
					intent.putExtra("dt_taken", date_taken);
					intent.putExtra("latitude", latitude);
					intent.putExtra("longitude", longitude);
					intent.putExtra("pheno", pheno);
					intent.putExtra("comment_count", Integer.parseInt(num_of_comments));
					
					mContext.startActivity(intent);
				}
			});
        dialog.setNegativeButton("Okay", null);
        dialog.show();
        */
        
    }
	class PopupPanel {
		View popup;
		boolean isVisible=false;
		
		LayoutInflater mInflater = LayoutInflater.from(mContext);
		
		PopupPanel(int layout) {
			ViewGroup parent=(ViewGroup)map.getParent();
			popup = mInflater.inflate(layout, parent, false);
									
			Log.i("K", "PARENT : " + parent);
			Log.i("K", "MAP : " + map);
			Log.i("K", "POP UP " + popup);
			
			popup.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					hide();
				}
			});
		}
		
		View getView() {
			return(popup);
		}
		
		void show() {
			RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT
			);
			
			Log.i("K", "SHOW!!");
			
			lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			lp.setMargins(0, 0, 0, 60);
			
			// if there is a pop-up message, hide it.
			hide();
			// and show the new one.
			((ViewGroup)map.getParent()).addView(popup, lp);
			isVisible=true;
		}
		
		void hide() {
			if (isVisible) {
				isVisible=false;
				((ViewGroup)popup.getParent()).removeView(popup);
			}
		}
	}
}