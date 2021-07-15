package social.entourage.android.newsfeed

import social.entourage.android.api.model.Message
import social.entourage.android.api.model.PushNotificationContent
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.navigation.EntBottomNavigationView
import timber.log.Timber
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

/**
 * Local storage class for feed items
 * Created by mihaiionescu on 20/03/2017.
 */
class UserFeedItemListCache : Serializable {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    private val items = HashMap<Int, MutableList<FeedItemCache>>()

    // ----------------------------------
    // Methods
    // ----------------------------------
    private fun getUserCache(userId: Int): MutableList<FeedItemCache> {
        // get the list
        return items[userId] ?: run {
            val userCache = ArrayList<FeedItemCache>()
            items[userId] = userCache
            return userCache
        }
    }

    fun saveFeedItemFromNotification(userId: Int, message: Message, isAdded: Boolean): Int {
        // sanity checks
        val content = message.content ?: return -1
        // get the list
        val userCache = getUserCache(userId)
        // search for a saved feeditem
        for (feedItemStorage in userCache) {
            if (feedItemStorage.feedId != content.joinableId) {
                continue
            }
            if (feedItemStorage.type == TimestampedObject.ENTOURAGE_CARD && content.isEntourageRelated
                    || feedItemStorage.type == TimestampedObject.TOUR_CARD && content.isTourRelated) {
                    if(isAdded) {
                        feedItemStorage.badgeCount++
                        EntBottomNavigationView.increaseBadgeCount()
                    } else {
                        if (feedItemStorage.badgeCount > 0) {
                            feedItemStorage.badgeCount--
                            EntBottomNavigationView.decreaseBadgeCount()
                        }
                    }
                return feedItemStorage.badgeCount
            }
        }
        if (isAdded) {
            // none found, add one
            userCache.add(FeedItemCache(content))
            EntBottomNavigationView.increaseBadgeCount()
            return 1
        }
        EntBottomNavigationView.decreaseBadgeCount()
        return -1
    }

    fun updateFeedItem(userId: Int, feedItem: FeedItem) {
        // get the list
        val userCache = getUserCache(userId)
        // search for a saved feeditem
        for (feedItemCache in userCache) {
            if (feedItemCache.feedId == feedItem.id && feedItemCache.type == feedItem.type) {
                val deltaNb = feedItemCache.update(feedItem)
                if(deltaNb>0) EntBottomNavigationView.increaseBadgeCount()
                else if(deltaNb<0) EntBottomNavigationView.decreaseBadgeCount()
                return
            }
        }
        if (feedItem.getUnreadMsgNb() > 0) {
            userCache.add(FeedItemCache(feedItem))
            EntBottomNavigationView.increaseBadgeCount()
        }
    }

    fun clear(userId: Int): Boolean {
        getUserCache(userId).clear()
        return true
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    private class FeedItemCache : Serializable {
        var type: Int
        var feedId: Long
        var badgeCount: Int

        constructor(feedItem: FeedItem) {
            type = feedItem.type
            feedId = feedItem.id
            badgeCount = feedItem.getUnreadMsgNb()
        }

        constructor(pushNotificationContent: PushNotificationContent) {
            type = if (pushNotificationContent.isEntourageRelated) TimestampedObject.ENTOURAGE_CARD else TimestampedObject.TOUR_CARD
            feedId = pushNotificationContent.joinableId
            badgeCount = 1
        }

        fun update(feedItem: FeedItem): Int {
            val nbChanged = feedItem.getUnreadMsgNb() - badgeCount
            if (nbChanged!=0) {
                badgeCount = feedItem.getUnreadMsgNb()
            }
            return nbChanged
        }

        companion object {
            private const val serialVersionUID = 6917587914260136512L
        }
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        private const val serialVersionUID = -7135455534881059190L
        const val KEY = "FeedItemsStorage"
    }
}