package social.entourage.android.entourage.information

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.layout_entourage_options.*
import kotlinx.android.synthetic.main.fragment_entourage_information.*
import kotlinx.android.synthetic.main.layout_invite_source.*
import kotlinx.android.synthetic.main.layout_public_entourage_information.*
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageComponent
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.tape.Events
import social.entourage.android.api.tape.Events.OnUserJoinRequestUpdateEvent
import social.entourage.android.entourage.EntourageCloseFragment
import social.entourage.android.location.EntourageLocation
import social.entourage.android.map.OnAddressClickListener
import social.entourage.android.tools.BusProvider
import social.entourage.android.tools.Utils
import social.entourage.android.tools.view.EntourageSnackbar
import java.util.*
import javax.inject.Inject

class EntourageInformationFragment : FeedItemInformationFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @Inject lateinit var presenter: EntourageInformationPresenter
    override fun presenter(): FeedItemInformationPresenter { return presenter}

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
        if (feedItem.status == FeedItem.STATUS_ON_GOING || feedItem.status == FeedItem.STATUS_OPEN) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE)
            //entourageService.stopFeedItem(feedItem);
            //hide the options
            entourage_info_options?.visibility = View.GONE
            //show close fragment
            if (activity != null) {
                EntourageCloseFragment.newInstance(feedItem).show(this.requireActivity().supportFragmentManager, EntourageCloseFragment.TAG)
            }
        }
    }

    override fun onJoinButton() {
        entourageServiceConnection.boundService?.let {
            showProgressBar()
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_ASK_JOIN)
            it.requestToJoinEntourage(feedItem as BaseEntourage)
            entourage_info_options?.visibility = View.GONE
        } ?: run {entourage_information_coordinator_layout?.let {EntourageSnackbar.make(it,  R.string.tour_join_request_message_error, Snackbar.LENGTH_SHORT).show()}}
    }

    override fun showInviteSource() {
        entourage_info_invite_source_layout?.visibility = View.VISIBLE
        invite_source_description?.setText(if (BaseEntourage.GROUPTYPE_OUTING.equals(feedItem.getGroupType(), ignoreCase = true)) R.string.invite_source_description_outing else R.string.invite_source_description)
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
        else if (content.joinableId != feedItem.id) {
            return false
        }
        //retrieve the last messages from server
        scrollToLastCard = true
        presenter.getFeedItemMessages(feedItem)
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
        val hideJoinButton = feedItem.isPrivate() || FeedItem.JOIN_STATUS_PENDING == feedItem.joinStatus || feedItem.isFreezed()
        entourage_option_join?.visibility =  if (hideJoinButton) View.GONE else View.VISIBLE
        entourage_option_contact?.visibility = View.GONE
        val myId = EntourageApplication.me(activity)?.id ?: return
        val author = feedItem.author ?: return
        if (author.userID != myId) {
            if ((FeedItem.JOIN_STATUS_PENDING == feedItem.joinStatus || FeedItem.JOIN_STATUS_ACCEPTED == feedItem.joinStatus) && !feedItem.isFreezed()) {
                entourage_option_quit?.visibility = View.VISIBLE
                entourage_option_quit?.setText(if (FeedItem.JOIN_STATUS_PENDING == feedItem.joinStatus) R.string.tour_info_options_cancel_request else R.string.tour_info_options_quit_tour)
            }
            entourage_option_report?.visibility = View.VISIBLE
        } else {
            entourage_option_stop?.visibility = if (feedItem.isFreezed() || !feedItem.canBeClosed()) View.GONE else View.VISIBLE
            entourage_option_stop?.setText( R.string.tour_info_options_freeze_tour)
            if (FeedItem.STATUS_OPEN == feedItem.status) {
                entourage_option_edit?.visibility = View.VISIBLE
            }
        }
        if (!feedItem.isSuspended()) {
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
        val startPoint = feedItem.getStartPoint() ?: return
        val position = startPoint.location

        // move camera
        val cameraPosition = CameraPosition(LatLng(startPoint.latitude, startPoint.longitude), EntourageLocation.INITIAL_CAMERA_FACTOR_ENTOURAGE_VIEW, 0F, 0F)
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        if (feedItem.showHeatmapAsOverlay()) {
            // add heatmap
            val icon = BitmapDescriptorFactory.fromResource(feedItem.getHeatmapResourceId())
            val groundOverlayOptions = GroundOverlayOptions()
                    .image(icon)
                    .position(position, BaseEntourage.HEATMAP_SIZE, BaseEntourage.HEATMAP_SIZE)
                    .clickable(true)
                    .anchor(0.5f, 0.5f)
            googleMap.addGroundOverlay(groundOverlayOptions)
        } else {
            // add marker
            AppCompatResources.getDrawable(requireContext(), feedItem.getHeatmapResourceId())?.let {drawable ->
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
        val metadata: BaseEntourage.Metadata? = if (feedItem is BaseEntourage) (feedItem as BaseEntourage).metadata else null
        val metadataVisible = (BaseEntourage.GROUPTYPE_OUTING.equals(feedItem.getGroupType(), ignoreCase = true) && metadata != null)
        entourage_info_metadata_layout?.visibility = if (metadataVisible) View.VISIBLE else View.GONE

        if (!metadataVisible) return

        // populate the data
        feedItem.author?.userName?.let {
            entourage_info_metadata_organiser?.text = getString(R.string.tour_info_metadata_organiser_format, it)
        }
        if (metadata == null) return
        if (BaseEntourage.GROUPTYPE_OUTING.equals(feedItem.getGroupType(), ignoreCase = true)) {
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
        presenter.getFeedItemMessages(feedItem, oldestChatMessageDate)
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

    // ----------------------------------
    // API callbacks
    // ----------------------------------
    override fun setReadOnly() {
    }
}