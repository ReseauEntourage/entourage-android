package social.entourage.android.entourage.information

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.squareup.otto.Subscribe
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_entourage_options.*
import kotlinx.android.synthetic.main.fragment_entourage_information.*
import kotlinx.android.synthetic.main.layout_detail_action_description.*
import kotlinx.android.synthetic.main.layout_detail_event_action_creator.*
import kotlinx.android.synthetic.main.layout_detail_event_action_date.*
import kotlinx.android.synthetic.main.layout_detail_event_action_location.*
import kotlinx.android.synthetic.main.layout_detail_event_action_top_view.*
import kotlinx.android.synthetic.main.layout_detail_event_description.*
import kotlinx.android.synthetic.main.layout_invite_source.*
import kotlinx.android.synthetic.main.layout_invite_source.view.*
import kotlinx.android.synthetic.main.layout_public_entourage_information.*
import org.joda.time.LocalDate
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageComponent
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.tape.Events
import social.entourage.android.api.tape.Events.OnUserJoinRequestUpdateEvent
import social.entourage.android.deeplinks.DeepLinksManager
import social.entourage.android.entourage.EntourageCloseFragment
import social.entourage.android.location.EntourageLocation
import social.entourage.android.map.OnAddressClickListener
import social.entourage.android.tools.BusProvider
import social.entourage.android.tools.Utils
import social.entourage.android.tools.view.EntourageSnackbar
import social.entourage.android.user.UserFragment
import social.entourage.android.user.partner.PartnerFragmentV2
import java.util.*
import javax.inject.Inject

