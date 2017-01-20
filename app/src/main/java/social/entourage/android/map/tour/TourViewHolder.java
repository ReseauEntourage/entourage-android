package social.entourage.android.map.tour;

import android.content.Context;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
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
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.model.Partner;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.LastMessage;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourAuthor;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.BaseCardViewHolder;
import social.entourage.android.tools.BusProvider;

/**
 * Created by mihaiionescu on 11/03/16.
 */
public class TourViewHolder extends BaseCardViewHolder {

    private TextView tourTitle;
    private ImageView photoView;
    private ImageView partnerLogoView;
    private TextView tourTypeTextView;
    private TextView tourAuthor;
    private TextView tourLocation;
    private TextView badgeCountView;
    private TextView numberOfPeopleTextView;
    private Button actButton;
    private TextView lastMessageTextView;

    private Tour tour;

    private Context context;

    private GeocoderTask geocoderTask;

    private OnClickListener onClickListener;

    public TourViewHolder(final View itemView) {
        super(itemView);
    }

    @Override
    protected void bindFields() {

        tourTitle = (TextView)itemView.findViewById(R.id.tour_card_title);
        photoView = (ImageView)itemView.findViewById(R.id.tour_card_photo);
        partnerLogoView = (ImageView)itemView.findViewById(R.id.tour_card_partner_logo);
        tourTypeTextView = (TextView)itemView.findViewById(R.id.tour_card_type);
        tourAuthor = (TextView)itemView.findViewById(R.id.tour_card_author);
        tourLocation = (TextView)itemView.findViewById(R.id.tour_card_location);
        badgeCountView = (TextView)itemView.findViewById(R.id.tour_card_badge_count);
        numberOfPeopleTextView = (TextView)itemView.findViewById(R.id.tour_card_people_count);
        actButton = (Button)itemView.findViewById(R.id.tour_card_button_act);
        lastMessageTextView = (TextView)itemView.findViewById(R.id.tour_card_last_message);

        onClickListener = new OnClickListener();

        itemView.setOnClickListener(onClickListener);
        tourAuthor.setOnClickListener(onClickListener);
        photoView.setOnClickListener(onClickListener);
        if (actButton != null) actButton.setOnClickListener(onClickListener);

        context = itemView.getContext();
    }

