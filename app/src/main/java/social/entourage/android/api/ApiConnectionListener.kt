package social.entourage.android.api

interface ApiConnectionListener {
    fun onNetworkException()

    fun onServerException(throwable: Throwable)

    fun onTechnicalException(throwable: Throwable)
}