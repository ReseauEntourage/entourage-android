package social.entourage.android.map.tour.TourInformation.discussion;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.model.ChatMessage;

/**
 * Chat Message Card for Tour Information Screen
 */
public class ChatMessageCardViewHolder extends LinearLayout {

    private ImageView mUserPhotoView;
    private TextView mMessageView;

    public ChatMessageCardViewHolder(Context context) {
        super(context, null, R.attr.ChatMessageViewStyle);
        init(null, 0, null);
    }

    public ChatMessageCardViewHolder(Context context, ChatMessage chatMessage) {
        super(context, null, R.attr.ChatMessageViewStyle);
        init(null, 0, chatMessage);
    }

    public ChatMessageCardViewHolder(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.ChatMessageViewStyle);
        init(attrs, 0, null);
    }

    public ChatMessageCardViewHolder(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle, null);
    }

    private void init(AttributeSet attrs, int defStyle, ChatMessage chatMessage) {

        int gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        int cardViewId = R.layout.tour_information_chat_message_others_card_view;
        if (chatMessage != null) {
            if (chatMessage.isMe()) {
                cardViewId = R.layout.tour_information_chat_message_me_card_view;
                gravity = Gravity.END | Gravity.CENTER_VERTICAL;
            }
        }
        setGravity(gravity);

        inflate(getContext(), cardViewId, this);

        mUserPhotoView = (ImageView)findViewById(R.id.tic_chat_user_photo);
        mMessageView = (TextView)findViewById(R.id.tic_chat_message);

        if (chatMessage != null) {
            populate(chatMessage);
        }
    }

    public void populate(ChatMessage chatMessage) {
        String avatarURL = chatMessage.getUserAvatarURL();
        if (avatarURL != null) {
            Picasso.with(getContext()).load(Uri.parse(avatarURL))
                    .transform(new CropCircleTransformation())
                    .into(mUserPhotoView);
        }

        mMessageView.setText(chatMessage.getContent());
    }
}
