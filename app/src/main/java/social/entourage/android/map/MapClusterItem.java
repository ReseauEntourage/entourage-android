package social.entourage.android.map;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.ui.IconGenerator;

import social.entourage.android.R;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.tools.Utils;

/**
 *
 * Created by Mihai Ionescu on 18/09/2018.
 */
public class MapClusterItem implements ClusterItem {

    private Object mapItem;

    public MapClusterItem(FeedItem feedItem) {
        mapItem = feedItem;
    }

    public MapClusterItem(Encounter encounter) {
        mapItem = encounter;
    }

    public Object getMapItem() {
        return mapItem;
    }

    @Override
    public LatLng getPosition() {
        if (mapItem != null) {
            if (mapItem instanceof FeedItem) {
                FeedItem feedItem = (FeedItem)mapItem;
                if (feedItem.getType() == FeedItem.TOUR_CARD) {
                    Tour tour = (Tour)mapItem;
                    if (tour.getTourPoints() != null) {
                        TourPoint lastPoint = tour.getTourPoints().get(tour.getTourPoints().size() - 1);
                        if (lastPoint != null) {
                            return new LatLng(lastPoint.getLatitude(), lastPoint.getLongitude());
                        }
                    }
                }
                else if (feedItem.getType() == FeedItem.ENTOURAGE_CARD) {
                    TourPoint location = ((Entourage)feedItem).getLocation();
                    if (location != null) {
                        return new LatLng(location.getLatitude(), location.getLongitude());
                    }
                }
            }
            else if (mapItem instanceof  Encounter) {
                Encounter encounter = (Encounter)mapItem;
                return new LatLng(encounter.getLatitude(), encounter.getLongitude());
            }
        }
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    public void applyMarkerOptions(Context context, final MarkerOptions markerOptions) {
        if (mapItem == null) return;
        if (mapItem instanceof FeedItem) {
            FeedItem feedItem = (FeedItem)mapItem;
            if (feedItem.getType() == FeedItem.TOUR_CARD) {
                IconGenerator iconGenerator = new IconGenerator(context);
                iconGenerator.setTextAppearance(R.style.OngoingTourMarker);
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(((Tour)feedItem).getOrganizationName()));

                markerOptions.icon(icon);
                markerOptions.anchor(0.5f, 1.0f);
            }
            else if (feedItem.getType() == FeedItem.ENTOURAGE_CARD) {
                Drawable drawable = context.getResources().getDrawable(((Entourage)mapItem).getHeatmapResourceId());
                BitmapDescriptor icon = Utils.getBitmapDescriptorFromDrawable(drawable, Entourage.getMarkerSize(context), Entourage.getMarkerSize(context));

                markerOptions.icon(icon);
            }
        }
        else if (mapItem instanceof  Encounter) {
            BitmapDescriptor encounterIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_encounter);

            markerOptions.icon(encounterIcon);
        }
    }
}
