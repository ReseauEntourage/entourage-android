package social.entourage.android.entourage.information.discussion;

import android.net.Uri;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import social.entourage.android.R;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.BaseCardViewHolder;
import social.entourage.android.deeplinks.DeepLinksManager;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.CropCircleTransformation;
import social.entourage.android.view.PartnerLogoImageView;

/**
 * Chat Message Card for Tour Information Screen
 */
public class ChatMessageCardViewHolder extends BaseCardViewHolder {

    private ImageView mUserPhotoView;
    private PartnerLogoImageView mPartnerLogoView;
    private TextView mUserNameView;
    private TextView mMessageView;
    private TextView mTimestampView;

    private int userId = 0;

    public ChatMessageCardViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {

        mUserPhotoView = itemView.findViewById(R.id.tic_chat_user_photo);
        mUserNameView = itemView.findViewById(R.id.tic_chat_user_name);
        mPartnerLogoView = itemView.findViewById(R.id.tic_chat_user_partner_logo);
        mMessageView = itemView.findViewById(R.id.tic_chat_message);
        mTimestampView = itemView.findViewById(R.id.tic_chat_timestamp);

        mUserPhotoView.setOnClickListener(v -> {
            if (userId == 0) return;
            BusProvider.getInstance().post(new Events.OnUserViewRequestedEvent(userId));
        });

    }

    @Override
    public void populate(final TimestampedObject data) {
        populate((ChatMessage)data);
    }

    public void populate(ChatMessage chatMessage) {
        // user avatar
        String avatarURL = chatMessage.getUserAvatarURL();
        if (avatarURL != null) {
            Picasso.get().load(Uri.parse(avatarURL))
                    .placeholder(R.drawable.ic_user_photo_small)
                    .transform(new CropCircleTransformation())
                    .into(mUserPhotoView);
        } else {
            mUserPhotoView.setImageResource(R.drawable.ic_user_photo_small);
        }
        // Partner logo
        if (mPartnerLogoView != null) {
            String partnerLogoURL = chatMessage.getPartnerLogoSmall();
            if (partnerLogoURL != null) {
                Picasso.get().load(Uri.parse(partnerLogoURL))
                        .placeholder(R.drawable.partner_placeholder)
                        .transform(new CropCircleTransformation())
                        .into(mPartnerLogoView);
            } else {
                mPartnerLogoView.setImageDrawable(null);
            }
        }

        // user name
        if (chatMessage.getUserName() != null) {
            mUserNameView.setText(chatMessage.getUserName());
        }

        // the actual chat
        mMessageView.setText(chatMessage.getContent());

        DeepLinksManager.INSTANCE.linkify(mMessageView);

        // chat timestamp
        mTimestampView.setText(DateFormat.format("H'h'mm", chatMessage.getTimestamp()));

        userId = chatMessage.getUserId();
    }

    public static int getLayoutResource() {
        return R.layout.tour_information_chat_message_others_card_view;
    }
}
