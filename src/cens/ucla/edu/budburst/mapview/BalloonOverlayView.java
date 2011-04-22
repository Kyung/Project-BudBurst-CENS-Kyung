package cens.ucla.edu.budburst.mapview;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.HelperDrawableManager;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperImageLoader;
import cens.ucla.edu.budburst.helper.HelperSharedPreference;
import cens.ucla.edu.budburst.helper.HelperValues;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * A view representing a MapView marker information balloon.
 * <p>
 * This class has a number of Android resource dependencies:
 * <ul>
 * <li>drawable/balloon_overlay_bg_selector.xml</li>
 * <li>drawable/balloon_overlay_close.png</li>
 * <li>drawable/balloon_overlay_focused.9.png</li>
 * <li>drawable/balloon_overlay_unfocused.9.png</li>
 * <li>layout/balloon_map_overlay.xml</li>
 * </ul>
 * </p>
 * 
 * @author Jeff Gilfelt
 *
 */
public class BalloonOverlayView extends FrameLayout {

	private ImageView speciesImage;
	private LinearLayout layout;
	private TextView title;
	private TextView snippet;
	private HelperDrawableManager dm;
	private Context mContext;
	private ProgressBar mSpinner;

	/**
	 * Create a new BalloonOverlayView.
	 * 
	 * @param context - The activity context.
	 * @param balloonBottomOffset - The bottom padding (in pixels) to be applied
	 * when rendering this view.
	 */
	public BalloonOverlayView(Context context, int balloonBottomOffset) {

		super(context);
		mContext = context;

		setPadding(10, 0, 10, balloonBottomOffset);
		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);
		
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.balloon_overlay, layout);
		
		title = (TextView) v.findViewById(R.id.balloon_item_title);
		snippet = (TextView) v.findViewById(R.id.balloon_item_snippet);

		
		
		speciesImage = (ImageView) v.findViewById(R.id.species_img);
		mSpinner = (ProgressBar) v.findViewById(R.id.progressbar);
		
		dm = new HelperDrawableManager(mSpinner);
		
		// when click the close button
		ImageView close = (ImageView) v.findViewById(R.id.close_img_button);
		close.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				layout.setVisibility(GONE);
			}
		});

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);
	}
	/**
	 * Sets the view data from a given overlay item.
	 * 
	 * @param item - The overlay item containing the relevant view data 
	 * (title and snippet). 
	 */
	
	
	/*
	 * 
	 * HelperDrawableManager dm = new HelperDrawableManager(mSpinner);
		dm.fetchDrawableOnThread("http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/images/treelists/" + mSpeciesID + ".jpg", speciesImage);
	 */
	
	public void setData(SpeciesOverlayItem item) {

		layout.setVisibility(VISIBLE);
		if (item.getTitle() != null) {
			title.setVisibility(VISIBLE);
			title.setText(item.getTitle());
		} else {
			title.setVisibility(GONE);
		}
		
		if (item.getSnippet() != null) {
			snippet.setVisibility(VISIBLE);
			snippet.setText(item.getSnippet());
		} else {
			snippet.setVisibility(GONE);
		}
		
		if (item.getImageUrl() != null) {
			speciesImage.setVisibility(VISIBLE);
			if(item.getCategory() == HelperValues.LOCAL_FLICKR) {
				dm.fetchDrawableOnThread(item.getImageUrl(), speciesImage);
			}
			else {
				mSpinner.setVisibility(View.GONE);
				
				OneTimeDBHelper oDBH = new OneTimeDBHelper(mContext);
				String scienceName = oDBH.getScienceName(mContext, item.getTitle(), item.getCategory());
				
				HelperFunctionCalls helper = new HelperFunctionCalls();
				Log.i("K", "Category : " + item.getCategory() + " Id : " + item.getSpeciesID());
				helper.showSpeciesThumbNail(mContext, item.getCategory(), item.getSpeciesID(), scienceName, speciesImage);
			}
			
		} else {
			speciesImage.setVisibility(GONE);
		}
	}
}

