package cens.ucla.edu.budburst.helper;

public class Values {
	// this class is to define the frequently used values...
	
	// return from where?
	public static final int RETURN_FROM_PLANT_INFORMATION = 0;
	public static final int RETURN_FROM_PLANT_QUICK = 1;
	public static final int GETPHENOPHASE_ONE_TIME = 2;
	
	// previous activity
	public static final int FROM_ABOUT = 99;
	public static final int FROM_PLANT_LIST = 100;
	public static final int FROM_QUICK_CAPTURE = 101;
	public static final int FROM_ONETIME_DIRECT = 102;
	public static final int FROM_MAIN_PAGE = 103;
	public static final int FROM_ONE_TIME_MAIN = 104;
	public static final int WHATSINVASIVE = 1000;
	public static final int FROM_PBB_PHENOPHASE = 1000;
	public static final int FROM_QC_PHENOPHASE = 1001;
	
	// GPS variable
	public static final String GPX_SERVICE = "cens.ucla.edu.helper.BackgroundService.SERVICE";
	
	// unknown plant
	public static final int UNKNOWN_SPECIES = 999;
	
	// category
	public static final int WILD_FLOWERS = 2;
	public static final int GRASSES = 3;
	public static final int DECIDUOUS_TREES = 4;
	public static final int DECIDUOUS_TREES_WIND = 5;
	public static final int EVERGREEN_TREES = 6;
	public static final int EVERGREEN_TREES_WIND = 7;
	public static final int CONIFERS = 8;
	
	
	public static final String TEMP_PATH = "/sdcard/pbudburst/wi_list/";
	public static final String BASE_PATH = "/sdcard/pbudburst/";
	
	//official/unofficial
	public static final Integer UNOFFICIAL = 0;
	public static final Integer OFFICIAL = 1;
}
