package social.entourage.android.entourage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.layout_entourage_options.*
import social.entourage.android.EntourageApplication
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.tape.Events.OnUserActEvent
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.tools.EntBus

abstract class FeedItemOptionsFragment : BaseDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    protected lateinit var feedItem: FeedItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        feedItem = arguments?.getSerializable(FeedItem.KEY_FEEDITEM) as FeedItem
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_entourage_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        entourage_option_edit?.setOnClickListener {onEditClicked()}
        entourage_option_quit?.setOnClickListener {onQuitClicked()}
        entourage_option_stop?.setOnClickListener {onStopClicked()}
        entourage_option_cancel?.setOnClickListener{onCancelClicked()}
        entourage_options?.setOnClickListener {onCancelClicked()}
        val me = EntourageApplication.me(activity) ?: return
        val author = feedItem.author ?: return
        if (author.userID != me.id) {
            entourage_option_quit?.visibility = View.VISIBLE
            entourage_option_quit?.setText(if (FeedItem.JOIN_STATUS_PENDING == feedItem.joinStatus) R.string.tour_info_options_cancel_request else R.string.tour_info_options_quit_tour)
        } else {
            entourage_option_stop?.visibility = if (feedItem.isClosed() || !feedItem.canBeClosed()) View.GONE else View.VISIBLE
            initializeView()
        }
    }

    abstract fun initializeView()

    override val slideStyle: Int
        get() = R.style.CustomDialogFragmentSlide

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------
    private fun onCancelClicked() {
        dismiss()
    }

    abstract fun onStopClicked()

    private fun onQuitClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_QUIT_ENTOURAGE)
        EntBus.post(OnUserActEvent(OnUserActEvent.ACT_QUIT, feedItem))
        dismiss()
    }

    abstract fun onEditClicked()

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.FeedItemOptions"
        fun show(feedItem: FeedItem, manager: FragmentManager) {
            val fragment = EntourageOptionsFragment()
            val args = Bundle()
            args.putSerializable(FeedItem.KEY_FEEDITEM, feedItem)
            fragment.arguments = args
            fragment.show(manager, TAG)
        }
    }
}