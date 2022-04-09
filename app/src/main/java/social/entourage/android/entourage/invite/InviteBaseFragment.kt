package social.entourage.android.entourage.invite

import android.os.Bundle
import android.view.View
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.EntourageComponent
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.base.BaseDialogFragment
import javax.inject.Inject

/**
 * A simple [BaseDialogFragment] subclass.
 */
open class InviteBaseFragment  : BaseDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    protected var feedItemUUID: String? = null
    protected var feedItemType = 0

    val presenter: InvitePresenter = InvitePresenter(this)
    var inviteFriendsListener: InviteFriendsListener? = null

    protected fun setFeedData(feedUUID: String?, feedItemType: Int) {
        val args = Bundle()
        args.putString(FeedItem.KEY_FEEDITEM_UUID, feedUUID)
        args.putInt(FeedItem.KEY_FEEDITEM_TYPE, feedItemType)
        this.arguments = args
    }

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            feedItemUUID = it.getString(FeedItem.KEY_FEEDITEM_UUID)
            feedItemType = it.getInt(FeedItem.KEY_FEEDITEM_TYPE)
        }
    }

    override fun onDetach() {
        super.onDetach()
        inviteFriendsListener = null
    }

    // ----------------------------------
    // PRESENTER CALLBACKS
    // ----------------------------------
    open fun onInviteSent(success: Boolean) {}
}