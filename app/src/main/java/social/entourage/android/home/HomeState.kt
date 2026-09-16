package social.entourage.android.home

object HomeState {
    var isContribProfile = false
    var signablePermission = false
}

interface OnHomeChangeLocationUpdate {
    fun onHomeChangeLocationUpdateClearFragment()
}