    public static TourViewHolder fromParent(final ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_tour_card, parent, false);
        return new TourViewHolder(view);
    }

    public static int getLayoutResource() {
        return R.layout.layout_tour_card;
    }

    @Override
    public void populate(final TimestampedObject data) {
        populate((Tour)data);
    }

    public void populate(Tour tour) {

        this.tour = tour;

        //configure the cell fields
        Resources res = itemView.getResources();

        //title
        tourTitle.setText(String.format(res.getString(R.string.tour_cell_title), tour.getOrganizationName()));

        TourAuthor author = tour.getAuthor();
        if (author == null) {
            //author
            tourAuthor.setText("--");
            photoView.setImageResource(R.drawable.ic_user_photo_small);
        } else {
            //author photo
            String avatarURLAsString = author.getAvatarURLAsString();
            if (avatarURLAsString != null) {
                Picasso.with(itemView.getContext())
                        .load(Uri.parse(avatarURLAsString))
                        .placeholder(R.drawable.ic_user_photo_small)
                        .transform(new CropCircleTransformation())
                        .into(photoView);
            } else {
                photoView.setImageResource(R.drawable.ic_user_photo_small);
            }
            //partner logo
            //todo partner logo
            /*
            Partner partner = author.getPartner();
            if (partner != null) {
                String partnerLogoURL = partner.getSmallLogoUrl();
                if (partnerLogoURL != null) {
                    Picasso.with(itemView.getContext())
                            .load(Uri.parse(partnerLogoURL))
                            .placeholder(null)
                            .transform(new CropCircleTransformation())
                            .into(partnerLogoView);
                }
                else {
                    partnerLogoView.setImageDrawable(null);
                }
            } else {
                partnerLogoView.setImageDrawable(null);
            }
            */
            if (avatarURLAsString != null) {
                Picasso.with(itemView.getContext())
                        .load(Uri.parse(avatarURLAsString))
                        .placeholder(R.drawable.ic_user_photo_small)
                        .transform(new CropCircleTransformation())
                        .into(partnerLogoView);
            } else {
                partnerLogoView.setImageResource(R.drawable.ic_user_photo_small);
            }

            //author
            tourAuthor.setText(String.format(res.getString(R.string.tour_cell_author), tour.getAuthor().getUserName()));
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

        //date and location i.e 1h - Arc de Triomphe
        // MI: for v2.1 we display the distance to starting point
        /*
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
        */

        String distanceAsString = "";
        TourPoint startPoint = tour.getStartPoint();
        if (startPoint != null) {
            distanceAsString = startPoint.distanceToCurrentLocation();
        }

        tourLocation.setText(String.format(res.getString(R.string.tour_cell_location), Tour.getStringDiffToNow(tour.getStartTime()), distanceAsString));

        //tour members
        numberOfPeopleTextView.setText(""+tour.getNumberOfPeople());

        //badge count
        int badgeCount = tour.getBadgeCount();
        if (badgeCount <= 0) {
            badgeCountView.setVisibility(View.GONE);
        }
        else {
            badgeCountView.setVisibility(View.VISIBLE);
            badgeCountView.setText("" + tour.getBadgeCount());
        }

        //act button
        if (actButton != null) {
            actButton.setVisibility(View.VISIBLE);
            if (tour.isFreezed()) {
                //actButton.setVisibility(View.GONE);
                actButton.setText(R.string.tour_cell_button_freezed);
                actButton.setCompoundDrawablesWithIntrinsicBounds(null, res.getDrawable(R.drawable.button_act_freezed), null, null);
            } else {
                //actButton.setVisibility(View.VISIBLE);
                String joinStatus = tour.getJoinStatus();
                if (Tour.JOIN_STATUS_PENDING.equals(joinStatus)) {
                    actButton.setText(R.string.tour_cell_button_pending);
                    actButton.setCompoundDrawablesWithIntrinsicBounds(null, res.getDrawable(R.drawable.button_act_pending), null, null);
                } else if (Tour.JOIN_STATUS_ACCEPTED.equals(joinStatus)) {
                    if (tour.getAuthor() != null) {
                        if (tour.getAuthor().getUserID() == EntourageApplication.me(itemView.getContext()).getId()) {
                            actButton.setText(R.string.tour_cell_button_ongoing);
                        } else {
                            actButton.setText(R.string.tour_cell_button_accepted);
                        }
                    } else {
                        actButton.setText(R.string.tour_cell_button_accepted);
                    }
                    actButton.setCompoundDrawablesWithIntrinsicBounds(null, res.getDrawable(R.drawable.button_act_accepted), null, null);
                } else if (Tour.JOIN_STATUS_REJECTED.equals(joinStatus)) {
                    actButton.setText(R.string.tour_cell_button_rejected);
                    actButton.setCompoundDrawablesWithIntrinsicBounds(null, res.getDrawable(R.drawable.button_act_rejected), null, null);
                } else {
                    actButton.setText(R.string.tour_cell_button_join);
                    actButton.setCompoundDrawablesWithIntrinsicBounds(null, res.getDrawable(R.drawable.button_act_join), null, null);
                }
            }
        }

        //last message
        if (lastMessageTextView != null) {
            LastMessage lastMessage = tour.getLastMessage();
            if (lastMessage != null) {
                lastMessageTextView.setText(lastMessage.getText());
            } else {
                lastMessageTextView.setText("");
            }
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
        tourLocation.setText(String.format(itemView.getResources().getString(R.string.tour_cell_location), Tour.getStringDiffToNow(tour.getStartTime()), location));

        geocoderTask = null;
    }



    //--------------------------
    // INNER CLASSES
    //--------------------------

    private class GeocoderTask extends AsyncTask<Tour, Void, Tour> {

        @Override
        protected Tour doInBackground(final Tour... params) {
            try {
                Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
                Tour tour = params[0];
                if (tour.getTourPoints().isEmpty()) return null;
                TourPoint tourPoint = tour.getTourPoints().get(0);
                List<Address> addresses = geoCoder.getFromLocation(tourPoint.getLatitude(), tourPoint.getLongitude(), 1);
                if (addresses != null && addresses.size() > 0) {
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

    private class OnClickListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            if (tour == null) return;
            if (v == photoView || v == tourAuthor) {
                BusProvider.getInstance().post(new Events.OnUserViewRequestedEvent(tour.getAuthor().getUserID()));
            }
            else if (v == actButton) {
                String joinStatus = tour.getJoinStatus();
                if (Tour.JOIN_STATUS_PENDING.equals(joinStatus)) {
                    BusProvider.getInstance().post(new Events.OnFeedItemInfoViewRequestedEvent(tour));
                } else if (Tour.JOIN_STATUS_ACCEPTED.equals(joinStatus)) {
//                    if (tour.getAuthor() != null) {
//                        if (tour.getAuthor().getUserID() == EntourageApplication.me(itemView.getContext()).getId()) {
                            BusProvider.getInstance().post(new Events.OnFeedItemCloseRequestEvent(tour));
                            return;
//                        }
//                    }
//                    BusProvider.getInstance().post(new Events.OnUserActEvent(Events.OnUserActEvent.ACT_QUIT, tour));
                } else if (Tour.JOIN_STATUS_REJECTED.equals(joinStatus)) {
                    //What to do on rejected status ?
                } else {
                    BusProvider.getInstance().post(new Events.OnUserActEvent(Events.OnUserActEvent.ACT_JOIN, tour));
                }

            }
            else if (v == itemView) {
                BusProvider.getInstance().post(new Events.OnFeedItemInfoViewRequestedEvent(tour));
            }
        }

    }
}
