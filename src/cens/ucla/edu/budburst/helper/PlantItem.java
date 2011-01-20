package cens.ucla.edu.budburst.helper;

public class PlantItem{
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
		protocolID = aProtocolID;
	}
	
	public int Picture;
	public String CommonName;
	public String SpeciesName;
	public int SpeciesID;
	public int protocolID;
}