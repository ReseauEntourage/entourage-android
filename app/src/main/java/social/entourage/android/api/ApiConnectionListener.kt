package social.entourage.android.api

import social.entourage.android.api.model.Newsfeed

interface ApiConnectionListener {
    fun onNetworkException()

    fun onServerException(throwable: Throwable)

    fun onTechnicalException(throwable: Throwable)
}