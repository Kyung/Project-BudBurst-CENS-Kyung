package cens.ucla.edu.budburst.floracaching;

public class FloracacheItem {
	private int mFloracacheID;
	private int mUserSpeciesID;
	private int mUserSpeciesCategoryID;
	private int mUserStationID;
	private int mProtocolID;
	private int mImageID;
	private double mLatitude;
	private double mLongitude;
	private String mFloracacheNotes;
	private String mFloracacheDate;
	private String mCommonName;
	private String mScienceName;
	private String mUserName;
	private String mUserNote;
	private String mDate;
	private String mObservedDate;
	
	public FloracacheItem() {}
	
	public void setFloracacheID(int floracacheID) {
		mFloracacheID = floracacheID;
	}
	
	public int getFloracacheID() {
		return mFloracacheID;
	}
	
	public void setUserName(String userName) {
		mUserName = userName;
	}
	
	public String getUserName() {
		return mUserName;
	}
	
	public void setUserSpeciesID(int speciesID) {
		mUserSpeciesID = speciesID;
	}
	
	public int getUserSpeciesID() {
		return mUserSpeciesID;
	}
	
	public void setUserSpeciesCategoryID(int category) {
		mUserSpeciesCategoryID = category;
	}
	
	public int getUserSpeciesCategoryID() {
		return mUserSpeciesCategoryID;
	}
	
	public void setUserStationID(int stationID) {
		mUserStationID = stationID;
	}
	
	public int getUserStationID() {
		return mUserStationID;
	}
	
	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}
	
	public double getLatitude() {
		return mLatitude;
	}
	
	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}
	
	public double getLongitude() {
		return mLongitude;
	}
	
	public void setFloracacheNotes(String note) {
		mFloracacheNotes = note;
	}
	
	public String getFloracacheNotes() {
		return mFloracacheNotes;
	}
	
	public void setFloracacheDate(String date) {
		mFloracacheDate = date;
	}
	
	public String getFloracacheDate() {
		return mFloracacheDate;
	}
	
	public void setCommonName(String commonName) {
		mCommonName = commonName;
	}
	
	public String getCommonName() {
		return mCommonName;
	}
	
	public void setScienceName(String scienceName) {
		mScienceName = scienceName;
	}
	
	public String getScienceName() {
		return mScienceName;
	}
	
	public void setProtocolID(int protocolID) {
		mProtocolID = protocolID;
	}
	
	public int getProtocolID() {
		return mProtocolID;
	}
	
	public void setUserNote(String note) {
		mUserNote = note;
	}
	
	public String getUserNote() {
		return mUserNote;
	}
	
	public void setDate(String date) {
		mDate = date;
	}
	
	public String getDate() {
		return mDate;
	}
	
	public void setImageID(int imageID) {
		mImageID = imageID;
	}
	
	public int getImageID() {
		return mImageID;
	}
	
	public void setObservedDate(String date) {
		mObservedDate = date;
	}
	
	public String getObservedDate() {
		return mObservedDate;
	}
	
	
}