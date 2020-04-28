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
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.map.BaseEntourage
import social.entourage.android.api.model.map.Entourage
import social.entourage.android.api.model.map.FeedItem
import social.entourage.android.api.model.map.TourUser
import social.entourage.android.api.tape.Events
import social.entourage.android.api.tape.Events.OnUserJoinRequestUpdateEvent
import social.entourage.android.entourage.EntourageCloseFragment
import social.entourage.android.location.EntourageLocation
import social.entourage.android.map.OnAddressClickListener
import social.entourage.android.tools.BusProvider
import social.entourage.android.tools.Utils
import social.entourage.android.view.EntourageSnackbar
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    override fun onJoinTourButton() {
        if (entourageServiceConnection.boundService != null) {
            showProgressBar()
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_ASK_JOIN)
            entourageServiceConnection.boundService!!.requestToJoinEntourage(feedItem as Entourage?)
            entourage_info_options?.visibility = View.GONE
        } else {
            EntourageSnackbar.make(entourage_information_coordinator_layout!!,  R.string.tour_join_request_message_error, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun showInviteSource() {
        entourage_info_invite_source_layout?.visibility = View.VISIBLE
        invite_source_description?.setText(if (BaseEntourage.TYPE_OUTING.equals(feedItem.groupType, ignoreCase = true)) R.string.invite_source_description_outing else R.string.invite_source_description)
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
        val hideJoinButton = feedItem.isPrivate || FeedItem.JOIN_STATUS_PENDING == feedItem.joinStatus || feedItem.isFreezed
        entourage_option_join?.visibility =  if (hideJoinButton) View.GONE else View.VISIBLE
        entourage_option_contact?.visibility = View.GONE
        if (feedItem.author == null) return
        val myId = EntourageApplication.me(activity)?.id ?: return
        if (feedItem.author.userID != myId) {
            if ((FeedItem.JOIN_STATUS_PENDING == feedItem.joinStatus || FeedItem.JOIN_STATUS_ACCEPTED == feedItem.joinStatus) && !feedItem.isFreezed) {
                entourage_option_quit?.visibility = View.VISIBLE
                entourage_option_quit?.setText(if (FeedItem.JOIN_STATUS_PENDING == feedItem.joinStatus) R.string.tour_info_options_cancel_request else R.string.tour_info_options_quit_tour)
            }
            entourage_option_report?.visibility = View.VISIBLE
        } else {
            entourage_option_stop?.visibility = if (feedItem.isFreezed || !feedItem.canBeClosed()) View.GONE else View.VISIBLE
            entourage_option_stop?.setText( R.string.tour_info_options_freeze_tour)
            if (FeedItem.STATUS_OPEN == feedItem.status) {
                entourage_option_edit?.visibility = View.VISIBLE
            }
        }
        if (!feedItem.isSuspended) {
            // Share button available only for entourages and non-members
            entourage_option_share?.visibility = View.VISIBLE
        }
        if (entourage_option_promote != null && membersList != null) {
            for (member in membersList!!) {
                if (member !is TourUser || member.userId != myId) continue
                if (member.groupRole?.equals("organizer", ignoreCase = true) == true) {
                    entourage_option_promote.visibility = View.VISIBLE
                }
                break
            }
        }
    }

    override fun addSpecificCards() {
    }

    override fun drawMap(googleMap: GoogleMap) {
        val startPoint = feedItem.startPoint ?: return
        val position = startPoint.location

        // move camera
        val cameraPosition = CameraPosition(LatLng(startPoint.latitude, startPoint.longitude), EntourageLocation.INITIAL_CAMERA_FACTOR_ENTOURAGE_VIEW, 0F, 0F)
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        if (feedItem.showHeatmapAsOverlay()) {
            // add heatmap
            val icon = BitmapDescriptorFactory.fromResource(feedItem.heatmapResourceId)
            val groundOverlayOptions = GroundOverlayOptions()
                    .image(icon)
                    .position(position, BaseEntourage.HEATMAP_SIZE, BaseEntourage.HEATMAP_SIZE)
                    .clickable(true)
                    .anchor(0.5f, 0.5f)
            googleMap.addGroundOverlay(groundOverlayOptions)
        } else {
            // add marker
            val drawable = AppCompatResources.getDrawable(requireContext(), feedItem.heatmapResourceId)
            val icon = Utils.getBitmapDescriptorFromDrawable(drawable!!, BaseEntourage.getMarkerSize(context), BaseEntourage.getMarkerSize(context))
            val markerOptions = MarkerOptions()
                    .icon(icon)
                    .position(position)
                    .draggable(false)
                    .anchor(0.5f, 0.5f)
            googleMap.addMarker(markerOptions)
        }
    }

    override fun initializeHiddenMap() {
    }

    override fun updateMetadataView() {
        if(entourage_info_metadata_layout==null) return
        // show the view only for outing
        val metadata: BaseEntourage.Metadata? = if (feedItem is Entourage) (feedItem as Entourage).metadata else null
        val metadataVisible = (BaseEntourage.TYPE_OUTING.equals(feedItem.groupType, ignoreCase = true) && metadata != null)
        entourage_info_metadata_layout.visibility = if (metadataVisible) View.VISIBLE else View.GONE

        if (!metadataVisible) return

        // populate the data
        if (feedItem.author != null) {
            entourage_info_metadata_organiser?.text = getString(R.string.tour_info_metadata_organiser_format, feedItem.author.userName)
        }
        if (metadata == null) return
        if (BaseEntourage.TYPE_OUTING.equals(feedItem.groupType, ignoreCase = true)) {
            //Format dates same day or different days.
            val startCalendar = Calendar.getInstance()
            startCalendar.time = (feedItem as Entourage).metadata.startDate
            val endCalendar = Calendar.getInstance()
            endCalendar.time = (feedItem as Entourage).metadata.endDate
            if (startCalendar[Calendar.DAY_OF_YEAR] == endCalendar[Calendar.DAY_OF_YEAR]) {
                entourage_info_metadata_datetime?.text = getString(R.string.tour_info_metadata_dateStart_hours_format,
                        metadata.getStartDateFullAsString(context),
                        metadata.getStartEndTimesAsString(context))
            } else {
                //du xx à hh au yy à hh
                entourage_info_metadata_datetime?.text = getString(R.string.tour_info_metadata_dateStart_End_hours_format,
                        metadata.getStartDateFullAsString(context),
                        metadata.getStartTimeAsString(context),
                        metadata.getEndDateFullAsString(context),
                        metadata.getEndTimeAsString(context))
            }
        } else {
            entourage_info_metadata_datetime?.text = getString(R.string.tour_info_metadata_date_format,
                    metadata.getStartDateAsString(context),
                    metadata.getStartTimeAsString(context))
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