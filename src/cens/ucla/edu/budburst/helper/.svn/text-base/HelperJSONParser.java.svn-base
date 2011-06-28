package cens.ucla.edu.budburst.helper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.SharedPreferences;
import android.util.Log;

public class HelperJSONParser {
	private JSONObject jObj = null;
	
	public HelperJSONParser() {
		jObj = new JSONObject();
	}
	
	public String getArea(String string) throws Exception {
		JSONObject json = new JSONObject(new JSONTokener(string));
		JSONArray jarray = json.getJSONArray("area");
		
		String getJSON = "";
		
		for(int i = 0 ; i < jarray.length() ; i++) {
			JSONObject obj = jarray.getJSONObject(i);
			getJSON += obj.getInt("id");
			getJSON += ";";
			getJSON += obj.getString("title");
			getJSON += ";";
			getJSON += obj.getDouble("latitude");
			getJSON += ";";
			getJSON += obj.getDouble("longitude");
			getJSON += ";";
			getJSON += obj.getDouble("distance");
			getJSON += "\n";
		}

		Log.i("K", "JSON : " + getJSON);
		return getJSON;
	}
	
	public String getFlickrTags(String string) throws Exception {
		
		Log.i("K", "STRING : " + string);
		JSONObject json = new JSONObject(string);
		
		JSONObject menuObject = json.getJSONObject("photos");
		JSONArray photoInfo = menuObject.getJSONArray("photo");
		
		String getJSON = "";
		
		for(int i = 0 ; i < photoInfo.length() ; i++) {
			JSONObject obj = photoInfo.getJSONObject(i);
			
			getJSON += obj.getString("id");
			getJSON += ";;";
			//getJSON += obj.getString("owner");
			//getJSON += ";;";
			getJSON += obj.getString("secret");
			getJSON += ";;";
			getJSON += obj.getString("farm");
			getJSON += ";;";
			if(obj.getString("title").equals("")) {
				getJSON += "no title";
			}
			else {
				getJSON += obj.getString("title");
			}
			getJSON += ";;";
			getJSON += obj.getString("datetaken");
			getJSON += ";;";
			getJSON += obj.getString("latitude");
			getJSON += ";;";
			getJSON += obj.getString("longitude");
			getJSON += ";;";
			getJSON += obj.getString("ownername");
			getJSON += ";;";
			getJSON += obj.getString("server");
			getJSON += ";;";
			getJSON += obj.getString("category");
			getJSON += "\n\n";
		}
		
		Log.i("K", "JSON : " + getJSON);
		return getJSON;
	}
	
public String getFlickrPBBTags(String string) throws Exception {
		
		Log.i("K", "STRING : " + string);
		JSONObject json = new JSONObject(string);
		
		JSONObject menuObject = json.getJSONObject("photos");
		JSONArray photoInfo = menuObject.getJSONArray("photo");
		
		String getJSON = "";
		
		for(int i = 0 ; i < photoInfo.length() ; i++) {
			JSONObject obj = photoInfo.getJSONObject(i);
			
			getJSON += obj.getString("common_name");
			getJSON += ";;";
			getJSON += obj.getString("science_name");
			getJSON += ";;";
			getJSON += obj.getString("phenophase");
			getJSON += ";;";
			getJSON += obj.getString("dt_taken");
			getJSON += ";;";
			getJSON += obj.getString("latitude");
			getJSON += ";;";
			getJSON += obj.getString("longitude");
			getJSON += ";;";
			getJSON += obj.getString("distance");
			getJSON += "\n\n";
		}
		
		Log.i("K", "JSON : " + getJSON);
		return getJSON;
	}
	
	
	
	public String getCommentTags(String string) throws Exception {
		JSONObject json = new JSONObject(new JSONTokener(string));
		JSONArray jarray = json.getJSONArray("comment");
		
		String getJSON = "";
		
		for(int i = 0 ; i < jarray.length() ; i++) {
			JSONObject obj = jarray.getJSONObject(i);
			getJSON += obj.getString("pheno_id");
			getJSON += ";;";
			getJSON += obj.getString("cname");
			getJSON += ";;";
			getJSON += obj.getString("sname");
			getJSON += ";;";
			getJSON += obj.getString("latitude");
			getJSON += ";;";
			getJSON += obj.getString("longitude");
			getJSON += ";;";
			getJSON += obj.getString("dt_taken");
			getJSON += ";;";
			getJSON += obj.getInt("comment_count");
			getJSON += ";;";
			getJSON += obj.getString("comment");
			getJSON += "\n\n\n\n";
		}
		
		Log.i("K", "JSON : " + getJSON);
		return getJSON;
	}
	
	public String getSiteName(String string) throws Exception {
		
		JSONObject jObj = new JSONObject(new JSONTokener(string));
		JSONObject jsonTag = jObj.getJSONObject("tag");
		
		return jsonTag.getString("area_name");
	}
	
	public String getSiteId(String string) throws Exception {
		
		JSONObject jObj = new JSONObject(new JSONTokener(string));
		JSONObject jsonTag = jObj.getJSONObject("tag");
		
		return jsonTag.getString("area_id");
	}
	
	public String getPlantTags(String string) throws Exception {
		JSONObject jObj1 = new JSONObject(new JSONTokener(string));
		JSONObject jObj2 = jObj1.getJSONObject("tag");
		JSONArray jArray = jObj2.getJSONArray("tags");
		
		String getJSON = "";
		
		for(int i = 0 ; i < jArray.length() ; i++) {
			JSONObject obj = jArray.getJSONObject(i);
			getJSON += obj.getString("common");
			getJSON += ";;";
			getJSON += obj.getString("science");
			getJSON += ";;";
			getJSON += obj.getString("text");
			getJSON += ";;";
			getJSON += obj.getString("imageName");
			getJSON += "\n\n\n\n";
		}

		Log.i("K", "JSON : " + getJSON);
		return getJSON;
	}
}
