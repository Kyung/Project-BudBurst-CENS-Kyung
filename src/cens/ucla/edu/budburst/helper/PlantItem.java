package cens.ucla.edu.budburst.helper;

public class PlantItem{
	
	public PlantItem(String aCommonName, String aSpeciesName, String aImageUrl, int aCategory) {
		Category = aCategory;
		CommonName = aCommonName;
		SpeciesName = aSpeciesName;
		imageUrl = aImageUrl;
	}
	
	public PlantItem(String aCommonName, String aSpeciesName) {
		CommonName = aCommonName;
		SpeciesName = aSpeciesName;
	}
	
	public PlantItem(int aSpeciesID, String aCommonName, String aSpeciesName) {
		SpeciesID = aSpeciesID;
		CommonName = aCommonName;
		SpeciesName = aSpeciesName;
	}
	
	public PlantItem(int aSpeciesID, String aCommonName, String aSpeciesName, String aCredit) {
		SpeciesID = aSpeciesID;
		CommonName = aCommonName;
		SpeciesName = aSpeciesName;
		Credit = aCredit;
	}
	
	public PlantItem(int aPicture, String aCommonName, String aSpeciesName, int aSpeciesID){
		Picture = aPicture;
		CommonName = aCommonName;
		SpeciesName = aSpeciesName;
		SpeciesID = aSpeciesID;
	}

	public PlantItem(int aPicture, String aCommonName, String aSpeciesName, int aSpeciesID, int aProtocolID){
		Picture = aPicture;
		CommonName = aCommonName;
		SpeciesName = aSpeciesName;
		SpeciesID = aSpeciesID;
		ProtocolID = aProtocolID;
	}
	
	//Species(String aTitle, String aCommon_name, String aScience_name, String aText, String aImage_url)
	
	public PlantItem(String aCommonName, String aSpeciesName, String aText, String aImage_url) {
		CommonName = aCommonName;
		SpeciesName = aSpeciesName;
		Description = aText;
		imageUrl = aImage_url;
	}
	
	public PlantItem(String aCommonName, String aSpeciesName, String aText, String aImageName, String aImageUrl) {
		CommonName = aCommonName;
		SpeciesName = aSpeciesName;
		Description = aText;
		ImageName = aImageName;
		imageUrl = aImageUrl;
	}
	
	public PlantItem(int aPicture, String aNote, int aPhenoImageID, String aPhenoName, int aPhenoID, Boolean aHeader){
		Picture = aPicture;
		Note = aNote;
		PhenoImageID = aPhenoImageID;
		PhenoName = aPhenoName;
		Header = aHeader;
		PhenoID = aPhenoID;
	}
	
	public PlantItem(int aPicture, String aCommonName, String aSpeciesName, int aSpeciesID, int aSiteID, int aProtocolID, int aPheno_done, int aTotal_pheno, boolean aTopItem, String aSiteName, boolean aMonitor, int aSynced){
		Picture = aPicture;
		CommonName = aCommonName;
		SpeciesName = aSpeciesName;
		SiteName = aSiteName;
		SpeciesID = aSpeciesID;
		SiteID = aSiteID;
		ProtocolID = aProtocolID;
		current_pheno = aPheno_done;
		total_pheno = aTotal_pheno;
		TopItem = aTopItem;
		Monitor = aMonitor;
		Synced = aSynced;
	}
	
	public PlantItem (int aPicture, String aDescription, int aPheno_ID, int aPhenoImageID, String aPheno_name, boolean aFlag, String aCamera_image, String aDate, int aOneTimePlantID, String aNote, boolean aHeader) {
		Picture = aPicture;
		Description = aDescription;
		PhenoID = aPheno_ID;
		PhenoImageID = aPhenoImageID;
		PhenoName = aPheno_name;
		Flag = aFlag;
		ImageName = aCamera_image;
		Date = aDate;
		OneTimePlantID = aOneTimePlantID;
		Note = aNote;
		Header = aHeader;
	}

	
	public String CommonName;
	public String SpeciesName;
	public String ImageName;
	public String SiteName;
	public String PhenoName;	
	public String Description;
	public String imageUrl;
	public String Note;
	public String Credit;
	public String Date;
	public int Picture;
	public int SpeciesID;
	public int ProtocolID;
	public int PhenoID;
	public int PhenoImageID;
	public int SiteID;
	public int current_pheno;
	public int total_pheno;
	public int Synced;
	public int OneTimePlantID;
	public int Category;
	public boolean Header;
	public boolean TopItem;
	public boolean Monitor;
	public boolean Flag;
	
}