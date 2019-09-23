package social.entourage.android.tour

import social.entourage.android.api.model.Newsfeed

interface NewsFeedListener {
    fun onNetworkException()

    fun onCurrentPositionNotRetrieved()

    fun onServerException(throwable: Throwable)

    fun onTechnicalException(throwable: Throwable)

    fun onNewsFeedReceived(newsFeeds: List<Newsfeed>)
}