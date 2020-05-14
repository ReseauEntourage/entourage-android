package social.entourage.android.entourage

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.layout_entourage_options.*
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.entourage.create.BaseCreateEntourageFragment

class EntourageOptionsFragment : FeedItemOptionsFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    override fun initializeView() {
        entourage_option_stop?.setText(R.string.tour_info_options_freeze_tour)
        if (FeedItem.STATUS_OPEN == feedItem.status) {
            entourage_option_edit?.visibility = View.VISIBLE
        }
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------
    override fun onStopClicked() {
        if (feedItem.status == FeedItem.STATUS_ON_GOING || feedItem.status == FeedItem.STATUS_OPEN) {
            //BusProvider.INSTANCE.getInstance().post(new Events.OnFeedItemCloseRequestEvent(feedItem, false));
            EntourageCloseFragment.newInstance(feedItem).show(requireActivity().supportFragmentManager, EntourageCloseFragment.TAG)
            dismiss()
        }
    }

    override fun onEditClicked() {
        if (feedItem.showEditEntourageView()) {
            BaseCreateEntourageFragment.newInstance(feedItem as BaseEntourage?).show(parentFragmentManager, BaseCreateEntourageFragment.TAG)
        } else {
            if (activity == null) return
            // just send an email
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            // Set the email to
            val addresses = arrayOf(getString(R.string.edit_action_email))
            intent.putExtra(Intent.EXTRA_EMAIL, addresses)
            // Set the subject
            val title = feedItem.getTitle() ?: ""
            val emailSubject = getString(R.string.edit_entourage_email_title, title)
            intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
            val description = feedItem.getDescription() ?:""
            val emailBody = getString(R.string.edit_entourage_email_body, description)
            intent.putExtra(Intent.EXTRA_TEXT, emailBody)
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                // Start the intent
                startActivity(intent)
            } else {
                // No Email clients
                Toast.makeText(context, R.string.error_no_email, Toast.LENGTH_SHORT).show()
            }
        }
        dismiss()
    }
}