package social.entourage.android.newsfeed;

import android.view.View;
import android.widget.TextView;

import social.entourage.android.R;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.BottomViewHolder;
import social.entourage.android.tools.BusProvider;

/**
 * Created by mihaiionescu on 10/05/2017.
 */

public class NewsfeedBottomViewHolder extends BottomViewHolder {

    public static final int CONTENT_TYPES = 3;

    public static final int CONTENT_TYPE_LOAD_MORE = 0;
    public static final int CONTENT_TYPE_NO_ITEMS = 1;
    public static final int CONTENT_TYPE_NO_MORE_ITEMS = 2;

    public static final int CONTENT_TYPE_LOAD_MORE_EVENTS = CONTENT_TYPE_LOAD_MORE + CONTENT_TYPES;
    public static final int CONTENT_TYPE_NO_ITEMS_EVENTS = CONTENT_TYPE_NO_ITEMS + CONTENT_TYPES;
    public static final int CONTENT_TYPE_NO_MORE_EVENTS = CONTENT_TYPE_NO_MORE_ITEMS + CONTENT_TYPES;

    private View allTabContent;
    private View eventsTabContent;

    private View loadMoreView;
    private TextView loadMoreTextView;
    private TextView noItemsTextView;

    private TextView loadMoreEventsView;

    private int contentType = -1;

    public NewsfeedBottomViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {
        content = itemView.findViewById(R.id.newsfeed_bottom_content);

        allTabContent = itemView.findViewById(R.id.newsfeed_tab_all_content);
        eventsTabContent = itemView.findViewById(R.id.newsfeed_tab_events_content);

        loadMoreView = itemView.findViewById(R.id.newsfeed_load_more_layout);
        noItemsTextView = itemView.findViewById(R.id.newsfeed_no_items);
        loadMoreTextView = itemView.findViewById(R.id.newsfeed_load_more);
        loadMoreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                BusProvider.INSTANCE.getInstance().post(new Events.OnNewsfeedLoadMoreEvent());
            }
        });

        loadMoreEventsView = itemView.findViewById(R.id.newsfeed_tab_events_load_more);
        loadMoreEventsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                BusProvider.INSTANCE.getInstance().post(new Events.OnNewsfeedLoadMoreEvent());
            }
        });
    }

    @Override
    public void populate(final boolean showContent, final int contentType) {
        super.populate(showContent, contentType);
        if (showContent) {
            // switch between content types
            if (this.contentType != contentType) {
                switch (contentType) {
                    case CONTENT_TYPE_LOAD_MORE:
                        loadMoreView.setVisibility(View.VISIBLE);
                        noItemsTextView.setVisibility(View.GONE);
                        break;
                    case CONTENT_TYPE_NO_ITEMS:
                        loadMoreView.setVisibility(View.GONE);
                        noItemsTextView.setVisibility(View.VISIBLE);
                        noItemsTextView.setText(R.string.map_empty_newsfeed);
                        break;
                    case CONTENT_TYPE_NO_MORE_ITEMS:
                        loadMoreView.setVisibility(View.GONE);
                        noItemsTextView.setVisibility(View.VISIBLE);
                        noItemsTextView.setText(R.string.newsfeed_no_more_items);
                        break;
                    default:
                        break;
                }
                allTabContent.setVisibility(contentType <= CONTENT_TYPE_NO_MORE_ITEMS ? View.VISIBLE : View.GONE);
                eventsTabContent.setVisibility(contentType <= CONTENT_TYPE_NO_MORE_ITEMS ? View.GONE : View.VISIBLE);

                this.contentType = contentType;
            }
        }
    }

    public static int getLayoutResource() {
        return R.layout.layout_newsfeed_bottom_card;
    }
}
