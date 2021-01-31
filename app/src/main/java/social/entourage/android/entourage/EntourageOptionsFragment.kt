package social.entourage.android.entourage

import android.view.View
import kotlinx.android.synthetic.main.layout_entourage_options.*
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.entourage.create.BaseCreateEntourageFragment

class EntourageOptionsFragment : FeedItemOptionsFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    val entourage:BaseEntourage
        get() = feedItem as BaseEntourage

    override fun initializeView() {
        entourage_option_stop?.setText(R.string.tour_info_options_freeze_tour)
        if (entourage.isOpen()) {
            entourage_option_edit?.visibility = View.VISIBLE
        }
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------
    override fun onStopClicked() {
        if (entourage.isOpen()) {
            //BusProvider.INSTANCE.getInstance().post(new Events.OnFeedItemCloseRequestEvent(feedItem, false));
            EntourageCloseFragment.newInstance(entourage).show(requireActivity().supportFragmentManager, EntourageCloseFragment.TAG)
            dismiss()
        }
    }

    override fun onEditClicked() {
        BaseCreateEntourageFragment.newInstance(entourage).show(parentFragmentManager, BaseCreateEntourageFragment.TAG)
        dismiss()
    }
}