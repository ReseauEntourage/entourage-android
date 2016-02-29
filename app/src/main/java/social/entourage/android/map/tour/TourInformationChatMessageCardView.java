package social.entourage.android.map.tour;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.model.ChatMessage;

/**
 * Chat Message Card for Tour Information Screen
 */
public class TourInformationChatMessageCardView extends LinearLayout {

    private ImageView mUserPhotoView;
    private TextView mMessageView;

    public TourInformationChatMessageCardView(Context context) {
        super(context, null, R.attr.ChatMessageViewStyle);
        init(null, 0, null);
    }

    public TourInformationChatMessageCardView(Context context, ChatMessage chatMessage) {
        super(context, null, R.attr.ChatMessageViewStyle);
        init(null, 0, chatMessage);
    }

    public TourInformationChatMessageCardView(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.ChatMessageViewStyle);
        init(attrs, 0, null);
    }

    public TourInformationChatMessageCardView(Context context, AttributeSet attrs, int defStyle) {
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
        //TODO: we need the user photo

        mMessageView.setText(chatMessage.getContent());
    }
}
