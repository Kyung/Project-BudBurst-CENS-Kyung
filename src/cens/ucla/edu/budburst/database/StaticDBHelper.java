//Excerpted From "http://www.reigndesign.com/blog/using-your-own-sqlite-database-in-android-applications/"
//This is a database helper class that retrieves static database, which is in assets folder.

package cens.ucla.edu.budburst.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import cens.ucla.edu.budburst.weeklyplant.WeeklyPlant;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/*
public class StaticDBHelper extends SQLiteOpenHelper {

	public StaticDBHelper(Context context) {
		super(context, "staticBudburst.db", null, 1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.i("K", "make a staticTimePlant table");
		
		db.execSQL("CREATE TABLE species (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"species_name TEXT, " +
				"common_name TEXT, " +
				"protocol_id INTEGER, " +
				"category INTEGER, " +
				"description TEXT" +
				");");
		
		db.execSQL("CREATE TABLE Onetime_Observation (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"Phenophase_Icon INTEGER, " +
				"Type TEXT, " +
				"Description TEXT, " +
				"Detail_Description TEXT, " +
				"Category INTEGER" +
				");");
		
		db.execSQL("CREATE TABLE Phenophase_Protocol_Icon (Detail_Description TEXT, " +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"Phenophase_ID INTEGER, " +
				"Phenophase_Icon INTETER, " +
				"Protocol_ID INTEGER, " +
				"Chrono_Order INTEGER, " +
				"type TEXT, " +
				"description TEXT, " +
				"Phenophase_Name TEXT " +
				");");
		
		db.execSQL("CREATE TABLE Onetime_Protocol_Icon (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"Phenophase_ID INTEGER, " +
				"Phenophase_Icon INTEGER, " +
				"Protocol_ID INTEGER, " +
				"Chrono_Order INTEGER, " +
				"type TEXT, " +
				"Description TEXT, " +
				"Phenophase_Name " +
				");");
		
		db.execSQL("INSERT INTO species VALUES(" +
				"null, " +
				"'Ambrosia psilostachya', " +
				"'Western Ragweed', " +
				2 + 
				0 +
				
				")");
		
		
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
}
*/

public class StaticDBHelper extends SQLiteOpenHelper{
 
	final private String TAG = "StaticDBHelper";
	
	// The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/cens.ucla.edu.budburst/databases/";
    private static String DB_NAME = "staticBudburst.db";
    private static String DB_NAME_TEMP = "staticBudburst2.db";
    private SQLiteDatabase myDataBase; 
    private final Context myContext;
 
    public StaticDBHelper(Context context) {
    	super(context, DB_NAME, null, 1);
        this.myContext = context;
    }	
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		Log.i("K", "inside onCreate in StaticDBHelper");
		//db.beginTransaction();
		//db.execSQL("DROP TABLE IF EXISTS android_metadata;");
		//db.execSQL("CREATE TABLE android_metadata (locale TEXT DEFAULT en_US);");
		//db.endTransaction();
		
		boolean dbExist = checkDataBase();
		
