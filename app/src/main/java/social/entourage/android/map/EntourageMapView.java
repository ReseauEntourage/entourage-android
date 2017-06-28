package social.entourage.android.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;

/**
 * Created by mihaiionescu on 28/06/2017.
 */

public class EntourageMapView extends MapView {

    public EntourageMapView(final Context context) {
        super(context);
    }

    public EntourageMapView(final Context context, final AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public EntourageMapView(final Context context, final AttributeSet attributeSet, final int i) {
        super(context, attributeSet, i);
    }

    public EntourageMapView(final Context context, final GoogleMapOptions googleMapOptions) {
        super(context, googleMapOptions);
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent motionEvent) {

        switch (motionEvent.getActionMasked()) {
            // Pressed on map: stop parent from scrolling
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;

            // Released on map or cancelled: parent can be normal again
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        // Process event as normal. If parent was disallowed touch events, the map will process the event.
        return super.dispatchTouchEvent(motionEvent);
    }

}
