package social.entourage.android.entourage.information

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.squareup.otto.Subscribe
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.entourage.layout_feeditem_options.*
import kotlinx.android.synthetic.main.fragment_tour_information.*
import kotlinx.android.synthetic.main.layout_feed_action_card.*
import kotlinx.android.synthetic.main.layout_invite_source.*
import kotlinx.android.synthetic.main.layout_private_tour_information.*
import kotlinx.android.synthetic.main.layout_public_tour_header.*
import kotlinx.android.synthetic.main.layout_public_tour_information.*
import kotlinx.android.synthetic.main.layout_tour_information_top_buttons.*
import org.joda.time.Days
import org.joda.time.LocalDate
import social.entourage.android.*
import social.entourage.android.api.model.*
import social.entourage.android.api.model.map.*
import social.entourage.android.api.tape.Events.*
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.carousel.CarouselFragment
import social.entourage.android.configuration.Configuration
import social.entourage.android.deeplinks.DeepLinksManager.linkify
import social.entourage.android.entourage.EntourageCloseFragment
import social.entourage.android.entourage.EntourageCloseFragment.Companion.newInstance
import social.entourage.android.entourage.create.BaseCreateEntourageFragment
import social.entourage.android.entourage.information.discussion.DiscussionAdapter
import social.entourage.android.entourage.information.members.MembersAdapter
import social.entourage.android.invite.InviteFriendsListener
import social.entourage.android.invite.contacts.InviteContactsFragment
import social.entourage.android.invite.phonenumber.InviteByPhoneNumberFragment
import social.entourage.android.location.EntourageLocation
import social.entourage.android.map.MapFragment.Companion.getTransparentColor
import social.entourage.android.map.MapFragment.Companion.isToday
import social.entourage.android.map.OnAddressClickListener
import social.entourage.android.service.EntourageService
import social.entourage.android.service.EntourageService.LocalBinder
import social.entourage.android.service.EntourageServiceListener
import social.entourage.android.tools.BusProvider
import social.entourage.android.tools.CropCircleTransformation
import social.entourage.android.tools.Utils
import social.entourage.android.view.EntourageSnackbar
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.ceil

class EntourageInformationFragment : EntourageDialogFragment(), EntourageServiceListener, InviteFriendsListener {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @JvmField
    @Inject
    var presenter: EntourageInformationPresenter? = null
    var entourageService: EntourageService? = null
    private val connection = ServiceConnection()
    private var isBound = false

    val discussionAdapter = DiscussionAdapter()

    private var membersAdapter: MembersAdapter? = null
    private var membersList: MutableList<TimestampedObject>? = ArrayList()

    private var apiRequestsCount = 0
    lateinit var feedItem: FeedItem
    private var requestedFeedItemUUID: String? = null
    private var requestedFeedItemType = 0
    private var requestedFeedItemShareURL: String? = null
    private var invitationId: Long = 0
    private var acceptInvitationSilently = false
    var showInfoButton = true
    private var oldestChatMessageDate: Date? = null
    private var needsMoreChatMessaged = true
    private var scrollToLastCard = true
    private val discussionScrollListener = OnScrollListener()
    private var scrollDeltaY = 0
    private var mapFragment: SupportMapFragment? = null
    private var hiddenMapFragment: SupportMapFragment? = null
    private var hiddenGoogleMap: GoogleMap? = null
    private var isTakingSnapshot = false
    private var mapSnapshot: Bitmap? = null
    private var takeSnapshotOnCameraMove = false
    private var tourTimestampList: MutableList<TourTimestamp> = ArrayList()
    var mListener: OnEntourageInformationFragmentFinish? = null

