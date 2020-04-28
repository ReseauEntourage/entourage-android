package social.entourage.android.entourage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.layout_entourage_options.*
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageEvents
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.map.Entourage
import social.entourage.android.api.model.map.FeedItem
import social.entourage.android.api.model.map.Tour
import social.entourage.android.api.tape.Events.OnFeedItemCloseRequestEvent
import social.entourage.android.api.tape.Events.OnUserActEvent
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.entourage.create.BaseCreateEntourageFragment
import social.entourage.android.tools.BusProvider.instance
import social.entourage.android.tools.Utils.getDateStringFromSeconds
import java.util.*

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
            BaseCreateEntourageFragment.newInstance(feedItem as Entourage?).show(parentFragmentManager, BaseCreateEntourageFragment.TAG)
        } else {
            if (activity == null) return
            // just send an email
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            // Set the email to
            val addresses = arrayOf(getString(R.string.edit_action_email))
            intent.putExtra(Intent.EXTRA_EMAIL, addresses)
            // Set the subject
            var title = feedItem.title
            if (title == null) title = ""
            val emailSubject = getString(R.string.edit_entourage_email_title, title)
            intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
            var description = feedItem.description
            if (description == null) description = ""
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