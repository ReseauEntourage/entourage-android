package social.entourage.android.newsfeed

import social.entourage.android.api.model.Message
import social.entourage.android.api.model.PushNotificationContent
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import timber.log.Timber
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

/**
 * Local storage class for feed items
 * Created by mihaiionescu on 20/03/2017.
 */
class UserFeedItemListCache : Serializable {
    //cache
    private var cacheCount = 0
    private var cacheInvitationCount = 0
    private var hasChanged = true

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
                feedItemStorage.badgeCount += if (isAdded) 1 else -1
                if (feedItemStorage.badgeCount < 0) {
                    feedItemStorage.badgeCount = 0
                }
                hasChanged = true
                return feedItemStorage.badgeCount
            }
        }
        if (isAdded) {
            // none found, add one
            userCache.add(FeedItemCache(content))
            hasChanged = true
            return 1
        }
        return -1
    }

    fun updateInvitationCount(count: Int) {
        if (count != cacheInvitationCount) {
            hasChanged = true
            cacheInvitationCount = count
        }
    }

    fun updateFeedItem(userId: Int, feedItem: FeedItem) {
        // get the list
        val userCache = getUserCache(userId)
        // search for a saved feeditem
        for (feedItemCache in userCache) {
            if (feedItemCache.feedId == feedItem.id && feedItemCache.type == feedItem.type) {
                hasChanged = hasChanged or feedItemCache.update(feedItem)
                return
            }
        }
        if (feedItem.getUnreadMsgNb() > 0) {
            userCache.add(FeedItemCache(feedItem))
            hasChanged = true
        }
    }

    fun clear(userId: Int): Boolean {
        getUserCache(userId).clear()
        hasChanged = false
        cacheCount = 0
        cacheInvitationCount = 0
        return true
    }

    fun getBadgeCount(userId: Int): Int {
        if (!hasChanged) {
            return cacheCount + cacheInvitationCount
        }
        Timber.d("old cacheCount=%d", cacheCount)
        cacheCount = 0
        hasChanged = false
        for (feedItem in getUserCache(userId)) {
            cacheCount += feedItem.badgeCount
        }
        Timber.d("new cacheCount=%d", cacheCount)
        return cacheCount + cacheInvitationCount
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    private class FeedItemCache : Serializable {
        var type: Int
        var feedId: Long
        var badgeCount: Int

        internal constructor(feedItem: FeedItem) {
            type = feedItem.type
            feedId = feedItem.id
            badgeCount = feedItem.getUnreadMsgNb()
        }

        internal constructor(pushNotificationContent: PushNotificationContent) {
            type = if (pushNotificationContent.isEntourageRelated) TimestampedObject.ENTOURAGE_CARD else TimestampedObject.TOUR_CARD
            feedId = pushNotificationContent.joinableId
            badgeCount = 1
        }

        fun update(feedItem: FeedItem): Boolean {
            var isChanged = false
            if (feedItem.getUnreadMsgNb() != badgeCount) {
                badgeCount = feedItem.getUnreadMsgNb()
                isChanged = true
            }
            if (feedItem.getUnreadMsgNb() != 0) {
                Timber.d("New unread messages")
            }
            return isChanged
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