class EntourageInformationFragment : FeedItemInformationFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @Inject lateinit var presenter: EntourageInformationPresenter
    override fun presenter(): FeedItemInformationPresenter { return presenter}

    val entourage:BaseEntourage
        get() = feedItem as BaseEntourage

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_entourage_information, container, false)
    }

    override fun getItemType(): Int {
        return TimestampedObject.ENTOURAGE_CARD

    }

    override fun setupComponent(entourageComponent: EntourageComponent?) {
        DaggerEntourageInformationComponent.builder()
                .entourageComponent(entourageComponent)
                .entourageInformationModule(EntourageInformationModule(this))
                .build()
                .inject(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        entourageServiceConnection.doBindService()
        BusProvider.instance.register(this)
    }

    override fun onDetach() {
        super.onDetach()
        entourageServiceConnection.doUnbindService()
        BusProvider.instance.unregister(this)
    }

    // ----------------------------------
    // Button Handling
    // ----------------------------------
    override fun onStopTourButton() {
        if (entourage.status == FeedItem.STATUS_ON_GOING || entourage.status == FeedItem.STATUS_OPEN) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE)
            //hide the options
            entourage_info_options?.visibility = View.GONE
            //show close fragment
            if (activity != null) {
                EntourageCloseFragment.newInstance(entourage).show(this.requireActivity().supportFragmentManager, EntourageCloseFragment.TAG)
            }
        }
    }

    override fun onJoinButton() {
        entourageServiceConnection.boundService?.let {
            showProgressBar()
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_ASK_JOIN)
            it.requestToJoinEntourage(entourage)
            entourage_info_options?.visibility = View.GONE
        } ?: run {entourage_information_coordinator_layout?.let {EntourageSnackbar.make(it,  R.string.tour_join_request_message_error, Snackbar.LENGTH_SHORT).show()}}
    }

    override fun showInviteSource(isShareOnly:Boolean) {
        entourage_info_invite_source_layout?.visibility = View.VISIBLE
        entourage_info_invite_source_layout?.invite_source_number_button?.visibility = if (isShareOnly) View.GONE else View.VISIBLE
        invite_source_description?.setText(entourage.getInviteSourceDescription())
    }


    // ----------------------------------
    // Chat push notification
    // ----------------------------------
    /**
     * @param message
     * @return true if pushNotif has been displayed on this fragment
     */
    override fun onPushNotificationChatMessageReceived(message: Message): Boolean {
        //we received a chat notification
        //check if it is referring to this feed item
        val content = message.content ?: return false
        if (content.isTourRelated ) {
            return false
        }
        else if (content.joinableId != entourage.id) {
            return false
        }
        //retrieve the last messages from server
        scrollToLastCard = true
        presenter.getFeedItemMessages(entourage)
        return true
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    override fun initializeOptionsView() {
        entourage_option_stop?.visibility = View.GONE
        entourage_option_quit?.visibility = View.GONE
        entourage_option_edit?.visibility = View.GONE
        entourage_option_share?.visibility = View.GONE
        entourage_option_report?.visibility = View.GONE
        entourage_option_join?.visibility = View.GONE
        entourage_option_contact?.visibility = View.GONE
        entourage_option_promote?.visibility = View.GONE

        val hideJoinButton = entourage.isPrivate() || FeedItem.JOIN_STATUS_PENDING == entourage.joinStatus || entourage.isFreezed()
        entourage_option_join?.visibility =  if (hideJoinButton) View.GONE else View.VISIBLE
        entourage_option_contact?.visibility = View.GONE
        val myId = EntourageApplication.me(activity)?.id ?: return
        val author = entourage.author ?: return
        if (author.userID != myId) {
            if ((FeedItem.JOIN_STATUS_PENDING == entourage.joinStatus || FeedItem.JOIN_STATUS_ACCEPTED == entourage.joinStatus) && !entourage.isFreezed()) {
                entourage_option_quit?.visibility = View.VISIBLE
                entourage_option_quit?.setText(if (FeedItem.JOIN_STATUS_PENDING == entourage.joinStatus) R.string.tour_info_options_cancel_request else R.string.tour_info_options_quit_tour)
            }
            entourage_option_report?.visibility = View.VISIBLE
        } else {
            entourage_option_stop?.visibility = if (entourage.isFreezed() || !entourage.canBeClosed()) View.GONE else View.VISIBLE
            entourage_option_stop?.setText( R.string.tour_info_options_freeze_tour)
            if (FeedItem.STATUS_OPEN == entourage.status) {
                entourage_option_edit?.visibility = View.VISIBLE
                entourage_option_reopen?.visibility = View.GONE
            }
            else {
                entourage_option_reopen?.visibility = View.VISIBLE
            }
        }
        if (!entourage.isSuspended()) {
            // Share button available only for entourages and non-members
            entourage_option_share?.visibility = View.VISIBLE
        }
        membersList?.forEach { member ->
            if (member.userId == myId
                    && member.groupRole?.equals("organizer", ignoreCase = true) == true) {
                entourage_option_promote?.visibility = View.VISIBLE
            }
        }
    }

    override fun addSpecificCards() {
    }

    override fun drawMap(googleMap: GoogleMap) {
        val startPoint = entourage.getStartPoint() ?: return
        val position = startPoint.location

        // move camera
        val cameraPosition = CameraPosition(LatLng(startPoint.latitude, startPoint.longitude), EntourageLocation.INITIAL_CAMERA_FACTOR_ENTOURAGE_VIEW, 0F, 0F)
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        if (entourage.showHeatmapAsOverlay()) {
            // add heatmap
            val icon = BitmapDescriptorFactory.fromResource(entourage.getHeatmapResourceId())
            val groundOverlayOptions = GroundOverlayOptions()
                    .image(icon)
                    .position(position, BaseEntourage.HEATMAP_SIZE, BaseEntourage.HEATMAP_SIZE)
                    .clickable(true)
                    .anchor(0.5f, 0.5f)
            googleMap.addGroundOverlay(groundOverlayOptions)
        } else {
            // add marker
            AppCompatResources.getDrawable(requireContext(), entourage.getHeatmapResourceId())?.let {drawable ->
                val icon = Utils.getBitmapDescriptorFromDrawable(drawable, BaseEntourage.getMarkerSize(requireContext()), BaseEntourage.getMarkerSize(requireContext()))
                val markerOptions = MarkerOptions()
                        .icon(icon)
                        .position(position)
                        .draggable(false)
                        .anchor(0.5f, 0.5f)
                googleMap.addMarker(markerOptions)
            }
        }
    }

    override fun initializeHiddenMap() {
    }

    override fun updateMetadataView() {
        if(entourage_info_metadata_layout==null) return
        // show the view only for outing
        val metadata: BaseEntourage.Metadata? = entourage.metadata
        val metadataVisible = (entourage.isEvent() && metadata != null)
        entourage_info_metadata_layout?.visibility = if (metadataVisible) View.VISIBLE else View.GONE

        if (!metadataVisible) return

        // populate the data
        entourage.author?.userName?.let {
            entourage_info_metadata_organiser?.text = getString(R.string.tour_info_metadata_organiser_format, it)
        }
        if (metadata == null) return
        if (entourage.isEvent()) {
            //Format dates same day or different days.
            val startCalendar = Calendar.getInstance()
            startCalendar.time = metadata.startDate ?: Date()
            val endCalendar = Calendar.getInstance()
            endCalendar.time = metadata.endDate ?: Date()
            if (startCalendar[Calendar.DAY_OF_YEAR] == endCalendar[Calendar.DAY_OF_YEAR]) {
                entourage_info_metadata_datetime?.text = getString(R.string.tour_info_metadata_dateStart_hours_format,
                        metadata.getStartDateFullAsString(requireContext()),
                        metadata.getStartEndTimesAsString(requireContext()))
            } else {
                //du xx à hh au yy à hh
                entourage_info_metadata_datetime?.text = getString(R.string.tour_info_metadata_dateStart_End_hours_format,
                        metadata.getStartDateFullAsString(requireContext()),
                        metadata.getStartTimeAsString(requireContext()),
                        metadata.getEndDateFullAsString(requireContext()),
                        metadata.getEndTimeAsString(requireContext()))
            }
        } else {
            entourage_info_metadata_datetime?.text = getString(R.string.tour_info_metadata_date_format,
                    metadata.getStartDateAsString(requireContext()),
                    metadata.getStartTimeAsString(requireContext()))
        }

        setAddressView(metadata)
    }

    private fun setAddressView(metadata: BaseEntourage.Metadata) {
        entourage_info_metadata_address?.let {
            val displayAddress = metadata.displayAddress
            it.text = displayAddress
            it.paintFlags = it.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            if (displayAddress != null) {
                it.setOnClickListener(OnAddressClickListener(requireActivity(), displayAddress))
            }
        }
    }

    override fun addDiscussionTourEndCard(now: Date) {
        // Retrieve the latest chat messages, which should contain the close feed message
        presenter.getFeedItemMessages(entourage, oldestChatMessageDate)
    }

    override fun updateFeedItemActionEvent() {
        if (entourage.isEvent()) {
            entourage_info_request_join_title?.text = getString(R.string.tour_info_request_join_title_entourage_new)
        } else {
            entourage_info_request_join_title?.text = getString(R.string.tour_info_request_join_title_entourage)
        }

        changeViewsVisibility(false)

        //Top view
        ui_image_event_top?.visibility = View.GONE
        if (entourage.isEvent() && !entourage.eventImageUrl.isNullOrEmpty()) {
            Picasso.get().load(entourage.eventImageUrl).into(ui_image_event_top)
            ui_image_event_top?.visibility = View.VISIBLE
        }

        ui_title_event_action_top?.text = entourage.getTitle()

        //Button action
        var title: Int
        var showIcon = View.VISIBLE
        ui_layout_event_action_top_action?.setBackgroundResource(R.drawable.bg_button_rounded_pre_onboard_orange_stroke)
        ui_tv_button_action_top?.setTextColor(ResourcesCompat.getColor(resources,R.color.accent,null))
        when(entourage.joinStatus) {
            FeedItem.JOIN_STATUS_ACCEPTED -> {
                title = R.string.tour_cell_button_accepted_other
                showIcon = View.GONE
                ui_layout_event_action_top_action?.setBackgroundResource(R.drawable.bg_button_rounded_pre_onboard_orange_plain)
                ui_tv_button_action_top?.setTextColor(ResourcesCompat.getColor(resources,R.color.white,null))
            }
            FeedItem.JOIN_STATUS_PENDING -> {
                showIcon = View.GONE
                title = R.string.tour_cell_button_pending_new
            }
            else -> {
                title = R.string.tour_info_request_join_button2_entourage
                if (entourage.isEvent()) {
                    title = R.string.tour_info_request_join_button_entourage
                }
            }
        }
        ui_iv_button_action_top.visibility = showIcon
        ui_tv_button_action_top.setText(title)

        if (entourage.status == FeedItem.STATUS_CLOSED) {
            ui_layout_event_action_top_action?.visibility = View.GONE
        }
        else {
            ui_layout_event_action_top_action?.visibility = View.VISIBLE
        }

        ui_layout_event_action_top_action?.setOnClickListener {
            if (entourage.joinStatus == FeedItem.JOIN_STATUS_PENDING) {
                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.setTitle("Attention")
                alertDialog.setMessage(R.string.confirm_cancel_demand)
                alertDialog.setNegativeButton(R.string.tour_info_options_close) { dialog,_ ->
                    dialog.dismiss()
                }
                alertDialog.setPositiveButton(R.string.validate_cancel_demand) { dialog,_ ->
                    dialog.dismiss()
                    quitEntourage()
                }

                alertDialog.show()
            }
            else if (entourage.joinStatus != FeedItem.JOIN_STATUS_ACCEPTED) {
                onJoinButton()
            }
        }

        ui_layout_event_action_top_share?.setOnClickListener {
            showInviteSource(true)
        }

        //Layout Date
        if (entourage.isEvent()) {
            layout_detail_event_action_date?.visibility = View.VISIBLE
            ui_tv_event_action_date?.text = getDateStringFromMetadata(entourage.metadata)
        }
        else {
            layout_detail_event_action_date?.visibility = View.GONE
        }

        entourage_info_map_layout.visibility = View.VISIBLE

        //Layout location
        if (entourage.isEvent()) {
            if (entourage.isOnlineEvent) {
                ui_iv_event_action_location.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.ic_detail_event_link,null))

                val eventUrl = if (entourage.eventUrl == null || entourage.eventUrl.equals("null")) "" else entourage.eventUrl
                ui_tv_event_action_location?.text = "${getString(R.string.detail_action_event_online)}\n${eventUrl}"
                DeepLinksManager.linkify(ui_tv_event_action_location)
                entourage_info_map_layout.visibility = View.GONE
            }
            else {
                ui_tv_event_action_location?.let {
                    val displayAddress = entourage.metadata?.displayAddress
                    it.text = displayAddress
                    it.paintFlags = it.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                    if (displayAddress != null) {
                        it.setOnClickListener { v->
                            OnAddressClickListener(requireActivity(),displayAddress,true).onClick(v)
                        }
                    }
                }
            }
        }
        else {
            ui_tv_event_action_location?.text = if (entourage.postal_code.isNullOrEmpty()) "-" else entourage.postal_code
        }

        //Layout creator
        updatePhotosAvatar(ui_action_event_creator_photo,ui_action_event_creator_logo)
        ui_action_event_creator_name?.text = entourage.author?.userName ?: ""
        val partner = entourage.author?.partner
        val role = partner?.userRoleTitle

        entourage.author?.userID?.let { userId ->
            layout_detail_event_action_creator?.setOnClickListener {
                val fragment = UserFragment.newInstance(userId)
                fragment.show(requireActivity(). supportFragmentManager, UserFragment.TAG)
            }
        }

        if (partner != null || !role.isNullOrEmpty()) {
            ui_action_event_creator_layout_bottom?.visibility = View.VISIBLE
            if (role != null && role.isNotEmpty()) {
                val roleStr = "$role -"
                ui_action_event_creator_role?.text = roleStr
                ui_action_event_creator_role?.visibility = View.VISIBLE
            }
            else {
                ui_action_event_creator_role?.visibility = View.GONE
            }

            ui_action_event_creator_bt_asso?.text = partner.name
            ui_action_event_creator_bt_asso?.setOnClickListener {
                partner.id.toInt().let { partnerId ->
                    BusProvider.instance.post(Events.OnShowDetailAssociation(partnerId))
                }
            }
        }
        else {
            ui_action_event_creator_layout_bottom?.visibility = View.INVISIBLE
        }

        if (entourage.isEvent()) {
            ui_action_event_creator_information?.text = getString(R.string.detail_action_event_info_rdv)
        }
        else if (entourage.actionGroupType == BaseEntourage.GROUPTYPE_ACTION_DEMAND) {
            ui_action_event_creator_information?.text = getText(R.string.detail_action_event_info_demand)
        }
        else {
            ui_action_event_creator_information?.text = getText(R.string.detail_action_event_info_gift)
        }

        //Layout description
        if (entourage.isEvent()) {
            layout_detail_event_description?.visibility = View.VISIBLE
            layout_detail_action_description?.visibility = View.GONE

            if (entourage.getDescription().isNullOrEmpty()) {
                ui_layout_event_description?.visibility = View.GONE
            }
            else {
                ui_layout_event_description?.visibility = View.VISIBLE
                ui_tv_detail_event_description?.text = entourage.getDescription()
            }
            DeepLinksManager.linkify(ui_tv_detail_event_description)
        }
        else {
            layout_detail_action_description?.visibility = View.VISIBLE
            layout_detail_event_description?.visibility = View.GONE

            if (entourage.getDescription().isNullOrEmpty()) {
                ui_layout_action_description?.visibility = View.GONE
            }
            else {
                ui_layout_action_description?.visibility = View.VISIBLE
                ui_tv_detail_action_description?.text = entourage.getDescription()
            }

            val timestamps = ArrayList<String?>()
            timestamps.add(getString(R.string.entourage_info_creation_time, formattedDaysIntervalFromToday(entourage.getCreationTime())))
            if (!LocalDate(entourage.getCreationTime()).isEqual(LocalDate())) {
                timestamps.add(getString(R.string.entourage_info_update_time, formattedDaysIntervalFromToday(entourage.updatedTime)))
            }
            ui_tv_detail_action_last_update?.text = TextUtils.join(" - ", timestamps)

            DeepLinksManager.linkify(ui_tv_detail_action_description)
        }
    }

    // ----------------------------------
    // Bus handling
    // ----------------------------------
    @Subscribe
    override fun onUserJoinRequestUpdateEvent(event: OnUserJoinRequestUpdateEvent) {
        super.onUserJoinRequestUpdateEvent(event)
    }

    @Subscribe
    override fun onEntourageUpdated(event: Events.OnEntourageUpdated) {
        super.onEntourageUpdated(event)
    }

    @Subscribe
    fun onShowEventDeeplink(event: Events.OnShowEventDeeplink) {
        dismiss()
    }

    //Use inside detail Event / action when click on asso name inside member section.
    @Subscribe
    fun onShowDetailAssociation(event: Events.OnShowDetailAssociation) {
        PartnerFragmentV2.newInstance(null, event.id).show(parentFragmentManager, PartnerFragmentV2.TAG)
    }

    // ----------------------------------
    // API callbacks
    // ----------------------------------
    override fun setReadOnly() {
    }
}