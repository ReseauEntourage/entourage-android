package social.entourage.android.map.tour;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Address;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Date;

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
            ImageLoader.getInstance().loadImage(avatarURLAsString, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(final String imageUri, final View view, final Bitmap loadedImage) {
                    photoView.setImageBitmap(loadedImage);
                }
            });
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
        Date tourStartDate = tour.getStartTime();
        long currentHours = System.currentTimeMillis()/1000/60/60;
        long startHours = currentHours;
        if (tourStartDate != null) {
            startHours = tourStartDate.getTime() / 1000 / 60 / 60;
        }
        tourLocation.setText(String.format(res.getString(R.string.tour_cell_location), (currentHours - startHours), "h", ""));

        //act button
        boolean canJoinTour = tour.canJoin();
        Button actButton = (Button)this.findViewById(R.id.tour_cell_button_act);
        actButton.setEnabled(canJoinTour);
        if (canJoinTour) {
            actButton.setText(R.string.tour_cell_button_enabled);
        } else {
            actButton.setText(R.string.tour_cell_button_disabled);
        }
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
        long currentHours = System.currentTimeMillis()/1000/60/60;
        long startHours = currentHours;
        Date tourStartDate = tour.getStartTime();
        if (tourStartDate != null) {
            startHours = tourStartDate.getTime()/1000/60/60;
        }
        String tourLocation = "";
        Address tourAddress = tour.getStartAddress();
        if (tourAddress != null) {
            tourLocation = tourAddress.getAddressLine(0);
            if (tourLocation == null) {
                tourLocation = "";
            }
        }
        tourLocationTextView.setText(String.format(getResources().getString(R.string.tour_cell_location), (currentHours - startHours), "h", tourLocation));
    }
}
