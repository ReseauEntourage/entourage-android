package social.entourage.android.map.tour.information.discussion;

import android.net.Uri;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.Partner;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.BaseCardViewHolder;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.CropCircleTransformation;
import social.entourage.android.tools.Utils;
import social.entourage.android.view.PartnerLogoImageView;

/**
 * User Card View in tour information screen
 */
public class UserJoinCardViewHolder extends BaseCardViewHolder {

    private View mPublicSection;
    private View mPrivateSection;

    private ImageView mPublicPhotoView;
    private PartnerLogoImageView mPublicPartnerLogo;
    private TextView mPublicUsernameView;
    private TextView mJoinStatusView;
    private View mPublicMessageSection;
    private TextView mPublicJoinMessage;
    private TextView mPublicTimestampView;

    private ImageView mPhotoView;
    private PartnerLogoImageView mPartnerLogoView;
    private TextView mPrivateUsernameView;
    private TextView mJoinMessage;
    private TextView mJoinDescription;
    private Button mAcceptButton;
    private Button mRefuseButton;
    private Button mViewProfileButton;

    private int userId;
    private FeedItem feedItem;

    public UserJoinCardViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {

        mPublicSection = itemView.findViewById(R.id.tic_public_info_section);
        mPrivateSection = itemView.findViewById(R.id.tic_private_info_section);

        mPublicPhotoView = (ImageView) itemView.findViewById(R.id.tic_public_info_photo);
        mPublicPartnerLogo = (PartnerLogoImageView) itemView.findViewById(R.id.tic_public_info_partner_logo);
        mPublicUsernameView = (TextView) itemView.findViewById(R.id.tic_public_info_username);
        mJoinStatusView = (TextView) itemView.findViewById(R.id.tic_join_status);
        mPublicJoinMessage = (TextView) itemView.findViewById(R.id.tic_public_join_message);
        mPublicMessageSection = itemView.findViewById(R.id.tic_public_info_message_layout);
        mPublicTimestampView = (TextView) itemView.findViewById(R.id.tic_public_info_timestamp);

        mPhotoView = (ImageView) itemView.findViewById(R.id.tic_photo);
        mPartnerLogoView = (PartnerLogoImageView) itemView.findViewById(R.id.tic_partner_logo);
        mPrivateUsernameView = (TextView) itemView.findViewById(R.id.tic_private_username);
        mJoinDescription = (TextView) itemView.findViewById(R.id.tic_join_description);
        mJoinMessage = (TextView) itemView.findViewById(R.id.tic_join_message);
        mAcceptButton = (Button) itemView.findViewById(R.id.tic_accept_button);
        mRefuseButton = (Button) itemView.findViewById(R.id.tic_refuse_button);
        mViewProfileButton = (Button) itemView.findViewById(R.id.tic_view_profile_button);

        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (userId == 0) return;
                BusProvider.getInstance().post(new Events.OnUserViewRequestedEvent(userId));
            }
        });

        mPartnerLogoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (userId == 0) return;
                BusProvider.getInstance().post(new Events.OnUserViewRequestedEvent(userId));
            }
        });

        mPublicPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (userId == 0) return;
                BusProvider.getInstance().post(new Events.OnUserViewRequestedEvent(userId));
            }
        });

        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (userId == 0 || feedItem == null) return;
                EntourageEvents.logEvent(Constants.EVENT_JOIN_REQUEST_ACCEPT);
                BusProvider.getInstance().post(
                        new Events.OnUserJoinRequestUpdateEvent(
                                userId,
                                FeedItem.JOIN_STATUS_ACCEPTED,
                                feedItem)
                );
            }
        });

        mRefuseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (userId == 0 || feedItem == null) return;
                EntourageEvents.logEvent(Constants.EVENT_JOIN_REQUEST_REJECT);
                BusProvider.getInstance().post(
                        new Events.OnUserJoinRequestUpdateEvent(
                                userId,
                                FeedItem.JOIN_STATUS_REJECTED,
                                feedItem)
                );
            }
        });

        if (mViewProfileButton != null) {
            mViewProfileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (userId == 0) return;
                    BusProvider.getInstance().post(new Events.OnUserViewRequestedEvent(userId));
                }
            });
        }
    }

    @Override
    public void populate(final TimestampedObject data) {
        this.populate((TourUser)data);
    }

    public void populate(TourUser user) {

        if (user.getDisplayName() == null || user.getStatus() == null) return;

        userId = user.getUserId();
        feedItem = user.getFeedItem();

        if (user.getStatus().equals(FeedItem.JOIN_STATUS_PENDING)) {
            populatePendingStatus(user);
        } else {
            populateJoinedStatus(user);
        }
    }

    private void populatePendingStatus(TourUser user) {
        mPrivateSection.setVisibility(View.VISIBLE);
        mPublicSection.setVisibility(View.GONE);

        mPrivateUsernameView.setText(user.getDisplayName());

        String avatarURL = user.getAvatarURLAsString();
        if (avatarURL != null) {
            Picasso.with(itemView.getContext()).load(Uri.parse(avatarURL))
                    .placeholder(R.drawable.ic_user_photo_small)
                    .transform(new CropCircleTransformation())
                    .into(mPhotoView);
        } else {
            mPhotoView.setImageResource(R.drawable.ic_user_photo_small);
        }

        // Partner logo
        Partner partner = user.getPartner();
        if (partner != null) {
            String partnerLogoURL = partner.getSmallLogoUrl();
            if (partnerLogoURL != null) {
                Picasso.with(itemView.getContext())
                        .load(Uri.parse(partnerLogoURL))
                        .placeholder(R.drawable.partner_placeholder)
                        .transform(new CropCircleTransformation())
                        .into(mPartnerLogoView);
            }
            else {
                mPartnerLogoView.setImageDrawable(null);
            }
        } else {
            mPartnerLogoView.setImageDrawable(null);
        }

        mJoinDescription.setText(getJoinStatus(user.getStatus(), user.getFeedItem().getType()==TimestampedObject.TOUR_CARD));

        mJoinMessage.setText(user.getMessage());

        // If we are not the creators of the entourage, hide the Accept and Refuse buttons
        User me = EntourageApplication.me(itemView.getContext());
        boolean isMyEntourage = false;
        if (me != null && feedItem != null && feedItem.getAuthor() != null) {
            isMyEntourage = me.getId() == feedItem.getAuthor().getUserID();
        }

        mAcceptButton.setVisibility(isMyEntourage ? View.VISIBLE : View.GONE);
        mRefuseButton.setVisibility(isMyEntourage ? View.VISIBLE : View.GONE);
    }

    private void populateJoinedStatus(TourUser user) {
        mPrivateSection.setVisibility(View.GONE);
        mPublicSection.setVisibility(View.VISIBLE);

        mPublicUsernameView.setText(user.getDisplayName());

        String avatarURL = user.getAvatarURLAsString();
        if (avatarURL != null) {
            Picasso.with(itemView.getContext()).load(Uri.parse(avatarURL))
                    .placeholder(R.drawable.ic_user_photo_small)
                    .transform(new CropCircleTransformation())
                    .into(mPublicPhotoView);
        } else {
            mPublicPhotoView.setImageResource(R.drawable.ic_user_photo_small);
        }

        // Partner logo
        Partner partner = user.getPartner();
        if (partner != null) {
            String partnerLogoURL = partner.getSmallLogoUrl();
            if (partnerLogoURL != null) {
                Picasso.with(itemView.getContext())
                        .load(Uri.parse(partnerLogoURL))
                        .placeholder(R.drawable.partner_placeholder)
                        .transform(new CropCircleTransformation())
                        .into(mPublicPartnerLogo);
            }
            else {
                mPublicPartnerLogo.setImageDrawable(null);
            }
        } else {
            mPublicPartnerLogo.setImageDrawable(null);
        }

        String joinStatus = getJoinStatus(user.getStatus(), user.getFeedItem().getType()==TimestampedObject.TOUR_CARD);

        mJoinStatusView.setText(Utils.fromHtml(itemView.getContext().getString(R.string.tour_info_text_join_html, user.getDisplayName(), joinStatus)), TextView.BufferType.SPANNABLE);

        String joinMessage = user.getMessage();
        String userStatus = user.getStatus();
        if (Tour.JOIN_STATUS_ACCEPTED.equals(userStatus) || Tour.JOIN_STATUS_CANCELLED.equals(userStatus)) {
            if (joinMessage != null && joinMessage.length() > 0) {
                mPublicMessageSection.setVisibility(View.VISIBLE);
                mPublicJoinMessage.setText(joinMessage);
                mPublicTimestampView.setText(DateFormat.format("H'h'm", user.getTimestamp()));
            } else {
                mPublicMessageSection.setVisibility(View.GONE);
            }
        } else {
            mPublicMessageSection.setVisibility(View.GONE);
        }

    }

    private String getJoinStatus(String joinStatus, boolean isTour) {
        switch (joinStatus) {
            case Tour.JOIN_STATUS_ACCEPTED:
                String joinString = itemView.getContext().getString(isTour ? R.string.tour_info_text_join_accepted : R.string.entourage_info_text_join_accepted);
                if (isTour) {
                    joinString = joinString+ (feedItem != null && feedItem.getAuthor() != null ? feedItem.getAuthor().getUserName() : "");
                }
                return joinString;
            case Tour.JOIN_STATUS_REJECTED:
                return itemView.getContext().getString(R.string.tour_info_text_join_rejected);
            case Tour.JOIN_STATUS_PENDING:
                return itemView.getContext().getString(isTour ? R.string.tour_join_request_received_message_short : R.string.entourage_join_request_received_message_short);
            case Tour.JOIN_STATUS_CANCELLED:
                return itemView.getContext().getString(isTour ? R.string.tour_info_text_join_cancelled_tour : R.string.tour_info_text_join_cancelled_entourage);
            case Tour.JOIN_STATUS_QUITED:
                return itemView.getContext().getString(isTour ? R.string.tour_info_text_join_quited_tour : R.string.tour_info_text_join_quited_entourage);
            default:
                return "";
        }
    }

    public static int getLayoutResource() {
        return R.layout.tour_information_user_join_card_view;
    }

}
