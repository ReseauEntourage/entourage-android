package social.entourage.android.old_v7.entourage

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.tape.Events.OnFeedItemCloseRequestEvent
import social.entourage.android.tools.EntBus
import kotlinx.android.synthetic.main.v7_fragment_entourage_close.*
import kotlinx.android.synthetic.main.v7_fragment_entourage_close.entourage_close_close_button
import kotlinx.android.synthetic.main.v7_fragment_event_close.*
import social.entourage.android.api.model.EntourageEvent
import social.entourage.android.tools.disable
import social.entourage.android.tools.enable
import android.text.Editable

/**
 * Offers options when closing an entourage
 * Use the [EntourageCloseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EntourageCloseFragment : DialogFragment() {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    private var feedItem: FeedItem? = null
    private var isAction = true

    private var isSuccess = false
    private var isFail = false

    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        feedItem = arguments?.getSerializable(FeedItem.KEY_FEEDITEM) as FeedItem?
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog?.window?.let {
            it.requestFeature(Window.FEATURE_NO_TITLE)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        // Inflate the layout for this fragment
        isAction = if(feedItem is EntourageEvent) false else true

        val layoutId = if(!isAction) R.layout.v7_fragment_event_close else R.layout.v7_fragment_entourage_close

        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        entourage_close_close_button?.setOnClickListener {onCloseClicked()}
        entourage_close_success_button?.setOnClickListener {onFailedClicked(null)}

        ui_bt_action_success?.setOnClickListener {
            isSuccess = !isSuccess
            isFail = false
            changeButtonsState()
        }
        ui_bt_action_fail?.setOnClickListener {
            isFail = !isFail
            isSuccess = false
            changeButtonsState()
        }

        ui_bt_action_validate?.setOnClickListener {
            ui_layout_pop_main?.visibility = View.INVISIBLE
            ui_layout_pop_ask_comment?.visibility = View.VISIBLE
        }

        ui_bt_action_ask_comment_yes?.setOnClickListener {
            ui_layout_pop_add_comment?.visibility = View.VISIBLE
            ui_layout_full_screen_fake?.visibility = View.VISIBLE
        }

        ui_bt_action_ask_comment_no?.setOnClickListener {
            if (isSuccess) {
                onSuccessClicked(null)
            }
            else {
                onFailedClicked(null)
            }
        }

        entourage_close_add_comment_button_close?.setOnClickListener {
            if (isSuccess) {
                onSuccessClicked(null)
            }
            else {
                onFailedClicked(null)
            }
        }

        ui_bt_action_add_comment_validate?.setOnClickListener {
            ui_et_add_comment?.text?.let {
                if (isSuccess) {
                    onSuccessClicked(it.toString())
                }
                else {
                    onFailedClicked(it.toString())
                }
            }
        }

        ui_et_add_comment?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                changeButtonAndValidateComment()
            }
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                changeButtonAndValidateComment()
            }
        })

        AnalyticsEvents.logEvent(AnalyticsEvents.SHOW_POP_CLOSE)

        ui_layout_pop_ask_comment?.visibility = View.GONE
        ui_layout_pop_add_comment?.visibility = View.GONE
        ui_layout_full_screen_fake?.visibility = View.GONE

        changeButtonsState()
        changeButtonAndValidateComment()
    }

    fun changeButtonAndValidateComment() {
        ui_et_add_comment?.text?.let {
            if (it.length > 0) {
                ui_bt_action_add_comment_validate?.enable()
            }
            else {
                ui_bt_action_add_comment_validate?.disable()
            }
        }
    }

    fun changeButtonsState() {
        if (isSuccess || isFail) {
            ui_bt_action_validate?.enable()
        }
        else {
            ui_bt_action_validate?.disable()
        }

        if (isSuccess) {
            ui_bt_action_success?.background = AppCompatResources.getDrawable(requireContext(),R.drawable.bg_button_rounded_orange_stroke_fill_light_pink)
            ui_bt_action_fail?.background = AppCompatResources.getDrawable(requireContext(),R.drawable.bg_button_rounded_orange_stroke_1px)
        }
        else if(isFail) {
            ui_bt_action_success?.background = AppCompatResources.getDrawable(requireContext(),R.drawable.bg_button_rounded_orange_stroke_1px)
            ui_bt_action_fail?.background = AppCompatResources.getDrawable(requireContext(),R.drawable.bg_button_rounded_orange_stroke_fill_light_pink)
        }
        else {
            ui_bt_action_success?.background = AppCompatResources.getDrawable(requireContext(),R.drawable.bg_button_rounded_orange_stroke_1px)
            ui_bt_action_fail?.background = AppCompatResources.getDrawable(requireContext(),R.drawable.bg_button_rounded_orange_stroke_1px)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.CustomDialogFragmentSlide
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------
    fun onCloseClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_CLOSE_POPUP_CANCEL)
        dismiss()
    }

    private fun onSuccessClicked(comment:String?) {
        feedItem?.let {
            EntBus.post(OnFeedItemCloseRequestEvent(it, showUI = false, success = true,comment))
          //  showEmail(R.string.entourage_close_email_title_success)
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_POP_CLOSE_SUCCESS)
            dismiss()
        }
    }

    private fun onFailedClicked(comment:String?) {
        feedItem?.let {
            EntBus.post(OnFeedItemCloseRequestEvent(it, showUI = false, success = false,comment))
           // showEmail(R.string.entourage_close_email_title_failed)
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_POP_CLOSE_FAILED)
            dismiss()
        }
    }

    // ----------------------------------
    // INNER METHODS
    // ----------------------------------
    private fun showEmail(@StringRes emailSubjectFormat: Int): Boolean {
        if (feedItem == null) return false
        // Build the email intent
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        // Set the email to
        val addresses = arrayOf(getString(R.string.contact_email))
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)
        // Set the subject
        val title = feedItem?.getTitle() ?: ""
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(emailSubjectFormat, title))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.error_no_email, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        val TAG: String? = EntourageCloseFragment::class.java.simpleName

        fun newInstance(feedItem: FeedItem?): EntourageCloseFragment {
            val fragment = EntourageCloseFragment()
            val args = Bundle()
            args.putSerializable(FeedItem.KEY_FEEDITEM, feedItem)
            fragment.arguments = args
            return fragment
        }
    }
}