    // Handler to hide invite success layout
    private val inviteSuccessHandler = Handler()
    private val inviteSuccessRunnable = Runnable { tour_info_invite_success_layout?.visibility = View.GONE }
    private var startedTypingMessage = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_tour_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupComponent(EntourageApplication.get().entourageComponent)
        val args = arguments
        if (args != null) {
            val newFeedItem = args.getSerializable(FeedItem.KEY_FEEDITEM) as FeedItem?
            invitationId = args.getLong(KEY_INVITATION_ID)
            if (newFeedItem != null) {
                if (newFeedItem.isPrivate) {
                    feedItem = newFeedItem
                    initializeView()
                    loadPrivateCards()
                } else {
                    // public entourage
                    // we need to retrieve the whole entourage again, just to send the distance and feed position
                    val feedRank = args.getInt(KEY_FEED_POSITION)
                    var distance = 0
                    val startPoint = newFeedItem.startPoint
                    if (startPoint != null) {
                        val currentLocation = EntourageLocation.getInstance().currentLocation
                        if (currentLocation != null) {
                            distance = ceil(startPoint.distanceTo(TourPoint(currentLocation.latitude, currentLocation.longitude)) / 1000.toDouble()).toInt() // in kilometers
                        }
                    }
                    presenter!!.getFeedItem(newFeedItem.uuid, newFeedItem.type, feedRank, distance)

                }
            } else {
                requestedFeedItemShareURL = args.getString(KEY_FEED_SHARE_URL)
                requestedFeedItemUUID = args.getString(FeedItem.KEY_FEEDITEM_UUID)
                requestedFeedItemType = args.getInt(FeedItem.KEY_FEEDITEM_TYPE)
                if (requestedFeedItemType == TimestampedObject.TOUR_CARD || requestedFeedItemType == TimestampedObject.ENTOURAGE_CARD) {
                    if (!requestedFeedItemShareURL.isNullOrEmpty()) {
                        presenter?.getFeedItem(requestedFeedItemShareURL!!, requestedFeedItemType)
                    } else {
                        presenter?.getFeedItem(requestedFeedItemUUID, requestedFeedItemType, 0, 0)
                    }
                }
            }
        }
        initializeCommentEditText()
        tour_info_close?.setOnClickListener {onCloseButton()}
        tour_info_icon?.setOnClickListener {onSwitchSections()}
        tour_info_title?.setOnClickListener {onSwitchSections()}
        tour_info_description_button?.setOnClickListener {onSwitchSections()}
        tour_info_comment_send_button.setOnClickListener {onAddCommentButton()}
        tour_card_photo?.setOnClickListener {onAuthorClicked()}
        tour_card_author?.setOnClickListener {onAuthorClicked()}
        tour_info_comment_record_button.setOnClickListener {onRecord()}
        feeditem_option_share?.setOnClickListener {onShareButton()}
        invite_source_share_button?.setOnClickListener {onShareButton()}
        tour_info_more_button?.setOnClickListener {onMoreButton()}
        tour_info_options.setOnClickListener {onCloseOptionsButton()}
        feeditem_option_cancel.setOnClickListener {onCloseOptionsButton()}
        feeditem_option_stop.setOnClickListener {onStopTourButton()}
        feeditem_option_quit.setOnClickListener {quitTour()}
        tour_info_request_join_button.setOnClickListener {onJoinTourButton()}
        feeditem_option_contact.setOnClickListener {onJoinTourButton()}
        feeditem_option_join.setOnClickListener {onJoinTourButton()}
        tour_info_act_button.setOnClickListener {onActButton()}
        feeditem_option_edit.setOnClickListener {onEditEntourageButton()}
        feeditem_option_report.setOnClickListener {onReportEntourageButton()}
        feeditem_option_promote?.setOnClickListener {onPromoteEntourageButton()}
        tour_info_member_add?.setOnClickListener {onMembersAddClicked()}
        invite_source_close_button?.setOnClickListener {onCloseInviteSourceClicked()}
        invite_source_close_bottom_button?.setOnClickListener {onCloseInviteSourceClicked()}
        invite_source_contacts_button?.setOnClickListener {inviteSourceContactsButton()}
        tour_info_invited_accept_button?.setOnClickListener {v -> onAcceptInvitationClicked(v)}
        tour_info_invited_reject_button?.setOnClickListener {v -> onRejectInvitationClicked(v)}
    }

    protected fun setupComponent(entourageComponent: EntourageComponent?) {
        DaggerEntourageInformationComponent.builder()
                .entourageComponent(entourageComponent)
                .entourageInformationModule(EntourageInformationModule(this))
                .build()
                .inject(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context !is OnEntourageInformationFragmentFinish) {
            throw ClassCastException("$context must implement OnEntourageInformationFragmentFinish")
        }
        mListener = context
        doBindService()
        BusProvider.getInstance().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
        if (isBound) {
            entourageService?.unregisterServiceListener(this)
        }
        doUnbindService()
        BusProvider.getInstance().unregister(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val textMatchList: List<String> = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: return
                if (textMatchList.isNotEmpty()) {
                    if (tour_info_comment?.text.toString() == "") {
                        tour_info_comment?.setText(textMatchList[0])
                    } else {
                        tour_info_comment?.setText(tour_info_comment!!.text.toString() + " " + textMatchList[0])
                    }
                    tour_info_comment?.setSelection(tour_info_comment!!.text.length)
                    EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_OK)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == READ_CONTACTS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val handler = Handler(Looper.getMainLooper())
                handler.post { onInviteContactsClicked() }
            } else {
                EntourageSnackbar.make(entourage_information_coordinator_layout!!, R.string.invite_contacts_permission_error, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun onInviteContactsClicked() {
        //TODO("Not yet implemented")
    }

    override fun onStart() {
        super.onStart()

        //setup scroll listener
        tour_info_discussion_view?.addOnScrollListener(discussionScrollListener)
    }

    override fun onStop() {
        tour_info_discussion_view?.removeOnScrollListener(discussionScrollListener)
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        inviteSuccessRunnable.run()
        inviteSuccessHandler.removeCallbacks(inviteSuccessRunnable)
    }

    override fun getBackgroundDrawable(): ColorDrawable {
        return ColorDrawable(resources.getColor(R.color.background))
    }

    val feedItemId: String?
        get() = feedItem.uuid

    val feedItemType: Long
        get() = feedItem.type.toLong()

    // ----------------------------------
    // Button Handling
    // ----------------------------------
    fun onCloseButton() {
        // If we are showing the public section and the feed item is private
        // switch to the private section
        // otherwise just close the view
        val isPublicSectionVisible = tour_info_public_section?.visibility == View.VISIBLE
        if (isPublicSectionVisible && feedItem.isPrivate) {
            onSwitchSections()
            return
        }
        // inform the app to refrehs the my entourages feed
        BusProvider.getInstance().post(OnMyEntouragesForceRefresh(feedItem))
        try {
            dismiss()
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    fun onSwitchSections() {
        // Ignore if the entourage is not loaded or is public
        if (!feedItem.isPrivate) {
            return
        }

        // Hide the keyboard
        if (activity != null) {
            val token = dialog?.currentFocus?.windowToken
            if (token != null) {
                (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow(token, 0)
            }
        }

        // For conversation, open the author profile
        if (FeedItem.ENTOURAGE_CARD == feedItem.type && Entourage.TYPE_CONVERSATION.equals(feedItem.groupType, ignoreCase = true)) {
            if (showInfoButton) {
                // only if this screen wasn't shown from the profile page
                BusProvider.getInstance().post(OnUserViewRequestedEvent(feedItem.author.userID))
            }
            return
        }

        // Switch sections
        val isPublicSectionVisible = tour_info_public_section?.visibility == View.VISIBLE
        tour_info_public_section?.visibility = if (isPublicSectionVisible) View.GONE else View.VISIBLE
        tour_info_private_section?.visibility = if (isPublicSectionVisible) View.VISIBLE else View.GONE
        updateHeaderButtons()
        if (!isPublicSectionVisible) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_SWITCH_PUBLIC)
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_PUBLIC_VIEW_MEMBER)
        } else {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_DISCUSSION_VIEW)
        }
    }

    fun onAddCommentButton() {
        tour_info_comment?.isEnabled = false
        tour_info_comment_send_button?.isEnabled = false
        if(tour_info_comment?.text.isNullOrBlank()) return
        presenter?.sendFeedItemMessage(feedItem, tour_info_comment.text.toString())
    }

    fun onAuthorClicked() {
        BusProvider.getInstance().post(OnUserViewRequestedEvent(feedItem.author.userID))
    }

    fun onRecord() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.encounter_leave_voice_message))
        try {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_SPEECH)
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            EntourageSnackbar.make(entourage_information_coordinator_layout!!, R.string.encounter_voice_message_not_supported, Snackbar.LENGTH_SHORT).show()
            EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_NOT_SUPPORTED)
        }
    }

    fun onShareButton() {
        // close the invite source view
        tour_info_invite_source_layout?.visibility = View.GONE

        // check if the user is the author
        var isMyEntourage = false
        val me = EntourageApplication.me(context)
        if (me != null) {
            val author = feedItem.author
            if (author != null) {
                isMyEntourage = me.id == author.userID
            }
        }

        // build the share text
        val shareLink = feedItem.shareURL ?:getString(R.string.entourage_share_link)
        val shareText = when {
            feedItem.type == TimestampedObject.ENTOURAGE_CARD && BaseEntourage.TYPE_OUTING.equals(feedItem.groupType) ->{
                val df: DateFormat = SimpleDateFormat(getString(R.string.entourage_create_date_format), Locale.getDefault())
                getString(R.string.entourage_share_text_for_event, feedItem.title,df.format(feedItem.startTime), feedItem.displayAddress, shareLink)
            }
            feedItem.type == TimestampedObject.ENTOURAGE_CARD && !BaseEntourage.TYPE_OUTING.equals(feedItem.groupType) ->{
                val entourageShareVerb = getString(if (isMyEntourage) R.string.entourage_share_text_verb_for_entourage_author else if (feedItem.isPrivate) R.string.entourage_share_text_verb_for_entourage_member else R.string.entourage_share_text_verb_for_entourage_nonmember)
                getString(R.string.entourage_share_text_for_entourage, entourageShareVerb, feedItem.title, shareLink)
            }
            else -> {
                val entourageShareText = if (isMyEntourage) R.string.entourage_share_text_author else if (feedItem.isPrivate) R.string.entourage_share_text_member else R.string.entourage_share_text_non_member
                getString(entourageShareText, shareLink)
            }
        }
        // start the share intent
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.entourage_share_intent_title)))
        EntourageEvents.logEvent(if (feedItem.isPrivate) EntourageEvents.EVENT_ENTOURAGE_SHARE_MEMBER else EntourageEvents.EVENT_ENTOURAGE_SHARE_NONMEMBER)
        tour_info_options?.visibility = View.GONE
    }

    fun onMoreButton() {
        val bottomUp = AnimationUtils.loadAnimation(activity,
                R.anim.bottom_up)
        tour_info_options?.startAnimation(bottomUp)
        tour_info_options?.visibility = View.VISIBLE
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_OPTIONS_OVERLAY)
    }

    fun onCloseOptionsButton() {
        val bottomDown = AnimationUtils.loadAnimation(activity,
                R.anim.bottom_down)
        tour_info_options?.startAnimation(bottomDown)
        tour_info_options?.visibility = View.GONE
    }

    fun onStopTourButton() {
        if (feedItem.status == FeedItem.STATUS_ON_GOING || feedItem.status == FeedItem.STATUS_OPEN) {
            if (feedItem.type == TimestampedObject.TOUR_CARD) {
                val tour = feedItem as Tour?
                //compute distance
                var distance = 0.0f
                val tourPointsList = tour!!.tourPoints
                if (tourPointsList.size > 1) {
                    var startPoint = tourPointsList[0]
                    for (i in 1 until tourPointsList.size) {
                        val p = tourPointsList[i]
                        distance += p.distanceTo(startPoint)
                        startPoint = p
                    }
                }
                tour.distance = distance

                //duration
                val now = Date()
                val duration = Date(now.time - tour.startTime.time)
                val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                tour.duration = dateFormat.format(duration)

                //hide the options
                tour_info_options?.visibility = View.GONE

                //show stop tour activity
                EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE)
                mListener?.showStopTourActivity(tour)
            } else if (feedItem.type == TimestampedObject.ENTOURAGE_CARD) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE)
                //entourageService.stopFeedItem(feedItem);
                //hide the options
                tour_info_options?.visibility = View.GONE
                //show close fragment
                if (activity != null) {
                    val entourageCloseFragment = newInstance(feedItem)
                    entourageCloseFragment.show(this.requireActivity().supportFragmentManager, EntourageCloseFragment.TAG, context)
                }
            }
        } else if (feedItem.type == TimestampedObject.TOUR_CARD && feedItem.status == FeedItem.STATUS_CLOSED) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE)
            entourageService?.freezeTour(feedItem as Tour?)
        }
    }

    private fun quitTour() {
        val me = EntourageApplication.me(activity)
        if(me ==null || entourageService == null){
            EntourageSnackbar.make(entourage_information_coordinator_layout!!,  R.string.tour_info_quit_tour_error, Snackbar.LENGTH_SHORT).show()
            return
        }
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_OPTIONS_QUIT)
        showProgressBar()
        entourageService!!.removeUserFromFeedItem(feedItem, me.id)
        tour_info_options?.visibility = View.GONE
    }

    fun onJoinTourButton() {
        if (entourageService != null) {
            showProgressBar()
            when (feedItem.type) {
                TimestampedObject.TOUR_CARD -> {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_ASK_JOIN)
                    entourageService!!.requestToJoinTour(feedItem as Tour?)
                }
                TimestampedObject.ENTOURAGE_CARD -> {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_ASK_JOIN)
                    entourageService!!.requestToJoinEntourage(feedItem as Entourage?)
                }
                else -> {
                    hideProgressBar()
                }
            }
            tour_info_options?.visibility = View.GONE
        } else {
            EntourageSnackbar.make(entourage_information_coordinator_layout!!,  R.string.tour_join_request_message_error, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun onActButton() {
        if (feedItem.joinStatus == Tour.JOIN_STATUS_PENDING) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_PENDING_OVERLAY)
            onMoreButton()
        }
    }

    fun onEditEntourageButton() {
        if (activity == null) return
        if (feedItem.showEditEntourageView()) {
            tour_info_options?.visibility = View.GONE
            BaseCreateEntourageFragment.newInstance(feedItem as Entourage).show(parentFragmentManager, BaseCreateEntourageFragment.TAG)
            //hide the options
        } else {
            // just send an email
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            // Set the email to
            val addresses = arrayOf(getString(R.string.edit_action_email))
            intent.putExtra(Intent.EXTRA_EMAIL, addresses)
            // Set the subject
            val title = feedItem.title ?:  ""
            val emailSubject = getString(R.string.edit_entourage_email_title, title)
            intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
            val description = feedItem.description ?: ""
            val emailBody = getString(R.string.edit_entourage_email_body, description)
            intent.putExtra(Intent.EXTRA_TEXT, emailBody)
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                //hide the options
                tour_info_options!!.visibility = View.GONE
                // Start the intent
                startActivity(intent)
            } else {
                // No Email clients
                EntourageSnackbar.make(entourage_information_coordinator_layout!!,  R.string.error_no_email, Snackbar.LENGTH_SHORT).show()
            }
        }
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_OPTIONS_EDIT)
    }

    fun onReportEntourageButton() {
        if (activity == null) return
        // Build the email intent
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        // Set the email to
        val addresses = arrayOf(getString(R.string.contact_email))
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)
        // Set the subject
        val title = feedItem.title ?: ""
        val emailSubject = getString(R.string.report_entourage_email_title, title)
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            //hide the options
            tour_info_options?.visibility = View.GONE
            // Start the intent
            startActivity(intent)
        } else {
            // No Email clients
            EntourageSnackbar.make(entourage_information_coordinator_layout!!,  R.string.error_no_email, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun onPromoteEntourageButton() {
        if (activity == null) return
        // Build the email intent
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        // Set the email to
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.contact_email)))
        // Set the subject
        val title = feedItem.title ?: ""
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.promote_entourage_email_title, title))
        // Set the body
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.promote_entourage_email_body, title))
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            //hide the options
            tour_info_options?.visibility = View.GONE
            // Start the intent
            startActivity(intent)
        } else {
            // No Email clients
            EntourageSnackbar.make(entourage_information_coordinator_layout!!,  R.string.error_no_email, Snackbar.LENGTH_SHORT).show()
        }
    }

    protected fun onUserAddClicked() {
        if (feedItem.isSuspended) {
            EntourageSnackbar.make(entourage_information_coordinator_layout!!,  R.string.tour_info_members_add_not_allowed, Snackbar.LENGTH_SHORT).show()
            return
        }
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_INVITE_FRIENDS)
        showInviteSource()
    }

    fun onMembersAddClicked() {
        if (feedItem.isPrivate && Configuration.showInviteView()) {
            // For members show the invite screen
            onUserAddClicked()
        } else {
            // For non-members, show the share screen
            onShareButton()
        }
    }

    private fun showInviteSource() {
        tour_info_invite_source_layout?.visibility = View.VISIBLE
        if (feedItem.type == TimestampedObject.ENTOURAGE_CARD) {
            invite_source_description?.setText(if (BaseEntourage.TYPE_OUTING.equals(feedItem.groupType, ignoreCase = true)) R.string.invite_source_description_outing else R.string.invite_source_description)
        } else {
            invite_source_description?.setText(R.string.invite_source_description)
        }
    }

    fun onCloseInviteSourceClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_INVITE_CLOSE)
        tour_info_invite_source_layout?.visibility = View.GONE
    }

    private fun inviteSourceContactsButton() {
        if (activity == null) return
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_INVITE_CONTACTS)
        // check the permissions
        if (PermissionChecker.checkSelfPermission(requireActivity(), Manifest.permission.READ_CONTACTS) != PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), READ_CONTACTS_PERMISSION_CODE)
            return
        }
        // close the invite source view
        tour_info_invite_source_layout?.visibility = View.GONE
        // open the contacts fragment
        val fragment = InviteContactsFragment.newInstance(feedItem.uuid, feedItem.type)
        fragment.show(parentFragmentManager, InviteContactsFragment.TAG)
        // set the listener
        fragment.setInviteFriendsListener(this)
    }

    fun onInvitePhoneNumberClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_INVITE_PHONE)
        // close the invite source view
        tour_info_invite_source_layout?.visibility = View.GONE
        // open the contacts fragment
        val fragment = InviteByPhoneNumberFragment.newInstance(feedItem.uuid, feedItem.type)
        fragment.show(parentFragmentManager, InviteByPhoneNumberFragment.TAG)
        // set the listener
        fragment.setInviteFriendsListener(this)
    }

    fun onAcceptInvitationClicked(view: View) {
        view.isEnabled = false
        presenter?.acceptInvitation(invitationId)
    }

    fun onRejectInvitationClicked(view: View) {
        view.isEnabled = false
        presenter?.rejectInvitation(invitationId)
    }
    // ----------------------------------
    // Chat push notification
    // ----------------------------------
    /**
     * @param message
     * @return true if pushNotif has been displayed on this fragment
     */
    fun onPushNotificationChatMessageReceived(message: Message): Boolean {
        //we received a chat notification
        //check if it is referring to this feed item
        val content = message.content ?: return false
        if (content.isTourRelated && feedItem.type == FeedItem.ENTOURAGE_CARD) {
            return false
        }
        else if (content.isEntourageRelated && feedItem.type == FeedItem.TOUR_CARD) {
            return false
        }
        else if (content.joinableId != feedItem.id) {
            return false
        }
        //retrieve the last messages from server
        scrollToLastCard = true
        presenter?.getFeedItemMessages(feedItem)
        return true
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun initializeView() {
        apiRequestsCount = 0

        // Hide the loading view
        tour_info_loading_view?.visibility = View.GONE
        updateFeedItemInfo()
        if (invitationId > 0) {
            // already a member
            // send a silent accept
            acceptInvitationSilently = true
            presenter?.acceptInvitation(invitationId)
            // ignore the invitation
            invitationId = 0
        }

        // switch to appropiate section
        if (feedItem.isPrivate) {
            updateJoinStatus()
            switchToPrivateSection()
        } else {
            switchToPublicSection()
        }

        // check if we are opening an invitation
        tour_info_invited_layout?.visibility = if (invitationId == 0L) View.GONE else View.VISIBLE

        // update the scroll list layout
        updatePublicScrollViewLayout()

        // for newly created entourages, open the invite friends screen automatically if the feed item is not suspended
        if (feedItem.isNewlyCreated && feedItem.showInviteViewAfterCreation() && !feedItem.isSuspended) {
            showInviteSource()
        }

        // check if we need to display the carousel
        val me = EntourageApplication.me(context) ?: return
        if (me.isOnboardingUser) {
            showCarousel()
            me.isOnboardingUser = false
            if (activity != null) {
                EntourageApplication.get(activity)?.entourageComponent?.authenticationController?.saveUser(me)
            }
        }
    }

    private fun initializeOptionsView() {
        feeditem_option_stop?.visibility = View.GONE
        feeditem_option_quit?.visibility = View.GONE
        feeditem_option_edit?.visibility = View.GONE
        feeditem_option_share?.visibility = View.GONE
        feeditem_option_report?.visibility = View.GONE
        feeditem_option_join?.visibility = View.GONE
        feeditem_option_contact?.visibility = View.GONE
        feeditem_option_promote?.visibility = View.GONE
        val hideJoinButton = feedItem.isPrivate || FeedItem.JOIN_STATUS_PENDING == feedItem.joinStatus || feedItem.isFreezed
        feeditem_option_join?.visibility =  if (feedItem.type == TimestampedObject.TOUR_CARD || hideJoinButton) View.GONE else View.VISIBLE
        feeditem_option_contact?.visibility = if (feedItem.type != TimestampedObject.TOUR_CARD ||hideJoinButton) View.GONE else View.VISIBLE
        if (feedItem.author == null) return
        val myId = EntourageApplication.me(activity)?.id ?: return
        if (feedItem.author.userID != myId) {
            if ((FeedItem.JOIN_STATUS_PENDING == feedItem.joinStatus || FeedItem.JOIN_STATUS_ACCEPTED == feedItem.joinStatus) && !feedItem.isFreezed) {
                feeditem_option_quit?.visibility = View.VISIBLE
                feeditem_option_quit?.setText(if (FeedItem.JOIN_STATUS_PENDING == feedItem.joinStatus) R.string.tour_info_options_cancel_request else R.string.tour_info_options_quit_tour)
            }
            if (feedItem.type == FeedItem.ENTOURAGE_CARD) {
                feeditem_option_report?.visibility = View.VISIBLE
            }
        } else {
            feeditem_option_stop?.visibility = if (feedItem.isFreezed || !feedItem.canBeClosed()) View.GONE else View.VISIBLE
            feeditem_option_stop?.setText(if (feedItem.isClosed || feedItem.type == FeedItem.ENTOURAGE_CARD) R.string.tour_info_options_freeze_tour else R.string.tour_info_options_stop_tour)
            if (feedItem.type == FeedItem.ENTOURAGE_CARD && FeedItem.STATUS_OPEN == feedItem.status) {
                feeditem_option_edit?.visibility = View.VISIBLE
            }
        }
        if (feedItem.type == FeedItem.ENTOURAGE_CARD && !feedItem.isSuspended) {
            // Share button available only for entourages and non-members
            feeditem_option_share?.visibility = View.VISIBLE
        }
        if (feeditem_option_promote != null && membersList != null && feedItem.type == FeedItem.ENTOURAGE_CARD) {
            for (member in membersList!!) {
                if (member !is TourUser || member.userId != myId) continue
                if (member.groupRole?.equals("organizer", ignoreCase = true) == true) {
                    feeditem_option_promote.visibility = View.VISIBLE
                }
                break
            }
        }
    }

    private fun updateHeaderButtons() {
        val isPublicSectionVisible = tour_info_public_section?.visibility == View.VISIBLE
        tour_info_more_button?.visibility = if (isPublicSectionVisible) View.VISIBLE else View.GONE
        tour_info_description_button?.visibility = if (isPublicSectionVisible || !showInfoButton) View.GONE else View.VISIBLE
        if (invitationId > 0) {
            tour_info_more_button?.visibility = View.GONE
        }
    }

    private fun updatePublicScrollViewLayout() {
        if (tour_info_public_scrollview == null) return
        val lp = tour_info_public_scrollview.layoutParams as RelativeLayout.LayoutParams
        val oldRule = lp.rules[RelativeLayout.ABOVE]
        val newRule:Int? = when {
            tour_info_invited_layout?.visibility == View.VISIBLE -> {
                tour_info_invited_layout?.id
            }
            tour_info_request_join_layout?.visibility == View.VISIBLE -> {
                tour_info_request_join_layout?.id
            }
            else -> tour_info_act_layout?.id
        }

        if (oldRule != newRule) {
            lp.addRule(RelativeLayout.ABOVE, newRule ?:oldRule)
            tour_info_public_scrollview.layoutParams = lp
            tour_info_public_scrollview.forceLayout()
        }
    }

    private fun initializeDiscussionList() {
        //init the recycler view
        tour_info_discussion_view?.layoutManager = LinearLayoutManager(context)
        discussionAdapter.removeAll()
        tour_info_discussion_view?.adapter = discussionAdapter

        //add the cards
        val cachedCardInfoList = feedItem.cachedCardInfoList
        if (cachedCardInfoList != null) {
            discussionAdapter.addItems(cachedCardInfoList)
        }

        //clear the added cards info
        feedItem.clearAddedCardInfoList()
        if (feedItem.type == TimestampedObject.TOUR_CARD) {
            val now = Date()
            //add the start time
            if (FeedItem.STATUS_ON_GOING == feedItem.status) {
                addDiscussionTourStartCard(now)
            }

            //check if we need to add the Tour closed card
            if (feedItem.isClosed) {
                addDiscussionTourEndCard(now)
            }
        }

        //scroll to last card
        scrollToLastCard()

        //find the oldest chat message received
        initOldestChatMessageDate()
    }

    private fun initOldestChatMessageDate() {
        for (timestampedObject in feedItem.cachedCardInfoList) {
            if (timestampedObject.javaClass != ChatMessage::class.java) continue
            val chatCreationDate = (timestampedObject as ChatMessage).creationDate ?: continue
            if (oldestChatMessageDate == null) {
                oldestChatMessageDate = chatCreationDate
            } else if (chatCreationDate.before(oldestChatMessageDate)) {
                oldestChatMessageDate = chatCreationDate
            }
        }
    }

    private fun initializeMap() {
        if (!isAdded) return
        try {
            if (mapFragment == null) {
                val googleMapOptions = GoogleMapOptions()
                googleMapOptions.zOrderOnTop(true)
                mapFragment = SupportMapFragment.newInstance(googleMapOptions)
            }
            childFragmentManager.beginTransaction().replace(R.id.tour_info_map_layout, mapFragment!!).commit()
            drawFeedItemOnMap()
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    private fun updateMap() {
        if (mapFragment == null || !mapFragment!!.isAdded) {
            initializeMap()
        } else {
            drawFeedItemOnMap()
        }
    }

    private fun drawFeedItemOnMap() {
        mapFragment?.getMapAsync { googleMap ->
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            googleMap.uiSettings.isMapToolbarEnabled = false
            googleMap.clear()
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                    activity, R.raw.map_styles_json))
            if (feedItem.type == TimestampedObject.TOUR_CARD) {
                val tour = feedItem as Tour?
                val tourPoints = tour?.tourPoints ?: return@getMapAsync
                if (tourPoints.size > 0) {
                    //setup the camera position to starting point
                    val startPoint = tourPoints[0]
                    val cameraPosition = CameraPosition(LatLng(startPoint.latitude, startPoint.longitude), EntourageLocation.INITIAL_CAMERA_FACTOR_ENTOURAGE_VIEW, 0F, 0F)
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    val markerOptions = MarkerOptions().position(LatLng(startPoint.latitude, startPoint.longitude))
                    googleMap.addMarker(markerOptions)

                    //add the tour points
                    val line = PolylineOptions()
                    val color = getTrackColor(tour.tourType, tour.startTime)
                    line.zIndex(2f)
                    line.width(15f)
                    line.color(color)
                    for (tourPoint in tourPoints) {
                        line.add(tourPoint.location)
                    }
                    googleMap.addPolyline(line)
                }
            } else if (feedItem.type == TimestampedObject.ENTOURAGE_CARD) {
                val startPoint = feedItem.startPoint  ?: return@getMapAsync
                val position = startPoint.location

                // move camera
                val cameraPosition = CameraPosition(LatLng(startPoint.latitude, startPoint.longitude), EntourageLocation.INITIAL_CAMERA_FACTOR_ENTOURAGE_VIEW, 0F, 0F)
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                if (feedItem.showHeatmapAsOverlay()) {
                    // add heatmap
                    val icon = BitmapDescriptorFactory.fromResource(feedItem.heatmapResourceId)
                    val groundOverlayOptions = GroundOverlayOptions()
                            .image(icon)
                            .position(position, Entourage.HEATMAP_SIZE, Entourage.HEATMAP_SIZE)
                            .clickable(true)
                            .anchor(0.5f, 0.5f)
                    googleMap.addGroundOverlay(groundOverlayOptions)
                } else {
                    // add marker
                    val drawable = AppCompatResources.getDrawable(requireContext(), feedItem.heatmapResourceId)
                    val icon = Utils.getBitmapDescriptorFromDrawable(drawable!!, Entourage.getMarkerSize(context), Entourage.getMarkerSize(context))
                    val markerOptions = MarkerOptions()
                            .icon(icon)
                            .position(position)
                            .draggable(false)
                            .anchor(0.5f, 0.5f)
                    googleMap.addMarker(markerOptions)
                }
            }
        }
    }

    private fun initializeHiddenMap() {
        if (!isAdded) return
        try {
            if (hiddenMapFragment == null) {
                val googleMapOptions = GoogleMapOptions()
                googleMapOptions.zOrderOnTop(true)
                hiddenMapFragment = SupportMapFragment.newInstance(googleMapOptions)
            }
            childFragmentManager.beginTransaction().replace(R.id.tour_info_hidden_map_layout, hiddenMapFragment!!).commit()
            hiddenMapFragment!!.getMapAsync { googleMap ->
                googleMap.uiSettings.isMyLocationButtonEnabled = false
                googleMap.uiSettings.isMapToolbarEnabled = false
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        activity, R.raw.map_styles_json))
                if (tourTimestampList.size > 0) {
                    val tourTimestamp = tourTimestampList[0]
                    if (tourTimestamp.tourPoint != null) {
                        //put the pin
                        val pin = MarkerOptions().position(tourTimestamp.tourPoint.location)
                        googleMap.addMarker(pin)
                        //move the camera
                        val camera = CameraUpdateFactory.newLatLngZoom(tourTimestamp.tourPoint.location, MAP_SNAPSHOT_ZOOM.toFloat())
                        googleMap.moveCamera(camera)
                    }
                } else {
                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(MAP_SNAPSHOT_ZOOM.toFloat()))
                }
                googleMap.setOnMapLoadedCallback { getMapSnapshot() }
                googleMap.setOnCameraIdleListener {
                    if (takeSnapshotOnCameraMove) {
                        getMapSnapshot()
                        hiddenGoogleMap = null
                    }
                }
                hiddenGoogleMap = googleMap
            }
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    private fun getMapSnapshot() {
        if (hiddenGoogleMap == null) return
        if (tourTimestampList.size == 0) {
            hiddenGoogleMap = null
            return
        }
        val tourTimestamp = tourTimestampList[0]
        isTakingSnapshot = true
        //take the snapshot
        hiddenGoogleMap!!.snapshot { bitmap -> //save the snapshot
            mapSnapshot = bitmap
            snapshotTaken(tourTimestamp)
            //signal it has finished taking the snapshot
            isTakingSnapshot = false
            //check if we need more snapshots
            if (tourTimestampList.size > 1) {
                val nextTourTimestamp = tourTimestampList[1]
                if (nextTourTimestamp.tourPoint != null) {
                    val distance = nextTourTimestamp.tourPoint.distanceTo(tourTimestamp.tourPoint)
                    val visibleRegion = hiddenGoogleMap!!.projection.visibleRegion
                    val nearLeft = visibleRegion.nearLeft
                    val nearRight = visibleRegion.nearRight
                    val result = floatArrayOf(0f)
                    Location.distanceBetween(nearLeft.latitude, nearLeft.longitude, nearRight.latitude, nearRight.longitude, result)
                    takeSnapshotOnCameraMove = distance < result[0]

                    //put the pin
                    hiddenGoogleMap!!.clear()
                    val pin = MarkerOptions().position(nextTourTimestamp.tourPoint.location)
                    hiddenGoogleMap!!.addMarker(pin)
                    //move the camera
                    val camera = CameraUpdateFactory.newLatLngZoom(nextTourTimestamp.tourPoint.location, MAP_SNAPSHOT_ZOOM.toFloat())
                    hiddenGoogleMap!!.moveCamera(camera)
                }
            } else {
                hiddenGoogleMap = null
            }
            tourTimestampList.remove(tourTimestamp)
        }
    }

    private fun snapshotTaken(tourTimestamp: TourTimestamp?) {
        if (mapSnapshot == null || tourTimestamp == null) return
        tourTimestamp.snapshot = mapSnapshot
        discussionAdapter.updateCard(tourTimestamp)
    }

    private fun getTrackColor(type: String, date: Date): Int {
        if (context == null) return Color.GRAY
        val color = ContextCompat.getColor(requireContext(), Tour.getTypeColorRes(type))
        return if (!isToday(date)) {
            getTransparentColor(color)
        } else color
    }

    private fun initializeCommentEditText() {
        tour_info_comment?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    tour_info_comment_record_button?.visibility = View.GONE
                    tour_info_comment_send_button?.visibility = View.VISIBLE
                    if (!startedTypingMessage) {
                        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_WRITE_MESSAGE)
                        startedTypingMessage = true
                    }
                } else {
                    tour_info_comment_record_button?.visibility = View.VISIBLE
                    tour_info_comment_send_button?.visibility = View.GONE
                    startedTypingMessage = false
                }
            }
        })
        tour_info_comment?.setOnClickListener {
            if (tour_info_discussion_view != null) {
                val lastVisibleItemPosition = (tour_info_discussion_view!!.layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition()
                tour_info_discussion_view!!.postDelayed({ tour_info_discussion_view?.scrollToPosition(lastVisibleItemPosition) }, 500)
            }
        }
    }

    private fun initializeMembersView() {
        if (membersAdapter == null) {
            // Initialize the recycler view
            tour_info_members?.layoutManager = LinearLayoutManager(context)
            membersAdapter = MembersAdapter()
            tour_info_members?.adapter = membersAdapter
        }

        // add the members
        membersAdapter?.addItems(membersList)

        // Show the members count
        val membersCount = feedItem.numberOfPeople
        tour_info_member_count?.text = getString(R.string.tour_info_members_count, membersCount)

        // hide the 'invite a friend' for a tour
        tour_info_member_add?.visibility = if (feedItem.type != TimestampedObject.TOUR_CARD) View.VISIBLE else View.GONE
    }

    private fun updateFeedItemInfo() {
        // Update the header
        tour_info_title?.text = feedItem.title
        val iconURL = feedItem.iconURL
        if (iconURL != null && tour_info_icon!= null) {
            Picasso.get().cancelRequest(tour_info_icon!!)
            Picasso.get()
                    .load(iconURL)
                    .placeholder(R.drawable.ic_user_photo_small)
                    .transform(CropCircleTransformation())
                    .into(tour_info_icon)
            tour_info_icon!!.visibility = View.VISIBLE
        } else {
            val iconDrawable = feedItem.getIconDrawable(context)
            if (iconDrawable == null) {
                tour_info_icon?.visibility = View.GONE
            } else {
                tour_info_icon?.visibility = View.VISIBLE
                tour_info_icon?.setImageDrawable(iconDrawable)
            }
        }
        tour_info_title_full?.text = feedItem.title
        //

        if (BaseEntourage.TYPE_OUTING.equals(feedItem.groupType, ignoreCase = true)) {
            tour_summary_group_type?.text = getResources().getString(R.string.entourage_type_outing)
            tour_summary_author_name?.text = ""
            tour_info_location?.visibility = View.GONE
        } else {
            tour_summary_group_type?.text = feedItem.getFeedTypeLong(context)
            tour_summary_author_name?.text = feedItem.author.userName
            tour_info_location?.visibility = View.VISIBLE
            tour_info_location?.text = feedItem.displayAddress
        }
        val avatarURLAsString = feedItem.author.avatarURLAsString
        if (avatarURLAsString != null && tour_info_author_photo!=null) {
            Picasso.get()
                    .load(Uri.parse(avatarURLAsString))
                    .placeholder(R.drawable.ic_user_photo_small)
                    .transform(CropCircleTransformation())
                    .into(tour_info_author_photo)
        } else {
            tour_info_author_photo?.setImageResource(R.drawable.ic_user_photo_small)
        }
        val partner = feedItem.author.partner
        if (partner != null && tour_info_partner_logo !=null) {
            val partnerLogoURL = partner.smallLogoUrl
            if (partnerLogoURL != null) {
                Picasso.get()
                        .load(Uri.parse(partnerLogoURL))
                        .placeholder(R.drawable.partner_placeholder)
                        .transform(CropCircleTransformation())
                        .into(tour_info_partner_logo)
            } else {
                tour_info_partner_logo!!.setImageDrawable(null)
            }
        } else {
            tour_info_partner_logo?.setImageDrawable(null)
        }
        tour_info_people_count?.text = getString(R.string.tour_cell_numberOfPeople, feedItem.numberOfPeople)

        // update description
        if(tour_info_description!=null) {
            if (feedItem.description != null && feedItem.description.isNotEmpty()) {
                tour_info_description?.text = feedItem.description
                tour_info_description?.visibility = View.VISIBLE
            } else {
                tour_info_description?.visibility = View.GONE
            }
            linkify(tour_info_description)
        }

        // metadata
        updateMetadataView()
        if (tour_info_timestamps != null) {
            if (BaseEntourage.TYPE_ACTION.equals(feedItem.groupType, ignoreCase = true)
                    || Tour.TYPE_TOUR.equals(feedItem.groupType, ignoreCase = true)) {
                showActionTimestamps(feedItem.creationTime, feedItem.updatedTime)
            } else {
                tour_info_timestamps?.visibility = View.GONE
            }
        }
    }

    private fun showActionTimestamps(createdTime: Date, updatedTime: Date) {
        val timestamps = ArrayList<String?>()
        timestamps.add(getString(R.string.entourage_info_creation_time, formattedDaysIntervalFromToday(createdTime)))
        if (!LocalDate(createdTime).isEqual(LocalDate())) {
            timestamps.add(getString(R.string.entourage_info_update_time, formattedDaysIntervalFromToday(updatedTime)))
        }
        tour_info_timestamps?.text = TextUtils.join(" - ", timestamps)
        tour_info_timestamps?.visibility = View.VISIBLE
    }

    private fun formattedDaysIntervalFromToday(rawDate: Date): String {
        val today = LocalDate()
        val date = LocalDate(rawDate)
        if (date.isEqual(today)) return this.getString(R.string.date_today).toLowerCase()
        val days = Days.daysBetween(date, today).days

        val dayString = when(days) {
            1 -> this.getString(R.string.date_yesterday).toLowerCase()
            in 2..14 -> String.format(getString(R.string.x_days_ago),days)
            in 15..31 -> getString(R.string.date_this_month)
            else -> {
                val nbMonths = days / 30
                String.format(getString(R.string.x_months_ago),nbMonths)
            }
        }
        return  dayString
    }

    private fun updateMetadataView() {
        if(tour_info_metadata_layout==null) return
        // show the view only for outing
        val metadata: BaseEntourage.Metadata? = if (feedItem.type == TimestampedObject.ENTOURAGE_CARD && feedItem is Entourage) (feedItem as Entourage).metadata else null
        val metadataVisible = if (feedItem.type == TimestampedObject.ENTOURAGE_CARD) (BaseEntourage.TYPE_OUTING.equals(feedItem.groupType, ignoreCase = true) && metadata != null)  else false
        tour_info_metadata_layout.visibility = if (metadataVisible) View.VISIBLE else View.GONE

        if (!metadataVisible) return

        // populate the data
        if (feedItem.author != null) {
            tour_info_metadata_organiser?.text = getString(R.string.tour_info_metadata_organiser_format, feedItem.author.userName)
        }
        if (metadata == null) return
        if (BaseEntourage.TYPE_OUTING.equals(feedItem.groupType, ignoreCase = true)) {
            //Format dates same day or different days.
            val startCalendar = Calendar.getInstance()
            startCalendar.time = (feedItem as Entourage).metadata.startDate
            val endCalendar = Calendar.getInstance()
            endCalendar.time = (feedItem as Entourage).metadata.endDate
            if (startCalendar[Calendar.DAY_OF_YEAR] == endCalendar[Calendar.DAY_OF_YEAR]) {
                tour_info_metadata_datetime?.text = getString(R.string.tour_info_metadata_dateStart_hours_format,
                        metadata.getStartDateFullAsString(context),
                        metadata.getStartEndTimesAsString(context))
            } else {
                //du xx à hh au yy à hh
                tour_info_metadata_datetime?.text = getString(R.string.tour_info_metadata_dateStart_End_hours_format,
                        metadata.getStartDateFullAsString(context),
                        metadata.getStartTimeAsString(context),
                        metadata.getEndDateFullAsString(context),
                        metadata.getEndTimeAsString(context))
            }
        } else {
            tour_info_metadata_datetime?.text = getString(R.string.tour_info_metadata_date_format,
                    metadata.getStartDateAsString(context),
                    metadata.getStartTimeAsString(context))
        }

        setAddressView(metadata)
    }

    private fun setAddressView(metadata: BaseEntourage.Metadata) {
        tour_info_metadata_address?.let {
            val displayAddress = metadata.displayAddress
            it.text = displayAddress
            it.paintFlags = it.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            if (displayAddress != null) {
                it.setOnClickListener(OnAddressClickListener(requireActivity(), displayAddress))
            }
        }
    }

    private fun switchToPublicSection() {
        tour_info_act_layout?.visibility = View.VISIBLE
        tour_info_public_section?.visibility = View.VISIBLE
        tour_info_private_section?.visibility = View.GONE
        updateHeaderButtons()
        initializeOptionsView()
        updateJoinStatus()
        initializeMap()
        initializeMembersView()
        if (feedItem.isPrivate) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_PUBLIC_VIEW_MEMBER)
        } else {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_PUBLIC_VIEW_NONMEMBER)
        }
    }

    private fun switchToPrivateSection() {
        tour_info_act_layout?.visibility = if (feedItem.isFreezed) View.VISIBLE else View.GONE
        tour_info_request_join_layout?.visibility = View.GONE
        tour_info_public_section?.visibility = View.GONE
        tour_info_private_section?.visibility = View.VISIBLE
        if (mapFragment == null) {
            initializeMap()
        }
        if (hiddenMapFragment == null) {
            initializeHiddenMap()
        }
        updateHeaderButtons()
        initializeOptionsView()

        //hide the comment section if the user is not accepted or tour is freezed
        if (feedItem.joinStatus != FeedItem.JOIN_STATUS_ACCEPTED || feedItem.isFreezed) {
            tour_info_comment_layout?.visibility = View.GONE
            tour_info_discussion_view?.setBackgroundResource(R.color.background_login_grey)
        } else {
            tour_info_comment_layout?.visibility = View.VISIBLE
            tour_info_discussion_view?.setBackgroundResource(R.color.background)
        }
        initializeDiscussionList()
        initializeMembersView()
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_DISCUSSION_VIEW)
    }

    private fun loadPrivateCards() {
        presenter?.getFeedItemJoinRequests(feedItem)
        presenter?.getFeedItemMembers(feedItem)
        presenter?.getFeedItemMessages(feedItem)
        if (feedItem.isMine(context)) {
            presenter?.getFeedItemEncounters(feedItem)
        }
    }

    private fun updateJoinStatus() {
        val dividerColor = R.color.accent
        var textColor = R.color.accent
        tour_info_request_join_layout?.visibility = View.GONE
        tour_info_act_layout?.visibility = View.VISIBLE
        tour_info_act_button?.setPadding(0, 0, 0, 0)
        tour_info_act_button?.setPaddingRelative(0, 0, 0, 0)
        if (feedItem.isFreezed) {
            // MI: Instead of hiding it, display the freezed text
            //tour_info_act_button.setEnabled(false);
            tour_info_act_button?.setTextColor(resources.getColor(feedItem.freezedCTAColor))
            tour_info_act_button?.setText(feedItem.freezedCTAText)
            tour_info_act_button?.setPadding(resources.getDimensionPixelOffset(R.dimen.act_button_right_padding), 0, 0, 0)
            tour_info_act_button?.setPaddingRelative(resources.getDimensionPixelOffset(R.dimen.act_button_right_padding), 0, 0, 0)
            tour_info_act_button?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        } else {
            when (feedItem.joinStatus) {
                Tour.JOIN_STATUS_PENDING -> {
                    tour_info_act_button?.isEnabled = true
                    tour_info_act_button?.setText(R.string.tour_cell_button_pending)
                    tour_info_act_button?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                }
                Tour.JOIN_STATUS_ACCEPTED -> {
                    tour_info_act_button?.setPadding(0, 0, resources.getDimensionPixelOffset(R.dimen.act_button_right_padding), 0)
                    tour_info_act_button?.setPaddingRelative(0, 0, resources.getDimensionPixelOffset(R.dimen.act_button_right_padding), 0)
                    tour_info_act_button?.isEnabled = false
                    tour_info_act_button?.setText(R.string.tour_cell_button_accepted)
                    tour_info_act_button?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                }
                Tour.JOIN_STATUS_REJECTED -> {
                    tour_info_act_button?.isEnabled = false
                    tour_info_act_button?.setText(R.string.tour_cell_button_rejected)
                    tour_info_act_button?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    textColor = R.color.tomato
                }
                else -> {
                    // Different layout for requesting to join
                    tour_info_act_layout?.visibility = View.INVISIBLE
                    tour_info_request_join_layout?.visibility = View.VISIBLE
                    tour_info_request_join_title?.setText(feedItem.joinRequestTitle)
                    tour_info_request_join_button?.setText(feedItem.joinRequestButton)
                    updatePublicScrollViewLayout()
                    return
                }
            }
            tour_info_act_button?.setTextColor(resources.getColor(textColor))
            tour_info_act_divider_left?.setBackgroundResource(dividerColor)
            tour_info_act_divider_right?.setBackgroundResource(dividerColor)
        }
        updatePublicScrollViewLayout()
    }

    private fun updateDiscussionList(scrollToLastCard: Boolean = true) {
        val addedCardInfoList = feedItem.addedCardInfoList ?: return
        if (addedCardInfoList.size == 0) {
            return
        }
        for (cardInfo in addedCardInfoList) {
            discussionAdapter.addCardInfoAfterTimestamp(cardInfo)
        }

        //clear the added cards info
        feedItem.clearAddedCardInfoList()
        if (scrollToLastCard) {
            //scroll to last card
            scrollToLastCard()
        }
    }

    private fun addDiscussionTourStartCard(now: Date) {
        val distance = 0f
        val duration = if (feedItem.startTime != null && !feedItem.isClosed) (now.time - feedItem.startTime.time) else 0
        val startPoint = feedItem.startPoint
        val tourTimestamp = TourTimestamp(
                feedItem.startTime,
                now,
                feedItem.type,
                FeedItem.STATUS_ON_GOING,
                startPoint,
                duration,
                distance
        )
        discussionAdapter.addCardInfo(tourTimestamp)
    }

    private fun addDiscussionTourEndCard(now: Date) {
        var distance = 0f
        val duration = if (feedItem.startTime != null && feedItem.endTime != null) {
            feedItem.endTime.time - feedItem.startTime.time
        } else 0
        var endPoint = feedItem.endPoint
        if (feedItem.type == TimestampedObject.TOUR_CARD) {
            val tour = feedItem as Tour?
            val tourPointsList = tour!!.tourPoints
            if (tourPointsList.size > 1) {
                var startPoint = tourPointsList[0]
                endPoint = tourPointsList[tourPointsList.size - 1]
                for (i in 1 until tourPointsList.size) {
                    val p = tourPointsList[i]
                    distance += p.distanceTo(startPoint)
                    startPoint = p
                }
            }
            val tourTimestamp = TourTimestamp(
                    feedItem.endTime,
                    if (feedItem.endTime != null) feedItem.endTime else now,
                    feedItem.type,
                    FeedItem.STATUS_CLOSED,
                    endPoint,
                    duration,
                    distance
            )
            discussionAdapter.addCardInfoAfterTimestamp(tourTimestamp)
        } else if (feedItem.type == TimestampedObject.ENTOURAGE_CARD) {
            // Retrieve the latest chat messages, which should contain the close feed message
            presenter?.getFeedItemMessages(feedItem, oldestChatMessageDate)
        }
    }

    private fun scrollToLastCard() {
        if (discussionAdapter.itemCount > 0) {
            tour_info_discussion_view?.scrollToPosition(discussionAdapter.itemCount - 1)
            feedItem.numberOfUnreadMessages = 0
        }
    }

    private fun setEncountersToReadOnly() {
        val encounters = feedItem.getTypedCardInfoList(TimestampedObject.ENCOUNTER)
        if (encounters.isNullOrEmpty()) return
        for (timestampedObject in encounters) {
            val encounter = timestampedObject as Encounter
            encounter.isReadOnly = true
            discussionAdapter.updateCard(encounter)
        }
    }

    fun showProgressBar() {
        apiRequestsCount++
        tour_info_progress_bar?.visibility = View.VISIBLE
    }

    protected fun hideProgressBar() {
        apiRequestsCount--
        if (apiRequestsCount <= 0) {
            tour_info_progress_bar?.visibility = View.GONE
            apiRequestsCount = 0
        }
    }

    private val onEntourageInformationFragmentFinish: OnEntourageInformationFragmentFinish?
        get() {
            val activity: Activity = activity ?: return null
            return activity as OnEntourageInformationFragmentFinish?
        }

    // ----------------------------------
    // Bus handling
    // ----------------------------------
    @Subscribe
    fun onUserJoinRequestUpdateEvent(event: OnUserJoinRequestUpdateEvent) {
        presenter?.updateUserJoinRequest(event.userId, event.update, event.feedItem)
    }

    @Subscribe
    fun onEntourageUpdated(event: OnEntourageUpdated) {
        val updatedEntourage = event.entourage ?: return
        // Check if it is our displayed entourage
        if (feedItem.type != updatedEntourage.type || feedItem.id != updatedEntourage.id) return
        // Update the UI
        feedItem = updatedEntourage
        updateFeedItemInfo()
        updateMap()
    }

    @Subscribe
    fun onEncounterUpdated(event: OnEncounterUpdated) {
        val updatedEncounter = event.encounter ?: return
        val oldEncounter = discussionAdapter.findCard(TimestampedObject.ENCOUNTER, updatedEncounter.id) as Encounter?
                ?: return
        feedItem.removeCardInfo(oldEncounter)
        discussionAdapter.updateCard(updatedEncounter)
    }

    // ----------------------------------
    // API callbacks
    // ----------------------------------
    fun onFeedItemReceived(feedItem: FeedItem) {
        if (activity == null || !isAdded) return
        hideProgressBar()
        this.feedItem = feedItem
        initializeView()
        if (feedItem.isPrivate) {
            loadPrivateCards()
        } else {
            presenter?.getFeedItemMembers(feedItem)
        }
    }

    fun onFeedItemNotFound() {
        if (activity == null || !isAdded) return
        hideProgressBar()
        if (context == null) return
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.tour_info_error_retrieve_entourage)
        builder.setPositiveButton(R.string.close) { _, _ -> dismiss() }
        builder.create().show()
    }

    @Synchronized
    fun onFeedItemNoUserReceived() {
        if (activity == null || !isAdded) return
        //hide the progress bar
        hideProgressBar()

        //update the discussion list
        updateDiscussionList()
    }

    @Synchronized
    fun onFeedItemUsersReceived(tourUsers: List<TourUser>, context: String?) {
        if (activity == null || !isAdded) return
        if (context == null) {
            // members
            onFeedItemMembersReceived(tourUsers)
            initializeOptionsView()
        } else {
            onFeedItemJoinRequestsReceived(tourUsers)
        }

        // users processed use standard updateUI
        onFeedItemNoUserReceived()
    }

    private fun onFeedItemMembersReceived(tourUsers: List<TourUser>) {
        membersList?.clear()
        // iterate over the received users
        for (tourUser in tourUsers) {
            //show only the accepted users
            if (tourUser.status != FeedItem.JOIN_STATUS_ACCEPTED) {
                // Remove the user from the members list, in case the user left the entourage
                membersAdapter?.removeCard(tourUser)
                //show the pending and cancelled requests too (by skipping the others)
                if (!(tourUser.status == FeedItem.JOIN_STATUS_PENDING || tourUser.status == FeedItem.JOIN_STATUS_CANCELLED)) {
                    continue
                }
            }
            tourUser.feedItem = feedItem
            tourUser.isDisplayedAsMember = true
            membersList?.add(tourUser)
        }
        initializeMembersView()
    }

    private fun onFeedItemJoinRequestsReceived(tourUsers: List<TourUser>) {
        val timestampedObjectList = ArrayList<TimestampedObject>()
        // iterate over the received users
        for (tourUser in tourUsers) {
            // skip the author
            if (tourUser.userId == feedItem.author?.userID) {
                continue
            }
            //show only the accepted users
            if (tourUser.status != FeedItem.JOIN_STATUS_ACCEPTED) {
                // Remove the user from the members list, in case the user left the entourage
                membersAdapter?.removeCard(tourUser)
                //show the pending and cancelled requests too (by skipping the others)
                if (!(tourUser.status == FeedItem.JOIN_STATUS_PENDING || tourUser.status == FeedItem.JOIN_STATUS_CANCELLED)) {
                    continue
                }
            }
            tourUser.feedItem = feedItem

            // check if we already have this user
            val oldUser = discussionAdapter.findCard(tourUser) as TourUser?
            if (oldUser != null && oldUser.status != tourUser.status) {
                discussionAdapter.updateCard(tourUser)
            } else {
                timestampedObjectList.add(tourUser)
            }
        }
        feedItem.addCardInfoList(timestampedObjectList)
    }

    fun onFeedItemNoNewMessages() {
        if (activity == null || !isAdded) return
        //hide the progress bar
        hideProgressBar()

        //update the discussion list
        updateDiscussionList(scrollToLastCard)
        scrollToLastCard = false
    }

    fun onFeedItemMessagesReceived(chatMessageList: List<ChatMessage>) {
        if (activity == null || !isAdded) return
        if (chatMessageList.isNotEmpty()) {
            //check who sent the message
            val authenticationController = EntourageApplication.get(activity).entourageComponent.authenticationController
            if (authenticationController.isAuthenticated) {
                val me = authenticationController.user.id
                for (chatMessage in chatMessageList) {
                    chatMessage.setIsMe(chatMessage.userId == me)
                }
            }
            //val timestampedObjectList: List<TimestampedObject> = ArrayList<TimestampedObject>(chatMessageList)
            if (feedItem.addCardInfoList(chatMessageList) > 0) {
                //remember the last chat message
                val lastChatMessage = feedItem.addedCardInfoList[feedItem.addedCardInfoList.size - 1] as ChatMessage
                feedItem.setLastMessage(lastChatMessage.content, lastChatMessage.userName)
                feedItem.updatedTime = lastChatMessage.timestamp

                //remember oldest chat message
                oldestChatMessageDate = (feedItem.addedCardInfoList[0] as ChatMessage).creationDate
            }
        } else {
            //no need to ask for more messages
            needsMoreChatMessaged = false
        }

        //all messages processed then use standard upadteUI
        onFeedItemNoNewMessages()
    }

    fun onFeedItemMessageSent(chatMessage: ChatMessage?) {
        hideProgressBar()
        tour_info_comment?.isEnabled = true
        tour_info_comment_send_button?.isEnabled = true
        if (chatMessage == null) {
            EntourageSnackbar.make(entourage_information_coordinator_layout!!,  R.string.tour_info_error_chat_message, Snackbar.LENGTH_SHORT).show()
            return
        }
        tour_info_comment?.setText("")

        //hide the keyboard
        if (tour_info_comment?.hasFocus() == true && activity != null) {
            (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow(tour_info_comment!!.windowToken, 0)
        }

        //add the message to the list
        chatMessage.setIsMe(true)
        feedItem.addCardInfo(chatMessage)
        updateDiscussionList()
    }

    fun onFeedItemEncountersReceived(encounterList: List<Encounter>?) {
        if (encounterList != null) {
            EntourageApplication.me(context)?.let {
                for (encounter in encounterList) {
                    encounter.setIsMyEncounter(encounter.userId == it.id)
                    encounter.isReadOnly = feedItem.isClosed
                }
            }
            feedItem.addCardInfoList(encounterList)
        }

        //hide the progress bar
        hideProgressBar()

        //update the discussion list
        updateDiscussionList()
    }

    /*protected fun onTourQuited(status: String?) {
        hideProgressBar()
        if (status == null) {
            EntourageSnackbar.make(entourage_information_coordinator_layout!!,  R.string.tour_info_quit_tour_error, Snackbar.LENGTH_SHORT).show()
        }
    }*/

    fun onUserJoinRequestUpdated(userId: Int, status: String, error: Int) {
        hideProgressBar()
        if (error == EntourageError.ERROR_NONE) {
            // Updated ok
            val messageId = if (FeedItem.JOIN_STATUS_REJECTED == status) R.string.tour_join_request_rejected else R.string.tour_join_request_success
            EntourageSnackbar.make(entourage_information_coordinator_layout!!,  messageId, Snackbar.LENGTH_SHORT).show()
            // Update the card
            val card = discussionAdapter.findCard(TimestampedObject.TOUR_USER_JOIN, userId.toLong()) as TourUser?
            if (card != null) {
                if (FeedItem.JOIN_STATUS_ACCEPTED == status) {
                    card.status = FeedItem.JOIN_STATUS_ACCEPTED
                    discussionAdapter.updateCard(card)
                    // Add the user to members list too
                    val clone = card.clone()
                    clone.isDisplayedAsMember = true
                    membersAdapter?.addCardInfo(clone)
                } else {
                    // remove from the adapter
                    discussionAdapter.removeCard(card)
                    // remove from cached cards
                    feedItem.removeCardInfo(card)
                }
            }
            // Remove the push notification
            if (FeedItem.JOIN_STATUS_ACCEPTED == status) {
                EntourageApplication.get()?.removePushNotification(feedItem, userId, PushNotificationContent.TYPE_NEW_JOIN_REQUEST)
            }
        } else if (error == EntourageError.ERROR_BAD_REQUEST) {
            // Assume that the other user cancelled the request
            val card = discussionAdapter.findCard(TimestampedObject.TOUR_USER_JOIN, userId.toLong()) as TourUser? ?:return
            when (status){
                FeedItem.JOIN_STATUS_ACCEPTED -> {
                    // Mark the user as accepted
                    card.status = FeedItem.JOIN_STATUS_ACCEPTED
                    discussionAdapter.updateCard(card)
                    // Add a quited card
                    val clone = card.clone()
                    clone.status = FeedItem.JOIN_STATUS_QUITED
                    discussionAdapter.addCardInfoAfterTimestamp(clone)
                }
                FeedItem.JOIN_STATUS_REJECTED -> {
                    // Mark the user as cancelled
                    card.status = FeedItem.JOIN_STATUS_CANCELLED
                    // Remove the message
                    card.message = ""
                    discussionAdapter.updateCard(card)
                }
                else -> {
                    // remove from the adapter
                    discussionAdapter.removeCard(card)
                }
            }
            // remove from cached cards
            feedItem.removeCardInfo(card)
        } else {
            // other Error
            EntourageSnackbar.make(entourage_information_coordinator_layout!!,  R.string.tour_join_request_error, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun onInvitationStatusUpdated(success: Boolean, status: String) {
        tour_info_invited_accept_button?.isEnabled = true
        tour_info_invited_reject_button?.isEnabled = true
        if (success) {
            invitationId = 0
            if (acceptInvitationSilently) {
                acceptInvitationSilently = false
            } else {
                // Update UI
                tour_info_invited_layout?.visibility = View.GONE
                EntourageSnackbar.make(entourage_information_coordinator_layout!!,  R.string.invited_updated_ok, Snackbar.LENGTH_SHORT).show()
                if (Invitation.STATUS_ACCEPTED == status) {
                    // Invitation accepted, refresh the lists and status
                    feedItem.joinStatus = FeedItem.JOIN_STATUS_ACCEPTED
                    switchToPrivateSection()
                    loadPrivateCards()
                    updateHeaderButtons()
                    //tour_info_act_layout.setVisibility(View.GONE);
                }
                updatePublicScrollViewLayout()
            }

            // Post an event
            BusProvider.getInstance().post(OnInvitationStatusChanged(feedItem, status))
        } else if (!acceptInvitationSilently) {
            EntourageSnackbar.make(entourage_information_coordinator_layout!!,  R.string.invited_updated_error, Snackbar.LENGTH_SHORT).show()
        }
    }

    // ----------------------------------
    // SERVICE BINDING METHODS
    // ----------------------------------
    fun doBindService() {
        if (activity != null) {
            try {
                val intent = Intent(activity, EntourageService::class.java)
                requireActivity().startService(intent)
                requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
            } catch (e: IllegalStateException) {
                Timber.w(e)
            }
        }
    }

    fun doUnbindService() {
        if (isBound) {
            activity?.unbindService(connection)
            isBound = false
        }
    }

    // ----------------------------------
    // Entourage Service listener implementation
    // ----------------------------------
    override fun onFeedItemClosed(closed: Boolean, updatedFeedItem: FeedItem) {
        //ignore requests that are not related to our feed item
        if (feedItem.type != updatedFeedItem.type) return
        if (updatedFeedItem.id != feedItem.id) return
        if (closed) {
            feedItem.status = updatedFeedItem.status
            feedItem.endTime = updatedFeedItem.endTime
            if (updatedFeedItem.status == FeedItem.STATUS_CLOSED && updatedFeedItem.isPrivate) {
                addDiscussionTourEndCard(Date())
                updateDiscussionList()
                setEncountersToReadOnly()
            }
            if (updatedFeedItem.isFreezed) {
                tour_info_comment_layout?.visibility = View.GONE
            }
            tour_info_options?.visibility = View.GONE
            initializeOptionsView()
            updateHeaderButtons()
            updateJoinStatus()
        } else {
            EntourageSnackbar.make(entourage_information_coordinator_layout!!, R.string.tour_close_fail, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onLocationUpdated(location: LatLng) {}
    override fun onLocationStatusUpdated(active: Boolean) {}
    override fun onUserStatusChanged(user: TourUser, feedItem: FeedItem) {
        //ignore requests that are not related to our feed item
        if (feedItem.type != this.feedItem.type || feedItem.id != this.feedItem.id) return
        hideProgressBar()
        //close the overlay
        onCloseOptionsButton()

        //update the local tour info
        val oldPrivateStatus = tour_info_private_section?.visibility == View.VISIBLE
        feedItem.joinStatus = user.status
        val currentPrivateStatus = feedItem.isPrivate
        //update UI
        if (oldPrivateStatus != currentPrivateStatus) {
            if (feedItem.isPrivate) {
                switchToPrivateSection()
                loadPrivateCards()
            } else {
                switchToPublicSection()
            }
        } else {
            updateHeaderButtons()
            initializeOptionsView()
            updateJoinStatus()
        }
    }

    // ----------------------------------
    // RecyclerView.OnScrollListener
    // ----------------------------------
    private fun showCarousel() {
        val h = Handler()
        h.postDelayed({
            // Check if the activity is still running
            if (activity?.isFinishing ==true) {
                return@postDelayed
            }
            if (!isVisible) {
                return@postDelayed
            }
            CarouselFragment().show(parentFragmentManager, CarouselFragment.TAG)
        }, Constants.CAROUSEL_DELAY_MILLIS)
    }

    // ----------------------------------
    // InviteFriendsListener
    // ----------------------------------
    override fun onInviteSent() {
        // Show the success layout
        tour_info_invite_success_layout?.visibility = View.VISIBLE
        // Start the timer to hide the success layout
        inviteSuccessHandler.postDelayed(inviteSuccessRunnable, Constants.INVITE_SUCCESS_HIDE_DELAY)
    }

    // ----------------------------------
    // CAROUSEL
    // ----------------------------------
    private inner class OnScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (!needsMoreChatMessaged) return
            scrollDeltaY += dy
            //check if user is scrolling up and pass the threshold
            if (dy < 0 && abs(scrollDeltaY) >= SCROLL_DELTA_Y_THRESHOLD) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val firstVisibleItemPosition = layoutManager!!.findFirstVisibleItemPosition()
                val adapterPosition = recyclerView.findViewHolderForLayoutPosition(firstVisibleItemPosition)!!.adapterPosition
                val timestamp = discussionAdapter.getCardAt(adapterPosition)?.timestamp
                if (timestamp != null && oldestChatMessageDate != null && timestamp.before(oldestChatMessageDate)) {
                    presenter?.getFeedItemMessages(feedItem, oldestChatMessageDate)
                }
                scrollDeltaY = 0
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    interface OnEntourageInformationFragmentFinish {
        fun showStopTourActivity(tour: Tour?)
        fun closeEntourageInformationFragment(fragment: EntourageInformationFragment?)
    }

    private inner class ServiceConnection : android.content.ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (activity != null) {
                entourageService = (service as LocalBinder).service
                entourageService?.registerServiceListener(this@EntourageInformationFragment)
                isBound = true
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            entourageService?.unregisterServiceListener(this@EntourageInformationFragment)
            entourageService = null
            isBound = false
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "fragment_tour_information"
        private const val VOICE_RECOGNITION_REQUEST_CODE = 2
        private const val READ_CONTACTS_PERMISSION_CODE = 3
        private const val SCROLL_DELTA_Y_THRESHOLD = 20
        private const val MAP_SNAPSHOT_ZOOM = 15
        private const val KEY_INVITATION_ID = "social.entourage.android_KEY_INVITATION_ID"
        private const val KEY_FEED_POSITION = "social.entourage.android.KEY_FEED_POSITION"
        private const val KEY_FEED_SHARE_URL = "social.entourage.android.KEY_FEED_SHARE_URL"

        // ----------------------------------
        // LIFECYCLE
        // ----------------------------------
        //TODO check that all values are not null
        fun newInstance(feedItem: FeedItem, invitationId: Long, feedRank: Int): EntourageInformationFragment {
            val fragment = EntourageInformationFragment()
            val args = Bundle()
            args.putSerializable(FeedItem.KEY_FEEDITEM, feedItem)
            args.putLong(KEY_INVITATION_ID, invitationId)
            args.putInt(KEY_FEED_POSITION, feedRank)
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun newInstance(feedItemUUID: String, feedItemType: Int, invitationId: Long): EntourageInformationFragment {
            val fragment = EntourageInformationFragment()
            val args = Bundle()
            args.putString(FeedItem.KEY_FEEDITEM_UUID, feedItemUUID)
            args.putInt(FeedItem.KEY_FEEDITEM_TYPE, feedItemType)
            args.putLong(KEY_INVITATION_ID, invitationId)
            fragment.arguments = args
            return fragment
        }

        fun newInstance(shareURL: String, feedItemType: Int): EntourageInformationFragment {
            val fragment = EntourageInformationFragment()
            val args = Bundle()
            args.putString(KEY_FEED_SHARE_URL, shareURL)
            args.putInt(FeedItem.KEY_FEEDITEM_TYPE, feedItemType)
            fragment.arguments = args
            return fragment
        }
    }
}