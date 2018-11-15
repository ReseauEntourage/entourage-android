package social.entourage.android.api.model.map;

import java.io.Serializable;

import social.entourage.android.R;

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

    @Override
    public int getJoinRequestTitle() {
        if (Entourage.TYPE_OUTING.equalsIgnoreCase(groupType)) return R.string.tour_info_request_join_title_outing;
        return super.getJoinRequestTitle();
    }
}
