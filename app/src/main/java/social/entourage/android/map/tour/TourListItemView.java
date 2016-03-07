package social.entourage.android.map.tour;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Address;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Date;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.Constants;
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

    private Button actButton;

    public TourListItemView(final Context context) {
        super(context);
        init();
    }

    public TourListItemView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TourListItemView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        actButton = (Button)this.findViewById(R.id.tour_cell_button_act);
    }

    public void populate(final Tour tour, final MapEntourageFragment mapFragment) {

        init();
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

        //title
        TextView tourTitle = (TextView)this.findViewById(R.id.tour_cell_title);
        tourTitle.setText(String.format(res.getString(R.string.tour_cell_title), tour.getOrganizationName()));

        //author photo
        final ImageView photoView = (ImageView)this.findViewById(R.id.tour_cell_photo);
        photoView.setTag(tour.getAuthor().getUserID());
        photoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                mapFragment.displayChosenUser((Integer)v.getTag());
            }
        });
        String avatarURLAsString = tour.getAuthor().getAvatarURLAsString();
        if (avatarURLAsString != null) {
            Picasso.with(this.getContext())
                    .load(Uri.parse(avatarURLAsString))
                    .transform(new CropCircleTransformation())
                    .into(photoView);
        }

        //Tour type
        String tourType = tour.getTourType();
        String tourTypeDescription = "";
        if (tourType != null) {
            if (tourType.equals(TourType.MEDICAL.getName())) {
                tourTypeDescription = res.getString(R.string.tour_type_medical).toLowerCase();
            } else if (tourType.equals(TourType.ALIMENTARY.getName())) {
                tourTypeDescription = res.getString(R.string.tour_type_alimentary).toLowerCase();
            } else if (tourType.equals(TourType.BARE_HANDS.getName())) {
                tourTypeDescription = res.getString(R.string.tour_type_bare_hands).toLowerCase();
            }
        }
        TextView tourTypeTextView = (TextView)this.findViewById(R.id.tour_cell_type);
        tourTypeTextView.setText(String.format(res.getString(R.string.tour_cell_type), tourTypeDescription));

        //author
        TextView tourAuthor = (TextView)this.findViewById(R.id.tour_cell_author);
        tourAuthor.setText(String.format(res.getString(R.string.tour_cell_author), tour.getAuthor().getUserName()));

        //date and location i.e 1h - Arc de Triomphe
        TextView tourLocation = (TextView)this.findViewById(R.id.tour_cell_location);
        tourLocation.setText(String.format(res.getString(R.string.tour_cell_location), getHoursDiffToNow(tour.getStartTime()), "h", ""));

        //act button
        updateJoinStatus(tour);
        actButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                mapFragment.act(tour);
            }
        });

        //tour members
        TextView numberOfPeopleTextView = (TextView)this.findViewById(R.id.tour_cell_people_count);
        numberOfPeopleTextView.setText(""+tour.getNumberOfPeople());
    }

    public void updateStartLocation(Tour tour) {
        TextView tourLocationTextView = (TextView)this.findViewById(R.id.tour_cell_location);
        String tourLocation = "";
        Address tourAddress = tour.getStartAddress();
        if (tourAddress != null) {
            tourLocation = tourAddress.getAddressLine(0);
            if (tourLocation == null) {
                tourLocation = "";
            }
        }
        tourLocationTextView.setText(String.format(getResources().getString(R.string.tour_cell_location), getHoursDiffToNow(tour.getStartTime()), "h", tourLocation));
    }

    public void updateJoinStatus(Tour tour) {
        String joinStatus = tour.getJoinStatus();
        if (joinStatus.equals(Tour.JOIN_STATUS_PENDING)) {
            actButton.setEnabled(false);
            actButton.setText(R.string.tour_cell_button_pending);
            actButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.button_act_pending), null, null);
        } else if (joinStatus.equals(Tour.JOIN_STATUS_ACCEPTED)) {
            actButton.setEnabled(false);
            actButton.setText(R.string.tour_cell_button_accepted);
            actButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.button_act_accepted), null, null);
        } else if (joinStatus.equals(Tour.JOIN_STATUS_REJECTED)) {
            actButton.setEnabled(false);
            actButton.setText(R.string.tour_cell_button_rejected);
            actButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.button_act_rejected), null, null);
        } else {
            actButton.setEnabled(true);
            actButton.setText(R.string.tour_cell_button_join);
            actButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.button_act_join), null, null);
        }
    }

    private long getHoursDiffToNow(Date fromDate) {
        long currentHours = System.currentTimeMillis() / Constants.MILLIS_HOUR;
        long startHours = currentHours;
        if (fromDate != null) {
            startHours = fromDate.getTime() / Constants.MILLIS_HOUR;
        }
        return (currentHours - startHours);
    }
}
