package social.entourage.android.newsfeed;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.core.content.ContextCompat;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Calendar;
import java.util.Date;

import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.Partner;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.BaseEntourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.LastMessage;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourAuthor;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.BaseCardViewHolder;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.CropCircleTransformation;
import social.entourage.android.tools.Utils;
import social.entourage.android.view.PartnerLogoImageView;

import static social.entourage.android.tools.Utils.getMonthAsString;

/**
 * Created by Mihai Ionescu on 24/03/2017.
 */

public class FeedItemViewHolder extends BaseCardViewHolder implements Target {

    private TextView tourTitle;
    private ImageView tourIcon;
    private ImageView photoView;
    private PartnerLogoImageView partnerLogoView;
    private TextView tourTypeTextView;
    private TextView tourAuthor;
    private TextView tourLocation;
    private TextView badgeCountView;
    private TextView numberOfPeopleTextView;
    private Button actButton;
    private View dividerLeft;
    private View dividerRight;
    private TextView lastMessageTextView;
    private TextView lastUpdateDateTextView;

    private FeedItem feedItem;

    private Context context;

    private OnClickListener onClickListener;

    public FeedItemViewHolder(final View itemView) {
        super(itemView);
    }

    @Override
    protected void bindFields() {

        tourTitle = itemView.findViewById(R.id.tour_card_title);
        tourIcon = itemView.findViewById(R.id.tour_card_icon);
        photoView = itemView.findViewById(R.id.tour_card_photo);
        partnerLogoView = itemView.findViewById(R.id.tour_card_partner_logo);
        tourTypeTextView = itemView.findViewById(R.id.tour_card_type);
        tourAuthor = itemView.findViewById(R.id.tour_card_author);
        tourLocation = itemView.findViewById(R.id.tour_card_location);
        badgeCountView = itemView.findViewById(R.id.tour_card_badge_count);
        numberOfPeopleTextView = itemView.findViewById(R.id.tour_card_people_count);
        actButton = itemView.findViewById(R.id.tour_card_button_act);
        lastMessageTextView = itemView.findViewById(R.id.tour_card_last_message);
        lastUpdateDateTextView = itemView.findViewById(R.id.tour_card_last_update_date);

        onClickListener = new OnClickListener();

        itemView.setOnClickListener(onClickListener);
        //tourAuthor.setOnClickListener(onClickListener);
        if (photoView != null) photoView.setOnClickListener(onClickListener);
        if (actButton != null) actButton.setOnClickListener(onClickListener);

        context = itemView.getContext();
    }

    public static int getLayoutResource() {
        return R.layout.layout_feed_action_card;
    }

    @Override
    public void populate(final TimestampedObject data) {
        populate((FeedItem) data);
    }

