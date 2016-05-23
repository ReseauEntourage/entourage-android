package social.entourage.android.map.tour.information.discussion;

import social.entourage.android.base.EntourageBaseAdapter;
import social.entourage.android.api.model.TimestampedObject;

/**
 * Created by mihaiionescu on 02/03/16.
 */
public class DiscussionAdapter extends EntourageBaseAdapter {

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
                TimestampedObject.TOUR_USER_JOIN,
                new ViewHolderFactory.ViewHolderType(UserJoinCardViewHolder.class, UserJoinCardViewHolder.getLayoutResource())
        );
        ViewHolderFactory.registerViewHolder(
                TimestampedObject.ENCOUNTER,
                new ViewHolderFactory.ViewHolderType(EncounterCardViewHolder.class, EncounterCardViewHolder.getLayoutResource())
        );

        setHasStableIds(false);
    }

    private void addSeparator() {
        Separator separator = new Separator();
        items.add(separator);
    }

    private void insertSeparator(int position) {
        Separator separator = new Separator();
        items.add(position, separator);
    }

    @Override
    public void addCardInfo(TimestampedObject cardInfo) {
        if (items.size() > 0) {
            addSeparator();
        }
        items.add(cardInfo);
    }

    @Override
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

}
