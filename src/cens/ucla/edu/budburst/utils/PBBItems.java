package cens.ucla.edu.budburst.utils;

import cens.ucla.edu.budburst.helper.HelperValues;
import android.os.Parcel;
import android.os.Parcelable;

public class PBBItems implements Parcelable{

	private int mSpeciesID;
	private int mProtocolID;
	private int mPhenophaseID;
	private int mCategory;
	private int mSiteID;
	private int mPlantID;
	private int mFloracacheID;
	private int mIsFloracache;
	private int mUserDefinedGroupID;
	private int mSpeciesImageID;
	private int mIsFlickr;
	//private int mPhenoIconID;
	
	private String mCommonName;
	private String mScienceName;
	private String mDate;
	private String mNote;
	private String mCameraImageName;
	private String mImageURL;
	
	//private String mPhenoText;
	//private String mPhenoDescription;
	
	private double mLatitude;
	private double mLongitude;
	
	public PBBItems() {
		// setting variables below with a default.
		mFloracacheID = HelperValues.IS_FLORACACHE_NO;
		mUserDefinedGroupID = HelperValues.IS_USER_DEFINED_NO;
		mIsFlickr = HelperValues.IS_FLICKR_NO;
	};
	
	public PBBItems(Parcel source) {
		
		mSpeciesID = source.readInt();
		mProtocolID = source.readInt();
		mPhenophaseID = source.readInt();
		mCategory = source.readInt();
		mSiteID = source.readInt();
		mPlantID = source.readInt();
		//mPhenoIconID = source.readInt();
		mFloracacheID = source.readInt();
		mIsFloracache = source.readInt();
		mUserDefinedGroupID = source.readInt();
		mSpeciesImageID = source.readInt();
		mIsFlickr = source.readInt();
		
		mCommonName = source.readString();
		mScienceName = source.readString();
		mDate = source.readString();
		mNote = source.readString();
		mCameraImageName = source.readString();
		mImageURL = source.readString();
		//mPhenoText = source.readString();
		//mPhenoDescription = source.readString();
		
		mLatitude = source.readDouble();
		mLongitude = source.readDouble();
			
	}
	
	public void setSpeciesID(int speciesID) {
		mSpeciesID = speciesID;
	}
	
	public void setProtocolID(int protocolID) {
		mProtocolID = protocolID;
	}
	
	public void setPhenophaseID(int phenophaseID) {
		mPhenophaseID = phenophaseID;
	}
	
	public void setCategory(int category) {
		mCategory = category;
	}
	
	public void setSiteID(int siteID) {
		mSiteID = siteID;
	}
	
	public void setPlantID(int plantID) {
		mPlantID = plantID;
	}
	
	public void setIsFloracache(int isFloracache) {
		mIsFloracache = isFloracache;
	}
	
	public void setFloracacheID(int floracacheID) {
		mFloracacheID = floracacheID;
	}
	
	public void setUserDefinedGroupID(int userDefinedGroupID) {
		mUserDefinedGroupID = userDefinedGroupID;
	}
	
	public void setIsFlicker(int isFlickr) {
		mIsFlickr = isFlickr;
	}
	
	/*
	public void setPhenoIconID(int phenoIconID) {
		mPhenoIconID = phenoIconID;
	}
	*/
	public void setCommonName(String commonName) {
		mCommonName = commonName;
	}
	
	public void setScienceName(String scienceName) {
		mScienceName = scienceName;
	}
	
	public void setDate(String date) {
		mDate = date;
	}
	
	public void setNote(String note) {
		mNote = note;
	}
	
	public void setLocalImageName(String imageName) {
		mCameraImageName = imageName;
	}
	
	public void setSpeciesImageID(int speciesImageName) {
		mSpeciesImageID = speciesImageName;
	}
	
	public void setImageURL(String imageURL) {
		mImageURL = imageURL;
	}
	
	/*
	public void setPhenoText(String phenoText) {
		mPhenoText = phenoText;
	}
	
	public void setPhenoDescription(String phenoDescription) {
		mPhenoDescription = phenoDescription;
	}
	*/
	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}
	
	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}
	
	public int getSpeciesID() {
		return mSpeciesID;
	}
	
	public int getProtocolID() {
		return mProtocolID;
	}

	public int getPhenophaseID() {
		return mPhenophaseID;
	}
	
	public int getFloracacheID() {
		return mFloracacheID;
	}
	
	public int getUserDefinedGroupID() {
		return mUserDefinedGroupID;
	}

	public int getIsFloracache() {
		return mIsFloracache;
	}
	
	public int getIsFlickr() {
		return mIsFlickr;
	}
	/*
	public int getPhenoIconID() {
		return mPhenoIconID;
	}
	*/
	public int getCategory() {
		return mCategory;
	}
	
	public int getSiteID() {
		return mSiteID;
	}
	
	public int getPlantID() {
		return mPlantID;
	}
	
	public String getCommonName() {
		return mCommonName;
	}
	
	public String getScienceName() {
		return mScienceName;
	}
	
	public String getDate() {
		return mDate;
	}
	
	public String getNote() {
		return mNote;
	}
	
	public String getCameraImageName() {
		return mCameraImageName;
	}
	
	public int getSpeciesImageID() {
		return mSpeciesImageID;
	}
	
	public String getImageURL() {
		return mImageURL;
	}
	/*
	public String getPhenoText() {
		return mPhenoText;
	}
	
	public String getPhenoDescription() {
		return mPhenoDescription;
	}
	*/
	public double getLatitude() {
		return mLatitude;
	}
	
	public double getLongitude() {
		return mLongitude;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
		dest.writeInt(mSpeciesID);
		dest.writeInt(mProtocolID);
		dest.writeInt(mPhenophaseID);
		dest.writeInt(mCategory);
		dest.writeInt(mSiteID);
		dest.writeInt(mPlantID);
		dest.writeInt(mFloracacheID);
		dest.writeInt(mIsFloracache);
		dest.writeInt(mUserDefinedGroupID);
		dest.writeInt(mSpeciesImageID);
		dest.writeInt(mIsFlickr);
		
		dest.writeString(mCommonName);
		dest.writeString(mScienceName);
		dest.writeString(mDate);
		dest.writeString(mNote);
		dest.writeString(mCameraImageName);
		dest.writeString(mImageURL);
		
		dest.writeDouble(mLatitude);
		dest.writeDouble(mLongitude);
		
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

		@Override
		public Object createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new PBBItems(source);
		}

		@Override
		public Object[] newArray(int size) {
			// TODO Auto-generated method stub
			return new PBBItems[size];
		}
	};
	
	
	
}
