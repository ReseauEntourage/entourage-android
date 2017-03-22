package social.entourage.android.map.entourage;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import social.entourage.android.Constants;
import social.entourage.android.R;
import social.entourage.android.api.model.Partner;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.LastMessage;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourAuthor;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.BaseCardViewHolder;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.CropCircleTransformation;
import social.entourage.android.view.PartnerLogoImageView;

/**
 * Created by mihaiionescu on 05/05/16.
 */
public class EntourageViewHolder extends BaseCardViewHolder {

    private TextView entourageTitle;
    private ImageView photoView;
    private PartnerLogoImageView partnerLogoView;
    private TextView entourageTypeTextView;
    private TextView entourageAuthor;
    private TextView entourageLocation;
    private TextView badgeCountView;
    private TextView numberOfPeopleTextView;
    private Button actButton;
    private TextView lastMessageTextView;

    private Entourage entourage;

    private Context context;

    private GeocoderTask geocoderTask;

    private OnClickListener onClickListener;

    public EntourageViewHolder(final View itemView) {
        super(itemView);
    }

    @Override
    protected void bindFields() {

        entourageTitle = (TextView) itemView.findViewById(R.id.tour_card_title);
        photoView = (ImageView) itemView.findViewById(R.id.tour_card_photo);
        partnerLogoView = (PartnerLogoImageView) itemView.findViewById(R.id.tour_card_partner_logo);
        entourageTypeTextView = (TextView) itemView.findViewById(R.id.tour_card_type);
        entourageAuthor = (TextView) itemView.findViewById(R.id.tour_card_author);
        entourageLocation = (TextView) itemView.findViewById(R.id.tour_card_location);
        badgeCountView = (TextView) itemView.findViewById(R.id.tour_card_badge_count);
        numberOfPeopleTextView = (TextView) itemView.findViewById(R.id.tour_card_people_count);
        actButton = (Button) itemView.findViewById(R.id.tour_card_button_act);
        lastMessageTextView = (TextView) itemView.findViewById(R.id.tour_card_last_message);

        onClickListener = new OnClickListener();

        itemView.setOnClickListener(onClickListener);
        entourageAuthor.setOnClickListener(onClickListener);
        photoView.setOnClickListener(onClickListener);
        if (actButton != null) actButton.setOnClickListener(onClickListener);

        context = itemView.getContext();
    }

