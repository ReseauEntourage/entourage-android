package social.entourage.android.map.tour;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import social.entourage.android.R;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.map.MapEntourageFragment;

/**
 * Created by mihaiionescu on 29/01/16.
 *
 * Handles the display of a tour in list view
 */
public class TourListItemView extends GridLayout {

    public TourListItemView(final Context context) {
        super(context);
    }

    public TourListItemView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public TourListItemView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void populate(final Tour tour, final MapEntourageFragment mapFragment) {
        //configure the cell
        this.setTag(tour.getId());
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                mapFragment.displayChosenTour(tour);
            }
        });

        //configure the cell fields
        Resources res = getResources();

        //title i.e Maraude Croix
        TextView tourTitle = (TextView)this.findViewById(R.id.tour_cell_title);
        tourTitle.setText(String.format(res.getString(R.string.tour_cell_title), tour.getOrganizationName()));

        //author photo - no data yet
        ImageView photoView = (ImageView)this.findViewById(R.id.tour_cell_photo);
        photoView.setTag(tour.getUserId());
        photoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                mapFragment.displayChosenUser((Integer)v.getTag());
            }
        });

        //author i.e par Mihai
        TextView tourAuthor = (TextView)this.findViewById(R.id.tour_cell_author);
        tourAuthor.setText(String.format(res.getString(R.string.tour_cell_author), tour.getUserId()));

        //date and location i.e 1h - Arc de Triomphe
        TextView tourLocation = (TextView)this.findViewById(R.id.tour_cell_location);
        long startHours = tour.getStartTime().getTime()/1000/60/60;
        long currentHours = System.currentTimeMillis()/1000/60/60;
        tourLocation.setText(String.format(res.getString(R.string.tour_cell_location), (currentHours - startHours), "h", ""));

        //tour type
        ImageView tourTypeView = (ImageView)this.findViewById(R.id.tour_cell_type);
        String tourType = tour.getTourType();
        if (tourType != null) {
            if (tourType.equals(TourType.MEDICAL.getName())) {
                tourTypeView.setImageDrawable(res.getDrawable(R.drawable.ic_medical));
            } else if (tourType.equals(TourType.ALIMENTARY.getName())) {
                tourTypeView.setImageDrawable(res.getDrawable(R.drawable.ic_alimentary));
            } else if (tourType.equals(TourType.BARE_HANDS.getName())) {
                tourTypeView.setImageDrawable(res.getDrawable(R.drawable.ic_bare_hands));
            }
        } else {
            tourTypeView.setImageDrawable(null);
        }

        //act button
        Button actButton = (Button)this.findViewById(R.id.tour_cell_button_act);
        actButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                mapFragment.act(tour);
            }
        });

        //tour members - No data yet
    }

    public void updateStartLocation(Tour tour) {
        TextView tourLocation = (TextView)this.findViewById(R.id.tour_cell_location);
        long startHours = tour.getStartTime().getTime()/1000/60/60;
        long currentHours = System.currentTimeMillis()/1000/60/60;
        tourLocation.setText(String.format(getResources().getString(R.string.tour_cell_location), (currentHours - startHours), "h", tour.getStartAddress().getAddressLine(0)));
    }
}
