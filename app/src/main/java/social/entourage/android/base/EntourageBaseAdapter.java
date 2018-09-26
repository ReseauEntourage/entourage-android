package social.entourage.android.base;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;

import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.map.MapTabItem;
import social.entourage.android.map.MapViewHolder;
import social.entourage.android.map.tour.information.discussion.ViewHolderFactory;
import social.entourage.android.newsfeed.NewsfeedBottomViewHolder;

/**
 * Created by mihaiionescu on 05/05/16.
 */
public class EntourageBaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected List<TimestampedObject> items = new ArrayList<>();

    protected ViewHolderFactory viewHolderFactory = new ViewHolderFactory();

    protected EntourageViewHolderListener viewHolderListener;

    protected boolean needsBottomView = false;
    private boolean showBottomView = false;
    private int bottomViewContentType;
    private MapTabItem selectedTab = MapTabItem.ALL_TAB;

    protected boolean needsTopView = false;
    private MapViewHolder mapViewHolder;
    private OnMapReadyCallback onMapReadyCallback;
    private View.OnClickListener onFollowButtonClickListener;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {

        BaseCardViewHolder cardViewHolder = viewHolderFactory.getViewHolder(parent, viewType);
        if (cardViewHolder != null) {
            cardViewHolder.setViewHolderListener(viewHolderListener);
        }

        if (viewType == TimestampedObject.TOP_VIEW) {
            mapViewHolder = (MapViewHolder)cardViewHolder;
            mapViewHolder.setMapReadyCallback(onMapReadyCallback);
            mapViewHolder.setFollowButtonOnClickListener(onFollowButtonClickListener);
        }

        return cardViewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (position == 0 && needsTopView) {
            MapViewHolder mapViewHolder = ((MapViewHolder)holder);
            mapViewHolder.populate(null);
            return;
        }
        if (position == getItemCount() - 1 && needsBottomView) {
            // map the actual content type using the selected tab
            int contentType = bottomViewContentType + NewsfeedBottomViewHolder.CONTENT_TYPES * selectedTab.getId();
            ((BottomViewHolder)holder).populate(showBottomView, contentType);
            return;
        }
        ((BaseCardViewHolder)holder).populate(items.get(position - (needsTopView ? 1 : 0)));
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size() + (needsTopView ? 1 : 0) + (needsBottomView ? 1 : 0); // +1 for the loader
    }

    public int getDataItemCount() {
        if (items == null) return 0;
        return items.size();
    }

    @Override
    public int getItemViewType(final int position) {
        if (position == 0 && needsTopView) {
            return TimestampedObject.TOP_VIEW;
        }
        if (position == getItemCount() - 1 && needsBottomView) {
            return TimestampedObject.BOTTOM_VIEW;
        }
        return items.get(position - (needsTopView ? 1 : 0)).getType();
    }

    public List<TimestampedObject> getItems() {
        return items;
    }

    public void addItems(List<TimestampedObject> addItems) {
        int positionStart = items.size() == 0 ? (needsTopView ? 1 : 0 ) : items.size()-1 + (needsTopView ? 1 : 0);
        for (int i = 0; i < addItems.size(); i++) {
            addCardInfo(addItems.get(i), false);
        }
        notifyItemRangeInserted(positionStart, addItems.size());
    }

    public void addCardInfo(TimestampedObject cardInfo) {
        addCardInfo(cardInfo, true);
    }

    public void addCardInfo(TimestampedObject cardInfo, boolean notifyView) {
        items.add(cardInfo);
        if (notifyView) {
            notifyItemInserted(items.size()-1 + (needsTopView ? 1 : 0));
        }
    }

    public void insertCardInfo(TimestampedObject cardInfo, int position) {
        //add the card
        items.add(position, cardInfo);
        notifyItemInserted(position + (needsTopView ? 1 : 0));
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
                notifyItemChanged(i + (needsTopView ? 1 : 0));
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
                notifyItemRangeRemoved(i + (needsTopView ? 1 : 0), 1);
                return;
            }
        }

    }

    public void removeAll() {
        int oldCount = getDataItemCount();
        if (oldCount == 0) return;
        items.clear();
        notifyItemRangeRemoved(needsTopView ? 1 : 0, oldCount);
    }

    public MapViewHolder getMapViewHolder() {
        return mapViewHolder;
    }

    public void setOnMapReadyCallback(final OnMapReadyCallback onMapReadyCallback) {
        this.onMapReadyCallback = onMapReadyCallback;
    }

    public void setOnFollowButtonClickListener(final View.OnClickListener onFollowButtonClickListener) {
        this.onFollowButtonClickListener = onFollowButtonClickListener;
    }

    public void setMapHeight(int height) {
        if (mapViewHolder == null || !needsTopView) return;
        mapViewHolder.setHeight(height);
    }

    public void setTabVisibility(int visibility) {
        if (mapViewHolder == null || !needsTopView) return;
        mapViewHolder.setTabVisibility(visibility);
    }

    public void setSelectedTab(MapTabItem selectedTab) {
        this.selectedTab = selectedTab;
        if (mapViewHolder == null || !needsTopView) return;
        mapViewHolder.setSelectedTab(selectedTab);
    }

    public void showBottomView(final boolean showBottomView, int bottomViewContentType, MapTabItem selectedTab) {
        this.showBottomView = showBottomView;
        this.bottomViewContentType = bottomViewContentType;
        this.selectedTab = selectedTab;
        if (items != null && needsBottomView) {
            notifyItemChanged(items.size() + (needsTopView ? 1 : 0));
        }
    }

    public boolean isShowBottomView() {
        return showBottomView;
    }

    public EntourageViewHolderListener getViewHolderListener() {
        return viewHolderListener;
    }

    public void setViewHolderListener(final EntourageViewHolderListener viewHolderListener) {
        this.viewHolderListener = viewHolderListener;
    }

}
