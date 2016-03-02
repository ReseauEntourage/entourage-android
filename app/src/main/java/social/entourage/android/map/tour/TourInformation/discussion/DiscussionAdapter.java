package social.entourage.android.map.tour.TourInformation.discussion;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import social.entourage.android.api.model.TimestampedObject;

/**
 * Created by mihaiionescu on 02/03/16.
 */
public class DiscussionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<TimestampedObject> items = new ArrayList<>();

    public DiscussionAdapter() {

        ViewHolderFactory.registerViewHolder(
                TimestampedObject.SEPARATOR,
                new ViewHolderFactory.ViewHolderType(SeparatorCardViewHolder.class, SeparatorCardViewHolder.getLayoutResource())
        );
        ViewHolderFactory.registerViewHolder(
                TimestampedObject.CHAT_MESSAGE_OTHER,
                new ViewHolderFactory.ViewHolderType(ChatMessageCardViewHolder.class, ChatMessageCardViewHolder.getLayoutResource())
        );
        ViewHolderFactory.registerViewHolder(
                TimestampedObject.CHAT_MESSAGE_ME,
                new ViewHolderFactory.ViewHolderType(ChatMessageMeCardViewHolder.class, ChatMessageMeCardViewHolder.getLayoutResource())
        );
        ViewHolderFactory.registerViewHolder(
                TimestampedObject.TOUR_STATUS,
                new ViewHolderFactory.ViewHolderType(LocationCardViewHolder.class, LocationCardViewHolder.getLayoutResource())
        );
        ViewHolderFactory.registerViewHolder(
                TimestampedObject.TOUR_USER,
                new ViewHolderFactory.ViewHolderType(UserCardViewHolder.class, UserCardViewHolder.getLayoutResource())
        );
        ViewHolderFactory.registerViewHolder(
                TimestampedObject.ENCOUNTER,
                new ViewHolderFactory.ViewHolderType(EncounterCardViewHolder.class, EncounterCardViewHolder.getLayoutResource())
        );

        setHasStableIds(false);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {

        return ViewHolderFactory.getViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ((BaseCardViewHolder)holder).populate(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return items.get(position).getType();
    }

    private void addSeparator() {
        Separator separator = new Separator();
        items.add(separator);
    }

    private void insertSeparator(int position) {
        Separator separator = new Separator();
        items.add(position, separator);
    }

    public void addItems(List<TimestampedObject> addItems) {
        int positionStart = items.size()-1;
        for (int i = 0; i < addItems.size(); i++) {
            addCardInfo(addItems.get(i));
        }
        int positionEnd = items.size()-1;
    }

    public void addCardInfo(TimestampedObject cardInfo) {
        if (items.size() > 0) {
            addSeparator();
        }
        items.add(cardInfo);
    }

    public void insertCardInfo(TimestampedObject cardInfo, int position) {
        if (position != 0) position--;
        //add separator if not adding at the top of the list
        if (position != 0) {
            insertSeparator(position);
            notifyItemInserted(position);
            position++;
        }
        //add the card
        items.add(position, cardInfo);
        notifyItemInserted(position);
        //add the separator if adding at the top of the list and more cards exist
        if (position == 0 && items.size() > 1) {
            position++;
            insertSeparator(position);
            notifyItemInserted(position);
        }
    }

    public void addCardInfoBeforeTimestamp(TimestampedObject cardInfo) {
        //search for the insert point
        for (int i = 0; i < items.size(); i++) {
            TimestampedObject timestampedObject = items.get(i);
            if (timestampedObject.getTimestamp() != null) {
                if (timestampedObject.getTimestamp().after(cardInfo.getTimestamp())) {
                    //we found the insert point
                    insertCardInfo(cardInfo, i);
                    return;
                }
            }
        }
        //not found, add it at the end of the list
        addCardInfo(cardInfo);
        notifyItemInserted(items.size()-1);
    }
}
