package social.entourage.android.old_v7.home

/**
 * HomeViewHolderListener.
 */
interface HomeViewHolderListener {
    fun onDetailClicked(item:Any,position:Int,isFromHeadline:Boolean,isAction:Boolean = false)
    fun onShowDetail(type: HomeCardType, isArrow:Boolean, subtype: HomeCardType)
    fun onShowChangeZone()
    fun onShowEntourageHelp()
    fun onShowChangeMode()
}