		if(dbExist) {
			Log.i("K", "already exists");
		}
		else {
			
			//this.getReadableDatabase();
			
			try {
	 			copyDataBase();
	 		} catch (IOException e) {
	 			Log.e(TAG, e.toString());
	     		throw new Error("Error copying database");
	     	}
		}
	}
	
	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		 
    	try{
    		String myPath = DB_PATH + DB_NAME;
    		Log.i("K", "myPath : " + myPath);
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    	}catch(SQLiteException e){
 
    		//database does't exist yet.
 
    	}
 
    	if(checkDB != null){
 
    		checkDB.close();
 
    	}
 
    	return checkDB != null ? true : false;
	}
	
	private void copyDataBase() throws IOException{

		//Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		//Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		//transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer))>0){
			myOutput.write(buffer, 0, length);
		}

		//Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
		
		openDataBase();
	}
	
	 public void openDataBase() throws SQLException{
    	 
	    //Open the database
	    String myPath = DB_PATH + DB_NAME;
	    myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE 
	    		| SQLiteDatabase.CREATE_IF_NECESSARY);
	 
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//Toast.makeText(myContext, "Upgrade Complete - Database", Toast.LENGTH_SHORT).show();
		
		db.beginTransaction();
		db.execSQL("DROP TABLE IF EXISTS android_metadata;");
		db.execSQL("DROP TABLE IF EXISTS species;");
		db.execSQL("DROP TABLE IF EXISTS Onetime_Observation;");
		db.execSQL("DROP TABLE IF EXISTS Phenophase_Protocol_Icon;");
		
		createDatabase(db ,DB_NAME_TEMP);
		
		/*
		SharedPreferences pref = myContext.getSharedPreferences("userinfo",0);
		SharedPreferences.Editor edit = pref.edit();				
		edit.putBoolean("db_upgraded", false);
		edit.commit();
		
		new AlertDialog.Builder(myContext)
    	.setTitle("Upgrade Database")
    	.setMessage("Need to upgrade Database. Please uninstall and reinstall the application")
    	.setPositiveButton("Uninstall Page", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent goToMarket = null;
				//goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market.android.com/details?id=cens.ucla.edu.budburst&feature=search_result"));
				goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/details?id=cens.ucla.edu.budburst&feature=search_result"));
				goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				myContext.startActivity(goToMarket);
				
				SharedPreferences pref = myContext.getSharedPreferences("userinfo",0);
				SharedPreferences.Editor edit = pref.edit();				
				edit.putBoolean("db_upgraded", true);
				edit.commit();
			}
		})
		.show();
		*/
	}
	
	public void createDatabase(SQLiteDatabase db, String databaseName) {
		
		db.execSQL("CREATE TABLE species (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"species_name TEXT, " +
				"common_name TEXT, " +
				"protocol_id INTEGER, " +
				"category INTEGER, " +
				"description TEXT" +
				");");
		
		db.execSQL("CREATE TABLE Onetime_Observation (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"Phenophase_Icon INTEGER, " +
				"Type TEXT, " +
				"Description TEXT, " +
				"Detail_Description TEXT, " +
				"Protocol_ID INTEGER" +
				");");
		
		db.execSQL("CREATE TABLE Phenophase_Protocol_Icon (Detail_Description TEXT, " +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"Phenophase_ID INTEGER, " +
				"Phenophase_Icon INTETER, " +
				"Protocol_ID INTEGER, " +
				"Chrono_Order INTEGER, " +
				"type TEXT, " +
				"description TEXT, " +
				"Phenophase_Name TEXT " +
				");");
		
		db.execSQL("CREATE TABLE android_metadata (locale TEXT);");
		
		Log.i("K", "CREATE TABLE android_metadata (locale TEXT);");
		
		Log.i("K", "CREATE New tables");
		
		try {
    		//Open your local db as the input stream
    		InputStream myInput = myContext.getAssets().open(DB_NAME);
    	
    		// Path to the just created empty db
    		String outFileName = DB_PATH + databaseName;
    	
    		//Open the empty db as the output stream
    		OutputStream myOutput = new FileOutputStream(outFileName);
    	
    		//transfer bytes from the inputfile to the outputfile
    		byte[] buffer = new byte[1024];
    		int length;
    		while ((length = myInput.read(buffer))>0){
    			myOutput.write(buffer, 0, length);
    		}
    		//Close the streams
    		myOutput.flush();
    		myOutput.close();
    		myInput.close();

 		} catch (IOException e) {
 			Log.e(TAG, e.toString());
     		throw new Error("Error copying database");
     	}
		
 		String []description_species = {
 				"Also Known As: Cuman ragweed, Perennial ragweed\n\nPlant Family: Sunflower (Asteraceae)\n\nDid you Know? The pollen from western ragweed is the bane of many who suffer from allergies. It was used medicinally, in teas for various purposes by several Native American tribes. The Kiowa used this plant rolled with different sages in sweathouses. Rodents eat the seeds and some herbivores eat the bitter foliage to a limited extent. Because the caterpillars of various moths feed on its foliage, and many songbirds and upland gamebirds eat its seeds throughout the winter, this plant has high ecological value. Western ragweed generally increases with grazing and disturbance, since it does not tolerate shade.\n\nIdentification Hints: Western ragweed has distinctive disclike heads with pinnately lobed leaves, with separate male and female flowering heads. Western ragweed can be confused with common ragweed (Ambrosia artemisiifolia) which is an annual and has creeping roots rather than a taproot and is more common in disturbed and waste areas.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Turkey foot\n\nPlant Family: Grasses (Poaceae)\n\nDid you Know? Big bluestem is sometimes referred to as an ice-cream grass because livestock and wildlife will often eat this species before eating other kinds of grasses. It is valued by homeowners and landscapers who are seeking a drought tolerant grass to provide a native look in their backyards. Big bluestem is the official prairie grass of Illinois. Big bluestem can tolerate a wide range of moisture conditions, which allows it to extend into drier areas like the Great Plains. Its leaves roll up during periods of drought to conserve water.\n\nIdentification Hints: This is one of the largest grasses in the tall grass prairie. Stems can be solid or pithy with a bluish color at the base. It forms a clump with upright stems. The flowers are distinctive in having groups of flowers that branch out from the stem in 3 to 7 digitate and usually have short fine hairs. Spikelets attached on short stems only have male flowers. In the Great Plains and on dry sites in the northern Midwest, you can also find sand bluestem (Andropogon halllii) which has long straight hairs on the flowering stems and grayish foliage.\n\nPhenological Observations of Interest: First Flower Stalk, First Pollen, End of Pollen, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Mosquito grass, Navajita azul, White grama, Red grama, Purple grama \n\nPlant Family: Grasses (Poaceae)\n\nDid you Know? Blue grama is valued as forage, and for landscaping and erosion control. It is tolerant of grazing. Blue Grama flowers are also used in dried flower arrangements. Blue grama is readily established from seed, but depends more on vegetative reproduction via tillers. Seed production is slow, and depends on soil moisture and temperature. Seeds dispersed by wind only reach a few meters; farther distances are reached with insects, birds, and mammals as dispersal agents. Blue grama is the state grass of Colorado and New Mexico, and is listed as an endangered species in Illinois.\n\nIdentification Hints: Blue grama has distinctive flowers clustered along branches of the flower stalks (spikes) which dangle from the stem, often described as resembling a human eyebrow. You can distinguish blue grama from similar species by its hairless stems and spikelets that extend to the tips of each flower stalk branch.\n\nPhenological Observations of Interest: First Flower Stalk, First Pollen, End of Pollen, First Ripe Fruit, All Leaves Withered",
 				"Plant Family: Sunflower (Asteraceae)\n\nDid you Know? It is difficult to find much written in a positive light about this aggressive, invasive alien weed species. It is thought that spotted knapwood was introduced to the US in the early 1900 in shipments of alfalfa seeds from Eastern Europe. Most animals do not like to eat knapweed because of its bitter taste. If eaten, it is harmful to their digestive tract. Many people are allergic to the chemicals in knapweed leaves, so always make sure you wear gloves when you hand pull plants. Knapweed is very drought resistant, does well after fire and other disturbances, and has chemicals that actually deter the root growth of native grasses, and can be used as a natural herbicide. Ironically, knapweed has been introduced in some places for bee keepers, since it can flower throughout the hot dry parts of the summer, when most native plants are dormant.\n\nIdentification Hints: There are many species of knapweed that have been introduced to North America, four of which are among the worst noxious weeds in the western US. Spotted knapweed is the only one with black tipped involucral bracts (the leaf-like structures that surround the cup upon which the flowers are attached), creating a spotted appearance. The garden plant Batchelors buttons (Centaurea cyanea) also can occur in disturbed habitats. It has smooth involucral bracts and entire leaves, but is not a major weed\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Creeping thistle, California thistle\n\nPlant Family: Sunflower (Asteraceae)\n\nDid you Know? Although a native species in most of the temperate regions in Europe, Canada thistle threatens many natural plant communities in the US. This highly invasive thistle prevents the coexistence of other plant species through shading, competition for soil resources, and possibly through the release of chemical toxins poisonous to other plants. Like all other thistles it is a biennial, so it puts out a basal rosette of leaves the first year, flowers the second year, then dies. So if you can pull the basal rosettes in the first year you can control its spread. Note that there many native thistle species, so make sure you do not accidentally pull native species! Natives generally have a large stem and often have whitish hairs on bottom sides of leaves or on the stem.\n\nIdentification Hints: Canada thistle is distinguished from other thistles by its deep running perennial rootstocks, dense clonal growth, more slender stems, spiny lobed leaves, and small compact flower heads. Other weedy thistles have winged stems and large flower clusters.",
 				"Also Known As: Cedar tree, Virginia redcedar, Southern redcedar\n\nPlant Family: Cypress (Cupressaceae)\n\nDid you Know? Many birds and mammals eat the berry-like cones of redcedar, especially in winter, including solitaires, grouse, waxwings, quail, rabbits, foxes, and raccoons. The aromatic oils in redcedar are effective in repelling clothing moths, and are used in perfumes. Redcedar mulch can be used to repel ants. Heartwood of redcedar is quite resistant to decay and is used in fenceposts, poles, closets, chests and pencils. There are several unrelated species that are also called cedar including western redcedar (Thuja), and members of the pine family (Cedrus).\n\nIdentification Hints: There are several cedars in the east which have scale-like leaves that could be confused with eastern redcedar. Northern white cedar and Atlantic white cedar have branchlets that are conspicuously flattened, and leaves are in 4 rows along the twigs. Common juniper, which is more shrub-like, also has needles in threes.\n\nPhenological Observations of Interest: First Needles, First Pollen, Full Pollen, First Ripe Fruit",
 				"Also Known As: Paradise apple, Common apple\n\nPlant Family: Rose (Rosaceae)\n\nDid you Know? Apple trees are planted in orchards for agricultural purposes and also as ornamentals. The common apple tree, now wide spread in the United States, was introduced from Europe. In fact, it is widely thought that the Romans were the first to cultivate apples into the tasty and juicy fruits they are today. They often naturalize in moist sites with good soils, or can be indicators of old homesteads or settlements.\n\nIdentification Hints: There are many sub-species and varieties of apple trees found across the United States. The common apple is distinguished by its tough rounded leaves which are hairy below. The leaves appear rolled as they emerge from buds. Young twigs often are hairy. Native apple species are generally more shrub-like and have more conspicuous lobes on leaves.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: Lucerne, Purple medick, Purple medick \n\nPlant Family: Pea or Legume (Fabaceae)\n\nDid you Know? Alfalfa is the most important forage crop in the world and was first grown to feed livestock in Iran where it is native. Alfalfa sprouts, the sprouted seeds, are a popular salad and sandwich ingredient. Alfalfa is also used in re-vegetation and restoration projects since it provides food for wildlife as well as helping to improve soils. As with other members of the pea family, it is a nitrogen fixing plant which means that bacteria in its root nodules can take nitrogen from the air.\n\nIdentification Hints: There are many clover-like plants that have leaves in threes, similar to alfalfa. Alfalfa is distinct in having teeth just on the upper half of the leaflets and a large dense head of purple flowers. Siberian alfalfa (Medicago falcata) has yellow flowers and fruits with straight pods. Sweetclover leaves are very similar but have teeth scattered all along leaf margins.",
 				"Also Known As: Tall panic grass, Blackbent, Tall prairiegrass, Wild redtop, Thatchgrass \n\nPlant Family: Grasses (Poaceae)\n\nDid you Know? Switchgrass is a key species of the tall grass prairie. It is used for forage and ornamental use. Switchgrass has several cultivars. As a crop, switchgrass is self seeding, which means that farmers do not have to plant and re-seed after annual harvesting. Once established, switichgrass can survive for ten years or longer. Switchgrass made news headlines recently because some researchers believe it has great potential for producing biofuel (substitute for diesel oil). It is a good candidate for use as a biofuel because of its prodigious growth and tolerance of a wide variety of conditions.\n\nIdentification Hints: Switchgrass is a large grass growing with bluestem and indian grass species in tall grass prairies. It is distinctive in having flowers in large open panicles. Indiangrass (sorgastrum) also has big panicles but its panicles are silky hairy and its spikelets are golden brown.\n\nPhenological Observations of Interest: First Flower Stalk, First Pollen, End of Pollen, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Bluestem, Bluejoint, Western couchgrass, Colorado bluestem, Smith bluestem\n\nPlant Family: Grasses (Poaceae)\n\nDid you Know? Western wheatgrass is used as forage for all classes of livestock and wildlife. It is a preferred feed in spring for cattle, horses, deer, and elk. This grass can be used in urban areas where irrigation water is limited to provide ground cover and to stabilize ditch banks, dikes, and roadsides. Western wheatgrass is the official state grass of both North and South Dakota.\n\nIdentification Hints: Western wheatgrass is similar to other wheatgrasses, however, it is coarser, its rhizomatous trait is more aggressive, and its coloration is blue-green or gray rather than green. The technical distinction of this species is the shape of the glume (the first leaf-like structure at the base of the spikelets), which is not widest in the middle but tapers from the base.\n\nPhenological Observations of Interest: First Flower Stalk, First Pollen, End of Pollen, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Colorado pinyon, Two-leaf pinyon \n\nPlant Family: Pine (Pinaceae)\n\nDid you Know? Pinyon pine only needs about 12 to 18 inches of rainfall per year making it one of the most drought resistant species of pine. The edible seeds, known as pinyon nuts, are highly prized for their delicate flavor. The seeds are harvested in the wild and sold commercially. Eaten raw, roasted, and in candies, they were once a staple food of southwestern Native people. Every autumn, local residents, especially Navajo Indians and Spanish-Americans, harvest quantities for the local and gourmet markets. Small pinyons are popular as Christmas trees.\n\nIdentification Hints: A distinct feature of Pinyon pine is that it has two needles per bundle. It is usually a short branched tree and can be a shrub. Its cones do not have spines. Other pinions, which are found in the southwestern US, usually have different numbers of needles in each bundle (fascicle).\n\nPhenological Observations of Interest: First Needles, First Pollen, Full Pollen, First Ripe Fruit",
 				"Also Known As: Prairie crocus, Windflower \n\nPlant Family: Buttercup (Ranunculaceae)\n\nDid you Know? The genus name, Lupinus, comes from the Latin word for wolf, since it was believed that lupines wolfed nutrients from the soil, preventing other plants from growing near it. In fact, lupines are able to fix nitrogen from the atmosphere, so that they are able to thrive in nutrient-poor soils where few other plants survive.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Wyeth lupine \n\nPlant Family: Pea or Legume (Fabaceae)\n\nDid you Know? The genus name, Lupinus, comes from the Latin word for wolf, since it was believed that lupines‚nutrients from the soil, preventing other plants from growing near it. In fact, lupines are able to fix nitrogen from the atmosphere, so that they are able to thrive in nutrient-poor soils where few other plants survive.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Plant Family: Purslane (Portulacaceae)\n\nDid you Know? The roots were harvested with a digging stick and eaten traditionally many Native American groups. Families in some cultural groups still gather the roots today. The roots are prepared for eating by removing the bark and boiling, steaming, or pit-roasting them and they are eaten fresh or dried. Also, the gray-crowned rosy finch feeds on the seeds. It was first collected by Meriwether Lewis of the Lewis and Clark expedition, who is honored by the genus name.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Plant Family: Poppy (Papaveraceae)\n\nDid you Know? An entire song has been written about California poppies. Based on the words to this song, can you imagine what the flowers do at night? Pretty poppies golden, In thy yellow cup Sunbeams bright, lend their light, Honey bees doth sup In thy bed so dainty, Soothe to slumbers deep, Poppies, golden poppies, Flowers fair and sweet, Pretty poppies golden bright, good night, good night, Nod your little golden heads, good night, good night. (Words by Mary A. Lombard, music by Leo Bruck wrote the music; Re-printed by E. E. Smith in The Golden Poppy)\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Rocky Mountain columbine \n\nPlant Family:  Buttercup (Ranunculaceae)\n\nDid you Know? Aquilegia is the Latin term for eagle; the five flower spurs of this plant resemble eagle talons. While most authors have spelled the epithet caerulea, the original spelling is coerulea. It has been noted that an infusion made from the roots of Aquilegia caerulea was used by the Gosivte tribe to treat abdominal pains or as a panacea. Colorado blue columbine is the state flower of Colorado, whose state song also happens to be ‚Where the Columbines Grow(A.J. Fynn, 1915).\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: European yarrow, Milfoil \n\nPlant Family: Sunflower (Asteraceae)\n\nDid you Know? The genus Achillea comes from the Greek god, Achilles. Achilles is told in stories to have saved the lives of many of his soldiers by applying yarrow to their wounds to stop bleeding during combat. The species name, millefolium, comes from the French terms mille, which means 1,000, and feuille, leaf, a reference to the plant's numerous leaf segments.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Western shooting star, Southern shooting star, dark-throat shooting star, prairie shooting star \n\nPlant Family: Primrose (Primulaceae)\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Eastern Columbine\n\nPlant Family:  Buttercup (Ranunculaceae)\n\nDid you Know? Eastern red columbine is pollinated by hummingbirds (notice the red, tubular flowers). In northern latitudes, bees are also important pollinators of this species. Seeds of the red columbine have been used for a variety of medicinal purposes, from treating kidney ailments to relieving rashes caused by poison ivy.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Woodland pinkroot \n\nPlant Family: Loganiaceae\n\nDid you Know? The bright red trumpet-shaped flowers attract hummingbirds. They are commonly planted in ornamental perennial gardens, bog or pond areas, and water gardens.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Plant Family: Arum (Araceae)\n\nDid you Know? Jack-in-the-pulpit produces crystals of calcium oxalate, which is toxic to herbivores. Researchers in Wisconsin have found that this plant has increased in abundance in forest understories over the past several decades, possibly because these crystals help it to deter white-tailed deer.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Western spring beauty \n\nPlant Family: Purslane (Portulacaceae)\n\nDid you Know? The roots of spring beauty are a favorite food of marmots and bears.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: White trillium, Eastern trillium \n\nPlant Family: Lily (Liliaceae)\n\nDid you Know? Trilliums do best on steep slopes, where they are less likely to be eaten by white-tailed deer.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Plant Family: Barberry (Berberidaceae)\n\nDid you Know? The fruits of the mayapple are edible, but beware of the highly poisonous roots and leaves!\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Western trillium \n\nPlant Family: Lily (Liliaceae)\n\nDid you Know? It is said that Paiute native Americans of central Oregon, as well as other tribes in the western U.S., have used root poultices of Trillium ovatum as a wash for sore eyes.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Pink evening primrose, Pink petticoats, Showy primrose \n\nPlant Family: Evening primrose (Onagraceae)\n\nDid you Know? Some evening primrose flowers open so quickly you can actually see the petals move!\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Wild passionflower, Maypop, Apricot vine, Old field apricot, Holy-Trinity flower, Molly-pop, Passion vine, Pop-apple, Granadilla, Maycock \n\nPlant Family: Passion-flower (Passifloraceae)\n\nDid you Know? ‚ÄúThe Houma, Cherokee and other Native American tribes used purple passionflower for food, drink, and medicinal purposes. The plant was also used as a sedative to treat nervous conditions and hysteria.‚Äù Contributed by: USDA NRCS National Plant Data Center\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Foxfire \n\nPlant Family: Phlox (Polemoniaceae)\n\nDid you Know? In many populations of scarlet gilia in Colorado, bumble bees rob flowers of their nectar without effectively pollinating the flowers or transferring pollen to other plants. Nectar-robbing decreases seed production in these populations, in which more 80% of the flowers may have their nectar stolen!\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Plant Family: Hydrophyllaceae\n\nDid you Know? Phacelia species are grown for honey-production in some countries. They are also used as ornamentals.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Plant Family: Sunflower (Asteraceae)\n\nDid you Know? Phacelia species are grown for honey-production in some countries. They are also used as ornamentals.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Bluejacket, Smooth spiderwort \n\nPlant Family: Spiderwort family (Commelinaceae)\n\nDid you Know? The blue hairs on the stamens (which can be seen on the close up photo) have been used to detect radiation. They turn pink when exposed to even low levels of radiation.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Trout lily\n\nPlant Family: Lily (Liliaceae)\n\nDid you Know? Traditionally the bulbs and leaves of this species were eaten, either raw or cooked. The plant was also used medicinally to heal ulcers and as a contraceptive. The plant is believed to be mildly emetic and antibiotic.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Virginia cowslip, Lungwort, Oysterleaf \n\nPlant Family: Borage (Boraginaceae)\n\nDid you Know? Virginia bluebells are pollinated by all kinds of long-tongued bees.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Woods strawberry, Common strawberry, Virginia strawberry \n\nPlant Family: Rose (Rosaceae)\n\nDid you Know? Indigenous peoples throughout parts of Canada picked and ate the savory fruit of this plant. Midwestern prairie and Great Lakes tribes, such as the Omaha, Hidatsa, Mandan, Dakota, Pawnee, Blackfoot, Cheyenne, and the Winnebago ate these strawberries raw, cooked or dried. The Winnebago and Blackfoot made a tea with an infusion of the young leaves of this plant. Also, plant attracts wildlife and butterflies.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Prairie crocus, Windflower \n\nPlant Family: Buttercup (Ranunculaceae)\n\nDid you Know? Native Americans and settlers used the fibrous inner bark as a source of fiber for rope, mats, fish nets, and baskets. Before the widespread availability of synthetics, American Linden was once the material choice for prosthetic limbs. It is still valued for its soft, light, easily worked wood, especially for turned items and hand carving.\n\nIdentification Hints: Lindens are highly prized ornamental trees with soft heart-shaped leaves with fine teeth on the leaf margin, uneven bases and fragrant yellow flowers. The American linden has large hairless leaves and hairless flower stalks as contrasted with the southern basswoods (Carolina basswood (T. caroliniana), white basswood (T. heterophylla)). The Florida basswood (T. floridana) has silvery undersides to the leaves. Three European species are common ornamentals: small leaved linden (T. cordata) which has small round leaves 2.5 to 6 cm (1 to 2.5 in) and whitish undersides (but small tufts of brown hairs), and the large-leaved lime (T. platyphyllos) with leaves 7.5 to 10 cm (3 to 4 in) long (but still smaller and more rounded than American linden) with fine short hairs on the under surface of the leaves.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Plant Family: Rose (Rosaceae)\n\nDid you Know? Antelope bitterbrush is long lived: it has been reported that a 115-year-old plant existed that was only 25 cm (10 in) high and spread over 1.8 m2 (7 square ft), while at a lower elevation the same botanist found a 128-year-old plant that was 3.6 m (12 ft) high and 6 m (20 ft) across. Antelope bitterbrush is also important browse for wildlife and livestock, and it supports several species of insects.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: Common aspen \n\nPlant Family: Willows (Salicaceae)\n\nDid you Know? Quaking aspen is the most widely distributed tree in North America. In Minnesota, Wisconsin, and Utah quaking aspen occupies more land than any other forest type. Stands of quaking aspen are good firebreaks, often dropping crown fires in conifer stands to the ground when they reach aspens and even sometimes extinguishing the fire because of the small amount of flammable accumulation. One male clone in the Wasatch Mountains of Utah occupies 17.2 acres (43 ha) and has more than 47,000 stem! Although individual ramets/trees of a clone may be short-lived, the clone may be long-lived.\n\nIdentification Hints: Quaking aspen is unique in its smooth rounded leaves which flitter in the slightest breeze, due to the thin flattened stems (petioles) and its bright white or cream colored bark. In the Midwest and northeastern US you can also see bigtooth aspen (Populus granditentata) which has coarse rounded teeth on the leaf margin, and fine hairs on stout twigs and dusty gray buds and brown or green bark.The European aspen (Populus tremula) is also similar but has rounded irregular teeth on its leaves, and grayish bark. Poplars and cottonwoods generally have triangular-shaped leaves.\n\nPhenological Observations of Interest: First Pollen, Full Pollen, End of Pollen, First Leaf, All Leaves Unfolded, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: Filbert \n\nPlant Family: Birch (Betulaceae)\n\nDid you Know? As the common name suggests, the husk (involucral tube) surrounding the nut extends beyond the nut by at least one inch to form a beak. Nuts ripen in late August and September and are edible, though most commercially-available hazelnuts come from hybrid plants.\n\nPhenological Observations of Interest: First Pollen, Full Pollen, End of Pollen, First Leaf, All Leaves Unfolded, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Plant Family: Maple (Aceraceae)\n\nDid you Know? The inner bark of Big Leaf maple used to be dried and ground up into a powder that was used to thicken soups, just as we sometimes use cornstarch or flour to thicken soups and stews today.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Plant Family: Sunflower (Asteraceae)\n\nDid you Know? Sage grouse depend on big sagebrush for food more than any other species. Up to 70 ‚Äì 75% of their diet is composed of the leaves and flower heads of big sagebrush. Antelope and mule deer also eat big sagebrush leaves and stems during the fall, winter, and spring. A variety of birds can also be found hiding in the brush provided by sagebrush, including sage grouse, sharp tailed grouse, prairie sparrows, chukar, quail, and gray partridge.\n\nPhenological Observations of Interest: First Leaf, First Pollen, Full Pollen, End of Pollen, First Ripe Fruit",
 				"Plant Family: Pea or Legume (Fabaceae)\n\nDid you Know? The wood of black locust is strong, hard, and extremely durable. It is very useful for fencing, mine timbers, and landscaping ties. The tree also serves as a good erosion control plant on critical and highly disturbed areas, due to its ease of establishment, rapid early growth and spread, and soil building abilities. In some regions it is considered invasive.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Plant Family: Honeysuckle (Caprifoliaceae)\n\nDid you Know? The flowers develop into blue berries which are an important source of food for birds and mammals. The blue berries were also dried and preserved for eating by Native Americans, and used by the early California immigrants to make jam and wine.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: Ash-leaf maple, California boxelder, Western boxelder, Manitoba maple \n\nPlant Family: Maple (Aceraceae)\n\nDid you Know? Boxelder has decreased in its native range because of clearing of bottomland forest for agriculture, but they have greatly increased in urban areas. The success of the species on disturbed urban sites is due to its prolific seed production and wide dispersal, ease of germination, tolerance of low oxygen conditions, and fast growth on clay or heavy fill. Boxelder also is found as a pioneer species on disturbed upland sites where a seed source is nearby. Boxelder trees are either male or female, although occasionally you may find a bisexual flower that has both anthers (male) and a stigma (female). No individual tree produces both pollen and seeds.\n\nIdentification Hints: Boxelder is unusual among maples in that it has compound leaves. Seedling and young saplings of Boxelder have a remarkable resemblance to poison ivy ‚so be careful!\n\nPhenological Observations of Interest: First Pollen, Full Pollen, End of Pollen, First Leaf, All Leaves Unfolded, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: Black chokecherry, Red chokecherry, California chokecherry, Virginia chokecherry \n\nPlant Family: Rose (Rosaceae)\n\nDid you Know? Chokecherry was first cultivated in North America as an orchard crop in the early 1700‚Äôs. It provided a staple for Native American tribes. Prussic (hydrocyanic) acid is found in the bark, leaves, and pits of chokecherry and there are numerous reports of cattle dying after eating these parts of the plant. The acid in chokecherry pits is neutralized by boiling or drying. The bark is used as a tea. The fruit is used to make jellies and jams. It has been used for a variety of medicinal purposes, including relief of diarrhea and sore throat.\n\nIdentification Hints: Chokecherry is distinctive in having flowers attached on stems forming long racemes. Most other cherries have flowers in short rounded clusters. In the Great Plains to the east black cherry (Prunus serotina) is similar in appearance. It can be a much larger tree up to 25 meter (80 feet) tall, and has sepals which persist with the fruits, has narrow leaves, and usually has brown to whitish hairs along the midrib on the lower side of the leaf.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: White coralberry, Snowberry \n\nPlant Family: Honeysuckle (Caprifoliaceae)\n\nDid you Know? Some groups in the south used the branches of common snowberry to make brooms and hollowed out the twigs to make pipe stems. The hollow stems can also be used as whistles. The berries have been used to settle upset stomachs after too much fatty food. The berries were also used as treatment for burns, rashes, and sores. Common snowberry is very important as a browse for many types of wildlife and livestock. It provides food and shelter for various birds and small mammals. The berries and stems can be mildly toxic to children and even fatal to some animals.\n\nIdentification Hints: An unusual feature of common snowberry is its hollow stems. The flowers of common snowberry are pinkish-white and bell shaped. Creeping snowberry (Symphoricarpos molis) is very similar, however, as its name implies, it is a lower growing shrub with a spreading habit.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: Creping barberry \n\nPlant Family: Barberry (Berberidaceae)\n\nDid you Know? Creeping Oregon grape is adapted to habitats that have regular wildfires. It regenerates after a fire when dormant buds sprout from the surface of the underground rhizomes.\n\nPhenological Observations of Interest: First Leaf, First Flower, Full Flower, End of Flowering, First Ripe Fruit",
 				"Also Known As: Mountain mahogany \n\nPlant Family: Rose (Rosaceae)\n\nDid you Know? The wood of curl-leaf mountain mahogany is extremely hard and so dense that it won‚ float in water. It makes an excellent fuel, giving off an intense heat while burning for a long time. It is occasionally used in the manufacture of small articles for domestic and industrial use.\n\nPhenological Observations of Interest: First Leaf, First Flower, Full Flower, End of Flowering, First Ripe Fruit",
 				"Also Known As: Interior douglas-fir, Coastal douglas-fir \n\nPlant Family: Pine (Pinaceae)\n\nDid you Know? Douglas-fir is the one of the most valuable lumber trees in the world. The wood is used as poles, beams, in bridges, as rail road ties, structural timber, in plywood, and to make furniture. It is found in many homes every December as a popular Christmas tree. Native Americans used the resin as an antiseptic in the treatment of burns, scrapes, and rashes. European explorers often placed young shoots in their boots to prevent athlete‚foot and nail fungus. The tallest Douglas-fir on recored is 100 meters (330 feet) high! This long lived species can exceed 1,000 years of age.\n\nIdentification Hints: Douglas-firs are called firs because like firs they have a series of needles that are separately attached to the twigs as contrasted with pines or larches which have needles in bundles (fascicles) or spurs. Pointed red buds distinguish Douglas-firs from true firs, such as grand-fir (Abies grandis) and subalpine fir (Abies lasiocarpa). True firs have sticky rounded green to whitish buds. True firs also have more stiff rounded needles (with a tiny notch at the tip), and cones which are erect and born on the upper sides of branches. The most distinctive feature of Douglas-firs are the ‚exerted bracts on the cone scales, which extend outside of scales and have long narrow point (the tail).\n\nPhenological Observations of Interest: First Needles, First Pollen, Full Pollen, First Ripe Fruit",
 				"Also Known As: Eastern juneberry, Shadbush, Shadow serviceberry, Canadian serviceberry \n\nPlant Family: Rose (Rosaceae)\n\nDid you Know? Serviceberries are subject to many disease and insect problems. Damage from these problems is usually cosmetic rather than life threatening. Also, it is an important browse and food plant for birds and other wildlife.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: White pine, Northern white pine, Soft pine \n\nPlant Family: Pine (Pinacae)\n\nDid you Know? During the age of sail, eastern white pine was valued for masts. In colonial times, many trees were marked with a broad arrow reserving them for use by the British Royal Navy. Today, wood from the eastern white pine is used for cabinets, toys, boxes, and similar items. It is frequently used for windbreaks.\n\nIdentification Hints: White pines are distinct in having needles in clusters of 5. Pinyon pines can also have 5 needles but have shorter stiffer needles and rigid cone scales. Eastern white pine is distinct in having slender needles, and long cones with thin flexible scales. It is very similar to western white pine, but has smaller cones averaging 12 cm (5 in) as contrasted with western white pine which has cones almost twice as large 24 cm (10 in). Both are common ornamentals.\n\nPhenological Observations of Interest: First Needles, First Pollen, Full Pollen, First Ripe Fruit",
 				"Also Known As: Eastern dogwood \n\nPlant Family: Dogwood (Cornaceae)\n\nDid you Know? Flowering dogwood is a very popular tree often used as an ornamental. You might find non native cultivars with different colored flowers (red and pink). Many species of birds and mammals browse the fruits, leaves, and twigs. The white wood is hard, tough, close-grained, and good for making tool handles.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall", 				
 				"Also Known As: Bearberry \n\nPlant Family: Heath (Ericaceae)\n\nDid you Know? It serves a dual role on sandy soils, as both a beautification plant as well as a critical area stabilizer. The thick, prostrate, vegetative mat and evergreen character are what make bearberry a very popular ground cover. It is often planted around home sites, sand dunes, sandy banks, and commercial sites.‚Äù Contributed by USDA NRCS Northeast Plant Materials Program\n\nPhenological Observations of Interest: First Leaf, First Flower, Full Flower, End of Flowering, First Ripe Fruit", 				
 				"Also Known As: Western mock orange, Wild mock orange \n\nPlant Family: Hydrangea (Hydrangeaceae)\n\nDid you Know? The genus name comes from the Egyptian king Ptolemy Philadelphus (309-247 BC), and the species name honors Meriwether Lewis, who first described the shrub along the Bitterroot River. It is a very popular ornamental shrub in temperate climates due to its intense sweet fragrance that is similar to orange trees. Poulticed or powdered leaves have been used for medicinal or culinary purposes, while the wood has been used for snowshoes and tools/weapons.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall", 				
 				"Also Known As: Longstraw pine, Southern yellow pine, Georgia pine \n\nPlant Family: Pine (Pinacae)\n\nDid you Know? Birds and small mammals eat the large seeds, ants feed on germinating seeds, and razorback hogs eat the roots of seedlings. Longleaf pine needles are used extensively for mulch. In pre-settlement times it was a major source of timber and naval stores (for rosins). It covered over 60 million acres, or most of the southern coastal plain. Now fewer than 4 million acres have these valuable pines.\n\nIdentification Hints: Longleaf pine can be a beautiful large ‚ pine in the South. There are three closely related 3-needle pines in the south: longleaf, slash, and loblolly. Longleaf has needles up to 46 cm (18 in) long, with stout twigs and silvery buds and cones 15 to 25 cm (6 to 10 in) long with small incurved prickles on the scales. The other pines have shorter cones 5 to 15 cm (2 to 6 in) with a sharp prickle on the scales, and red-brown buds.\n\nPhenological Observations of Interest: First Needles, First Pollen, Full Pollen, First Ripe Fruit", 				
 				"Also Known As: Western dogwood \n\nPlant Family: Dogwood (Cornaceae)\n\nDid you Know? Pacific dogwood bark was used by Nlaka `pamux, indigenous people of the Pacific Northwest Coast, to make brown dye. Bark has also been used as a blood purifier, lung strengthener, stomach treatment and possibly to cure malaria.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall", 				
 				"Also Known As: Paperbark birch, Silver birch, Canoe birch \n\nPlant Family: Birch (Betulaceae)\n\nDid you Know? ‚sap and inner bark is used as emergency food. White birch can be tapped in the spring to obtain sap from which beer, syrup, wine or vinegar is made. The inner bark can be dried and ground into a meal and used as a thickener in soups or added to flour used in making bread. A tea is made from the root bark and young leaves of white birch. It was also used by native Americans to make canoes, buckets, and baskets. ‚North American Indian tribes used white birch to treat skin problems of various rashes; skin sores, and burns.‚ Prepared By Lincoln M. Moore @ USDA NRCS National Plant Data Center\n\nPhenological Observations of Interest: First Pollen, Full Pollen, End of Pollen, First Leaf, All Leaves Unfolded, First Ripe Fruit, 50% Color, 50% Leaf Fall", 				
 				"Also Known As: Yellow pine, Western yellow pine, Bull pine \n\nPlant Family: Pine (Pinaceae)\n\nDid you Know? Ponderosa pine got its name because of its ponderous, or heavy, wood. It is one of the most widely distributed pines in western North America. Ponderosa pine is a major source of timber, which is especially suited for window frames and panel doors. Ponderosa pine forests are also important as wildlife habitat. Quail, nutcrackers, squirrels, and many other kinds of wildlife consume the seeds. Dispersal is aided by chipmunks that store the seeds in their caches.\n\nIdentification Hints: Ponderosa pine is the iconic pine of the interior west, with its thick colorful bark which can be bright orange or yellow in color in open sunny spots. It is the only 3-needle pine in the Rockies. In California, and near its borders in Oregon and Nevada and in the Southwest there are other 3-needle pines and ornamental pines that can be confused with ponderosa. Jeffrey pine is very similar but usually has larger cones up to 38 cm (15 in), and purplish twigs (the twigs are orange to red in ponderosa). Jeffrey pine twigs have a pineapple-like odor. In Arizona and New Mexico Apache pine is found which has longer needles up to 38 cm (15 in).\n\nPhenological Observations of Interest: First Needles, First Pollen, Full Pollen, First Ripe Fruit",
 				"Also Known As: Scarlet maple, Soft maple, Swamp maple \n\nPlant Family: Maple (Aceraceae)\n\nDid you Know? Because of its brilliant red color in the fall, Red maple is prized as an ornamental. The sap can be used for producing maple syrup, however its sap has only about half of the sugar content as sugar maple. Native Americans used rep maple bark as an analgesic, wash for inflamed eyes and cataracts, and as a remedy for hives and muscular aches.\n\nIdentification Hints: There are many native and ornamental maples. Red maples are distinctive in having the red flowers emerge a week or more before the leaves. Silver maple (A. saccharinum) has greenish-yellow flowers that emerge well before leaves and has large 5-lobed leaves with whitish undersides (only 3 main lobes in red maple). The other ornamental maples generally have green undersides of the leaves (as contrasted with white or gray in red maple) and usually do not have red flowers.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Plant Family: Dogwood (Cornaceae)\n\nDid you Know? Red-osier is a popular ornamental shrub, due to the bright red color of its twigs in winter.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: Douglas maple, Mountain maple \n\nPlant Family: Maple (Aceraceae)\n\nDid you Know? ‚Rocky Mountain maple is a highly valued big game browse species. Moose, elk, mule deer, and white-tailed deer to varying degrees throughout the year eat its leaves and twigs, but it is especially important as a winter food source.‚Contributed By: USDA NRCS National Plant Data Center & the Biota of North America Program\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: Golden-hardhack, Potentilla \n\nPlant Family: Rose (Rosaceae)\n\nDid you Know? Floribundameans freely blooming. Cheyenne Indians used the dried, powdered leaves rubbed over hands, arms and body for Contrary dance. The Blackfoot Indians used the leaves mixed with dried meat as a deodorant and spice, and to fill pillows. They used the dry, flaky bark as tinder when starting a fire with twirling sticks. The Eskimo used the dried leaves to make tea.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: Tulip tree \n\nPlant Family: Magnolia (Magnoliaceae)\n\nDid you Know? First Nations used the inner bark medicinally as worming medicine, antiarthritic, cough syrup and cholera remedy. Pioneers hollowed out the massive trunk to make a long, lightweight canoe. One of the chief commercial hardwoods, Tulip Poplar is used for furniture, as well as for crates, toys, musical instruments, and pulpwood. This is a favorite nesting tree for birds and the flowers attract hummingbirds.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: Juneberry, Saskatoon berry \n\nPlant Family: Rose (Rosaceae)\n\nDid you Know? Serviceberry is considered a valuable browse species for deer and elk in winter habitat areas. The human uses for this shrub are seemingly endless. It was essential to many Native peoples because not only are the sweet, juicy berries good fresh but they dry very well (like raisins) and save well through out the winter. They can be dried individually or in cakes.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Plant Family: Rose (Rosaceae)\n\nDid you Know? The roots, stems, leaves, flowers, and fruits are used for foods and therapeutic materials. The hips are a source of vitamin C. They are dried to flavor tea, jelly, fruitcake, and pudding. Native Americans boiled the inner bark and roots to treat diarrhea and stomach aliments. A tea made from the bark was used to treat muscle problems.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: Faceclock, Blowball \n\nPlant Family: Sunflower (Asteraceae)\n\nDid you Know? The popular name comes from dent de lion, French for ‚Äúlion‚Äôs tooth,‚Äù referring to the distinctive teeth on the leaves. The tender young leaves, rich in vitamins and minerals, make good salad or cooked greens. The mildly laxative and diuretic leaves have been used in medicinal teas, digestive aids, wine, and rustic beers. It originated in Europe and is one of the most widely distributed plants. Dandelion is particularly efficient in producing seeds because it does so without pollination (this also explains why it can make seeds so early in the season).\n\nIdentification Hints: Dandelions are one of the most common and easily identifiable weeds in the country. They have bright yellow heads that turn into round balls of silver tufted seeds, with no leaves on the flower stem. The leaves are distinctive in having a large lobe at the tip, and many sharply pointed outward-facing teeth or lobes along the sides.",
 				"Plant Family: Pea or Legume (Fabaceae)\n\nDid you Know? Trifolium repens, like other members of the pea family, fix nitrogen (a limiting factor in plant growth). This makes clover an important agricultural and rangeland plant‚Äîby planting it with grasses it is possible to increase the grass yield. Clover leaves and flowers are also good forage for wildlife, such as moose, grizzly bear, white-tailed deer, and blue grouse. Clover is used widely by bees to produce honey.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Common Mustard \n\nPlant Family: Mustard (Brassicaceae)\n\nDid you Know? This species is native to Europe, but it has become widespread in the United States.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Common forsythia, Garden forsythia \n\nPlant Family: Olive (Oleaceae)\n\nDid you Know? Forsythia was named for the British royal gardener William Forsyth (1737-1804) who brought this beautiful shrub home from a trip to China.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: Lilac \n\nPlant Family: Olive (Oleaceae)\n\nDid you Know? Homesick settlers from Europe introduced common lilac. Bushes still can be seen thriving near abandoned pioneer homesteads. Ethnobotanical uses for the plant have been fever reducer, malaria treatment, perfume, tonic, and homeopathy.\n\nIdentification Hints: Common lilac is distinctive in having smooth (hairless) dark heart-shaped leaves which are arranged in opposite pairs, and twigs with opposite (lateral) buds, but no large terminal bud at the tip (so branches do not grow straight out). There are hundreds of varieties, but only a few closely related species. One (Syringa oblata) has rounded leaves (just as wide as long), and several species including the ‚ÄúChinese‚Äù lilac (Syringa chinensis) which have leaves which taper at their base.\n\nPhenological Observations of Interest: First Leaf, All Leaves Unfolded, First Flower, Full Flower, End of Flowering, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: Common dead nettle \n\nPlant Family: Mint (Lamiaceae)\n\nDid you Know? ‚Dead nettle refers to these plants looking similar to nettles but not having any sting hairs. Deadnettles are native to Europe. Despite its weedy and invasive nature, henbit deadnettle provides valuable erosion control in many cropland fields especially in the southern U.S. Henbit deadnettle is a member of the mint family, however, it does not have a strong or distinctive odor common among most members of the mint family.\n\nIdentification Hints: The square stem (like all plants in the mint family) is an excellent way to differentiate henbit deadnettle from Persian speedwell when neither plant is in flower (speedwell also tends to be much smaller and is in moist habitats). The red dead nettle (Lamium purpureum) has smaller flowers (not extending above the leaves) and reddish upper leaves. Henbit deadnettle is sometimes confused with Creeping Charlie (Glechoma hederacea), another weedy member of the mint family that has flowers that are more blue-purple.\n\nPhenological Observations of Interest: First Flower, End of Flowering, First Ripe Fruit, All Leaves Withered",
 				"Also Known As: Eastern cottonwood, Common cottonwood, Plains poplar \n\nPlant Family: Willow (Salicaceae)\n\nDid you Know? Although Plains cottonwoods are very fast growing trees, they are not long lived trees. They are susceptible to disease, fire, and drought. The light wood was important as a construction material to Native Americans and European settlers to the Mid-west and Great Plains states. It is the state tree of Kansas, Nebraska, and Wyoming. Note that in older books plains cottonwood and eastern cottonwood were considered separate species.\n\nIdentification Hints: Large coarse triangular leaves with flattened stems and glands at the tips distinguish it from most other species of cottonwood.\n\nPhenological Observations of Interest: First Pollen, Full Pollen, End of Pollen, First Leaf, All Leaves Unfolded, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: Black cottonwood, California poplar \n\nPlant Family: Willows (Salicaceae)\n\nDid you Know? In urban areas, the aggressive root system of black cottonwood can invade and damage drainage systems. Balsam poplar is a commercially valuable tree with the primary products including particle board, plywood, veneer, and lumber. Native Americans used the resin from buds to treat sore throats, coughs, lung pain, and rheumatism. It is still used in some modern natural health ointments. These trees create some of the most biodiverse communities in the Pacific Northwest and northern Rockies providing habitat for many bird and insect species. They are declining because of dams and other alterations of river habitats.\n\nIdentification Hints: Cottonwoods are extremely variable because they often hybridize between species. Balsam poplar is unique in having rounded lance-shaped leaves with short stems. Plains cottonwood has a more triangular (deltoid) leaf shape with rounded teeth and may occur close to black cottonwoods at the eastern foot of the Rocky Mountains. Another species, narrow-leaved cottonwoods, have leaves (2.5 times longer than wide). Balsam poplar is a closely related subspecies which has capsules which split in 2 (rather than 3 parts).\n\nPhenological Observations of Interest: First Pollen, Full Pollen, End of Pollen, First Leaf, All Leaves Unfolded, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: Evergreen magnolia, Bull-bay, Laurier tulipier \n\nPlant Family: Magnolia (Magnoliaceae)\n\nDid you Know? The Southern magnolia is one of the most beautiful trees and is therefore highly valued as an ornamental. The Choctaw and Kosati tribes used the bark of this tree as a dermatological aid. The Southern magnolia is the state tree of both Louisiana and Mississippi.\n\nIdentification Hints: Magnolias have distinctive large, shiny evergreen leaves, and large fragrant flowers with many long narrow petals. Sweetbay magnolia (Magnolia virginiana) also occurs in southern coastal plains but has thinner leaves with whitish undersides.\n\nPhenological Observations of Interest: First Leaf, First Flower, Full Flower, End of Flowering, First Ripe Fruit",
 				"Also Known As: Swamp cypress, Pond cypress, Red cypress, Gulf cypress, White cypress \n\nPlant Family: Cypress (Cupressaceae)\n\nDid you Know? They are called ‚bald‚ because of their deciduous nature which is unusual in conifers. Because it is resistant to wood-rotting fungi, bald cypress is valued as softwoood lumber for shingles, trim, and especially for greenhouse benches and racks. Cypress swamps provide important habitat for endangered species like limpkins and wood storks. Cypress swamps store flood waters, serve as recharge areas for ground water, and filter pollutants from surface water. It is often planted as an ornamental tree. Trees can live over 1,200 years.\n\nIdentification Hints: The flat deciduous needles distinguish bald cypress from most other conifers. The ornamental dawn cedar (Metasequoia glyptostroboides) is similar in appearance to small bald cypresses but has branchlets and needles arranged opposite (rather than alternate as found in bald cypress).\n\nPhenological Observations of Interest: First Pollen, Full Pollen, End of Pollen, First Leaf, All Leaves Unfolded, First Ripe Fruit, 50% Color, 50% Leaf Fall",
 				"Also Known As: Virginia live oak \n\nPlant Family: Beech (Fagaceae)\n\nDid you Know? Southern live oak is considered to be one of the ‚Äònoblest trees in the world and virtually an emblem of the Old South. In many parts of the south, these trees are protected for public enjoyment. The acorns of southern live oaks are an important food source for many birds and mammals. This is a very fast growing tree and is therefore are often planted for erosion control. It also has potential for use in re-vegetating coal mine spoils. Properly cared for, southern live oaks can survive for centuries.\n\nIdentification Hints: There are several smaller evergreen oaks along the coastal plain. Southern live oak is unique in its large size, spreading branches, smooth oblong leather leaves with light colored undersides.\n\nPhenological Observations of Interest: First Leaf, First Pollen, Full Pollen, End of Pollen, First Ripe Fruit",
 				"This plant has been chosen from a list that does not contain an associated description of the plant."
 		};
 		
 		String myPath = DB_PATH + databaseName;
 		
 		SQLiteDatabase tempDB = SQLiteDatabase.openDatabase(myPath, null, (SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY));
 		Cursor cursor = tempDB.rawQuery("SELECT _id, species_name, common_name, protocol_id, category, description FROM species ORDER BY _id ASC;", null);
 		
 		int count = 0;
 		while(cursor.moveToNext()) {
 			db.execSQL("INSERT INTO species VALUES(" +
 					cursor.getInt(0) + ", \"" +
 					cursor.getString(1) + "\", \"" +
 					cursor.getString(2) + "\", " +
 					cursor.getInt(3) + ", " +
 					cursor.getInt(4) + ", \"" +
 					description_species[count++] + "\"" +
 					")");
 		}
 		cursor.close();
 		
 		cursor = tempDB.rawQuery("SELECT _id, Phenophase_Icon, Type, Description, Detail_Description, Protocol_ID FROM Onetime_Observation;", null);
 		while(cursor.moveToNext()) {
 			db.execSQL("INSERT INTO Onetime_Observation VALUES(" +
 					cursor.getInt(0) + ", " +
 					cursor.getInt(1) + ", \"" +
 					cursor.getString(2) + "\", \"" +
 					cursor.getString(3) + "\", \"" +
 					cursor.getString(4) + "\", " +
 					cursor.getInt(5) + 
 					")");
 		}
 		cursor.close();
 		
 		cursor = tempDB.rawQuery("SELECT Detail_Description, _id, Phenophase_ID, Phenophase_Icon, Protocol_ID, Chrono_Order, type, description, Phenophase_Name FROM Phenophase_Protocol_Icon;", null);
 		while(cursor.moveToNext()) {
 			db.execSQL("INSERT INTO Phenophase_Protocol_Icon VALUES(" + 
 					"\"" +
 					cursor.getString(0) + "\", " +
 					cursor.getInt(1) + ", " +
 					cursor.getInt(2) + ", " +
 					cursor.getInt(3) + ", " +
 					cursor.getInt(4) + ", " +
 					cursor.getInt(5) + ", \"" + 
 					cursor.getString(6) + "\", \"" +
 					cursor.getString(7) + "\", \"" +
 					cursor.getString(8) + "\"" +
 					")");
 		}
 		cursor.close();
 		
 		cursor = tempDB.rawQuery("SELECT locale FROM android_metadata;", null);
 		while(cursor.moveToNext()) {
 			db.execSQL("INSERT INTO android_metadata VALUES("+
 			"\"" + cursor.getString(0) + "\""+ ")");
 		}
 		
 		cursor.close();
 		tempDB.close();
 		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
 
    @Override
	public synchronized void close() {
       	super.close();
    	if(myDataBase != null)
    		myDataBase.close();
	}
    
    public int getSpeciesID(Context cont, String speciesName) {
    	StaticDBHelper staticDBHelper = new StaticDBHelper(cont);
		SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();
		
    	Cursor cursor = staticDB.rawQuery("SELECT _id FROM species WHERE common_name = \"" + speciesName + "\" COLLATE NOCASE", null);
    	int getID = 999;
    	
    	while(cursor.moveToNext()) {
    		getID = cursor.getInt(0);
    	}
    	cursor.close();
    	staticDB.close();
    	
    	return getID;
    }
    
    public String[] getSpeciesName(Context cont, int speciesID) {
    	StaticDBHelper staticDBHelper = new StaticDBHelper(cont);
		SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();
    	
    	Cursor cursor = staticDB.rawQuery("SELECT species_name, description FROM species WHERE _id = " + speciesID, null);
    	String []getName = new String[2];
    	
    	while(cursor.moveToNext()) {
    		getName[0] = cursor.getString(0); // ScienceName
    		getName[1] = cursor.getString(1); // Description
    	}
    	cursor.close();
    	staticDB.close();
    	
    	return getName;
    }
    
    public HashMap getPhenoName(Context cont, int phenoID) {
    	StaticDBHelper staticDBHelper = new StaticDBHelper(cont);
		SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();
    	
    	Cursor cursor = staticDB.rawQuery("SELECT Phenophase_Icon, Type, Description, Detail_Description FROM Onetime_Observation WHERE _id=" + phenoID, null);
    	HashMap hMap = new HashMap();
    	
    	while(cursor.moveToNext()) {
    		hMap.put("pIcon", cursor.getInt(0));
    		hMap.put("pType", cursor.getString(1));
    		hMap.put("pDesc", cursor.getString(2));
    		hMap.put("pDetail", cursor.getString(3));
    	}
    	cursor.close();
    	staticDB.close();
    	
    	return hMap;
    }
}
