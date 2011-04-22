package cens.ucla.edu.budburst.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class PBBItems implements Parcelable{

	private int mSpeciesID;
	private int mProtocolID;
	private int mPhenophaseID;
	private int mCategory;
	private int mSiteID;
	private int mPlantID;
	//private int mPhenoIconID;
	
	private String mCommonName;
	private String mScienceName;
	private String mDate;
	private String mNote;
	private String mLocalImageName;
	//private String mPhenoText;
	//private String mPhenoDescription;
	
	private double mLatitude;
	private double mLongitude;
	
	public PBBItems() {;};
	
	public PBBItems(Parcel source) {
		
		mSpeciesID = source.readInt();
		mProtocolID = source.readInt();
		mPhenophaseID = source.readInt();
		mCategory = source.readInt();
		mSiteID = source.readInt();
		mPlantID = source.readInt();
		//mPhenoIconID = source.readInt();
		
		
		mCommonName = source.readString();
		mScienceName = source.readString();
		mDate = source.readString();
		mNote = source.readString();
		mLocalImageName = source.readString();
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
		mLocalImageName = imageName;
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
	
	public String getImageName() {
		return mLocalImageName;
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
		
		dest.writeString(mCommonName);
		dest.writeString(mScienceName);
		dest.writeString(mDate);
		dest.writeString(mNote);
		dest.writeString(mLocalImageName);
		
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
