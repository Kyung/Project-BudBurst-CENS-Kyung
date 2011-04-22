package cens.ucla.edu.budburst.helper;

public class FloracacheItem {
	private int mFloracacheID;
	private int mUserID;
	private int mUserSpeciesID;
	private int mUserSpeciesCategoryID;
	private int mUserStationID;
	private int mProtocolID;
	private double mLatitude;
	private double mLongitude;
	private String mFloracacheNotes;
	private String mFloracacheDate;
	private String mCommonName;
	private String mScienceName;
	
	
	public FloracacheItem(int floracacheID, int userID, 
			int userSpeciesID, int userSpeciesCategoryID,
			int userStationID, double latitude, double longitude,
			String floracacheNotes, String floracacheDate,
			String commonName, String scienceName, int protocolID) {
		
		mFloracacheID = floracacheID;
		mUserID = userID;
		mUserSpeciesID = userSpeciesID;
		mUserSpeciesCategoryID = userSpeciesCategoryID;
		mUserStationID = userStationID;
		mLatitude = latitude;
		mLongitude = longitude;
		mFloracacheNotes = floracacheNotes;
		mFloracacheDate = floracacheDate;
		mCommonName = commonName;
		mScienceName = scienceName;
		mProtocolID = protocolID;
		
	}
	
	public int getFloracacheID() {
		return mFloracacheID;
	}
	
	public int getUserID() {
		return mUserID;
	}
	
	public int getUserSpeciesID() {
		return mUserSpeciesID;
	}
	
	public int getUserSpeciesCategoryID() {
		return mUserSpeciesCategoryID;
	}
	
	public int getUserStationID() {
		return mUserStationID;
	}
	
	public double getLatitude() {
		return mLatitude;
	}
	
	public double getLongitude() {
		return mLongitude;
	}
	
	public String getFloracacheNotes() {
		return mFloracacheNotes;
	}
	
	public String getFloracacheDate() {
		return mFloracacheDate;
	}
	
	public String getCommonName() {
		return mCommonName;
	}
	
	public String getScienceName() {
		return mScienceName;
	}
	public int getProtocolID() {
		return mProtocolID;
	}
	
	
}
