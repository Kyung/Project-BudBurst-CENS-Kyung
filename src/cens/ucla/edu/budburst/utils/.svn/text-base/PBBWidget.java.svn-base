package cens.ucla.edu.budburst.utils;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.firstActivity;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperSharedPreference;
import cens.ucla.edu.budburst.myplants.PBBPlantList;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class PBBWidget extends AppWidgetProvider{
	
	private SyncDBHelper mSyncHelper;
	private OneTimeDBHelper mOneTimeHelper;
	private AlarmManager mAlarmManager;
	private PendingIntent pendingIntent;
	private HelperSharedPreference mHelper;
	private static final int WIDGET_UPDATE_INTERVAL = 15 * 1000;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		String action = intent.getAction();
		
		// if receiving widget update intent
		if(action.equals("android.appwidget.action.APPWIDGET_UPDATE")) {
			removePreviousAlarm();
			
			long firstTime = System.currentTimeMillis() + WIDGET_UPDATE_INTERVAL;
			pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		    mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		    mAlarmManager.set(AlarmManager.RTC, firstTime, pendingIntent);
		}
		// if receiving widget disabled intent
		else if(action.equals("android.appwidget.action.APPWIDGET_DISABLED")){
			Log.i("K", "PBBWidget : onDisabled()");
			removePreviousAlarm();
		}
		else if(action.equals("android.appwidget.action.APPWIDGET_DELETED")){
			Log.i("K", "PBBWidget : onDeleted()");
			removePreviousAlarm();
		}
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.i("K", "PBBWidget : onDeleted()");
		removePreviousAlarm();
	}
	
	@Override
	public void onDisabled(Context context) {
		Log.i("K", "PBBWidget : onDisabled()");
		removePreviousAlarm();
	}
	
	private void removePreviousAlarm() {
		if(pendingIntent != null && mAlarmManager != null) {
			
			Log.i("K", "PBBWidget : removeAlarm()");
			
			pendingIntent.cancel();
			mAlarmManager.cancel(pendingIntent);
		}
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		
		Log.i("K", "PBBWidget : onUpdate()");

		// Build an update that holds the updated widget contents
		RemoteViews updateViews = buildUpdate(context);
		
		ComponentName thisWidget = new ComponentName(context, PBBWidget.class);
		appWidgetManager.updateAppWidget(thisWidget, updateViews);
	}
	
	public RemoteViews buildUpdate(Context context) {
		RemoteViews updateViews = null;
		
		updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		
		Intent defineIntent = null;
		
		mHelper = new HelperSharedPreference(context);
		
		// check if the user has logged in the application.
		// if not, start the application from beginning
		if(mHelper.getPreferenceString("Username", "") == "") {
			updateViews.setTextViewText(R.id.total_num, "Please login first");
			
			defineIntent = new Intent(context, firstActivity.class);
		}
		// move to the user plant list page
		else {
			updateViews.setTextViewText(R.id.total_num, "Total Observed : " + getNumSpecies(context));
			updateViews.setTextViewText(R.id.num_unsynced, "Unsynced : " + getNumUnsynced(context) + 
										" / Floracache : " + getNumFloracache(context));
			
			defineIntent = new Intent(context, PBBPlantList.class);
		}
		
		pendingIntent = PendingIntent.getActivity(context, 0, defineIntent, 0);
		updateViews.setOnClickPendingIntent(R.id.widget_layer, pendingIntent);
		
		return updateViews;
	}
	
	private int getNumUnsynced(Context context) {
		
		int totalNum = 0;
		
		mSyncHelper = new SyncDBHelper(context);
		totalNum = mSyncHelper.getTotalNumberOfUnSynced(context);
		
		mOneTimeHelper = new OneTimeDBHelper(context);
		totalNum += mOneTimeHelper.getTotalUnsynced(context);
		
		return totalNum;
	}
	
	private int getNumSpecies(Context context) {
		
		int totalNum = 0;
		
		mSyncHelper = new SyncDBHelper(context);
		totalNum = mSyncHelper.getTotalNumberOfPlants(context);
		
		mOneTimeHelper = new OneTimeDBHelper(context);
		totalNum += mOneTimeHelper.getTotalNumberOfQCOPlants(context);
		
		return totalNum;
	}
	
	private int getNumFloracache(Context context) {
		int totalNum = 0;
		
		mOneTimeHelper = new OneTimeDBHelper(context);
		totalNum += mOneTimeHelper.getTotalNumberOfFloracache(context);
		
		return totalNum;
	}
}
