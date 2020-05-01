package social.entourage.android.api.model.map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
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
    public static final String KEY_FEEDITEM_UUID = "social.entourage.android.KEY_FEEDITEM_UUID";
    public static final String KEY_FEEDITEM_TYPE = "social.entourage.android.KEY_FEEDITEM_TYPE";

    public static final String STATUS_OPEN = "open";
    public static final String STATUS_CLOSED = "closed";
    public static final String STATUS_ON_GOING = "ongoing";
    public static final String STATUS_FREEZED = "freezed";
    public static final String STATUS_SUSPENDED = "suspended";

    public static final String JOIN_STATUS_NOT_REQUESTED = "not_requested";
    public static final String JOIN_STATUS_PENDING = "pending";
    public static final String JOIN_STATUS_ACCEPTED = "accepted";
    public static final String JOIN_STATUS_REJECTED = "rejected";
    public static final String JOIN_STATUS_CANCELLED = "cancelled";
    public static final String JOIN_STATUS_QUITED = "quited"; // This status does not exist on server, just locally

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @Expose(serialize = false)
    protected long id;

    @Expose(serialize = false)
    @Nullable protected String uuid;

    @NotNull protected String status;

    @Nullable protected FeedItemAuthor author;

    @SerializedName("updated_at")
    @NotNull
    protected Date updatedTime;

    @Expose(serialize = false, deserialize = false)
    @Nullable protected transient Address startAddress;

    @Expose(serialize = false)
    @SerializedName("number_of_people")
    protected int numberOfPeople;

    @Expose(serialize = false)
    @SerializedName("number_of_unread_messages")
    protected int numberOfUnreadMessages;

    @Expose(serialize = false)
    @SerializedName("join_status")
    @NotNull protected String joinStatus;

    @Expose(serialize = false)
    @SerializedName("last_message")
    @Nullable
    protected LastMessage lastMessage;

    @Expose(serialize = false)
    @SerializedName("share_url")
    @Nullable protected String shareURL;

    //number of notifs received that should be added to nuber of unread messages
    @Expose(serialize = false, deserialize = false)
    protected int badgeCount = 0;

    //CardInfo cache support

    @Expose(serialize = false, deserialize = false)
    @NotNull transient List<TimestampedObject> cachedCardInfoList;

    @Expose(serialize = false, deserialize = false)
    @NotNull transient List<TimestampedObject> addedCardInfoList;

    //Flag to indicate a newly created feed item
    @Expose(serialize = false, deserialize = false)
    private boolean isNewlyCreated = false;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public FeedItem() {
        initializeCardLists();
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------


    @Nullable public FeedItemAuthor getAuthor() {
        return author;
    }

    public void setAuthor(@NotNull final FeedItemAuthor author) {
        this.author = author;
    }

    public int getBadgeCount() {
        return badgeCount+numberOfUnreadMessages;
    }

    public void increaseBadgeCount(boolean isChatMessage) {
        if(!isChatMessage) {
            badgeCount++;
        } else {
            numberOfUnreadMessages++;//numberOfUnreadMessages will be updated elsewhere
        }
    }

    public void decreaseBadgeCount() {
        if(badgeCount>0) {
            badgeCount--;
        }
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @NotNull public String getUUID() {
        return uuid == null ? "" : uuid;
    }

    @NotNull public String getJoinStatus() {
        return joinStatus;
    }

    public void setJoinStatus(@NotNull final String joinStatus) {
        this.joinStatus = joinStatus;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(final int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public int getNumberOfUnreadMessages() {
        return numberOfUnreadMessages;
    }

    public void setNumberOfUnreadMessages(final int numberOfUnreadMessages) {
        this.numberOfUnreadMessages = numberOfUnreadMessages;
    }

    @Nullable public Address getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(@Nullable final Address startAddress) {
        this.startAddress = startAddress;
    }

    @NotNull public String getStatus() {
        return status;
    }

    public void setStatus(@NotNull final String status) {
        this.status = status;
    }

    @NotNull public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(@NotNull final Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Nullable public LastMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(@Nullable final String text, @Nullable final String author) {
        if(lastMessage == null) {
            lastMessage = new LastMessage();
        }
        lastMessage.setMessage(text, author);
    }

    @Nullable public String getShareURL() {
        return shareURL;
    }

    public void setShareURL(@Nullable final String shareURL) {
        this.shareURL = shareURL;
    }

    @NonNull
    public List<TimestampedObject> getCachedCardInfoList() {
        return cachedCardInfoList;
    }

    @NotNull public List<TimestampedObject> getAddedCardInfoList() {
        return addedCardInfoList;
    }

    public boolean isNewlyCreated() {
        return isNewlyCreated;
    }

    public void setNewlyCreated(final boolean newlyCreated) {
        isNewlyCreated = newlyCreated;
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

    public boolean isMine(Context context) {
        if (author != null) {
            User me = EntourageApplication.me(context);
            if (me != null) {
                return author.getUserID() == me.getId();
            }
        }
        return false;
    }

    public boolean isSuspended() {
        return STATUS_SUSPENDED.equals(status);
    }

    // ----------------------------------
    // UI METHODS
    // ----------------------------------

    @Nullable public Drawable getIconDrawable(Context context) {
        return null;
    }

    @Nullable public String getIconURL() {
        return null;
    }

    public boolean showHeatmapAsOverlay() {
        return true;
    }

    public int getHeatmapResourceId() {
        return R.drawable.heat_zone;
    }

    public int getFeedTypeColor() {return 0;}

    public boolean canBeClosed() { return true; }

    public boolean showAuthor() { return true; }

    public @StringRes int getJoinRequestTitle() { return R.string.tour_info_request_join_title_tour; }

    public @StringRes int getJoinRequestButton() { return R.string.tour_info_request_join_button_tour; }

    public @StringRes int getQuitDialogTitle() {
        return R.string.tour_info_quit_tour_title;
    }

    public @StringRes int getQuitDialogMessage() {
        return R.string.tour_info_quit_tour_description;
    }

    public boolean showInviteViewAfterCreation() {
        return true;
    }

    public boolean showEditEntourageView() {
        return true;
    }

    public @StringRes int getFreezedCTAText() {
        return R.string.tour_cell_button_freezed;
    }

    public @ColorRes int getFreezedCTAColor() {
        return R.color.greyish;
    }

    public @StringRes int getClosingLoaderMessage() {
        return R.string.loader_title_tour_finish;
    }

    public @StringRes int getClosedToastMessage() {
        return isFreezed() ? R.string.tour_freezed : R.string.local_service_stopped;
    }

    // ----------------------------------
    // COPY OBJECT METHODS
    // ----------------------------------

    @Override
    public void copyLocalFields(@NotNull final TimestampedObject other) {
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

    public void addCardInfo(@NotNull TimestampedObject cardInfo) {
        if (cachedCardInfoList.contains(cardInfo)) {
            return;
        }
        cachedCardInfoList.add(cardInfo);
        addedCardInfoList.add(cardInfo);

        Collections.sort(cachedCardInfoList, new TimestampedObject.TimestampedObjectComparatorOldToNew());
    }

    public void removeCardInfo(@NotNull TimestampedObject cardInfo) {
        cachedCardInfoList.remove(cardInfo);
    }

    public int addCardInfoList(@NotNull List<TimestampedObject> cardInfoList) {
        for (final TimestampedObject timestampedObject : cardInfoList) {
            if (cachedCardInfoList.contains(timestampedObject)) {
                continue;
            }
            cachedCardInfoList.add(timestampedObject);
            addedCardInfoList.add(timestampedObject);
        }
        if(cachedCardInfoList.size() > 0) {
            Collections.sort(cachedCardInfoList, new TimestampedObject.TimestampedObjectComparatorOldToNew());
        }
        if (addedCardInfoList.size() > 0) {
            Collections.sort(addedCardInfoList, new TimestampedObject.TimestampedObjectComparatorOldToNew());
        }
        return addedCardInfoList.size();
    }

    @NotNull public List<TimestampedObject> getTypedCardInfoList(int cardType) {
        List<TimestampedObject> typedCardInfoList = new ArrayList<>();
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

    private void readObject(@NotNull ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException
    {
        inputStream.defaultReadObject();
        initializeCardLists();
    }

    // ----------------------------------
    // ABSTRACT METHODS
    // ----------------------------------

    //TODO check if real groupType, not actionGroupType
    @Nullable public abstract String getGroupType();

    //@Nullable public abstract String getFeedType();
    @NotNull public abstract String getFeedTypeLong(Context context);

    @Nullable public abstract String getTitle();

    @Nullable public abstract String getDescription();

    @NotNull public abstract Date getCreationTime();
    @Nullable public abstract Date getStartTime();

    @Nullable public abstract Date getEndTime();
    public abstract void setEndTime(@NotNull Date endTime);

    @Nullable public abstract String getDisplayAddress();

    @Nullable public abstract LocationPoint getStartPoint();
}
