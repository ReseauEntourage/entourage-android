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
public class FPFEntourage extends Entourage implements Serializable {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TYPE_PRIVATE_CIRCLE = "private_circle";
    public static final String TYPE_NEIGHBORHOOD = "neighborhood";

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @SerializedName("group_type")
    String groupType;

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(final String groupType) {
        this.groupType = groupType;
    }

    // ----------------------------------
    // FeedItem overrides
    // ----------------------------------

    @Override
    public Drawable getIconDrawable(final Context context) {
        if (TYPE_PRIVATE_CIRCLE.equalsIgnoreCase(groupType)) {
            return AppCompatResources.getDrawable(context, R.drawable.ic_heart);
        }
        if (TYPE_NEIGHBORHOOD.equalsIgnoreCase(groupType)) {
            return AppCompatResources.getDrawable(context, R.drawable.ic_neighborhood);
        }
        return super.getIconDrawable(context);
    }

    @Override
    public String getFeedTypeLong(final Context context) {
        if (TYPE_PRIVATE_CIRCLE.equalsIgnoreCase(groupType) || TYPE_NEIGHBORHOOD.equalsIgnoreCase(groupType)) {
            return context.getString(R.string.entourage_type_format, context.getString(R.string.entourage_type_demand));
        }
        return super.getFeedTypeLong(context);
    }
}
