package social.entourage.android.map.tour.information.discussion;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.view.View;

import social.entourage.android.R;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.BaseCardViewHolder;
import social.entourage.android.view.HtmlTextView;

/**
 * View Holder that displays information about a change in the status of a feed item
 * Created by Mihai Ionescu on 04/10/2018.
 */
public class StatusCardViewHolder extends BaseCardViewHolder {

    private HtmlTextView messageTextView;

    public StatusCardViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {
        messageTextView = itemView.findViewById(R.id.tic_status_message);
    }

    @Override
    public void populate(final TimestampedObject data) {
        if ( data == null || !(data instanceof ChatMessage) ) return;
        ChatMessage chatMessage = (ChatMessage) data;
        ChatMessage.Metadata metadata = chatMessage.getMetadata();
        if (metadata == null) return;

        Context context = itemView.getContext();

        String colorHex = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(context, R.color.accent)));
        String htmlText = context.getString(
                R.string.status_message_card_details,
                colorHex,
                chatMessage.getUserName(),
                chatMessage.getContent()
                );
        messageTextView.setHtmlString(htmlText);
    }

    public static int getLayoutResource() {
        return R.layout.tour_information_status_card_view;
    }
}
