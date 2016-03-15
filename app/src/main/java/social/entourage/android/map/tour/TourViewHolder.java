package social.entourage.android.map.tour;

import android.content.Context;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.R;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

/**
 * Created by mihaiionescu on 11/03/16.
 */
public class TourViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView tourTitle;
    private ImageView photoView;
    private TextView tourTypeTextView;
    private TextView tourAuthor;
    private TextView tourLocation;
    private TextView badgeCountView;
    private TextView numberOfPeopleTextView;
    private Button actButton;

    private Tour tour;

    private Context context;

    private GeocoderTask geocoderTask;

    public TourViewHolder(final View itemView) {
        super(itemView);

        tourTitle = (TextView)itemView.findViewById(R.id.tour_card_title);
        photoView = (ImageView)itemView.findViewById(R.id.tour_card_photo);
        tourTypeTextView = (TextView)itemView.findViewById(R.id.tour_card_type);
        tourAuthor = (TextView)itemView.findViewById(R.id.tour_card_author);
        tourLocation = (TextView)itemView.findViewById(R.id.tour_card_location);
        badgeCountView = (TextView)itemView.findViewById(R.id.tour_card_badge_count);
        numberOfPeopleTextView = (TextView)itemView.findViewById(R.id.tour_card_people_count);
        actButton = (Button)itemView.findViewById(R.id.tour_card_button_act);

        itemView.setOnClickListener(this);
        photoView.setOnClickListener(this);
        actButton.setOnClickListener(this);

        context = itemView.getContext();
    }

    public static TourViewHolder fromParent(final ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_tour_card, parent, false);
        return new TourViewHolder(view);
    }

    public void populate(Tour tour) {

        this.tour = tour;

        //configure the cell fields
        Resources res = itemView.getResources();

        //title
        tourTitle.setText(String.format(res.getString(R.string.tour_cell_title), tour.getOrganizationName()));

        //author photo
        String avatarURLAsString = tour.getAuthor().getAvatarURLAsString();
        if (avatarURLAsString != null) {
            Picasso.with(itemView.getContext())
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
        tourTypeTextView.setText(String.format(res.getString(R.string.tour_cell_type), tourTypeDescription));

        //author
        tourAuthor.setText(String.format(res.getString(R.string.tour_cell_author), tour.getAuthor().getUserName()));

        //date and location i.e 1h - Arc de Triomphe
        String location = "";
        Address tourAddress = tour.getStartAddress();
        if (tourAddress != null) {
            location = tourAddress.getAddressLine(0);
            if (location == null) {
                location = "";
            }
        }
        else {
            if (geocoderTask != null) {
                geocoderTask.cancel(true);
            }
            geocoderTask = new GeocoderTask();
            geocoderTask.execute(tour);
        }
        tourLocation.setText(String.format(res.getString(R.string.tour_cell_location), Tour.getHoursDiffToNow(tour.getStartTime()), "h", location));

        //tour members
        numberOfPeopleTextView.setText(""+tour.getNumberOfPeople());

        //act button
        String joinStatus = tour.getJoinStatus();
        if (joinStatus.equals(Tour.JOIN_STATUS_PENDING)) {
            actButton.setEnabled(false);
            actButton.setText(R.string.tour_cell_button_pending);
            actButton.setCompoundDrawablesWithIntrinsicBounds(null, res.getDrawable(R.drawable.button_act_pending), null, null);
        } else if (joinStatus.equals(Tour.JOIN_STATUS_ACCEPTED)) {
            actButton.setEnabled(false);
            actButton.setText(R.string.tour_cell_button_accepted);
            actButton.setCompoundDrawablesWithIntrinsicBounds(null, res.getDrawable(R.drawable.button_act_accepted), null, null);
        } else if (joinStatus.equals(Tour.JOIN_STATUS_REJECTED)) {
            actButton.setEnabled(false);
            actButton.setText(R.string.tour_cell_button_rejected);
            actButton.setCompoundDrawablesWithIntrinsicBounds(null, res.getDrawable(R.drawable.button_act_rejected), null, null);
        } else {
            actButton.setEnabled(true);
            actButton.setText(R.string.tour_cell_button_join);
            actButton.setCompoundDrawablesWithIntrinsicBounds(null, res.getDrawable(R.drawable.button_act_join), null, null);
        }

    }

    private void updateStartLocation(Tour tour) {
        if (tour == null || tour != this.tour) return;
        String location = "";
        Address tourAddress = tour.getStartAddress();
        if (tourAddress != null) {
            location = tourAddress.getAddressLine(0);
            if (location == null) {
                location = "";
            }
        }
        tourLocation.setText(String.format(itemView.getResources().getString(R.string.tour_cell_location), Tour.getHoursDiffToNow(tour.getStartTime()), "h", location));

        geocoderTask = null;
    }

    @Override
    public void onClick(final View v) {
        if (tour == null) return;
        if (v == photoView) {
            BusProvider.getInstance().post(new Events.OnUserViewRequestedEvent(tour.getAuthor().getUserID()));
        }
        else if (v == actButton) {
            BusProvider.getInstance().post(new Events.OnUserActEvent(Events.OnUserActEvent.ACT_JOIN, tour));
        }
        else if (v == itemView) {
            BusProvider.getInstance().post(new Events.OnTourInfoViewRequestedEvent(tour));
        }
    }

    private class GeocoderTask extends AsyncTask<Tour, Void, Tour> {

        @Override
        protected Tour doInBackground(final Tour... params) {
            try {
                Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
                Tour tour = params[0];
                if (tour.getTourPoints().isEmpty()) return null;
                TourPoint tourPoint = tour.getTourPoints().get(0);
                List<Address> addresses = geoCoder.getFromLocation(tourPoint.getLatitude(), tourPoint.getLongitude(), 1);
                if (addresses.size() > 0) {
                    tour.setStartAddress(addresses.get(0));
                }
                return tour;
            }
            catch (IOException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(final Tour tour) {
            updateStartLocation(tour);
        }
    }
}
