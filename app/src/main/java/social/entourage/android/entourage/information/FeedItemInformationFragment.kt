package social.entourage.android.entourage.information

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
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
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.PermissionChecker
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_entourage_information.*
import kotlinx.android.synthetic.main.layout_detail_action_description.*
import kotlinx.android.synthetic.main.layout_detail_event_action_creator.*
import kotlinx.android.synthetic.main.layout_detail_event_action_date.*
import kotlinx.android.synthetic.main.layout_detail_event_action_location.*
import kotlinx.android.synthetic.main.layout_detail_event_action_top_view.*
import kotlinx.android.synthetic.main.layout_detail_event_description.*
import kotlinx.android.synthetic.main.layout_entourage_information_top_buttons.*
import kotlinx.android.synthetic.main.layout_entourage_options.*
import kotlinx.android.synthetic.main.layout_feed_action_card.*
import kotlinx.android.synthetic.main.layout_invite_source.*
import kotlinx.android.synthetic.main.layout_private_entourage_information.*
import kotlinx.android.synthetic.main.layout_public_entourage_header.*
import kotlinx.android.synthetic.main.layout_public_entourage_information.*
import org.joda.time.Days
import org.joda.time.LocalDate
import social.entourage.android.*
import social.entourage.android.api.model.*
import social.entourage.android.api.model.feed.*
import social.entourage.android.api.tape.Events.*
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.configuration.Configuration
import social.entourage.android.entourage.create.BaseCreateEntourageFragment
import social.entourage.android.entourage.information.discussion.DiscussionAdapter
import social.entourage.android.entourage.information.members.MembersAdapter
import social.entourage.android.entourage.invite.InviteFriendsListener
import social.entourage.android.entourage.invite.contacts.InviteContactsFragment
import social.entourage.android.base.location.EntLocation
import social.entourage.android.service.EntService
import social.entourage.android.service.EntourageServiceListener
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.EntError
import social.entourage.android.tools.ShareMessageFragment
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.EntSnackbar
import social.entourage.android.tour.TourInformationFragment
import timber.log.Timber
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil

abstract class FeedItemInformationFragment : BaseDialogFragment(), EntourageServiceListener, InviteFriendsListener {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    protected val serviceConnection = ServiceConnection()

    protected val discussionAdapter = DiscussionAdapter()

    private var membersAdapter: MembersAdapter? = null
    protected var membersList: MutableList<EntourageUser>? = ArrayList()

    private var apiRequestsCount = 0
    protected lateinit var feedItem: FeedItem
    private var invitationId: Long = 0
    private var acceptInvitationSilently = false
    private var showInfoButton = true
    protected var oldestChatMessageDate: Date? = null
    private var needsMoreChatMessaged = true
    protected var scrollToLastCard = true
    private val discussionScrollListener = OnScrollListener()
    private var scrollDeltaY = 0
    protected var mapFragment: SupportMapFragment? = null

    // Handler to hide invite success layout
    private val inviteSuccessHandler = Handler()
    private val inviteSuccessRunnable = Runnable { entourage_info_invite_success_layout?.visibility = View.GONE }
    private var startedTypingMessage = false
    private var isFromActions = false

