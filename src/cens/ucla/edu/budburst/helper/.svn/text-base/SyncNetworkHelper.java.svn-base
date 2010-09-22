package cens.ucla.edu.budburst.helper;

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
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

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
	
	static public String upload_new_site(String username, String password, 
			String site_id, String site_name, String latitude, String longitude, 
			String state, String comments){
		
		try{
	        // Add your data  
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
	        String result = null;

			HttpClient httpclient = new DefaultHttpClient();  
		    		    
		    String url = new String("" +
		    		"http://cens.solidnetdns.com/~jinha/PBB/PBsite_CENS/phone/phone_service.php" +
		    		"?add_site&username=" + 
		    		username+"&password="+password);
		    HttpPost httppost = new HttpPost(url);

        	nameValuePairs.add(new BasicNameValuePair("site_id", site_id));  
        	nameValuePairs.add(new BasicNameValuePair("site_name", site_name));
        	nameValuePairs.add(new BasicNameValuePair("latitude", latitude));
        	nameValuePairs.add(new BasicNameValuePair("longitude", longitude));
        	nameValuePairs.add(new BasicNameValuePair("state", state));
        	nameValuePairs.add(new BasicNameValuePair("comments", comments));
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
	
	static public String upload_new_plant(String username, String password, Context cont
			,String species_id, String site_id){
		try{
			
	        // Add your data  
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        String result = null;

			HttpClient httpclient = new DefaultHttpClient();  
		    		    
		    String url = new String("" +
		    		"http://cens.solidnetdns.com/~jinha/PBB/PBsite_CENS/phone/phone_service.php" +
		    		"?add_plant&username=" + 
		    		username+"&password="+password);
		    HttpPost httppost = new HttpPost(url);

        	nameValuePairs.add(new BasicNameValuePair("species_id", species_id));  
        	nameValuePairs.add(new BasicNameValuePair("site_id", site_id));  
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
	        Log.d(TAG, response.toString());
	        
			return result;
		}
		catch(Exception e){
			Log.e(TAG, e.toString());
			return null;
		}
	}
	

	
	static public String upload_new_obs(String username, String password, Context cont,
			String species_id, String site_id, String phenophase_id,
			String time, String note, String image_id){
		try{
	        // Add your data  
			String result = null;

			HttpClient httpclient = new DefaultHttpClient();  
		    String url = new String("" +
		    		"http://cens.solidnetdns.com/~jinha/PBB/PBsite_CENS/phone/phone_service.php" +
		    		"?submit_obs&username=" +
		    		username+"&password="+password);
		    HttpPost httppost = new HttpPost(url);
        	
        	MultipartEntity entity = new MultipartEntity();
        	entity.addPart("species_id", new StringBody(species_id));
        	entity.addPart("site_id", new StringBody(site_id));
        	entity.addPart("phenophase_id", new StringBody(phenophase_id));
        	entity.addPart("time", new StringBody(time));
        	entity.addPart("note", new StringBody(note));

		    if(!image_id.equals("")){
			    File file = new File("/sdcard/pbudburst/" + image_id.toString() + ".jpg");
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
}

class ContentDownloader {

	final private String TAG = "ContentDownloader"; 
	
    private String url;
    private static final int SIZE = 1024; 
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

        URLConnection urlConnection = null;
        InputStream inputStream = null;
        BufferedInputStream bufferedInput = null; 

        FileOutputStream outputStream = null;
        BufferedOutputStream bufferedOutput = null;

        try{
            urlConnection = urlObject.openConnection();
            inputStream = urlConnection.getInputStream();
            bufferedInput = new BufferedInputStream(inputStream);

            outputStream = new FileOutputStream(this.destinationFile);
            bufferedOutput = new BufferedOutputStream(outputStream);

            byte[] buffer = new byte[SIZE];
            while (true){
                int noOfBytesRead = bufferedInput.read(buffer, 0, buffer.length);
                if (noOfBytesRead == -1){
                    break;
                }
                bufferedOutput.write(buffer, 0, noOfBytesRead);
            }	
        }catch (IOException e) {
        	Log.e(TAG, e.toString());
            e.printStackTrace();
            return false;
        }finally{
            closeStreams(new InputStream[]{bufferedInput, inputStream}, 
                new OutputStream[]{bufferedOutput, outputStream});
        }
        System.out.println("Downloading completed");
        return true;
    }

    private void closeStreams(
        InputStream[] inputStreams, OutputStream[] outputStreams){

        try{
            for (InputStream inputStream : inputStreams){
                if (inputStream != null){
                    inputStream.close();
                }
            }
            for (OutputStream outputStream : outputStreams){
                if (outputStream != null){
                    outputStream.close();
                }
            }
        }catch(IOException exception){
        	Log.e(TAG, exception.toString());
            exception.printStackTrace();
        }
    }
}

