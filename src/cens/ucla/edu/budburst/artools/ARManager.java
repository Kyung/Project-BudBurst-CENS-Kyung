package cens.ucla.edu.budburst.artools;


import java.util.ArrayList;
import java.util.List;

import cens.ucla.edu.budburst.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.hardware.Camera.Size;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ARManager extends Activity {

	Camera mCamera;
	
	private SurfaceHolder.Callback mSurfaceListener = 
        new SurfaceHolder.Callback() {

            public void surfaceCreated(SurfaceHolder holder) {
                // SurfaceView°¡ »ý¼ºµÇ¸é Ä«¸Þ¶ó¸¦ ¿­
                mCamera = Camera.open();               
                // SDK1.5¿¡¼­ setPreviewDisplayÀÌ IO ExceptionÀ» throwÇÑ´Ù
                try {
					mCamera.setPreviewDisplay(holder);
				} catch (Exception e) {
					mCamera.release(); 
		            mCamera = null; 

					e.printStackTrace();
				}
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                // SurfaceView°¡ »èÁ¦µÇ´Â ½Ã°£¿¡ Ä«¸Þ¶ó¸¦ °³¹æ
            	mCamera.stopPreview(); 
                mCamera.release(); 
                mCamera = null; 
            
            }

            public void surfaceChanged(SurfaceHolder holder,
                                       int format,
                                       int width,
                                       int height) {
                // ¹Ì¸®º¸±â Å©±â¸¦ ¼³Á¤
            	Camera.Parameters parameters = mCamera.getParameters(); 
            	 
                //List<Size> sizes = parameters.getPictureSize();
            	Size size = parameters.getPreviewSize();
                //Size optimalSize = getOptimalPreviewSize(sizes, width, height); 
                //parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            	parameters.setPreviewSize(size.width, size.height);
                
                mCamera.setParameters(parameters); 
                mCamera.startPreview();               
            }
        };
        
        private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) { 
            final double ASPECT_TOLERANCE = 0.05; 
            double targetRatio = (double) w / h; 
            if (sizes == null) return null; 
     
            Size optimalSize = null; 
            double minDiff = Double.MAX_VALUE; 
     
            int targetHeight = h; 
     
            // Try to find an size match aspect ratio and size 
            for (Size size : sizes) { 
                double ratio = (double) size.width / size.height; 
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue; 
                if (Math.abs(size.height - targetHeight) < minDiff) { 
                    optimalSize = size; 
                    minDiff = Math.abs(size.height - targetHeight); 
                } 
            } 
     
            // Cannot find the one match the aspect ratio, ignore the requirement 
            if (optimalSize == null) { 
                minDiff = Double.MAX_VALUE; 
                for (Size size : sizes) { 
                    if (Math.abs(size.height - targetHeight) < minDiff) { 
                        optimalSize = size; 
                        minDiff = Math.abs(size.height - targetHeight); 
                    } 
                } 
            } 
            return optimalSize; 
        } 
        
	// DEVICE
	final int maxAngle	= 90;
	public static Activity activity;

	// GPS
	LocationManager 	locationManager;
	LocationListener 	gpsListener;
	boolean 			isGPS;

	// SENSOR
	SensorManager		sensorManager;	
	Location 			cLocation;
	String 				provider;
	float 				z,x,y;
	float[]				sValues;
	String 				pitchState;

	// CORE Variable
	public static double 	deviceDegree;
	ArrayList<ARViewObject> 	arViews;	

	// Projector View
	ARProjector 		arProjector;
	FrameLayout			mainLayout;

	// Temporary Variable
	public static ARObject[] 			arObjects;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.armanager);

		this.activity = this;
		this.mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
		Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        
        SurfaceView surface =
            (SurfaceView) findViewById(R.id.SurfaceView01);
        SurfaceHolder holder = surface.getHolder();

        // SurfaceView ¸®½º³Ê¸¦ µî·Ï
        holder.addCallback(mSurfaceListener);
        // ¿ÜºÎ ¹öÆÛ¸¦ »ç¿ëÇÏµµ·Ï ¼³Á¤
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
		
		new AlertDialog.Builder(this)
		.setTitle("Check GPS")
		.setMessage("Turn on GPS")
		.setPositiveButton("Yes", new AlertDialog.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				isGPS = true;
				initialize();
			}
		})
		.setNegativeButton("No", new AlertDialog.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				isGPS = false;
				initialize();
			}
		})        
		.setCancelable(true)
		.create()
		.show();
	}

	// ±¤¿ª ÃÊ±âÈ­
	public void initialize(){		

		arViews = new ArrayList<ARViewObject>();
		arProjector = (ARProjector) findViewById(R.id.projector);
		locationInitialize();		
		demoObjectGenerate();
	}	

	// µ¥¸ð Object »ý¼º
	public void demoObjectGenerate() {		
		ARObject demo1 = new ARObject();
		ARLocation demo1Location = new ARLocation();
		ARViewObject demo1View = new ARViewObject();
		
		ARObject demo2 = new ARObject();
		ARLocation demo2Location = new ARLocation();
		ARViewObject demo2View = new ARViewObject();
		
		// UCLA
		demo1Location.latitude = 34.0694;
		demo1Location.longitude = -118.4430;
		demo1.setArLocation(demo1Location);
		demo1.setArName("UCLA");
		
		demo1View.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.p39));
		demo1.setArViewObject(demo1View);
		

		// Mar Vista Park
		// 34.017949,-118.426094
		demo2Location.latitude = 34.017949;
		demo2Location.longitude = -118.426094;
		demo2.setArLocation(demo2Location);
		demo2.setArName("Mar Vista Park");
		
		demo2View.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon));
		demo2.setArViewObject(demo2View);
		
		arObjects = new ARObject[]{ demo1 , demo2 };
	}

	// SENSOR ÀÚ¿ø ÃÊ±âÈ­
	protected void onResume() {
		super.onResume();
		if (sensorManager == null) {
			// Locate the SensorManager using Activity.getSystemService
			sensorManager = (SensorManager) getSystemService(
					Context.SENSOR_SERVICE);
		}
		sensorManager.registerListener(mSensorListener, SensorManager.SENSOR_ORIENTATION, SensorManager.SENSOR_DELAY_FASTEST);
	}

	// GPS ¹× SENSOR ÀÚ¿ø Release
	protected void onPause() {
		super.onPause();
		if (sensorManager != null) 
			sensorManager.unregisterListener(mSensorListener);
		if (locationManager != null)
			locationManager.removeUpdates(mLocationListener);		
	}

	// GPS ÃÊ±âÈ­
	public void locationInitialize() {
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		if(isGPS) {
			provider = LocationManager.GPS_PROVIDER;
		}
		else {
			provider = LocationManager.NETWORK_PROVIDER;
		}		
		locationManager.requestLocationUpdates(provider, 1000, 10, mLocationListener);
	}

	// È­¸é UPDATE 
	public void update() {
		
		deviceDegree = z;	
		if(deviceDegree > 360) 
			deviceDegree -= 360;
		
		/*
		String text = "Orientationâ : "+deviceDegree + " degree\n";
		
		if(cLocation != null) {
			
			for(int i = 0 ; i < arObjects.length ; i++) {
				text += i + ": " + arObjects[i].getArLocation().angle + "degree / Dist : " + arObjects[i].getArLocation().distance + "\n";
			}
			
			text += "X :" + arObjects[0].getArViewObject().getPosition().getX() + " / Y :" + arObjects[0].getArViewObject().getPosition().getX();			
			
		}
		((TextView)findViewById(R.id.ex)).setText(text);
		*/
		
	}

	// GPS ¸®½º³Ê
	private final LocationListener mLocationListener = new LocationListener() {
		public void onLocationChanged(Location location){
			cLocation = location;			
			arProjector.setProjectorSwitch(true);
			for(int i = 0 ; i < arObjects.length ; i++ ) 
				// GPS°ª º¯ÇÒ¶§ ARObjectÀÇ Location°ª Àç¼³Á¤]				
				arObjects[i].setArLocation(new ARLocationProvider(cLocation, arObjects[i]).getARLoction());			
		}
		public void onProviderDisabled(String provider){}
		public void onProviderEnabled(String provider){}
		public void onStatusChanged(String provider, int status, Bundle extras){}
	};

	// SENSOR ¸®½º³Ê
	@SuppressWarnings("deprecation")
	private final SensorListener mSensorListener = new SensorListener() {
		public void onSensorChanged(int sensor, float[] values) {
			sValues = values;		
			if(arViews != null) arViews.clear();
			
			if(cLocation!=null) {
				ARViewObject[] arViewObjects = new ARViewObject[arObjects.length];
				for(int i = 0 ; i < arObjects.length ; i++ ) {				
					// ¼¾¼­°ª º¯È­½Ã ARViewÀÇ x,y°ª Àç¼³Á¤
					arObjects[i] = new ARViewProvider(ARManager.activity, arObjects[i]).getARViewObject();
					
					//arViewObjects[i] = arObjects[i].getArViewObject();
				}
				arProjector.setARView(arObjects);
				
			}
			sensorFiltering();
			update();
		}
		public void onAccuracyChanged(int sensor, int accuracy) {

		}
	};

	// ¼¾¼­°ª ÇÊÅÍ
	public void sensorFiltering(){
		z = Math.round(sValues[0] * 100) / 100;
		x = Math.round(sValues[1] * 100) / 100;
		y = Math.round(sValues[2] * 100) / 100;
	}

}