    abstract fun presenter(): FeedItemInformationPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_entourage_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupComponent(EntourageApplication.get().components)
        arguments?.let { isFromActions = it.getBoolean(KEY_ISFROMACTIONS) }
        invitationId = arguments?.getLong(KEY_INVITATION_ID) ?: 0
        (arguments?.getSerializable(FeedItem.KEY_FEEDITEM) as? FeedItem)?.let { newFeedItem ->
            feedItem = newFeedItem

            if (feedItem is EntourageEvent) {
                AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_FEEDDETAIL_EVENT)
            }
            else {
                AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_FEEDDETAIL_ACTION)
            }

            if (newFeedItem.isPrivate()) {
                initializeView()
                loadPrivateCards()
            } else {
                // public entourage
                // we need to retrieve the whole entourage again, just to send the distance and feed position
                val feedRank = arguments?.getInt(KEY_FEED_POSITION) ?: 0
                val distance = newFeedItem.getStartPoint()?.let { startPoint ->
                    EntLocation.currentLocation?.let { currentLocation ->
                        ceil(startPoint.distanceTo(LocationPoint(currentLocation.latitude, currentLocation.longitude)) / 1000.toDouble()).toInt() // in kilometers
                    } ?: 0
                } ?: 0
                presenter().getFeedItem(newFeedItem.uuid
                        ?: "", newFeedItem.type, feedRank, distance)
            }
        } ?: run {
            onFeedItemNotFound()
            dismiss()
            return
        }

        initializeCommentEditText()
        //TODO split into smaller functions
        entourage_info_close?.setOnClickListener {onCloseButton()}
        entourage_info_icon?.setOnClickListener {onSwitchSections()}
        entourage_info_title?.setOnClickListener {onSwitchSections()}
        entourage_info_description_button?.setOnClickListener {onSwitchSections()}
        entourage_info_comment_send_button?.setOnClickListener {onAddCommentButton()}
        tour_card_photo?.setOnClickListener {onAuthorClicked()}
        tour_card_author?.setOnClickListener {onAuthorClicked()}
        entourage_info_comment_record_button?.setOnClickListener {onRecord()}
        entourage_option_share?.setOnClickListener {
            showInviteSource(true)
            entourage_info_options?.visibility = View.GONE
        }
        invite_source_share_button?.setOnClickListener { onShareEntourageButton()}
        entourage_info_more_button?.setOnClickListener {onMoreButton()}
        entourage_info_options?.setOnClickListener {onCloseOptionsButton()}
        entourage_option_cancel?.setOnClickListener {onCloseOptionsButton()}
        entourage_option_stop?.setOnClickListener {onStopTourButton()}
        entourage_option_quit?.setOnClickListener {quitEntourage()}
        entourage_info_request_join_button?.setOnClickListener {onJoinButton()}
        entourage_option_contact?.setOnClickListener {onJoinButton()}
        entourage_option_join?.setOnClickListener {onJoinButton()}
        entourage_info_act_button?.setOnClickListener {onActButton()}
        entourage_option_edit?.setOnClickListener {onEditEntourageButton()}
        entourage_option_promote?.setOnClickListener {onPromoteEntourageButton()}
        entourage_info_member_add?.setOnClickListener {onMembersAddClicked()}
        invite_source_close_button?.setOnClickListener {onCloseInviteSourceClicked()}
        invite_source_close_bottom_button?.setOnClickListener {onCloseInviteSourceClicked()}
        invite_source_contacts_button?.setOnClickListener {onShareButton()}
        entourage_info_invited_accept_button?.setOnClickListener { v -> onAcceptInvitationClicked(v)}
        entourage_info_invited_reject_button?.setOnClickListener { v -> onRejectInvitationClicked(v)}
        invite_source_number_button?.setOnClickListener { inviteSourceContactsButton() }

        entourage_option_reopen?.setOnClickListener {onReopenEntourage()}

        ui_iv_button_faq?.setOnClickListener { onShowFaq() }
    }

    protected abstract fun setupComponent(entourageComponent: EntourageComponent?)

    abstract fun getItemType(): Int

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val textMatchList: List<String> = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: return
                if (textMatchList.isNotEmpty()) {
                    entourage_info_comment?.let {
                        if (it.text.toString().isBlank()) {
                            it.setText(textMatchList[0])
                        } else {
                            it.setText(it.text.toString() + " " + textMatchList[0])
                        }
                        it.setSelection(it.text.length)
                    }
                    AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_OK)
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
                entourage_information_coordinator_layout?.let {
                    EntSnackbar.make(it, R.string.invite_contacts_permission_error, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun onInviteContactsClicked() {
        //TODO("Not yet implemented")
    }

    override fun onStart() {
        super.onStart()

        //setup scroll listener
        //TODO MOVE TO ViewCreated ?
        entourage_info_discussion_view?.addOnScrollListener(discussionScrollListener)
    }

    override fun onStop() {
        entourage_info_discussion_view?.removeOnScrollListener(discussionScrollListener)
        EntBus.post(OnRefreshActionsInfos())

        try {
            super.onStop()
        }
        catch(e: Exception) {
            Timber.w(e)
        }
    }

    override fun onPause() {
        super.onPause()
        inviteSuccessRunnable.run()
        inviteSuccessHandler.removeCallbacks(inviteSuccessRunnable)
    }

    override val backgroundDrawable: ColorDrawable?
        get() = ColorDrawable(ResourcesCompat.getColor(resources, R.color.background, null))

    val feedItemId: String?
        get() = feedItem.uuid

    // ----------------------------------
    // Button Handling
    // ----------------------------------
    private fun onCloseButton() {
        // If we are showing the public section and the feed item is private
        // switch to the private section
        // otherwise just close the view
        if (entourage_info_public_section?.visibility == View.VISIBLE && feedItem.isPrivate() && !isFromActions) {
            onSwitchSections()
        } else {
            // inform the app to refresh the my entourages feed
            EntBus.post(OnMyEntouragesForceRefresh(feedItem))
            EntourageApplication.get(context).updateBadgeCountForFeedItem(feedItem)
            try {
                dismiss()
            } catch (e: IllegalStateException) {
                Timber.w(e)
            }
        }
    }

    override fun onDestroy() {
        EntBus.post(OnRefreshActionsInfos())
        super.onDestroy()
    }

    private fun onSwitchSections() {
        // Ignore if the entourage is not loaded or is public
        if (!feedItem.isPrivate()) {
            return
        }

        // Hide the keyboard
        activity?.let {
            dialog?.currentFocus?.windowToken?.let { token ->
                (it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow(token, 0)
            }
        }

        // For conversation, open the author profile
        if (feedItem is EntourageConversation) {
            if (showInfoButton) {
                // only if this screen wasn't shown from the profile page
                feedItem.author?.let {EntBus.post(OnUserViewRequestedEvent(it.userID))}
            }
        } else {
            // Switch sections
            val isPublicSectionVisible = entourage_info_public_section?.visibility == View.VISIBLE
            entourage_info_public_section?.visibility = if (isPublicSectionVisible) View.GONE else View.VISIBLE
            entourage_info_private_section?.visibility = if (isPublicSectionVisible) View.VISIBLE else View.GONE
            updateHeaderButtons()
            if (!isPublicSectionVisible) {
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_VIEW_SWITCH_PUBLIC)
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_PUBLIC_VIEW_MEMBER)
                entourage_info_title?.visibility = View.INVISIBLE
                entourage_info_icon?.visibility = View.INVISIBLE
            } else {
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_DISCUSSION_VIEW)
                entourage_info_title?.visibility = View.VISIBLE
                entourage_info_icon?.visibility = View.VISIBLE
            }
        }
    }

    private fun onAddCommentButton() {
        entourage_info_comment_send_button?.isEnabled = false
        entourage_info_comment?.let {
            it.isEnabled = false
            if(it.text?.isNotBlank() == true)
                presenter().sendFeedItemMessage(feedItem, it.text.toString())
        }
    }

    private fun onAuthorClicked() {
        feedItem.author?.let {
            EntBus.post(OnUserViewRequestedEvent(it.userID))
        }
    }

    private fun onRecord() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.encounter_leave_voice_message))
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_VIEW_SPEECH)
        try {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            entourage_information_coordinator_layout?.let {EntSnackbar.make(it, R.string.encounter_voice_message_not_supported, Snackbar.LENGTH_SHORT).show()}
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_NOT_SUPPORTED)
        }
    }

    private fun onShareEntourageButton() {
        // close the invite source view
        entourage_info_invite_source_layout?.visibility = View.GONE

        feedItem.uuid?.let {
            ShareMessageFragment.newInstance(it).show(parentFragmentManager, ShareMessageFragment.TAG)
        }
    }

    private fun onShareButton() {
        // close the invite source view
        entourage_info_invite_source_layout?.visibility = View.GONE

        // build the share text
        val shareLink = feedItem.shareURL ?:getString(R.string.entourage_share_link)
        val shareText = getString(R.string.entourage_share_text_for_entourage, shareLink)
        // start the share intent
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.entourage_share_intent_title)))
        AnalyticsEvents.logEvent(if (feedItem.isPrivate()) AnalyticsEvents.EVENT_ENTOURAGE_SHARE_MEMBER else AnalyticsEvents.EVENT_ENTOURAGE_SHARE_NONMEMBER)
        entourage_info_options?.visibility = View.GONE
    }

    private fun onMoreButton() {
        val bottomUp = AnimationUtils.loadAnimation(activity,
                R.anim.bottom_up)
        entourage_info_options?.startAnimation(bottomUp)
        entourage_info_options?.visibility = View.VISIBLE
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_VIEW_OPTIONS_OVERLAY)
    }

    private fun onCloseOptionsButton() {
        val bottomDown = AnimationUtils.loadAnimation(activity, R.anim.bottom_down)
        entourage_info_options?.startAnimation(bottomDown)
        entourage_info_options?.visibility = View.GONE
    }

    protected abstract fun onStopTourButton()

    protected fun quitEntourage() {
        val me = EntourageApplication.me(activity)
        if(me ==null || serviceConnection.boundService == null){
            entourage_information_coordinator_layout?.let {EntSnackbar.make(it,  R.string.tour_info_quit_tour_error, Snackbar.LENGTH_SHORT).show()}
            return
        }
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_VIEW_OPTIONS_QUIT)
        showProgressBar()
        serviceConnection.boundService?.removeUserFromFeedItem(feedItem, me.id)
        entourage_info_options?.visibility = View.GONE
    }

    protected abstract fun onJoinButton()

    private fun onActButton() {
        if (feedItem.joinStatus == FeedItem.JOIN_STATUS_PENDING) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_PENDING_OVERLAY)
            onMoreButton()
        }
    }

    private fun onEditEntourageButton() {
        if (activity == null) return
        entourage_info_options?.visibility = View.GONE
        BaseCreateEntourageFragment.newInstance(feedItem as BaseEntourage).show(parentFragmentManager, BaseCreateEntourageFragment.TAG)
        //hide the options
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_VIEW_OPTIONS_EDIT)
    }

    private fun onPromoteEntourageButton() {
        if (activity == null) return
        // Build the email intent
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        // Set the email to
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.contact_email)))
        // Set the subject
        val title = feedItem.getTitle() ?: ""
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.promote_entourage_email_title, title))
        // Set the body
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.promote_entourage_email_body, title))
        try {
            //hide the options
            entourage_info_options?.visibility = View.GONE
            // Start the intent
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // No Email clients
            entourage_information_coordinator_layout?.let {EntSnackbar.make(it,  R.string.error_no_email, Snackbar.LENGTH_SHORT).show()}
        }
    }

    private fun onUserAddClicked() {
        if (feedItem.isSuspended()) {
            entourage_information_coordinator_layout?.let {EntSnackbar.make(it,  R.string.tour_info_members_add_not_allowed, Snackbar.LENGTH_SHORT).show()}
            return
        }
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_VIEW_INVITE_FRIENDS)
        showInviteSource(false)
    }

    private fun onMembersAddClicked() {
        if (feedItem.isPrivate() && Configuration.showInviteView()) {
            // For members show the invite screen
            onUserAddClicked()
        } else {
            // For non-members, show the share screen
            showInviteSource(true)
        }
    }

    private fun onReopenEntourage() {
        serviceConnection.boundService?.reopenFeedItem(feedItem)
    }

    protected abstract fun showInviteSource(isShareOnly:Boolean)

    private fun onCloseInviteSourceClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_VIEW_INVITE_CLOSE)
        entourage_info_invite_source_layout?.visibility = View.GONE
    }

    private fun inviteSourceContactsButton() {
        if (activity == null) return
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_VIEW_INVITE_CONTACTS)
        // check the permissions
        if (PermissionChecker.checkSelfPermission(requireActivity(), Manifest.permission.READ_CONTACTS) != PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), READ_CONTACTS_PERMISSION_CODE)
            return
        }
        // close the invite source view
        entourage_info_invite_source_layout?.visibility = View.GONE
        // open the contacts fragment
        val fragment = InviteContactsFragment.newInstance(feedItem.uuid, feedItem.type)
        fragment.show(parentFragmentManager, InviteContactsFragment.TAG)
        // set the listener
        fragment.inviteFriendsListener = this
    }

    private fun onAcceptInvitationClicked(view: View) {
        view.isEnabled = false
        presenter().acceptInvitation(invitationId)
    }

    private fun onRejectInvitationClicked(view: View) {
        view.isEnabled = false
        presenter().rejectInvitation(invitationId)
    }

    private fun onShowFaq() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_FEEDITEMINFO_FAQ)
        (activity as? MainActivity)?.showWebViewForLinkId(Constants.FAQ_LINK_ID)
    }
    // ----------------------------------
    // Chat push notification
    // ----------------------------------
    /**
     * @param message
     * @return true if pushNotif has been displayed on this fragment
     */
    abstract fun onPushNotificationChatMessageReceived(message: Message): Boolean

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    protected open fun initializeView() {
        apiRequestsCount = 0

        // Hide the loading view
        entourage_info_loading_view?.visibility = View.GONE
        updateFeedItemInfo()

        // switch to appropiate section (in case of an invitation we swtich to public section
        if (feedItem.isPrivate() && invitationId==0L) {
            updateJoinStatus()
            switchToPrivateSection()
        } else {
            switchToPublicSection()
        }

        // check if we are opening an invitation
        entourage_info_invited_layout?.visibility = if (invitationId == 0L) View.GONE else View.VISIBLE

        // update the scroll list layout
        updatePublicScrollViewLayout()

        // for newly created entourages, open the invite friends screen automatically if the feed item is not suspended
        if (feedItem.isNewlyCreated && feedItem.showInviteViewAfterCreation() && !feedItem.isSuspended()) {
            showInviteSource(false)
        }

        if(isFromActions) {
            onSwitchSections()
        }
    }

    private fun updateHeaderButtons() {
        val isPublicSectionVisible = entourage_info_public_section?.visibility == View.VISIBLE
        entourage_info_more_button?.visibility = if (isPublicSectionVisible) View.VISIBLE else View.GONE
        entourage_info_description_button?.visibility = if (isPublicSectionVisible || !showInfoButton) View.GONE else View.VISIBLE
        if (invitationId > 0) {
            entourage_info_more_button?.visibility = View.GONE
        }
    }

    private fun updatePublicScrollViewLayout() {
        val lp = (entourage_info_public_scrollview?.layoutParams as? RelativeLayout.LayoutParams) ?: return
        val oldRule = lp.rules[RelativeLayout.ABOVE]
        val newRule:Int? = when {
            entourage_info_invited_layout?.visibility == View.VISIBLE -> {
                entourage_info_invited_layout?.id
            }
            entourage_info_request_join_layout?.visibility == View.VISIBLE -> {
                entourage_info_request_join_layout?.id
            }
            else -> entourage_info_act_layout?.id
        }

        if (oldRule != newRule) {
            lp.addRule(RelativeLayout.ABOVE, newRule ?:oldRule)
            entourage_info_public_scrollview.layoutParams = lp
            entourage_info_public_scrollview.forceLayout()
        }
    }

    protected abstract fun initializeOptionsView()

    private fun initializeDiscussionList() {
        //init the recycler view
        entourage_info_discussion_view?.layoutManager = LinearLayoutManager(context)
        discussionAdapter.removeAll()
        entourage_info_discussion_view?.adapter = discussionAdapter

        //add the cards
        discussionAdapter.addItems(feedItem.cachedCardInfoList)

        //clear the added cards info
        feedItem.clearAddedCardInfoList()
        addSpecificCards()
        //scroll to last card
        scrollToLastCard()

        //find the oldest chat message received
        initOldestChatMessageDate()
    }

    protected abstract fun addSpecificCards()

    private fun initOldestChatMessageDate() {
        for (timestampedObject in feedItem.cachedCardInfoList) {
            if (timestampedObject !is ChatMessage) continue
            val chatCreationDate = timestampedObject.creationDate
            if (oldestChatMessageDate == null) {
                oldestChatMessageDate = chatCreationDate
            } else if (chatCreationDate.before(oldestChatMessageDate)) {
                oldestChatMessageDate = chatCreationDate
            }
        }
    }

    protected fun initializeMap() {
        if (!isAdded) return
        try {
            if (mapFragment == null) {
                val googleMapOptions = GoogleMapOptions()
                googleMapOptions.zOrderOnTop(true)
                mapFragment = SupportMapFragment.newInstance(googleMapOptions)
            }
            mapFragment?.let {
                childFragmentManager.beginTransaction().replace(R.id.entourage_info_map_layout, it).commit()
                drawFeedItemOnMap()
            }
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    protected fun drawFeedItemOnMap() {
        mapFragment?.getMapAsync { googleMap ->
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            googleMap.uiSettings.isMapToolbarEnabled = false
            googleMap.clear()
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity, R.raw.map_styles_json))
            drawMap(googleMap)
        }
    }

    protected abstract fun drawMap(googleMap: GoogleMap)

    abstract fun initializeHiddenMap()

    private fun initializeCommentEditText() {
        entourage_info_comment?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    entourage_info_comment_record_button?.visibility = View.GONE
                    entourage_info_comment_send_button?.visibility = View.VISIBLE
                    if (!startedTypingMessage) {
                        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_VIEW_WRITE_MESSAGE)
                        startedTypingMessage = true
                    }
                } else {
                    entourage_info_comment_record_button?.visibility = View.VISIBLE
                    entourage_info_comment_send_button?.visibility = View.GONE
                    startedTypingMessage = false
                }
            }
        })
        entourage_info_comment?.setOnClickListener {
            entourage_info_discussion_view?.let {view ->
                (view.layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition()?.let { lastVisibleItemPosition ->
                    view.postDelayed({ view.scrollToPosition(lastVisibleItemPosition) }, 500)
                }
            }
        }
    }

    private fun initializeMembersView() {
        if (membersAdapter == null) {
            // Initialize the recycler view
            entourage_info_members?.layoutManager = LinearLayoutManager(context)
            //Fix recyclerview inside scrollview + fix inside xml
            entourage_info_public_scrollview?.isNestedScrollingEnabled = true
            entourage_info_members?.isNestedScrollingEnabled = false
            membersAdapter = MembersAdapter()
            entourage_info_members?.adapter = membersAdapter
        }

        membersAdapter?.removeAll()
        // add the members
        membersList?.let { membersAdapter?.addItems(it) }

        // Show the members count
        val membersCount = feedItem.numberOfPeople
        entourage_info_member_count?.text = getString(R.string.tour_info_members_count, membersCount)

        // hide the 'invite a friend' for a tour
      //  entourage_info_member_add?.visibility = if (feedItem.type != TimestampedObject.TOUR_CARD) View.VISIBLE else View.GONE
    }

    private fun updateFeedItemInfo() {
        // Update the header
        entourage_info_title?.text = feedItem.getTitle()
        entourage_info_icon?.let { iconView ->
            feedItem.getIconURL()?.let { iconURL ->
                iconView.setPadding(0,0,0,0)
                Glide.with(this).clear(iconView)
                Glide.with(this)
                        .load(iconURL)
                        .placeholder(R.drawable.ic_user_photo_small)
                        .circleCrop()
                        .into(iconView)
                iconView.visibility = View.VISIBLE
            } ?: run {
                feedItem.getIconDrawable(requireContext())?.let { iconDrawable ->
                    iconView.visibility = View.VISIBLE
                    Glide.with(this)
                            .load(iconDrawable)
                            .into(iconView)
                } ?: run {
                    iconView.visibility = View.GONE
                }
            }
        }
        updateFeedItemActionEvent()
    }

    open fun updateFeedItemActionEvent() {}

    fun changeViewsVisibility(isTour:Boolean) {
        val visibilityOldLayouts = if (isTour) View.VISIBLE else View.GONE
        val visibilityNewLayouts = if (isTour) View.GONE else View.VISIBLE
        layout_detail_event_action_top_view?.visibility = visibilityNewLayouts
        layout_detail_event_action_date?.visibility = visibilityNewLayouts
        layout_detail_event_action_location?.visibility = visibilityNewLayouts
        layout_detail_event_action_creator?.visibility = visibilityNewLayouts
        layout_detail_action_description?.visibility = visibilityNewLayouts
        layout_detail_event_description?.visibility = visibilityNewLayouts
        layout_detail_event_action_selector?.visibility = visibilityNewLayouts

        layout_public_entourage_header?.visibility = visibilityOldLayouts
        layout_view_separator?.visibility = visibilityOldLayouts
        layout_infos1?.visibility = visibilityOldLayouts
        entourage_info_member_count?.visibility = visibilityOldLayouts
        entourage_info_member_add?.visibility = visibilityOldLayouts
    }

    fun updatePhotosAvatar(author:ImageView?,logo:ImageView?) {
        author?.let { authorPhotoView ->
            feedItem.author?.avatarURLAsString?.let { avatarURLAsString ->
                Glide.with(this)
                        .load(Uri.parse(avatarURLAsString))
                        .placeholder(R.drawable.ic_user_photo_small)
                        .circleCrop()
                        .into(authorPhotoView)
            } ?: run {
                Glide.with(this)
                        .load(R.drawable.ic_user_photo_small)
                        .into(authorPhotoView)
            }
        }
        logo?.let { partnerLogoView ->
            feedItem.author?.partner?.smallLogoUrl?.let { partnerLogoURL ->
                Glide.with(this)
                        .load(Uri.parse(partnerLogoURL))
                        .placeholder(R.drawable.partner_placeholder)
                        .circleCrop()
                        .into(partnerLogoView)
            } ?: run {
                partnerLogoView.setImageDrawable(null)
            }
        }
    }
    fun getDateStringFromMetadata(metadata: BaseEntourage.Metadata?) : String {

        metadata?.let {
            val startCalendar = Calendar.getInstance()
            startCalendar.time = metadata.startDate ?: Date()
            val endCalendar = Calendar.getInstance()
            endCalendar.time = metadata.endDate ?: Date()
            return if (startCalendar[Calendar.DAY_OF_YEAR] == endCalendar[Calendar.DAY_OF_YEAR]) {
                getString(R.string.tour_info_metadata_dateStart_hours_format_new,
                        metadata.getStartDateFullAsString(requireContext()),
                        metadata.getStartEndTimesAsString(requireContext()))
            } else {
                //du xx à hh au yy à hh
                getString(R.string.tour_info_metadata_dateStart_End_hours_format_new,
                        metadata.getStartDateFullAsString(requireContext()),
                        metadata.getStartTimeAsString(requireContext()),
                        metadata.getEndDateFullAsString(requireContext()),
                        metadata.getEndTimeAsString(requireContext()))
            }
        } ?: run {
            return ""
        }
    }

    fun showActionTimestamps(createdTime: Date, updatedTime: Date) {
        val timestamps = ArrayList<String?>()
        timestamps.add(getString(R.string.entourage_info_creation_time, formattedDaysIntervalFromToday(createdTime)))
        if (!LocalDate(createdTime).isEqual(LocalDate())) {
            timestamps.add(getString(R.string.entourage_info_update_time, formattedDaysIntervalFromToday(updatedTime)))
        }
        entourage_info_timestamps?.text = TextUtils.join(" - ", timestamps)
        entourage_info_timestamps?.visibility = View.VISIBLE
    }

    fun formattedDaysIntervalFromToday(rawDate: Date?): String {
        if (rawDate == null) return this.getString(R.string.date_today)
            .lowercase(Locale.getDefault())

        val today = LocalDate()
        val date = LocalDate(rawDate)
        if (date.isEqual(today)) return this.getString(R.string.date_today)
            .lowercase(Locale.getDefault())
        val days = Days.daysBetween(date, today).days

        return when (days) {
            1 -> getString(R.string.date_yesterday).lowercase(Locale.getDefault())
            in 2..14 -> String.format(getString(R.string.x_days_ago),days)
            in 15..31 -> getString(R.string.date_this_month)
            else -> {
                val nbMonths = days / 30
                String.format(getString(R.string.x_months_ago),nbMonths)
            }
        }
    }

    protected abstract fun updateMetadataView()

    private fun switchToPublicSection() {
        entourage_info_act_layout?.visibility = View.VISIBLE
        entourage_info_public_section?.visibility = View.VISIBLE
        entourage_info_private_section?.visibility = View.GONE

        entourage_info_title?.visibility = View.INVISIBLE
        entourage_info_icon?.visibility = View.INVISIBLE

        updateHeaderButtons()
        initializeOptionsView()
        updateJoinStatus()
        initializeMap()
        initializeMembersView()
        if (feedItem.isPrivate()) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_PUBLIC_VIEW_MEMBER)
        } else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_PUBLIC_VIEW_NONMEMBER)
        }
    }

    protected open fun switchToPrivateSection() {
        entourage_info_act_layout?.visibility = if (feedItem.isClosed()) View.VISIBLE else View.GONE
        entourage_info_request_join_layout?.visibility = View.GONE
        entourage_info_public_section?.visibility = View.GONE
        entourage_info_private_section?.visibility = View.VISIBLE

        entourage_info_title?.visibility = View.VISIBLE
        entourage_info_icon?.visibility = View.VISIBLE

        if (mapFragment == null) {
            initializeMap()
        }
        initializeHiddenMap()
        updateHeaderButtons()
        initializeOptionsView()

        //hide the comment section if the user is not accepted or tour is freezed
        if (feedItem.joinStatus != FeedItem.JOIN_STATUS_ACCEPTED || feedItem.isClosed()) {
            entourage_info_comment_layout?.visibility = View.GONE
            entourage_info_discussion_view?.setBackgroundResource(R.color.background_login_grey)
        } else {
            entourage_info_comment_layout?.visibility = View.VISIBLE
            entourage_info_discussion_view?.setBackgroundResource(R.color.background)
        }
        initializeDiscussionList()
        initializeMembersView()
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_DISCUSSION_VIEW)

        ui_layout_report.visibility = View.GONE
    }

    protected open fun loadPrivateCards() {
        presenter().getFeedItemJoinRequests(feedItem)
        presenter().getFeedItemMembers(feedItem)
        presenter().getFeedItemMessages(feedItem)
    }

    private fun updateJoinStatus() {
        val dividerColor = R.color.accent
        var textColor = R.color.accent
        entourage_info_request_join_layout?.visibility = View.GONE
        entourage_info_act_layout?.visibility = View.VISIBLE
        entourage_info_act_button?.setPadding(0, 0, 0, 0)
        entourage_info_act_button?.setPaddingRelative(0, 0, 0, 0)
        if (feedItem.isClosed()) {
            // MI: Instead of hiding it, display the freezed text
            //tour_info_act_button.setEnabled(false);
            entourage_info_act_button?.setTextColor(ResourcesCompat.getColor(resources, feedItem.getClosedCTAColor(), null))
            entourage_info_act_button?.setText(feedItem.getClosedCTAText())
            entourage_info_act_button?.setPadding(resources.getDimensionPixelOffset(R.dimen.act_button_right_padding), 0, 0, 0)
            entourage_info_act_button?.setPaddingRelative(resources.getDimensionPixelOffset(R.dimen.act_button_right_padding), 0, 0, 0)
            entourage_info_act_button?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        } else {
            when (feedItem.joinStatus) {
                FeedItem.JOIN_STATUS_PENDING -> {
                    entourage_info_act_button?.isEnabled = true
                    entourage_info_act_button?.setText(R.string.tour_cell_button_pending)
                    entourage_info_act_button?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                }
                FeedItem.JOIN_STATUS_ACCEPTED -> {
                    entourage_info_act_button?.setPadding(0, 0, resources.getDimensionPixelOffset(R.dimen.act_button_right_padding), 0)
                    entourage_info_act_button?.setPaddingRelative(0, 0, resources.getDimensionPixelOffset(R.dimen.act_button_right_padding), 0)
                    entourage_info_act_button?.isEnabled = false
                    entourage_info_act_button?.setText(R.string.tour_cell_button_accepted)
                    entourage_info_act_button?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                }
                FeedItem.JOIN_STATUS_REJECTED -> {
                    entourage_info_act_button?.isEnabled = false
                    entourage_info_act_button?.setText(R.string.tour_cell_button_rejected)
                    entourage_info_act_button?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    textColor = R.color.tomato
                }
                else -> {
                    // Different layout for requesting to join
                    entourage_info_act_layout?.visibility = View.GONE
                    entourage_info_request_join_layout?.visibility = View.VISIBLE
//                    entourage_info_request_join_title?.setText(feedItem.getJoinRequestTitle())
                    if (feedItem is EntourageEvent) {
                        entourage_info_request_join_button?.text = getString(R.string.tour_info_request_join_button_event).uppercase(
                            Locale.getDefault()
                        )
                    }
                    else {
                        entourage_info_request_join_button?.text = getString(R.string.tour_info_request_join_button_entourage).uppercase(
                            Locale.getDefault()
                        )
                    }
                   // entourage_info_request_join_button?.setText(feedItem.getJoinRequestButton())

                    updatePublicScrollViewLayout()
                    return
                }
            }
            entourage_info_act_button?.setTextColor(ResourcesCompat.getColor(resources, textColor, null))
            entourage_info_act_divider_left?.setBackgroundResource(dividerColor)
            entourage_info_act_divider_right?.setBackgroundResource(dividerColor)
        }
        updatePublicScrollViewLayout()
    }

    protected fun updateDiscussionList(scrollToLastCard: Boolean = true) {
        val addedCardInfoList = feedItem.addedCardInfoList
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

    protected open fun addDiscussionTourEndCard(now: Date) {
        // Retrieve the latest chat messages, which should contain the close feed message
        presenter().getFeedItemMessages(feedItem, oldestChatMessageDate)
    }

    private fun scrollToLastCard() {
        if (discussionAdapter.itemCount > 0) {
            entourage_info_discussion_view?.scrollToPosition(discussionAdapter.itemCount - 1)
            feedItem.numberOfUnreadMessages = 0
        }
    }

    fun showProgressBar() {
        apiRequestsCount++
        entourage_info_progress_bar?.visibility = View.VISIBLE
    }

    protected fun hideProgressBar() {
        apiRequestsCount--
        if (apiRequestsCount <= 0) {
            entourage_info_progress_bar?.visibility = View.GONE
            apiRequestsCount = 0
        }
    }

    // ----------------------------------
    // Bus handling
    // ----------------------------------
    open fun onUserJoinRequestUpdateEvent(event: OnUserJoinRequestUpdateEvent) {
        presenter().updateUserJoinRequest(event.userId, event.update, event.feedItem)
    }

    open fun onEntourageUpdated(event: OnEntourageUpdated) {
        val updatedEntourage = event.entourage
        // Check if it is our displayed entourage
        if (feedItem.type != updatedEntourage.type || feedItem.id != updatedEntourage.id) return
        // Update the UI
        feedItem = updatedEntourage
        updateFeedItemInfo()
    }

    // ----------------------------------
    // API callbacks
    // ----------------------------------
    fun onFeedItemReceived(feedItem: FeedItem) {
        if (activity == null || !isAdded) return
        hideProgressBar()
        this.feedItem = feedItem
        initializeView()
        if (feedItem.isPrivate()) {
            loadPrivateCards()
        } else {
            presenter().getFeedItemMembers(feedItem)
        }
    }

    fun onFeedItemNotFound() {
        //if (activity == null || !isAdded) return
        hideProgressBar()

        entourage_information_coordinator_layout?.let {
            EntSnackbar.make(it, R.string.tour_info_error_retrieve_entourage, Snackbar.LENGTH_SHORT).show()
        }
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
    fun onFeedItemUsersReceived(entourageUsers: List<EntourageUser>, context: String?) {
        if (activity == null || !isAdded) return
        if (context == null) {
            // members
            onFeedItemMembersReceived(entourageUsers)
            initializeOptionsView()
        } else {
            onFeedItemJoinRequestsReceived(entourageUsers)
        }

        // users processed use standard updateUI
        onFeedItemNoUserReceived()
    }

    private fun onFeedItemMembersReceived(entourageUsers: List<EntourageUser>) {
        membersList?.clear()
        // iterate over the received users
        for (entourageUser in entourageUsers) {
            //show only the accepted users
            if (entourageUser.status != FeedItem.JOIN_STATUS_ACCEPTED) {
                // Remove the user from the members list, in case the user left the entourage
                membersAdapter?.removeCard(entourageUser)
            } else {
                entourageUser.feedItem = feedItem
                entourageUser.isDisplayedAsMember = true
                membersList?.add(entourageUser)
            }
        }
        initializeMembersView()
    }

    private fun onFeedItemJoinRequestsReceived(entourageUsers: List<EntourageUser>) {
        val timestampedObjectList = ArrayList<EntourageUser>()
        // iterate over the received users
        for (entourageUser in entourageUsers) {
            // skip the author
            if (entourageUser.userId == feedItem.author?.userID) {
                continue
            }
            //show only the accepted users
            if (entourageUser.status != FeedItem.JOIN_STATUS_ACCEPTED) {
                // Remove the user from the members list, in case the user left the entourage
                membersAdapter?.removeCard(entourageUser)
                //show the pending and cancelled requests too (by skipping the others)
                if (!(entourageUser.status == FeedItem.JOIN_STATUS_PENDING || entourageUser.status == FeedItem.JOIN_STATUS_CANCELLED)) {
                    continue
                }
            }
            entourageUser.feedItem = feedItem

            // check if we already have this user
            val oldUser = discussionAdapter.findCard(entourageUser) as? EntourageUser
            if (oldUser != null && oldUser.status != entourageUser.status) {
                discussionAdapter.updateCard(entourageUser)
            } else {
                timestampedObjectList.add(entourageUser)
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
            EntourageApplication.get(activity).components.authenticationController.me?.id?.let { me ->
                for (chatMessage in chatMessageList) {
                    chatMessage.isMe = chatMessage.userId == me
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
        entourage_info_comment?.isEnabled = true
        entourage_info_comment_send_button?.isEnabled = true
        if (chatMessage == null) {
            entourage_information_coordinator_layout?.let {
                EntSnackbar.make(it, R.string.tour_info_error_chat_message, Snackbar.LENGTH_SHORT).show()
            }
            return
        }
        entourage_info_comment?.setText("")

        //hide the keyboard
        entourage_info_comment?.let{
            if (it.hasFocus() && activity != null) {
                (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow(it.windowToken, 0)
            }
        }

        //add the message to the list
        chatMessage.isMe = true
        feedItem.addCardInfo(chatMessage)
        updateDiscussionList()
    }

    fun onUserJoinRequestUpdated(userId: Int, status: String, error: Int) {
        hideProgressBar()
        if (error == EntError.ERROR_NONE) {
            // Updated ok
            val messageId = if (FeedItem.JOIN_STATUS_REJECTED == status) R.string.tour_join_request_rejected else R.string.tour_join_request_success
            entourage_information_coordinator_layout?.let {EntSnackbar.make(it,  messageId, Snackbar.LENGTH_SHORT).show()}
            // Update the card
            (discussionAdapter.findCard(TimestampedObject.TOUR_USER_JOIN, userId.toLong()) as? EntourageUser)?.let { card ->
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
                EntourageApplication.get().removePushNotification(feedItem, userId, PushNotificationContent.TYPE_NEW_JOIN_REQUEST)
            }
        } else if (error == EntError.ERROR_BAD_REQUEST) {
            // Assume that the other user cancelled the request
            (discussionAdapter.findCard(TimestampedObject.TOUR_USER_JOIN, userId.toLong()) as? EntourageUser)?.let { card ->
                when (status) {
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
            }
        } else {
            // other Error
            entourage_information_coordinator_layout?.let {EntSnackbar.make(it,  R.string.tour_join_request_error, Snackbar.LENGTH_SHORT).show()}
        }
    }

    fun onInvitationStatusUpdated(success: Boolean, status: String) {
        entourage_info_invited_accept_button?.isEnabled = true
        entourage_info_invited_reject_button?.isEnabled = true
        if (success) {
            invitationId = 0
            if (acceptInvitationSilently) {
                acceptInvitationSilently = false
            } else {
                // Update UI
                entourage_info_invited_layout?.visibility = View.GONE
                entourage_information_coordinator_layout?.let {EntSnackbar.make(it,  R.string.invited_updated_ok, Snackbar.LENGTH_SHORT).show()}
                if (Invitation.STATUS_ACCEPTED == status) {
                    // Invitation accepted, refresh the lists and status
                    feedItem.joinStatus = FeedItem.JOIN_STATUS_ACCEPTED
                    switchToPrivateSection()
                    loadPrivateCards()
                    updateHeaderButtons()
                }
                updatePublicScrollViewLayout()
            }

            // Post an event
            EntBus.post(OnInvitationStatusChanged(feedItem, status))
        } else if (!acceptInvitationSilently) {
            entourage_information_coordinator_layout?.let {EntSnackbar.make(it,  R.string.invited_updated_error, Snackbar.LENGTH_SHORT).show()}
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
            updatedFeedItem.getEndTime()?.let { feedItem.setEndTime(it) }
            if (updatedFeedItem.isClosed() && updatedFeedItem.isPrivate()) {
                addDiscussionTourEndCard(Date())
                updateDiscussionList()
                setReadOnly()
            }
            if (updatedFeedItem.isClosed()) {
                entourage_info_comment_layout?.visibility = View.GONE
            }
            entourage_info_options?.visibility = View.GONE
            initializeOptionsView()
            updateHeaderButtons()
            updateJoinStatus()
        } else {
            entourage_information_coordinator_layout?.let {EntSnackbar.make(it, R.string.tour_close_fail, Snackbar.LENGTH_SHORT).show()}
        }
    }

    open fun setReadOnly() {}

    override fun onLocationUpdated(location: LatLng) {}

    override fun onLocationStatusUpdated(active: Boolean) {}

    override fun onUserStatusChanged(user: EntourageUser, updatedFeedItem: FeedItem) {
        //ignore requests that are not related to our feed item
        if (updatedFeedItem.type != feedItem.type || updatedFeedItem.id != feedItem.id) return
        hideProgressBar()
        //close the overlay
        onCloseOptionsButton()

        //update the local tour info
        user.status?.let { status ->
            feedItem.joinStatus = status
            val oldPrivateStatus = entourage_info_private_section?.visibility == View.VISIBLE
            //update UI
            if (oldPrivateStatus != feedItem.isPrivate()) {
                if (updatedFeedItem.isPrivate()) {
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

            updateFeedItemInfo()
        }
    }

    // ----------------------------------
    // InviteFriendsListener
    // ----------------------------------
    override fun onInviteSent() {
        // Show the success layout
        entourage_info_invite_success_layout?.visibility = View.VISIBLE
        // Start the timer to hide the success layout
        inviteSuccessHandler.postDelayed(inviteSuccessRunnable, Constants.INVITE_SUCCESS_HIDE_DELAY)
    }

    fun hideInfoButton() {showInfoButton = false}

    private inner class OnScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (!needsMoreChatMessaged) return
            scrollDeltaY += dy
            //check if user is scrolling up and pass the threshold
            if (dy < 0 && abs(scrollDeltaY) >= SCROLL_DELTA_Y_THRESHOLD) {
                (recyclerView.layoutManager as LinearLayoutManager?)?.findFirstVisibleItemPosition()?.let { firstVisibleItemPosition ->
                    val adapterPosition = recyclerView.findViewHolderForLayoutPosition(firstVisibleItemPosition)?.adapterPosition ?: return@let
                    val timestamp = discussionAdapter.getCardAt(adapterPosition)?.timestamp ?: return@let
                    if (oldestChatMessageDate != null && timestamp.before(oldestChatMessageDate)) {
                        presenter().getFeedItemMessages(feedItem, oldestChatMessageDate)
                    }
                }
                scrollDeltaY = 0
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}
    }

    protected inner class ServiceConnection : android.content.ServiceConnection {
        private var isBound = false
        var boundService: EntService? = null

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (activity != null) {
                boundService = (service as EntService.LocalBinder).service
                boundService?.registerServiceListener(this@FeedItemInformationFragment)
                isBound = true
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            boundService?.unregisterServiceListener(this@FeedItemInformationFragment)
            boundService = null
            isBound = false
        }
        // ----------------------------------
        // SERVICE BINDING METHODS
        // ----------------------------------
        fun doBindService() {
            if (activity != null) {
                try {
                    val intent = Intent(activity, EntService::class.java)
                    requireActivity().startService(intent)
                    requireActivity().bindService(intent, this, Context.BIND_AUTO_CREATE)
                } catch (e: IllegalStateException) {
                    Timber.w(e)
                }
            }
        }

        fun doUnbindService() {
            if (isBound) {
                boundService?.unregisterServiceListener(this@FeedItemInformationFragment)
                activity?.unbindService(this)
                isBound = false
            }
        }

    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "fragment_entourage_information"
        private const val VOICE_RECOGNITION_REQUEST_CODE = 2
        private const val READ_CONTACTS_PERMISSION_CODE = 3
        private const val SCROLL_DELTA_Y_THRESHOLD = 20
        private const val KEY_INVITATION_ID = "social.entourage.android_KEY_INVITATION_ID"
        private const val KEY_FEED_POSITION = "social.entourage.android.KEY_FEED_POSITION"
        private const val KEY_ISFROMACTIONS = "isFromActions"
        //private const val KEY_FEED_SHARE_URL = "social.entourage.android.KEY_FEED_SHARE_URL"

        // ----------------------------------
        // LIFECYCLE
        // ----------------------------------
        //TODO check that all values are not null
        fun newInstance(feedItem: FeedItem, invitationId: Long, feedRank: Int, isFromActions:Boolean = false): FeedItemInformationFragment {
            val fragment = if (feedItem.type == TimestampedObject.TOUR_CARD) TourInformationFragment() else EntourageInformationFragment()
            val args = Bundle()
            args.putSerializable(FeedItem.KEY_FEEDITEM, feedItem)
            args.putLong(KEY_INVITATION_ID, invitationId)
            args.putInt(KEY_FEED_POSITION, feedRank)
            args.putBoolean(KEY_ISFROMACTIONS,isFromActions)
            fragment.arguments = args
            return fragment
        }
    }
}