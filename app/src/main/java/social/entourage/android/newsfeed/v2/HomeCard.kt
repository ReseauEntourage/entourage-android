package social.entourage.android.newsfeed.v2

import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONObject
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.feed.Announcement
import social.entourage.android.api.model.feed.NewsfeedItem
import timber.log.Timber

/**
 * Created on 3/17/21.
 */

/***
 * HomeCard
 */
class HomeCard {
    var type = HomeCardType.NONE
    var subtype = HomeCardType.NONE
    var arrayCards = ArrayList<NewsfeedItem>()

    class OnGetHomeFeed(val responseString: String)

    companion object {
        fun parsingFeed(responseString:String) : ArrayList<HomeCard> {
            val array = ArrayList<HomeCard>()
            val rootJson = JSONObject(responseString)

            if (rootJson["metadata"] is JSONObject) {
                val metadata = rootJson["metadata"] as JSONObject
                if (metadata["order"] is JSONArray) {
                    val orders = metadata["order"] as JSONArray
                    for (i in 0 until orders.length()) {
                        (orders[i] as? String)?.let {
                            val _obj = rootJson[it]
                            if (_obj is JSONArray) {
                                array.add(parsingArray(_obj,it))
                            }
                            if (_obj is JSONObject) {
                                array.add(parsingDictionnary(_obj,it))
                            }
                        }
                    }
                }
            }
            return array
        }

        private fun parsingArray(jsonArray:JSONArray, name:String) : HomeCard {
            val homeCard = HomeCard()
            homeCard.type = HomeCardType.getStr(name)
            homeCard.subtype = HomeCardType.getSubStr(name)

            if (homeCard.type == HomeCardType.HEADLINES) {
                return homeCard
            }

            homeCard.arrayCards.clear()
            for (i in 0 until jsonArray.length()) {
                (jsonArray[i] as? JSONObject)?.let { jsonObject ->
                    homeCard.arrayCards.add(getFeedItem(jsonObject))
                }
            }
            return homeCard
        }

        private fun getFeedItem(jsonObject: JSONObject) : NewsfeedItem {
            val newsfeed = NewsfeedItem()
            newsfeed.type = BaseEntourage.NEWSFEED_TYPE

            try {
                NewsfeedItem.getClassFromString(BaseEntourage.NEWSFEED_TYPE,
                        jsonObject["group_type"].toString(),
                        jsonObject["entourage_type"].toString())?.let { newsfeedClass ->
                    val gson = GsonBuilder()
                            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                            .create()
                    newsfeed.data = gson.fromJson<Any>(jsonObject.toString(), newsfeedClass)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
            return newsfeed
        }

        private fun getFeedItemFromHeadline(jsonObject: JSONObject) : NewsfeedItem {
            val newsfeed = NewsfeedItem()

            (jsonObject["type"] as? String)?.let {
                newsfeed.type = it
                val jsonData = jsonObject["data"] as? JSONObject
                jsonData?.let { jsonData ->
                    try {
                        val groupType = if (it.equals(Announcement.NEWSFEED_TYPE)) null else jsonData["group_type"] as? String
                        val entourageType = if (it.equals(Announcement.NEWSFEED_TYPE)) null else jsonData["entourage_type"] as? String
                        NewsfeedItem.getClassFromString(it,
                                groupType,
                                entourageType)?.let { newsfeedClass ->
                            val gson = GsonBuilder()
                                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                                    .create()
                            newsfeed.data = gson.fromJson<Any>(jsonData.toString(), newsfeedClass)
                        }
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
            }
            return newsfeed
        }

        private fun parsingDictionnary(jsonObject:JSONObject, name:String) : HomeCard {
            val homeCard = HomeCard()
            homeCard.type = HomeCardType.getStr(name)
            homeCard.subtype = HomeCardType.getSubStr(name)

            if (homeCard.type == HomeCardType.HEADLINES) {
                if (jsonObject["metadata"] is JSONObject) {
                    val metadata = jsonObject["metadata"] as JSONObject
                    if (metadata["order"] is JSONArray) {
                        val orders = metadata["order"] as JSONArray
                        for (i in 0 until orders.length()) {
                            (orders[i] as? String)?.let {
                                val jsonHeadline = jsonObject[it]
                                if (jsonHeadline is JSONObject) {
                                    homeCard.arrayCards.add(getFeedItemFromHeadline(jsonHeadline))
                                }
                            }
                        }
                    }
                }
            }
            return homeCard
        }
    }
}

/***
 * HomeCardType Enum
 */
enum class HomeCardType {
    HEADLINES,
    EVENTS,
    ACTIONS,
    ACTIONS_ASK,
    ACTIONS_CONTRIB,
    NONE;

    fun getName() : Int {
        when(this) {
            HEADLINES -> return R.string.home_title_headlines
            ACTIONS -> return R.string.home_title_actions
            EVENTS -> return R.string.home_title_events
            ACTIONS_ASK -> return R.string.home_title_actions_ask
            ACTIONS_CONTRIB -> return R.string.home_title_actions_contrib
            NONE -> return R.string.home_title_none
        }
    }
    companion object {
        fun getStr(name:String) : HomeCardType {
            when(name) {
                "entourages","entourage_ask_for_helps","entourage_contributions" -> return ACTIONS
                "outings" -> return EVENTS
                "headlines" -> return HEADLINES
                else -> return NONE
            }
        }
        fun getSubStr(name:String) : HomeCardType {
            when(name) {
                "entourage_ask_for_helps" -> return ACTIONS_ASK
                "entourage_contributions" -> return ACTIONS_CONTRIB
                else -> return NONE
            }
        }
    }
}