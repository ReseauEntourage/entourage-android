package social.entourage.android.api.model.map;

import android.content.Context;
import android.location.Address;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import social.entourage.android.EntourageApplication;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.User;

/**
 * Created by mihaiionescu on 18/05/16.
 */
public abstract class FeedItem extends TimestampedObject implements Serializable {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final long serialVersionUID = 6130079334883067122L;

    public static final String KEY_FEEDITEM = "social.entourage.android.KEY_FEEDITEM";
    public static final String KEY_FEEDITEM_ID = "social.entourage.android.KEY_FEEDITEM_ID";
    public static final String KEY_FEEDITEM_TYPE = "social.entourage.android.KEY_FEEDITEM_TYPE";

    public static final String STATUS_OPEN = "open";
    public static final String STATUS_CLOSED = "closed";
    public static final String STATUS_ON_GOING = "ongoing";
    public static final String STATUS_FREEZED = "freezed";

    public static final String JOIN_STATUS_NOT_REQUESTED = "not_requested";
    public static final String JOIN_STATUS_PENDING = "pending";
    public static final String JOIN_STATUS_ACCEPTED = "accepted";
    public static final String JOIN_STATUS_REJECTED = "rejected";
    public static final String JOIN_STATUS_CANCELLED = "cancelled";
    public static final String JOIN_STATUS_QUITED = "quited"; // This status does not exist on server, just locally

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @Expose(serialize = false, deserialize = true)
    protected long id;

    protected String status;

    protected TourAuthor author;

    @SerializedName("updated_at")
    protected Date updatedTime;

    @Expose(serialize = false, deserialize = false)
    protected transient Address startAddress;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("number_of_people")
    protected int numberOfPeople;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("join_status")
    protected String joinStatus;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("last_message")
    protected LastMessage lastMessage;

    @Expose(serialize = false)
    @SerializedName("share_url")
    protected String shareURL;

    @Expose(serialize = false, deserialize = false)
    protected int badgeCount = 0;

    //CardInfo cache support

    @Expose(serialize = false, deserialize = false)
    transient List<TimestampedObject> cachedCardInfoList;

    @Expose(serialize = false, deserialize = false)
    transient List<TimestampedObject> addedCardInfoList;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public FeedItem() {
        initializeCardLists();
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------


    public TourAuthor getAuthor() {
        return author;
    }

    public void setAuthor(final TourAuthor author) {
        this.author = author;
    }

    public int getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(final int badgeCount) {
        this.badgeCount = badgeCount;
    }

    public void increaseBadgeCount() {
        badgeCount++;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getJoinStatus() {
        return joinStatus;
    }

    public void setJoinStatus(final String joinStatus) {
        this.joinStatus = joinStatus;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(final int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public Address getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(final Address startAddress) {
        this.startAddress = startAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(final Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public LastMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(final LastMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getShareURL() {
        return shareURL;
    }

    public void setShareURL(final String shareURL) {
        this.shareURL = shareURL;
    }

    public List<TimestampedObject> getCachedCardInfoList() {
        return cachedCardInfoList;
    }

    public List<TimestampedObject> getAddedCardInfoList() {
        return addedCardInfoList;
    }

    public boolean isClosed() {
        return STATUS_CLOSED.equals(status) || STATUS_FREEZED.equals(status);
    }

    public boolean isOngoing() {
        return STATUS_ON_GOING.equals(status);
    }

    public boolean isPrivate() {
        return JOIN_STATUS_ACCEPTED.equals(joinStatus);
    }

    public boolean isFreezed() {
        return STATUS_FREEZED.equals(status);
    }

    public boolean isMine() {
        if (author != null) {
            User me = EntourageApplication.me();
            if (me != null) {
                return author.getUserID() == me.getId();
            }
        }
        return false;
    }

    @Override
    public void copyLocalFields(final TimestampedObject other) {
        super.copyLocalFields(other);
        FeedItem otherFeedItem = (FeedItem)other;
        this.badgeCount = otherFeedItem.badgeCount;
    }

    // ----------------------------------
    // CARD INFO METHODS
    // ----------------------------------

    private void initializeCardLists() {
        this.cachedCardInfoList = new ArrayList<>();
        this.addedCardInfoList = new ArrayList<>();
    }

    public void addCardInfo(TimestampedObject cardInfo) {
        if (cardInfo == null) return;
        if (cachedCardInfoList.contains(cardInfo)) {
            return;
        }
        cachedCardInfoList.add(cardInfo);
        addedCardInfoList.add(cardInfo);

        Collections.sort(cachedCardInfoList, new TimestampedObject.TimestampedObjectComparatorOldToNew());
    }

    public void removeCardInfo(TimestampedObject cardInfo) {
        if (cardInfo == null) return;
        cachedCardInfoList.remove(cardInfo);
    }

    public int addCardInfoList(List<TimestampedObject> cardInfoList) {
        if (cardInfoList == null) return 0;
        for (final TimestampedObject timestampedObject : cardInfoList) {
            if (cachedCardInfoList.contains(timestampedObject)) {
                continue;
            }
            cachedCardInfoList.add(timestampedObject);
            addedCardInfoList.add(timestampedObject);
        }
        if (addedCardInfoList.size() > 0) {
            Collections.sort(cachedCardInfoList, new TimestampedObject.TimestampedObjectComparatorOldToNew());
            Collections.sort(addedCardInfoList, new TimestampedObject.TimestampedObjectComparatorOldToNew());
        }
        return addedCardInfoList.size();
    }

    public List<TimestampedObject> getTypedCardInfoList(int cardType) {
        List<TimestampedObject> typedCardInfoList = new ArrayList<>();
        if (cachedCardInfoList == null) return typedCardInfoList;
        for (final TimestampedObject timestampedObject : cachedCardInfoList) {
            if (timestampedObject.getType() == cardType) {
                typedCardInfoList.add(timestampedObject);
            }
        }
        return typedCardInfoList;
    }

    public void clearAddedCardInfoList() {
        addedCardInfoList.clear();
    }

    // ----------------------------------
    // SERIALIZATION METHODS
    // ----------------------------------

    private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException
    {
        inputStream.defaultReadObject();
        initializeCardLists();
    }

    // ----------------------------------
    // ABSTRACT METHODS
    // ----------------------------------

    public abstract String getFeedType();
    public abstract String getFeedTypeLong(Context context);

    public abstract String getTitle();

    public abstract String getDescription();

    public abstract Date getStartTime();

    public abstract Date getEndTime();
    public abstract void setEndTime(Date endTime);

    public abstract TourPoint getStartPoint();

    public abstract TourPoint getEndPoint();

}
