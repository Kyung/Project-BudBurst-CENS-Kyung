package cens.ucla.edu.budburst.helper;

public class HelperListItem {
	public HelperListItem(String aHeader, String aTitle, String aImage_url, String aDescription){
		Header = aHeader;
		Title = aTitle;
		ImageUrl = aImage_url;
		Description = aDescription;
	}
	
	public String Header;
	public String Title;
	public String ImageUrl;
	public String Description;
}
