package cens.ucla.edu.budburst.mapview;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import cens.ucla.edu.budburst.R;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class SpeciesItemizedOverlay extends ItemizedOverlay {

    private Context mContext;
    private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
 
    public SpeciesItemizedOverlay(Drawable defaultMarker, Context context) {
        super(boundCenterBottom(defaultMarker));
        // TODO Auto-generated constructor stub
        mContext = context;
    }

    @Override
    protected OverlayItem createItem(int i) {
        // TODO Auto-generated method stub
        return mOverlays.get(i);
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return mOverlays.size();
    }

    public void addOverlay(OverlayItem overlay)
    {
        mOverlays.add(overlay);
        populate();
    }   
}
