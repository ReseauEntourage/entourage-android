package social.entourage.android.newsfeed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.FeedItem;

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

    public void updateFeedItemStorage(int userId, FeedItem feedItem) {
        // get the list
        List<FeedItemStorage> userFeeds = getUserFeeds(userId);
        // search for a saved feeditem
        for (final FeedItemStorage feedItemStorage : userFeeds) {
            if (feedItemStorage.feedId == feedItem.getId() && feedItemStorage.type == feedItem.getType()) {
                feedItemStorage.updateFrom(feedItem);
                return;
            }
        }
        // none found, add one
        userFeeds.add(new FeedItemStorage(feedItem));
    }

    public void updateFeedItemStorage(int userId, Message message, boolean isAdded) {
        // sanity checks
        PushNotificationContent content = message.getContent();
        if (content == null) {
            return;
        }
        // get the list
        List<FeedItemStorage> userFeeds = getUserFeeds(userId);
        // search for a saved feeditem
        ListIterator<FeedItemStorage> listIterator = userFeeds.listIterator();
        FeedItemStorage targetFeedItemStorage = null;
        while (listIterator.hasNext()) {
            FeedItemStorage feedItemStorage = listIterator.next();
            if (feedItemStorage.feedId == content.getJoinableId()) {
                if (feedItemStorage.type == TimestampedObject.ENTOURAGE_CARD && content.isEntourageRelated()) {
                    targetFeedItemStorage = feedItemStorage;
                    break;
                }
                if (feedItemStorage.type == TimestampedObject.TOUR_CARD && content.isTourRelated()) {
                    targetFeedItemStorage = feedItemStorage;
                    break;
                }
            }
        }
        if (targetFeedItemStorage != null) {
            targetFeedItemStorage.badgeCount += isAdded ? 1 : -1;
            if (targetFeedItemStorage.badgeCount < 0) {
                targetFeedItemStorage.badgeCount = 0;
            }
        } else {
            if (isAdded) {
                // none found, add one
                userFeeds.add(new FeedItemStorage(content));
            }
        }
    }

    public void updateFeedItemFromStorage(int userId, FeedItem feedItem) {
        // get the list
        List<FeedItemStorage> userFeeds = getUserFeeds(userId);
        // search for a saved feeditem
        for (final FeedItemStorage feedItemStorage : userFeeds) {
            if (feedItemStorage.feedId == feedItem.getId() && feedItemStorage.type == feedItem.getType()) {
                feedItemStorage.updateTo(feedItem);
                return;
            }
        }
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private static class FeedItemStorage implements Serializable {

        private static final long serialVersionUID = 6917587786160136512L;

        private int type;
        private long feedId;
        private int badgeCount;

        public FeedItemStorage(FeedItem feedItem) {
            this.type = feedItem.getType();
            this.feedId = feedItem.getId();
            this.badgeCount = feedItem.getBadgeCount();
        }

        public FeedItemStorage(PushNotificationContent pushNotificationContent) {
            this.type = pushNotificationContent.isEntourageRelated() ? TimestampedObject.ENTOURAGE_CARD : TimestampedObject.TOUR_CARD;
            this.feedId = pushNotificationContent.getJoinableId();
            this.badgeCount = 1;
        }

        public void updateFrom(FeedItem feedItem) {
            this.badgeCount = feedItem.getBadgeCount();
        }

        public void updateTo(FeedItem feedItem) {
            feedItem.setBadgeCount(badgeCount);
        }
    }

}
