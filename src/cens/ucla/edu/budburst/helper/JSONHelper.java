package cens.ucla.edu.budburst.helper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class JSONHelper {
	private JSONObject jObj = null;
	
	public JSONHelper() {
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
	
	public String getPlantTags(String string) throws Exception {
		JSONObject json = new JSONObject(new JSONTokener(string));
		JSONArray jarray = json.getJSONArray("tag");
		
		String getJSON = "";
		
		for(int i = 0 ; i < jarray.length() ; i++) {
			JSONObject obj = jarray.getJSONObject(i);
			getJSON += obj.getString("title");
			getJSON += ";;";
			getJSON += obj.getString("common");
			getJSON += ";;";
			getJSON += obj.getString("science");
			getJSON += ";;";
			getJSON += obj.getString("text");
			getJSON += ";;";
			getJSON += obj.getString("imageUrl");
			getJSON += "\n\n\n\n";
		}

		Log.i("K", "JSON : " + getJSON);
		return getJSON;
	}
}
