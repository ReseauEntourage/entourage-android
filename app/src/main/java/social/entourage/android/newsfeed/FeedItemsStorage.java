package social.entourage.android.newsfeed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.FeedItem;
import timber.log.Timber;

/**
 * Local storage class for feed items
 * Created by mihaiionescu on 20/03/2017.
 */

public class FeedItemsStorage implements Serializable {

    // ----------------------------------
    // Constants
    // ----------------------------------

    private static final long serialVersionUID = -7135458066881059190L;

    public static final String KEY = "FeedItemsStorage";

    //cache
    private int cacheCount = 0;
    private int cacheInvitationCount = 0;
    private boolean hasChanged = true;


    // ----------------------------------
    // Attributes
    // ----------------------------------

    private HashMap<Integer, List<FeedItemStorage>> feeds = new HashMap<>();

    // ----------------------------------
    // Methods
    // ----------------------------------

    private List<FeedItemStorage> getUserFeeds(int userId) {
        // get the list
        List<FeedItemStorage> userFeeds = feeds.get(userId);
        // check if there is one
        if (userFeeds == null) {
            // create a list
            userFeeds = new ArrayList<>();
            // save the list
            feeds.put(userId, userFeeds);
        }
        return userFeeds;
    }

    public void saveFeedItem(int userId, FeedItem feedItem) {
        // get the list
        List<FeedItemStorage> userFeeds = getUserFeeds(userId);
        // search for a saved feeditem
        for (final FeedItemStorage feedItemStorage : userFeeds) {
            if (feedItemStorage.feedId == feedItem.getId() && feedItemStorage.type == feedItem.getType()) {
                hasChanged |= feedItemStorage.updateFrom(feedItem);
                return;
            }
        }
        // none found, add one
        userFeeds.add(new FeedItemStorage(feedItem));
        hasChanged = true;
    }

    public int saveFeedItem(int userId, Message message, boolean isAdded) {
        // sanity checks
        PushNotificationContent content = message.getContent();
        if (content == null) {
            return -1;
        }
        // get the list
        List<FeedItemStorage> userFeeds = getUserFeeds(userId);
        // search for a saved feeditem
        for (final FeedItemStorage feedItemStorage : userFeeds) {
            if (feedItemStorage.feedId != content.getJoinableId()) {
                continue;
                }
            if ((feedItemStorage.type == TimestampedObject.ENTOURAGE_CARD && content.isEntourageRelated())
                    ||(feedItemStorage.type == TimestampedObject.TOUR_CARD && content.isTourRelated())) {
                feedItemStorage.badgeCount += isAdded ? 1 : -1;
                if (feedItemStorage.badgeCount < 0) {
                    feedItemStorage.badgeCount = 0;
                }
                hasChanged = true;
                return feedItemStorage.badgeCount;
            }
            }

            if (isAdded) {
                // none found, add one
                userFeeds.add(new FeedItemStorage(content));
            hasChanged =true;
                return 1;
            }
        return -1;
    }

    public void updateInvitationCount(int count) {
        if(count!=cacheInvitationCount) {
            hasChanged = true;
            cacheInvitationCount = count;
        }
    }

    public void updateFeedItem(int userId, FeedItem feedItem) {
        // get the list
        List<FeedItemStorage> userFeeds = getUserFeeds(userId);
        // search for a saved feeditem
        for (final FeedItemStorage feedItemStorage : userFeeds) {
            if (feedItemStorage.feedId == feedItem.getId() && feedItemStorage.type == feedItem.getType()) {
                hasChanged |= feedItemStorage.updateTo(feedItem);
                return;
            }
        }
        if(feedItem.getBadgeCount()>0) {
            userFeeds.add(new FeedItemStorage(feedItem));
            hasChanged = true;
        }
    }

    public boolean clear(int userId) {
        List myList = getUserFeeds(userId);
        if(myList!=null) {
            myList.clear();
            hasChanged =false;
            cacheCount = 0;
            cacheInvitationCount = 0;
            return true;
        }
        return false;
    }

    public int getBadgeCount(int userId) {
        if(!hasChanged) {
            return cacheCount+cacheInvitationCount;
        }
        Timber.d("old cacheCount=%d", cacheCount);
        List<FeedItemStorage> userFeeds = getUserFeeds(userId);
        cacheCount =0;
        hasChanged = false;
        for (final FeedItemStorage feedItem: userFeeds) {
            cacheCount += feedItem.badgeCount;
        }
        Timber.d("new cacheCount=%d", cacheCount);
        return cacheCount+cacheInvitationCount;
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private static class FeedItemStorage implements Serializable {

        private static final long serialVersionUID = 6917587786160136512L;

        private int type;
        private long feedId;
        private int badgeCount;

        FeedItemStorage(FeedItem feedItem) {
            this.type = feedItem.getType();
            this.feedId = feedItem.getId();
            this.badgeCount = feedItem.getBadgeCount();
        }

        FeedItemStorage(PushNotificationContent pushNotificationContent) {
            this.type = pushNotificationContent.isEntourageRelated() ? TimestampedObject.ENTOURAGE_CARD : TimestampedObject.TOUR_CARD;
            this.feedId = pushNotificationContent.getJoinableId();
            this.badgeCount = 1;
        }

        boolean updateFrom(FeedItem feedItem) {
            boolean isChanged = false;
            if(this.badgeCount != feedItem.getBadgeCount()) {
                this.badgeCount = feedItem.getBadgeCount();
                isChanged = true;
            }
            return isChanged;
        }

        boolean updateTo(FeedItem feedItem) {
            boolean isChanged = false;
            if(feedItem.getBadgeCount()!= badgeCount) {
                badgeCount = feedItem.getBadgeCount();
                isChanged = true;
            }
            if(feedItem.getBadgeCount()!= 0) {
                Timber.d("New unread messages");
            }
            return isChanged;
        }
    }

}
