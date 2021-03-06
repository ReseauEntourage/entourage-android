package social.entourage.android.newsfeed.v2

/**
 * HomeViewHolderListener.
 */
interface HomeViewHolderListener {
    fun onDetailClicked(item:Any,position:Int,isFromHeadline:Boolean,isAction:Boolean = false)
    fun onShowDetail(type:HomeCardType,isArrow:Boolean)
    fun onShowChangeZone()
    fun onShowEntourageHelp()
}