    public static EntourageViewHolder fromParent(final ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_tour_card, parent, false);
        return new EntourageViewHolder(view);
    }

    public static int getLayoutResource() {
        return R.layout.layout_tour_card;
    }

    @Override
    public void populate(final TimestampedObject data) {
        populate((Entourage)data);
    }

    public void populate(Entourage entourage) {

        this.entourage = entourage;

        //configure the cell fields
        Resources res = itemView.getResources();

        //title
        entourageTitle.setText(String.format(res.getString(R.string.tour_cell_title), entourage.getTitle()));

        //author + photo
        TourAuthor author = entourage.getAuthor();
        if(author == null) {
            entourageAuthor.setText("--");//@todo this should not happen!
            photoView.setImageResource(R.drawable.ic_user_photo_small);
            partnerLogoView.setImageDrawable(null);
        } else {
            entourageAuthor.setText(String.format(res.getString(R.string.tour_cell_author), author.getUserName()));
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
            if (partnerLogoView != null) {
                Partner partner = author.getPartner();
                if (partner != null) {
                    String partnerLogoURL = partner.getSmallLogoUrl();
                    if (partnerLogoURL != null) {
                        Picasso.with(itemView.getContext())
                                .load(Uri.parse(partnerLogoURL))
                                .placeholder(R.drawable.partner_placeholder)
                                .transform(new CropCircleTransformation())
                                .into(partnerLogoView);
                    }
                    else {
                        partnerLogoView.setImageDrawable(null);
                    }
                } else {
                    partnerLogoView.setImageDrawable(null);
                }
            }
        }
        //Tour type
        String entourageType = entourage.getEntourageType();
        String entourageTypeDescription = "";
        if (entourageType != null) {
            if (Entourage.TYPE_CONTRIBUTION.equals(entourageType)) {
                entourageTypeDescription = context.getString(R.string.entourage_type_contribution);
            }
            else if (Entourage.TYPE_DEMAND.equals(entourageType)) {
                entourageTypeDescription = context.getString(R.string.entourage_type_demand);
            }
        }
        entourageTypeTextView.setText(String.format(res.getString(R.string.entourage_type_format), entourageTypeDescription));

        //date and distance
        String distanceAsString = "";
        TourPoint location = entourage.getLocation();
        if (location != null) {
            distanceAsString = location.distanceToCurrentLocation();
        }

        entourageLocation.setText(String.format(res.getString(R.string.tour_cell_location), Tour.getStringDiffToNow(entourage.getStartTime()), distanceAsString));

        //tour members
        numberOfPeopleTextView.setText(""+entourage.getNumberOfPeople());

        //badge count
        int badgeCount = entourage.getBadgeCount();
        if (badgeCount <= 0) {
            badgeCountView.setVisibility(View.GONE);
        }
        else {
            badgeCountView.setVisibility(View.VISIBLE);
            badgeCountView.setText("" + entourage.getBadgeCount());
        }

        //act button
        if (actButton != null) {
            actButton.setVisibility(View.VISIBLE);
            actButton.setPadding(0, 0, res.getDimensionPixelOffset(R.dimen.act_button_right_padding), 0);
            if (Build.VERSION.SDK_INT >= 16) {
                actButton.setPaddingRelative(0, 0, res.getDimensionPixelOffset(R.dimen.act_button_right_padding), 0);
            }
            if (entourage.isFreezed()) {
                actButton.setText(R.string.tour_cell_button_freezed);
                actButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                actButton.setPadding(0, 0, 0, 0);
                if (Build.VERSION.SDK_INT >= 16) {
                    actButton.setPaddingRelative(0, 0, 0, 0);
                }
            } else {
                String joinStatus = entourage.getJoinStatus();
                if (Tour.JOIN_STATUS_PENDING.equals(joinStatus)) {
                    actButton.setText(R.string.tour_cell_button_pending);
                    actButton.setCompoundDrawablesWithIntrinsicBounds(res.getDrawable(R.drawable.button_act_pending), null, null, null);
                } else if (Tour.JOIN_STATUS_ACCEPTED.equals(joinStatus)) {
                    actButton.setText(R.string.tour_cell_button_accepted);
                    actButton.setCompoundDrawablesWithIntrinsicBounds(res.getDrawable(R.drawable.button_act_accepted), null, null, null);
                } else if (Tour.JOIN_STATUS_REJECTED.equals(joinStatus)) {
                    actButton.setText(R.string.tour_cell_button_rejected);
                    actButton.setCompoundDrawablesWithIntrinsicBounds(res.getDrawable(R.drawable.button_act_rejected), null, null, null);
                } else {
                    actButton.setText(R.string.tour_cell_button_view);
                    actButton.setCompoundDrawablesWithIntrinsicBounds(res.getDrawable(R.drawable.button_act_join), null, null, null);
                }
            }
        }

        //last message
        if (lastMessageTextView != null) {
            LastMessage lastMessage = entourage.getLastMessage();
            if (lastMessage != null) {
                lastMessageTextView.setText(lastMessage.getText());
            } else {
                lastMessageTextView.setText("");
            }
            lastMessageTextView.setTypeface(null, entourage.getBadgeCount() == 0 ? Typeface.NORMAL : Typeface.BOLD);
        }

    }

    private void updateStartLocation(Entourage entourage) {
        if (entourage == null || entourage != this.entourage) return;
        String location = "";
        Address tourAddress = entourage.getStartAddress();
        if (tourAddress != null) {
            location = tourAddress.getAddressLine(0);
            if (location == null) {
                location = "";
            }
        }
        entourageLocation.setText(String.format(itemView.getResources().getString(R.string.tour_cell_location), Tour.getStringDiffToNow(entourage.getStartTime()), location));

        geocoderTask = null;
    }

    //------------------
    // INNER CLASSES
    //------------------

    private class GeocoderTask extends AsyncTask<Entourage, Void, Entourage> {

        @Override
        protected Entourage doInBackground(final Entourage... params) {
            try {
                Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
                Entourage entourage = params[0];
                if (entourage.getLocation() == null) return null;
                TourPoint tourPoint = entourage.getLocation();
                List<Address> addresses = geoCoder.getFromLocation(tourPoint.getLatitude(), tourPoint.getLongitude(), 1);
                if (addresses != null && addresses.size() > 0) {
                    entourage.setStartAddress(addresses.get(0));
                }
                return entourage;
            }
            catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(final Entourage entourage) {
            updateStartLocation(entourage);
        }
    }

    private class OnClickListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            if (entourage == null) return;
            if (v == photoView || v == entourageAuthor) {
                BusProvider.getInstance().post(new Events.OnUserViewRequestedEvent(entourage.getAuthor().getUserID()));
            }
            else if (v == actButton) {
                String joinStatus = entourage.getJoinStatus();
                if (Tour.JOIN_STATUS_PENDING.equals(joinStatus)) {
                    FlurryAgent.logEvent(Constants.EVENT_FEED_PENDING_OVERLAY);
                    BusProvider.getInstance().post(new Events.OnFeedItemCloseRequestEvent(entourage));
                } else if (Tour.JOIN_STATUS_ACCEPTED.equals(joinStatus)) {
                    BusProvider.getInstance().post(new Events.OnFeedItemCloseRequestEvent(entourage));
                } else if (Tour.JOIN_STATUS_REJECTED.equals(joinStatus)) {
                    //What to do on rejected status ?
                } else {
                    BusProvider.getInstance().post(new Events.OnFeedItemInfoViewRequestedEvent(entourage));
                }

            }
            else if (v == itemView) {
                BusProvider.getInstance().post(new Events.OnFeedItemInfoViewRequestedEvent(entourage));
            }
        }

    }

}