    public void populate(FeedItem feedItem) {

        this.feedItem = feedItem;

        //configure the cell fields
        Resources res = itemView.getResources();

        //title
        if (tourTitle != null) {
            tourTitle.setText(String.format(res.getString(R.string.tour_cell_title), feedItem.getTitle()));
            tourTitle.setTypeface(null, feedItem.getBadgeCount() == 0 ? Typeface.NORMAL : Typeface.BOLD);
            if (showCategoryIcon() && tourIcon == null) {
                // add the icon for entourages
                Picasso.get()
                        .cancelRequest(this);
                String iconURL = feedItem.getIconURL();
                if (iconURL != null) {
                    tourTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    Picasso.get()
                            .load(iconURL)
                            .error(R.drawable.ic_user_photo_small)
                            .transform(new CropCircleTransformation())
                            .into(this);
                } else {
                    tourTitle.setCompoundDrawablesWithIntrinsicBounds(feedItem.getIconDrawable(context), null, null, null);
                }
            }
        }
        //icon
        if (showCategoryIcon() && tourIcon != null) {
            // add the icon for entourages
            Picasso.get()
                    .cancelRequest(tourIcon);
            String iconURL = feedItem.getIconURL();
            if (iconURL != null) {
                tourIcon.setImageDrawable(null);
                Picasso.get()
                        .load(iconURL)
                        .placeholder(R.drawable.ic_user_photo_small)
                        .transform(new CropCircleTransformation())
                        .into(tourIcon);
            } else {
                tourIcon.setImageDrawable(feedItem.getIconDrawable(context));
            }
        }

        TourAuthor author = feedItem.getAuthor();
        if (author == null) {
            //author
            if (tourAuthor != null) {
                tourAuthor.setText("--");
            }
            if (photoView != null) {
                photoView.setImageResource(R.drawable.ic_user_photo_small);
            }
        } else {
            //author photo
            if (photoView != null) {
                String avatarURLAsString = author.getAvatarURLAsString();
                if (avatarURLAsString != null) {
                    Picasso.get()
                            .load(Uri.parse(avatarURLAsString))
                            .placeholder(R.drawable.ic_user_photo_small)
                            .transform(new CropCircleTransformation())
                            .into(photoView);
                } else {
                    photoView.setImageResource(R.drawable.ic_user_photo_small);
                }
            }
            // Partner logo
            if (partnerLogoView != null) {
                Partner partner = author.getPartner();
                if (partner != null) {
                    String partnerLogoURL = partner.getSmallLogoUrl();
                    if (partnerLogoURL != null) {
                        Picasso.get()
                                .load(Uri.parse(partnerLogoURL))
                                .placeholder(R.drawable.partner_placeholder)
                                .transform(new CropCircleTransformation())
                                .into(partnerLogoView);
                    } else {
                        partnerLogoView.setImageDrawable(null);
                    }
                } else {
                    partnerLogoView.setImageDrawable(null);
                }
            }

            //author
            if (tourAuthor != null) {
                tourAuthor.setText(String.format(res.getString(R.string.tour_cell_author), author.getUserName()));
            }
        }
        if (!feedItem.showAuthor()) {
            if (tourAuthor != null) tourAuthor.setText("");
            if (photoView != null) photoView.setImageDrawable(null);
            if (partnerLogoView != null) partnerLogoView.setImageDrawable(null);
        }

        //Metadata
        if (feedItem instanceof BaseEntourage) {
            BaseEntourage.Metadata metadata = ((BaseEntourage) feedItem).getMetadata();
            if (metadata != null) {
                // hide author name for events
                if (tourAuthor != null && metadata.getStartDate() != null) tourAuthor.setText("");
            }
        }

        //Feed Item type
        if (tourTypeTextView != null) {
            tourTypeTextView.setText(feedItem.getFeedTypeLong(context));
            if (feedItem.getFeedTypeColor() != 0) {
                tourTypeTextView.setTextColor(ContextCompat.getColor(context, feedItem.getFeedTypeColor()));
            }
        }

        if (tourLocation != null) {
            String distanceAsString = "";
            TourPoint startPoint = feedItem.getStartPoint();
            if (startPoint != null) {
                distanceAsString = startPoint.distanceToCurrentLocation();
            }

            if (distanceAsString.equalsIgnoreCase("")) {
                tourLocation.setText("");
            } else {
                tourLocation.setText(String.format(res.getString(R.string.tour_cell_location), distanceAsString));
            }
        }

        //tour members
        if (numberOfPeopleTextView != null) {
            numberOfPeopleTextView.setText(res.getString(R.string.tour_cell_numberOfPeople, feedItem.getNumberOfPeople()));
        }

        //badge count
        if (badgeCountView != null) {
            int badgeCount = feedItem.getBadgeCount();
            if (badgeCount <= 0) {
                badgeCountView.setVisibility(View.GONE);
            } else {
                badgeCountView.setVisibility(View.VISIBLE);
                badgeCountView.setText(res.getString(R.string.badge_count_format, feedItem.getBadgeCount()));
            }
        }

        //act button
        if (actButton != null) {
            int dividerColor = R.color.accent;
            int textColor = R.color.accent;
            actButton.setVisibility(View.VISIBLE);
            if (feedItem.isFreezed()) {
                actButton.setText(feedItem.getFreezedCTAText());
                dividerColor = R.color.greyish;
                textColor = feedItem.getFreezedCTAColor();
            } else {
                String joinStatus = feedItem.getJoinStatus();
                if (Tour.JOIN_STATUS_PENDING.equals(joinStatus)) {
                    actButton.setText(R.string.tour_cell_button_pending);
                } else if (Tour.JOIN_STATUS_ACCEPTED.equals(joinStatus)) {
                    if (feedItem.getAuthor() != null) {
                        if (feedItem.getType() == TimestampedObject.TOUR_CARD && feedItem.isOngoing()) {
                            actButton.setText(R.string.tour_cell_button_ongoing);
                        } else {
                            actButton.setText(R.string.tour_cell_button_accepted);
                        }
                    } else {
                        actButton.setText(R.string.tour_cell_button_accepted);
                    }
                } else if (Tour.JOIN_STATUS_REJECTED.equals(joinStatus)) {
                    actButton.setText(R.string.tour_cell_button_rejected);
                    textColor = R.color.tomato;
                } else {
                    actButton.setText(R.string.tour_cell_button_view);
                    dividerColor = R.color.greyish;
                }
            }
            actButton.setTextColor(res.getColor(textColor));

            if (dividerLeft != null) dividerLeft.setBackgroundResource(dividerColor);
            if (dividerRight != null) dividerRight.setBackgroundResource(dividerColor);
        }

        //last message
        if (lastMessageTextView != null) {
            LastMessage lastMessage = feedItem.getLastMessage();
            if (lastMessage != null) {
                lastMessageTextView.setText(lastMessage.getText());
            } else {
                lastMessageTextView.setText("");
            }
            lastMessageTextView.setVisibility(lastMessageTextView.getText().length() == 0 ? View.GONE : View.VISIBLE);
            lastMessageTextView.setTypeface(null, feedItem.getBadgeCount() == 0 ? Typeface.NORMAL : Typeface.BOLD);
            lastMessageTextView.setTextColor(feedItem.getBadgeCount() == 0 ? res.getColor(R.color.feeditem_card_details_normal) : res.getColor(R.color.feeditem_card_details_bold));
        }

        //last update date
        if (lastUpdateDateTextView != null) {
            Date lastUpdateDate = feedItem.getUpdatedTime();
            lastUpdateDateTextView.setText(Utils.formatLastUpdateDate(lastUpdateDate, context));
            lastUpdateDateTextView.setTypeface(null, feedItem.getBadgeCount() == 0 ? Typeface.NORMAL : Typeface.BOLD);
            lastUpdateDateTextView.setTextColor(feedItem.getBadgeCount() == 0 ? res.getColor(R.color.feeditem_card_details_normal) : res.getColor(R.color.feeditem_card_details_bold));
        }

    }

