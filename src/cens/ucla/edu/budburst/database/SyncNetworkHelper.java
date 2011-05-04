package cens.ucla.edu.budburst.database;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;



//This class is to download and to upload data with server



public class SyncNetworkHelper extends Activity{
	
	static final String TAG = "SyncNetworkHelper"; 
	
	public SyncNetworkHelper(){
	}
	
	static public String get_species_id(String username, String password) {
		
		try {
			String result = null;
			HttpClient httpClient = new DefaultHttpClient();
			String url = new String("" +
		    		"http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/phone/phone_service.php" +
		    		"?get_spcies_id&username=" + 
		    		username+"&password="+password);
			
			HttpPost httppost = new HttpPost(url);
			
			HttpResponse response = httpClient.execute(httppost);
        	BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent())); 
        	StringBuilder result_str = new StringBuilder();
			for(;;){
				String line = rd.readLine();
				if (line == null) 
					break;
				result_str.append(line+'\n');
			}
        	result = result_str.toString();
	        Log.d(TAG, result);
	        return result;
			
		}
		catch(Exception e){
		
		}
	
		return null;
	}
	
	static public String upload_new_site(String username, String password, 
			String site_id, String site_name, String latitude, String longitude, String accuracy,
			String comments, String hdisturbance, String shading, String irrigation, String habitat, String official){
		
		try{
	        // Add your data  
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(9);
	        String result = null;

			HttpClient httpclient = new DefaultHttpClient();  
		    		    
		    String url = new String("" +
		    		"http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/phone/phone_service.php" +
		    		"?add_site&username=" + 
		    		username+"&password="+password);
		    HttpPost httppost = new HttpPost(url);

        	nameValuePairs.add(new BasicNameValuePair("site_id", site_id));  
        	nameValuePairs.add(new BasicNameValuePair("site_name", site_name));
        	nameValuePairs.add(new BasicNameValuePair("latitude", latitude));
        	nameValuePairs.add(new BasicNameValuePair("longitude", longitude));
        	nameValuePairs.add(new BasicNameValuePair("accuracy", accuracy));
        	nameValuePairs.add(new BasicNameValuePair("comments", comments));
        	nameValuePairs.add(new BasicNameValuePair("human_disturbance", hdisturbance));
        	nameValuePairs.add(new BasicNameValuePair("shading", shading));
        	nameValuePairs.add(new BasicNameValuePair("irrigation", irrigation));
        	nameValuePairs.add(new BasicNameValuePair("habitat", habitat));
        	nameValuePairs.add(new BasicNameValuePair("official", official));
        	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  
	  
	        // Execute HTTP Post Request  
        	HttpResponse response = httpclient.execute(httppost);
        	BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent())); 
        	StringBuilder result_str = new StringBuilder();
			for(;;){
				String line = rd.readLine();
				if (line == null) 
					break;
				result_str.append(line+'\n');
			}
        	result = result_str.toString();
	        Log.d(TAG, result);
	        
			return result;
		}catch(Exception e){
			
		}
		
		return null;
	}

	static public String upload_onetime_ob(String username, String password, 
			int plant_id, int species_id, int site_id,
			int protocol_id, String cname, String sname, 
			int active, int category, int isFloracache,
			int floracacheID) {
		try{
			
			Log.i("K", "plant id : " + plant_id + " species_id : " + species_id + " site_id : " + site_id + " protocol_id : " + protocol_id + " cname : " + cname + " sname : " + sname + " active : " + active);
			
	        // Add your data  
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			HttpClient httpClient = new DefaultHttpClient();
	        String result = null;
    
			String url = new String("http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/phone/onetime_plant.php?username=" + username + "&password=" + password);
		    HttpPost httpPost = new HttpPost(url);
		    
		    nameValuePairs.add(new BasicNameValuePair("plant_id", Integer.toString(plant_id))); 
        	nameValuePairs.add(new BasicNameValuePair("species_id", Integer.toString(species_id)));  
        	nameValuePairs.add(new BasicNameValuePair("protocol_id", Integer.toString(protocol_id)));  
        	nameValuePairs.add(new BasicNameValuePair("site_id", Integer.toString(site_id)));  
        	nameValuePairs.add(new BasicNameValuePair("cname", cname));
        	nameValuePairs.add(new BasicNameValuePair("sname", sname));
        	nameValuePairs.add(new BasicNameValuePair("active", Integer.toString(active)));
        	nameValuePairs.add(new BasicNameValuePair("category", Integer.toString(category)));
        	nameValuePairs.add(new BasicNameValuePair("is_floracache", Integer.toString(isFloracache)));
        	nameValuePairs.add(new BasicNameValuePair("floracache_id", Integer.toString(floracacheID)));
        	httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));


			HttpResponse response = httpClient.execute(httpPost);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent())); 
	        	String line = br.readLine();
				
				Log.i("K", "Result(upload onetime ob) : " + line);
				
				if(line.equals("UPLOADED_OK")) {
					return "UPLOADED_OK";
				}
				else {
					Log.e("K", "UPLOADED FAILED!!");
				}
			}
		}catch(Exception e){
			
		}
		
		return null;
	}
	
	static public String upload_new_plant(String username, String password, Context cont
			,String species_id, String site_id, Integer active, String common_name, Integer protocol_id, Integer category){
		try{
			
	        // Add your data  
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        String result = null;

			HttpClient httpclient = new DefaultHttpClient();  
		    		    
		    String url = new String("" +
		    		"http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/phone/phone_service.php" +
		    		"?add_plant&username=" + 
		    		username+"&password="+password);
		    HttpPost httppost = new HttpPost(url);
		    
		    Log.i("K","species_id : " + species_id + " , site_id : " + site_id + 
		    		", ACTIVE : " + active + ", Common Name : " + common_name + 
		    		" Protocl_id : " + protocol_id + ", category : " + category);
		    
		    String active_str = active.toString();
		    String protocol_id_str = protocol_id.toString();
		    String category_str = category.toString();
		    
        	nameValuePairs.add(new BasicNameValuePair("species_id", species_id));  
        	nameValuePairs.add(new BasicNameValuePair("site_id", site_id));  
        	nameValuePairs.add(new BasicNameValuePair("active", active_str));
        	nameValuePairs.add(new BasicNameValuePair("common_name", common_name));
        	nameValuePairs.add(new BasicNameValuePair("protocol_id", protocol_id_str));
        	nameValuePairs.add(new BasicNameValuePair("category", category_str));
        	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        	
        	
        	Log.i("K", "HTTP POST : " + httppost.toString());
	  
	        // Execute HTTP Post Request  
        	HttpResponse response = httpclient.execute(httppost);
        	BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent())); 
        	StringBuilder result_str = new StringBuilder();
			for(;;){
				String line = rd.readLine();
				if (line == null) 
					break;
				result_str.append(line+'\n');
			}
        	result = result_str.toString();
	        Log.d("K", "Add Plant Response : " + result);
	        
			return result;
		}
		catch(Exception e){
			Log.e("K", e.toString());
			return null;
		}
	}
	
	
	static public String upload_quick_observations(String username, String password, Context context,
			Integer plant_id, Integer phenophase_id, Double latitude, Double longitude, Float accuracy, String image_id,
			String dt_taken, String notes) {
		
		try {
			String result = null;
			
			HttpClient httpClient = new DefaultHttpClient();
		    String url = new String("" +
		    		"http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/phone/onetime_observation.php?username=" +
		    		username+"&password="+password);
		    
		    Log.i("K", "URL : " + url);
		    HttpPost httppost = new HttpPost(url);

		    Log.i("K", "plant_id " + plant_id + " phenophase_id : " + phenophase_id + " latitude : " + latitude + " longitude : " + longitude + " date : " + dt_taken + " notes : " + notes);
		    
        	MultipartEntity entity = new MultipartEntity();
        	entity.addPart("plant_id", new StringBody(Integer.toString(plant_id)));
        	entity.addPart("phenophase_id", new StringBody(Integer.toString(phenophase_id)));
        	//entity.addPart("latitude", new StringBody(latitude.toString()));
        	//entity.addPart("longitude", new StringBody(longitude.toString()));
        	//entity.addPart("accuracy", new StringBody(Float.toString(accuracy)));
        	entity.addPart("date", new StringBody(dt_taken));
        	entity.addPart("note", new StringBody(notes));

        	File file = new File("/sdcard/pbudburst/" + image_id.toString() + ".jpg");
		    if(file.exists()) {
		    	Log.i("K", "=============FILE IS IN THE SDCARD=============");
		    	entity.addPart("image", new FileBody(file));
		    }
		   
		    httppost.setEntity(entity);
        	
	        // Execute HTTP Post Request  
        	HttpResponse response = httpClient.execute(httppost);
        
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent())); 
	        	String line = br.readLine();
				
				Log.i("K", "Result(upload onetime ob) : " + line);
				if(line.equals("UPLOADED_OK")) {
					return "UPLOADED_OK";
				}
				else {
					Log.e("K", "UPLOADED FAILED!!");
				}
			}
		}
		catch(Exception e){
			Log.e(TAG, e.toString());
			return null;
		}
		
		return null;
	}

	
	static public String upload_new_obs(String username, String password, Context cont,
			String species_id, String site_id, String phenophase_id,
			String time, String note, String image_id){
		try{
	        // Add your data  
			String result = null;

			HttpClient httpclient = new DefaultHttpClient();  
		    String url = new String("" +
		    		"http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/phone/phone_service.php" +
		    		"?submit_obs&username=" +
		    		username+"&password="+password);
		    HttpPost httppost = new HttpPost(url);
        	
        	MultipartEntity entity = new MultipartEntity();
        	entity.addPart("species_id", new StringBody(species_id));
        	entity.addPart("site_id", new StringBody(site_id));
        	entity.addPart("phenophase_id", new StringBody(phenophase_id));
        	entity.addPart("time", new StringBody(time));
        	entity.addPart("note", new StringBody(note));
        	
        	
        	File file = new File("/sdcard/pbudburst/" + image_id.toString() + ".jpg");

		    if(file.exists()){
	        	entity.addPart("image", new FileBody(file));
		    }
		    
        	httppost.setEntity(entity);
        	
	        // Execute HTTP Post Request  
        	HttpResponse response = httpclient.execute(httppost);
        	result = response.toString();
	        Log.d(TAG, response.toString());
	        return result;
		}
		catch(Exception e){
			Log.e(TAG, e.toString());
			return null;
		}
	}
	
	
	//Download data
	static public String download_json(String url_addr){
		StringBuilder result = new StringBuilder();
		
		Log.i("K", "IN DOWNLOAD JSON");
		
		try{
			URL url = new URL(url_addr);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			if(conn != null){
				conn.setConnectTimeout(10000);
				conn.setUseCaches(false);
				if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
					BufferedReader br = new BufferedReader(
							new InputStreamReader(conn.getInputStream()));
					for(;;){
						String line = br.readLine();
						if (line == null) 
							break;
						result.append(line+'\n');
					}
					
					Log.i("K", "RESULT : " + result);
					
					br.close();
				}
				else{
					conn.disconnect();
					return null;
				}
				conn.disconnect();
			}
		}catch(Exception e){
			Log.e(TAG, e.toString());
			return null;
		}
		return result.toString();
	}
	
	//Download image
	static public Boolean download_image(String url_addr, int image_id){
		try{
			String BASE_PATH = "/sdcard/pbudburst/";
			
			if(!new File(BASE_PATH).exists()){
				try{new File(BASE_PATH).mkdirs();}
				catch(Exception e){
					Log.e(TAG, e.toString());
					return false;
				}
			}

			String image_URL = url_addr + "?image_id=" + image_id;
			String path  = BASE_PATH + image_id + ".jpg";
			ContentDownloader downloader = new ContentDownloader(image_URL);
			if(!downloader.downloadContentsTo(path))
				return false;
			return true;
		}catch(Exception e){
			Log.e(TAG,e.toString());
			return false;
		}
	}
	
	
	//Download image
	static public void download_image_for_onetime(String url_addr, int image_id){
		try{
			String BASE_PATH = "/sdcard/pbudburst/";
			
			if(!new File(BASE_PATH).exists()){
				try{new File(BASE_PATH).mkdirs();}
				catch(Exception e){
					Log.e(TAG, e.toString());
				}
			}

			String image_URL = url_addr + "?image_id=" + image_id;
			String path  = BASE_PATH + "quick_" + image_id + ".jpg";
			ContentDownloader downloader = new ContentDownloader(image_URL);
			downloader.downloadContentsTo(path);
		}catch(Exception e){
			Log.e(TAG,e.toString());
		}
	}
}

