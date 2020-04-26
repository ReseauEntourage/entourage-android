package social.entourage.android.base;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.FeedItem;
import timber.log.Timber;

/**
 * Created by mihaiionescu on 05/05/16.
 */
public class EntourageBaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected List<TimestampedObject> items = new ArrayList<>();

    protected ViewHolderFactory viewHolderFactory = new ViewHolderFactory();

    protected EntourageViewHolderListener viewHolderListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {

        BaseCardViewHolder cardViewHolder = viewHolderFactory.getViewHolder(parent, viewType);
        cardViewHolder.setViewHolderListener(viewHolderListener);
        return cardViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if(position<getPositionOffset()) {
            Timber.e("Adapter trying to populate Top View");
            return;
        }
        ((BaseCardViewHolder)holder).populate(items.get(position - getPositionOffset()));
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size() + getPositionOffset();
    }

    public int getDataItemCount() {
        if (items == null) return 0;
        return items.size();
    }

    @Override
    public int getItemViewType(final int position) {
        if(position<getPositionOffset()) {
            return TimestampedObject.TOP_VIEW;
        }
        return items.get(position - getPositionOffset()).getType();
    }

    public List<TimestampedObject> getItems() {
        return items;
    }

    public void addItems(List<TimestampedObject> addItems) {
        int positionStart = (items.size() == 0 ? 0 : items.size()-1) + getPositionOffset();
        for (TimestampedObject to : addItems) {
            addCardInfo(to, false);
        }
        notifyItemRangeInserted(positionStart, addItems.size());
    }

    public void addCardInfo(TimestampedObject cardInfo) {
        addCardInfo(cardInfo, true);
    }

    public void addCardInfo(TimestampedObject cardInfo, boolean notifyView) {
        items.add(cardInfo);
        if (notifyView) {
            notifyItemInserted(items.size()-1 + getPositionOffset());
        }
    }

    public void insertCardInfo(TimestampedObject cardInfo, int position) {
        //add the card
        items.add(position, cardInfo);
        notifyItemInserted(position + getPositionOffset());
    }

    public synchronized void addCardInfoAfterTimestamp(TimestampedObject cardInfo) {
        //search for the insert point
        for (int i = 0; i < items.size(); i++) {
            TimestampedObject timestampedObject = items.get(i);
            if (timestampedObject.getTimestamp() != null && cardInfo.getTimestamp() != null) {
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
            if (timestampedObject.getTimestamp() != null && cardInfo.getTimestamp() != null) {
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
        for (TimestampedObject timestampedObject : items) {
            if (timestampedObject.equals(card)) {
                return timestampedObject;
            }
        }
        return null;
    }

    public TimestampedObject findCard(int type, long id) {
        for (TimestampedObject timestampedObject : items) {
            if (timestampedObject.getType() == type && timestampedObject.getId() == id) {
                return timestampedObject;
            }
        }
        return null;
    }

    public TimestampedObject findCard(int type, String uuid) {
        for (int i = 0; i < items.size(); i++) {
            TimestampedObject timestampedObject = items.get(i);
            if (timestampedObject.getType() == type && timestampedObject instanceof FeedItem) {
                FeedItem feedItem = (FeedItem) timestampedObject;
                if (feedItem.getUUID().equalsIgnoreCase(uuid)) {
                    return timestampedObject;
                }
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
                notifyItemChanged(i + getPositionOffset());
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
                notifyItemRangeRemoved(i + getPositionOffset(), 1);
                return;
            }
        }

    }

    public void removeAll() {
        int oldCount = getDataItemCount();
        if (oldCount == 0) return;
        items.clear();
        notifyItemRangeRemoved(getPositionOffset(), oldCount);
    }

    public EntourageViewHolderListener getViewHolderListener() {
        return viewHolderListener;
    }

    public void setViewHolderListener(final EntourageViewHolderListener viewHolderListener) {
        this.viewHolderListener = viewHolderListener;
    }

    protected int getPositionOffset() {
        return 0;
    }

}