    protected boolean showCategoryIcon() {
        return true;
    }

    //--------------------------
    // PICASSO TARGET IMPLEMENTATION
    //--------------------------

    @Override
    public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
        int targetWidth = itemView.getResources().getDimensionPixelOffset(R.dimen.feeditem_icon_width);
        int targetHeight = itemView.getResources().getDimensionPixelOffset(R.dimen.feeditem_icon_height);
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false));
        tourTitle.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
        tourTitle.setCompoundDrawablesWithIntrinsicBounds(errorDrawable, null, null, null);
    }

    @Override
    public void onPrepareLoad(final Drawable placeHolderDrawable) {

    }

    //--------------------------
    // INNER CLASSES
    //--------------------------

    private class OnClickListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            if (feedItem == null) return;
            if (v == photoView || v == tourAuthor) {
                if (feedItem.getAuthor() != null) {
                    BusProvider.getInstance().post(new Events.OnUserViewRequestedEvent(feedItem.getAuthor().getUserID()));
                }
            }
            else if (v == actButton) {
                String joinStatus = feedItem.getJoinStatus();
                if (Tour.JOIN_STATUS_PENDING.equals(joinStatus)) {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_PENDING_OVERLAY);
                    BusProvider.getInstance().post(new Events.OnFeedItemCloseRequestEvent(feedItem));
                } else if (Tour.JOIN_STATUS_ACCEPTED.equals(joinStatus)) {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_OPEN_ACTIVE_OVERLAY);
                    BusProvider.getInstance().post(new Events.OnFeedItemCloseRequestEvent(feedItem));
                } else if (Tour.JOIN_STATUS_REJECTED.equals(joinStatus)) {
                    //TODO: What to do on rejected status ?
                } else {
                    // The server wants the position starting with 1
                    BusProvider.getInstance().post(new Events.OnFeedItemInfoViewRequestedEvent(feedItem, getAdapterPosition()+1));
                }

            }
            else if (v == itemView) {
                if (viewHolderListener != null) {
                    viewHolderListener.onViewHolderDetailsClicked(0);
                }
                // The server wants the position starting with 1
                BusProvider.getInstance().post(new Events.OnFeedItemInfoViewRequestedEvent(feedItem, getAdapterPosition()+1));
            }
        }

    }

}
