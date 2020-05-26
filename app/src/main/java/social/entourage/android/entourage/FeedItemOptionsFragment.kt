package social.entourage.android.entourage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.layout_entourage_options.*
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.tape.Events.OnUserActEvent
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.tools.BusProvider.instance
import social.entourage.android.tour.TourOptionsFragment

abstract class FeedItemOptionsFragment : EntourageDialogFragment() {
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
            entourage_option_stop?.visibility = if (feedItem.isFreezed() || !feedItem.canBeClosed()) View.GONE else View.VISIBLE
            initializeView()
        }
    }

    abstract fun initializeView()

    override fun getSlideStyle(): Int {
        return R.style.CustomDialogFragmentSlide
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------
    private fun onCancelClicked() {
        dismiss()
    }

    abstract fun onStopClicked()

    private fun onQuitClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_QUIT_ENTOURAGE)
        instance.post(OnUserActEvent(OnUserActEvent.ACT_QUIT, feedItem))
        dismiss()
    }

    abstract fun onEditClicked()

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.FeedItemOptions"
        fun show(feedItem: FeedItem, manager: FragmentManager) {
            val fragment = if(feedItem.type == TimestampedObject.TOUR_CARD) TourOptionsFragment() else EntourageOptionsFragment()
            val args = Bundle()
            args.putSerializable(FeedItem.KEY_FEEDITEM, feedItem)
            fragment.arguments = args
            fragment.show(manager, TAG)
        }
    }
}