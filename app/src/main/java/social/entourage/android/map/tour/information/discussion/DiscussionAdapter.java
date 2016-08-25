package social.entourage.android.map.tour.information.discussion;

import social.entourage.android.base.EntourageBaseAdapter;
import social.entourage.android.api.model.TimestampedObject;

/**
 * Created by mihaiionescu on 02/03/16.
 */
public class DiscussionAdapter extends EntourageBaseAdapter {

    public DiscussionAdapter() {

        viewHolderFactory.registerViewHolder(
                TimestampedObject.SEPARATOR,
                new ViewHolderFactory.ViewHolderType(SeparatorCardViewHolder.class, SeparatorCardViewHolder.getLayoutResource())
        );
        viewHolderFactory.registerViewHolder(
                TimestampedObject.CHAT_MESSAGE_OTHER,
                new ViewHolderFactory.ViewHolderType(ChatMessageCardViewHolder.class, ChatMessageCardViewHolder.getLayoutResource())
        );
        viewHolderFactory.registerViewHolder(
                TimestampedObject.CHAT_MESSAGE_ME,
                new ViewHolderFactory.ViewHolderType(ChatMessageMeCardViewHolder.class, ChatMessageMeCardViewHolder.getLayoutResource())
        );
        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOUR_STATUS,
                new ViewHolderFactory.ViewHolderType(LocationCardViewHolder.class, LocationCardViewHolder.getLayoutResource())
        );
        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOUR_USER_JOIN,
                new ViewHolderFactory.ViewHolderType(UserJoinCardViewHolder.class, UserJoinCardViewHolder.getLayoutResource())
        );
        viewHolderFactory.registerViewHolder(
                TimestampedObject.ENCOUNTER,
                new ViewHolderFactory.ViewHolderType(EncounterCardViewHolder.class, EncounterCardViewHolder.getLayoutResource())
        );

        setHasStableIds(false);
    }

    private void addSeparator() {
        Separator separator = new Separator();
        items.add(separator);
        notifyItemInserted(items.size()-1);
    }

    private void insertSeparator(int position) {
        Separator separator = new Separator();
        items.add(position, separator);
        notifyItemInserted(position);
    }

    @Override
    public void addCardInfo(TimestampedObject cardInfo) {
        if (items.size() > 0) {
            addSeparator();
        }
        items.add(cardInfo);
        notifyItemInserted(items.size()-1);
    }

    @Override
    public void insertCardInfo(TimestampedObject cardInfo, int position) {
        if (position != 0) position--;
        //add separator if not adding at the top of the list
        if (position != 0) {
            insertSeparator(position);
            position++;
        }
        //add the card
        items.add(position, cardInfo);
        notifyItemInserted(position);
        //add the separator if adding at the top of the list and more cards exist
        if (position == 0 && items.size() > 1) {
            position++;
            insertSeparator(position);
        }
    }

}
