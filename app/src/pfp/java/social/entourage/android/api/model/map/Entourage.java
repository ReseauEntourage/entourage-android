package social.entourage.android.api.model.map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.appcompat.content.res.AppCompatResources;

import java.io.Serializable;

import social.entourage.android.R;

/**
 * PFP Entourage
 * Created by Mihai Ionescu on 05/06/2018.
 */
public class Entourage extends BaseEntourage implements Serializable {

    // ----------------------------------
    // Constants
    // ----------------------------------

    private static final long serialVersionUID = -7858700650513499498L;

    // ----------------------------------
    // Constructors
    // ----------------------------------

    public Entourage() {
    }

    public Entourage(final String entourageType, final String category, final String title, final String description, final TourPoint location) {
        super(entourageType, category, title, description, location);
    }

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
            return R.drawable.ic_heart_marker;
        }
        return super.getHeatmapResourceId();
    }

    @Override
    public boolean canBeClosed() {
        if (TYPE_PRIVATE_CIRCLE.equalsIgnoreCase(groupType)) {
            return false;
        }
        return super.canBeClosed();
    }

    @Override
    public boolean showAuthor() {
        if (TYPE_PRIVATE_CIRCLE.equalsIgnoreCase(groupType)) {
            return false;
        }
        return super.showAuthor();
    }

    @Override
    public int getJoinRequestTitle() {
        if (TYPE_OUTING.equalsIgnoreCase(groupType)) return R.string.tour_info_request_join_title_outing;
        return super.getJoinRequestTitle();
    }

    @Override
    public int getJoinRequestButton() {
        if (TYPE_OUTING.equalsIgnoreCase(groupType)) return R.string.tour_info_request_join_button_outing;
        return super.getJoinRequestButton();
    }

    @Override
    public String getFeedTypeLong(final Context context) {
        if (TYPE_PRIVATE_CIRCLE.equalsIgnoreCase(groupType)) {
            return context.getString(R.string.entourage_type_private_circle);
        }
        return super.getFeedTypeLong(context);
    }

    @Override
    public int getFeedTypeColor() {
        if (TYPE_PRIVATE_CIRCLE.equalsIgnoreCase(groupType)) {
            return R.color.action_type_private_circle;
        }
        return super.getFeedTypeColor();
    }

    @Override
    public boolean showInviteViewAfterCreation() {
        return false;
    }

    @Override
    public boolean showEditEntourageView() {
        if (TYPE_PRIVATE_CIRCLE.equalsIgnoreCase(groupType) || TYPE_NEIGHBORHOOD.equalsIgnoreCase(groupType)) {
            return false;
        }
        return super.showEditEntourageView();
    }

}