class ContentDownloader {

	final private String TAG = "ContentDownloader"; 
	
    private String url;
    private String destinationFile;

    public ContentDownloader(String url){
        this.url = url;
    }

    public boolean downloadContentsTo(String destinationFile){
        this.destinationFile = destinationFile;
        
        if(new File(destinationFile).exists())
        	return true;

        URL urlObject = null;
        try {
            urlObject = new URL(url);
        }catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }

        Log.i("K", "destinationFile : " + destinationFile);
        
        
        BufferedOutputStream bufferedOutput = null;

        try{
        	URLConnection conn = urlObject.openConnection();
        	conn.connect();
        	
        	InputStream is = conn.getInputStream();
        	BufferedInputStream bufferedInput = new BufferedInputStream(is);
            
            ByteArrayBuffer baf = new ByteArrayBuffer(50);
            
            int current = 0;
            while((current = bufferedInput.read()) != -1) {
            	baf.append((byte)current);
            }
            
            FileOutputStream fos = new FileOutputStream(this.destinationFile);
            fos.write(baf.toByteArray());
            fos.close();
            is.close();
	
        }catch (IOException e) {
        	Log.e(TAG, e.toString());
            e.printStackTrace();
            return false;
        }
        System.out.println("Downloading completed");
        return true;
    }
}

