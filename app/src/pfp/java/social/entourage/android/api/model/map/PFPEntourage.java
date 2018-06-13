package social.entourage.android.api.model.map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import social.entourage.android.R;

/**
 * PFP Entourage
 * Created by Mihai Ionescu on 05/06/2018.
 */
public class PFPEntourage extends Entourage implements Serializable {

    // ----------------------------------
    // FeedItem overrides
    // ----------------------------------

    @Override
    public Drawable getIconDrawable(final Context context) {
        if (TYPE_PRIVATE_CIRCLE.equalsIgnoreCase(groupType)) {
            return AppCompatResources.getDrawable(context, R.drawable.ic_heart);
        }
        return super.getIconDrawable(context);
    }

    @Override
    public boolean showHeatmapAsOverlay() {
        if (TYPE_PRIVATE_CIRCLE.equalsIgnoreCase(groupType)) {
            return false;
        }
        return super.showHeatmapAsOverlay();
    }

    @Override
    public int getHeatmapResourceId() {
        if (TYPE_PRIVATE_CIRCLE.equalsIgnoreCase(groupType)) {
            return R.drawable.ic_heart;
        }
        return super.getHeatmapResourceId();
    }

    @Override
    public String getFeedTypeLong(final Context context) {
        if (TYPE_PRIVATE_CIRCLE.equalsIgnoreCase(groupType)) {
            return context.getString(R.string.entourage_type_format, context.getString(R.string.entourage_type_demand));
        }
        return super.getFeedTypeLong(context);
    }
}
