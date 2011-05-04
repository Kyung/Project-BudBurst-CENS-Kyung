package cens.ucla.edu.budburst.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class HelperDrawableManager {
	private static final int COMPLETE = 0;
	private ProgressBar mSpinner;
	private final HashMap<String, Drawable> drawableMap;
	
	public HelperDrawableManager(ProgressBar mSpinner) {
		this.mSpinner = mSpinner;
		drawableMap = new HashMap<String, Drawable>();
	}
	
	public Drawable fetchDrawable(String url) {
		if(drawableMap.containsKey(url)) {
			return drawableMap.get(url);
		}
		
		try {
			InputStream is = fetch(url);
			Drawable drawable = Drawable.createFromStream(is, "src");
			drawableMap.put(url, drawable);
			
			return drawable;
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void fetchDrawableOnThread(final String url, final ImageView imageView) {
		
		if(drawableMap.containsKey(url)) {
			Log.i("K", "url : " + drawableMap.get(url));
			imageView.setImageDrawable(drawableMap.get(url));
		}
		
		mSpinner.setVisibility(View.VISIBLE);
		imageView.setVisibility(View.GONE);
		
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				switch(message.what) {
				case COMPLETE:
					imageView.setImageDrawable((Drawable) message.obj);
					imageView.setVisibility(View.VISIBLE);
					mSpinner.setVisibility(View.GONE);
					break;
				}
			}
		};
		
		Thread thread = new Thread() {
			@Override
			public void run() {
				Drawable drawable = fetchDrawable(url);
				Message message = handler.obtainMessage(COMPLETE, drawable);
				handler.sendMessage(message);
			}
		};
		
		thread.start();
	}
	
	public InputStream fetch(String url) throws MalformedURLException, IOException {
		
		HttpGet httpGet = new HttpGet(url);
		
		HttpParams httpParams = new BasicHttpParams();
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
		
		int timeoutSocket = 5000;
		HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);
		
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		HttpResponse response = httpClient.execute(httpGet);
		
		return response.getEntity().getContent();
	}
}
