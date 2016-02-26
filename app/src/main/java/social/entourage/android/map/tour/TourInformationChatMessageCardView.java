package social.entourage.android.map.tour;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import social.entourage.android.R;
import social.entourage.android.api.model.ChatMessage;

/**
 * Chat Message Card for Tour Information Screen
 */
public class TourInformationChatMessageCardView extends LinearLayout {

    private ImageView mUserPhotoView;
    private TextView mMessageView;

    public TourInformationChatMessageCardView(Context context) {
        super(context, null, R.attr.TourInformationCardViewStyle);
        init(null, 0);
    }

    public TourInformationChatMessageCardView(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.TourInformationCardViewStyle);
        init(attrs, 0);
    }

    public TourInformationChatMessageCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        inflate(getContext(), R.layout.tour_information_chat_message_card_view, this);

        mUserPhotoView = (ImageView)findViewById(R.id.tic_chat_user_photo);
        mMessageView = (TextView)findViewById(R.id.tic_chat_message);

    }

    public void populate(ChatMessage chatMessage) {
        //TODO: we need the user photo

        mMessageView.setText(chatMessage.getContent());
    }
}
