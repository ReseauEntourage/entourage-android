package social.entourage.android.entourage.invite

import android.os.Bundle
import android.view.View
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.EntourageComponent
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.base.EntourageDialogFragment
import javax.inject.Inject

/**
 * A simple [EntourageDialogFragment] subclass.
 */
open class InviteBaseFragment  : EntourageDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    protected var feedItemUUID: String? = null
    protected var feedItemType = 0

    @JvmField
    @Inject
    var presenter: InvitePresenter? = null
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupComponent(get(activity).entourageComponent)
    }

    protected fun setupComponent(entourageComponent: EntourageComponent?) {
        DaggerInviteComponent.builder()
                .entourageComponent(entourageComponent)
                .inviteModule(InviteModule(this))
                .build()
                .inject(this)
    }

    // ----------------------------------
    // PRESENTER CALLBACKS
    // ----------------------------------
    open fun onInviteSent(success: Boolean) {}
}