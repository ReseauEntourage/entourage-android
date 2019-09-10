package social.entourage.android.entourage.information.discussion;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import social.entourage.android.R;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.BaseCardViewHolder;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.view.HtmlTextView;

/**
 * {@link BaseCardViewHolder} subclass, used to display outing information received as chat message,
 * in the feed item details screen<br/><br/>
 * Created by Mihai Ionescu on 30/07/2018.
 */
public class OutingCardViewHolder extends BaseCardViewHolder {

    private HtmlTextView mAuthorTextView;
    private TextView mTitleTextView;
    private TextView mAddressTextView;
    private TextView mDateTextView;
    private Button mViewButton;

    private String outingUUID;

    public OutingCardViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {
        mAuthorTextView = itemView.findViewById(R.id.tic_outing_author);
        mTitleTextView = itemView.findViewById(R.id.tic_outing_title);
        mAddressTextView = itemView.findViewById(R.id.tic_outing_address);
        mDateTextView = itemView.findViewById(R.id.tic_outing_date);
        mViewButton = itemView.findViewById(R.id.tic_outing_view_button);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (outingUUID == null || outingUUID.length() == 0) return;
                BusProvider.getInstance().post(new Events.OnFeedItemInfoViewRequestedEvent(Entourage.ENTOURAGE_CARD, outingUUID, null));
            }
        };

        itemView.setOnClickListener(onClickListener);
        if (mViewButton != null) mViewButton.setOnClickListener(onClickListener);

    }

    @Override
    public void populate(final TimestampedObject data) {
        populate((ChatMessage)data);
    }

    private void populate(ChatMessage chatMessage) {
        if (chatMessage == null) return;
        ChatMessage.Metadata metadata = chatMessage.getMetadata();
        if (metadata == null) return;
        Context context = itemView.getContext();

        String colorHex = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(context, R.color.action_type_outing)));
        String htmlText = context.getString(
                ChatMessage.Metadata.OPERATION_CREATED.equalsIgnoreCase(metadata.getOperation()) ? R.string.outing_message_card_created_author : R.string.outing_message_card_updated_author,
                chatMessage.getUserName(),
                colorHex);
        mAuthorTextView.setHtmlString(htmlText);
        mAuthorTextView.setMovementMethod(null); // to allow the itemview to handle the click

        mTitleTextView.setText(metadata.getTitle());

        mAddressTextView.setText(metadata.getDisplayAddress());

        if (metadata.getStartsAt() != null) {
            DateFormat df = new SimpleDateFormat(context.getString(R.string.entourage_create_date_format), Locale.getDefault());
            mDateTextView.setText(df.format(metadata.getStartsAt()));
        }

        outingUUID = metadata.getUUID();
    }

    public static int getLayoutResource() {
        return R.layout.tour_information_outing_card_view;
    }
}
