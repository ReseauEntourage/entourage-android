package social.entourage.android.base;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.map.tour.information.discussion.ViewHolderFactory;

/**
 * Created by mihaiionescu on 05/05/16.
 */
public class EntourageBaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected List<TimestampedObject> items = new ArrayList<>();

    protected ViewHolderFactory viewHolderFactory = new ViewHolderFactory();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {

        return viewHolderFactory.getViewHolder(parent, viewType);
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

    public List<TimestampedObject> getItems() {
        return items;
    }

    public void addItems(List<TimestampedObject> addItems) {
        int positionStart = items.size()-1;
        for (int i = 0; i < addItems.size(); i++) {
            addCardInfo(addItems.get(i), false);
        }
        int positionEnd = items.size()-1;
        notifyItemRangeInserted(positionStart, positionEnd-positionStart);
    }

    public void addCardInfo(TimestampedObject cardInfo) {
        addCardInfo(cardInfo, true);
    }

    public void addCardInfo(TimestampedObject cardInfo, boolean notifyView) {
        items.add(cardInfo);
        if (notifyView) {
            notifyItemInserted(items.size()-1);
        }
    }

    public void insertCardInfo(TimestampedObject cardInfo, int position) {
        //add the card
        items.add(position, cardInfo);
        notifyItemInserted(position);
    }

    public synchronized void addCardInfoAfterTimestamp(TimestampedObject cardInfo) {
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
        addCardInfo(cardInfo, true);
    }

    public void addCardInfoBeforeTimestamp(TimestampedObject cardInfo) {
        //search for the insert point
        for (int i = 0; i < items.size(); i++) {
            TimestampedObject timestampedObject = items.get(i);
            if (timestampedObject.getTimestamp() != null) {
                if (timestampedObject.getTimestamp().before(cardInfo.getTimestamp())) {
                    //we found the insert point
                    insertCardInfo(cardInfo, i);
                    return;
                }
            }
        }
        //not found, add it at the end of the list
        addCardInfo(cardInfo, true);
    }

    public TimestampedObject getCardAt(int position) {
        if (position < 0 || position >= items.size()) return null;
        return items.get(position);
    }

    public TimestampedObject findCard(TimestampedObject card) {
        if (card == null) {
            return null;
        }
        for (int i = 0; i < items.size(); i++) {
            TimestampedObject timestampedObject = items.get(i);
            if (timestampedObject.equals(card)) {
                return timestampedObject;
            }
        }
        return null;
    }

    public TimestampedObject findCard(int type, long id) {
        for (int i = 0; i < items.size(); i++) {
            TimestampedObject timestampedObject = items.get(i);
            if (timestampedObject.getType() == type && timestampedObject.getId() == id) {
                return timestampedObject;
            }
        }
        return null;
    }

    public void updateCard(TimestampedObject card) {
        if (card == null) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            TimestampedObject timestampedObject = items.get(i);
            if (timestampedObject.equals(card)) {
                card.copyLocalFields(timestampedObject);
                items.remove(i);
                items.add(i, card);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void removeCard(TimestampedObject card) {
        if (card == null) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            TimestampedObject timestampedObject = items.get(i);
            if (timestampedObject.equals(card)) {
                items.remove(i);
                notifyItemRangeRemoved(i, 1);
                return;
            }
        }

    }

    public void removeAll() {
        items.clear();
        notifyDataSetChanged();
    }
}
