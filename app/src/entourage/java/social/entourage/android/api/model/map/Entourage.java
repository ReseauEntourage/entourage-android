package social.entourage.android.api.model.map;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.map.entourage.category.EntourageCategory;
import social.entourage.android.map.entourage.category.EntourageCategoryManager;

/**
 * Created by Mihai Ionescu on 28/04/16.
 */
public class Entourage extends BaseEntourage implements Serializable {

    private static final long serialVersionUID = -1228932044085412292L;

    public Entourage() {
    }

    public Entourage(final String entourageType, final String category, final String title, final String description, final TourPoint location) {
        super(entourageType, category, title, description, location);
    }
}
