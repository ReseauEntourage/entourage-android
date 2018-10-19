package social.entourage.android.map.tour.information.discussion;

import java.util.Date;
import java.util.HashMap;

import social.entourage.android.base.EntourageBaseAdapter;
import social.entourage.android.api.model.TimestampedObject;

/**
 * Created by mihaiionescu on 02/03/16.
 */
public class DiscussionAdapter extends EntourageBaseAdapter {

    private HashMap<Date, Integer> mSections = new HashMap<>();

    public DiscussionAdapter() {

        viewHolderFactory.registerViewHolder(
                TimestampedObject.DATE_SEPARATOR,
                new ViewHolderFactory.ViewHolderType(DateSeparatorViewHolder.class, DateSeparatorViewHolder.getLayoutResource())
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
        viewHolderFactory.registerViewHolder(
                TimestampedObject.CHAT_MESSAGE_OUTING,
                new ViewHolderFactory.ViewHolderType(OutingCardViewHolder.class, OutingCardViewHolder.getLayoutResource())
        );
        viewHolderFactory.registerViewHolder(
                TimestampedObject.STATUS_UPDATE_CARD,
                new ViewHolderFactory.ViewHolderType(StatusCardViewHolder.class, StatusCardViewHolder.getLayoutResource())
        );

        setHasStableIds(false);
    }

    private void addDateSeparator(Date date, boolean notifyView) {
        DateSeparator separator = new DateSeparator();
        separator.setDate(date);
        items.add(separator);
        mSections.put(date, items.size()-1);
        if (notifyView) {
            notifyItemInserted(items.size() - 1);
        }
    }

    private void insertDateSeparator(Date date, int position) {
        DateSeparator separator = new DateSeparator();
        separator.setDate(date);
        items.add(position, separator);
        mSections.put(date, position);
        notifyItemInserted(position);
    }

    @Override
    public void addCardInfo(TimestampedObject cardInfo, boolean notifyView) {
        Date timestamp = cardInfo.getTimestamp();
        if (timestamp != null) {
            Date dateOnly = new Date(0);
            dateOnly.setYear(timestamp.getYear());
            dateOnly.setMonth(timestamp.getMonth());
            dateOnly.setDate(timestamp.getDate());
            if (!mSections.containsKey(dateOnly)) {
                addDateSeparator(dateOnly, notifyView);
            }
        }
        items.add(cardInfo);
        if (notifyView) {
            notifyItemInserted(items.size()-1);
        }
    }

    @Override
    public void insertCardInfo(TimestampedObject cardInfo, int position) {
        Date timestamp = cardInfo.getTimestamp();
        if (timestamp != null) {
            Date dateOnly = new Date(0);
            dateOnly.setYear(timestamp.getYear());
            dateOnly.setMonth(timestamp.getMonth());
            dateOnly.setDate(timestamp.getDate());
            if (!mSections.containsKey(dateOnly)) {
                insertDateSeparator(dateOnly, position);
                position++;
            }
        }
        //add the card
        items.add(position, cardInfo);
        notifyItemInserted(position);
    }

}
