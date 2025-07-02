package social.entourage.android.events.details.feed

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.Status
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentAboutEventBinding
import social.entourage.android.events.EventModel
import social.entourage.android.events.EventsPresenter
import social.entourage.android.events.create.Recurrence
import social.entourage.android.events.details.feed.AboutEventFragmentDirections.actionEventAboutToEventMembers
import social.entourage.android.groups.details.feed.GroupMembersPhotosAdapter
import social.entourage.android.groups.details.members.MembersType
import social.entourage.android.language.LanguageManager
import social.entourage.android.profile.myProfile.InterestsAdapter
import social.entourage.android.tools.displayDistance
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.setHyperlinkClickable
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.Utils.enableCopyOnLongClick
import social.entourage.android.tools.utils.underlineWithDistanceUnder
import java.text.SimpleDateFormat

const val ZOOM = 15f

class AboutEventFragment : Fragment(), OnMapReadyCallback {

    private var _binding: NewFragmentAboutEventBinding? = null
    val binding: NewFragmentAboutEventBinding get() = _binding!!
    var event: EventModel? = null
    private var interestsList: ArrayList<String> = ArrayList()
    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }

    private val args: AboutEventFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentAboutEventBinding.inflate(inflater, container, false)
        //mMap = binding.mapView
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        event = args.group
        setView()
        handleMembersButton()
        openLink()
        handleJoinButton()
        handleAddToCalendarButton()
        handleBackButton()
        eventPresenter.isUserParticipating.observe(requireActivity(), ::handleJoinResponse)
        eventPresenter.hasUserLeftEvent.observe(requireActivity(), ::handleJoinResponse)
        eventPresenter.getMembers.observe(viewLifecycleOwner, ::handleResponseGetMembers)
        fragmentResult()

        AnalyticsEvents.logEvent(AnalyticsEvents.Event_detail_full)
    }

    private fun setView() {
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        with(binding) {
            eventName.text = event?.name

            if(event != null && event?.members_count!! > 1 ){
                eventMembersNumberLocation.text = String.format(
                    getString(R.string.members_number),
                    event?.members_count
                )
            }else{
                eventMembersNumberLocation.text = String.format(
                    getString(R.string.members_number_singular),
                    event?.members_count
                )
            }
            binding.location.icon = AppCompatResources.getDrawable(
                requireContext(),
                if (event?.online == true) R.drawable.new_web else R.drawable.new_location
            )

            (if (event?.online == true) event?.eventUrl else event?.metadata?.displayAddress)?.let {
                event?.let { it1 ->

                    binding.location.content.underlineWithDistanceUnder(
                        it, it1.displayDistance(requireContext()),requireContext()
                    )
                }
            }
            binding.placesLimit.content.text =
                String.format(getString(R.string.limited_places), event?.metadata?.placeLimit)
            if(event?.metadata?.placeLimit == null || event?.metadata?.placeLimit == 0){
                binding.placesLimit.root.isVisible = false
            }else{
                binding.placesLimit.root.isVisible = true
            }
            binding.mapView.isVisible = event?.online == false
            binding.cvMapView.isVisible = event?.online == false

            val recurrence = getString(
                when (event?.recurrence) {
                    Recurrence.NO_RECURRENCE.value -> R.string.juste_once
                    Recurrence.EVERY_WEEK.value -> R.string.every_week
                    Recurrence.EVERY_TWO_WEEKS.value -> R.string.every_two_week
                    else -> R.string.empty_description
                }
            )
            event?.metadata?.startsAt?.let {
                var locale = LanguageManager.getLocaleFromPreferences(requireContext())
                binding.dateStartsAt.content.text =
                    String.format(
                        getString(R.string.date_recurrence_event),
                        SimpleDateFormat(
                            context?.getString(R.string.feed_event_date),
                            locale
                        ).format(
                            it
                        ), recurrence
                    )
            }
            event?.metadata?.let {
                var locale = LanguageManager.getLocaleFromPreferences(requireContext())
                binding.time.content.text =
                    String.format(
                        getString(R.string.start_and_end_time_event),
                        it.startsAt?.let { it1 ->
                            SimpleDateFormat(
                                context?.getString(R.string.feed_event_time),
                                locale
                            ).format(
                                it1
                            )
                        }, it.endsAt?.let { it1 ->
                            SimpleDateFormat(
                                context?.getString(R.string.feed_event_time),
                                locale
                            ).format(
                                it1
                            )
                        }
                    )
            }
            if (event?.updatedAt != null) {
                var locale = LanguageManager.getLocaleFromPreferences(requireContext())
                updatedDate.text = String.format(
                    getString(R.string.updated_at), event?.updatedAt?.let {
                        SimpleDateFormat(
                            context?.getString(R.string.feed_event_date),
                            locale
                        ).format(
                            it
                        )
                    }
                )
            } else {
                var locale = LanguageManager.getLocaleFromPreferences(requireContext())
                updatedDate.text = String.format(
                    getString(R.string.created_at), event?.createdAt?.let {
                        SimpleDateFormat(
                            context?.getString(R.string.feed_event_date),
                            locale
                        ).format(
                            it
                        )
                    }
                )
            }
            initializeMembersPhotos()
            eventDescription.text = event?.description
            eventDescription.setHyperlinkClickable()
            eventDescription.enableCopyOnLongClick(requireContext())
            initializeInterests()
            initializeGroups()
            if (event?.status == Status.CLOSED)
                handleEventCanceled()
        }

        event?.author?.let { author ->
            binding.organizer.icon = AppCompatResources.getDrawable(requireContext(),R.drawable.ic_event_header_organiser)
            binding.organizer.content.text = String.format(getString(R.string.event_organisez_by), author.userName)

            author.partner?.name?.let { partnerName->
                if(!partnerName.isEmpty()){
                    binding.tvAssociation.text = String.format(getString(R.string.event_organisez_asso),partnerName)
                    binding.tvAssociation.visibility = View.VISIBLE
                }
            }
        }
        getPrincipalMember()
        updateButtonJoin()
    }


    fun getPrincipalMember(){
        if(event != null) {
            if(event?.id != null ){
                eventPresenter.getEventMembers(event!!.id!!)
            }
        }
    }

    fun handleResponseGetMembers(allMembers: MutableList<EntourageUser>?) {
        if (allMembers != null) {
            for(member in allMembers){
                if(member.id.toInt() == event?.author?.userID){
                    if(member.communityRoles?.contains("Équipe Entourage") == true || member.communityRoles?.contains("Ambassadeur") == true){
                        binding.tvAssociation.text = getString(R.string.event_organisez_entourage)
                        binding.tvAssociation.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun handleEventCanceled() {
        with(binding) {
            var locale = LanguageManager.getLocaleFromPreferences(requireContext())
            updatedDate.text = String.format(
                getString(R.string.canceled_at), event?.previousAt?.let {
                    SimpleDateFormat(
                        context?.getString(R.string.feed_event_date),
                        locale
                    ).format(
                        it
                    )
                }
            )
            eventName.setTextColor(getColor(requireContext(), R.color.grey))
            with(dateStartsAt) {
                content.setTextColor(getColor(requireContext(), R.color.grey))
                ivIcon.setColorFilter(getColor(requireContext(), R.color.grey))
            }
            with(time) {
                content.setTextColor(getColor(requireContext(), R.color.grey))
                ivIcon.setColorFilter(getColor(requireContext(), R.color.grey))
            }
            with(placesLimit) {
                content.setTextColor(getColor(requireContext(), R.color.grey))
                ivIcon.setColorFilter(getColor(requireContext(), R.color.grey))
            }
            with(location) {
                content.setTextColor(getColor(requireContext(), R.color.grey))
                ivIcon.setColorFilter(getColor(requireContext(), R.color.grey))
            }
            toKnow.setTextColor(getColor(requireContext(), R.color.grey))
            eventDescription.setTextColor(getColor(requireContext(), R.color.grey))
            canceled.visibility = View.VISIBLE
            actions.visibility = View.GONE
        }
    }

    private fun fragmentResult() {
        setFragmentResultListener(Const.REQUEST_KEY_SHOULD_REFRESH) { _, bundle ->
            val shouldRefresh = bundle.getBoolean(Const.SHOULD_REFRESH)
            if (shouldRefresh) {
                event?.let {
                    it.member = !it.member
                    setView()
                }
            }
        }
    }

    private fun handleMembersButton() {
        binding.members.setOnClickListener {
            event?.id?.let {
                val action =
                    actionEventAboutToEventMembers(
                        it,
                        MembersType.EVENT
                    )
                findNavController().navigate(action)
            }
        }
    }

    private fun handleJoinResponse(hasJoined: Boolean) {
        if (hasJoined) {
            event?.let {
                it.member = !it.member
            }
            updateButtonJoin()
        }
    }

    private fun initializeMembersPhotos() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = event?.members?.let { GroupMembersPhotosAdapter(it) }
        }
    }

    private fun openMap() {
        val geoUri =
            String.format(getString(R.string.geoUri), event?.metadata?.displayAddress)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
        startActivityForResult(intent, 0)
    }

    private fun openLink() {
        binding.location.root.setOnClickListener {
            if (event?.online != true) {
                openMap()
            } else {
                var url = event?.eventUrl
                url?.let {
                    url = Utils.checkUrlWithHttps(it)
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivityForResult(browserIntent, 0)
                }
            }
        }
    }

    private fun handleBackButton() {
        binding.header.headerIconBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleJoinButton() {
        if (event?.admin == false)
            binding.join.setOnClickListener {
                if (event?.member == true) {
                    CustomAlertDialog.showWithCancelFirst(
                        requireContext(),
                        getString(R.string.leave_event),
                        getString(R.string.leave_event_dialog_content),
                        getString(R.string.exit),
                    ) {
                        event?.id?.let { id ->
                            eventPresenter.leaveEvent(id)
                        }
                    }
                } else {
                    event?.id?.let { id ->
                        eventPresenter.participate(id)
                    }
                }

            }
    }

    private fun handleAddToCalendarButton() {
        binding.calendar.setOnClickListener {
            event?.let { event -> Utils.showAddToCalendarPopUp(requireContext(), event) }
        }
    }

    private fun updateButtonJoin() {
        if(isAdded){
            lateinit var label: String
            val textColor: Int
            val background: Drawable?
            val rightDrawable: Drawable?

            if (event?.member == true) {
                label = getString(R.string.participating)
                textColor = ContextCompat.getColor(requireContext(), R.color.orange)
                background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.new_bg_rounded_button_orange_stroke,
                    null
                )
                rightDrawable = ResourcesCompat.getDrawable(resources, R.drawable.new_check, null)
            } else {
                label = getString(R.string.participate)
                textColor = ContextCompat.getColor(requireContext(), R.color.white)
                background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.new_bg_rounded_button_orange_fill,
                    null
                )
                rightDrawable =
                    ResourcesCompat.getDrawable(resources, R.drawable.new_plus_white, null)
            }
            with(binding) {
                join.text = label
                join.setTextColor(textColor)
                join.background = background
                join.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    rightDrawable,
                    null
                )
            }
        }
    }

    private fun initializeInterests() {
        binding.categories.text =
            getString(if (interestsList.size == 1) R.string.category else R.string.categories)
        if (interestsList.isEmpty()) {
            binding.categories.visibility = View.GONE
            binding.interests.visibility = View.GONE
        } else {
            with(binding.interests) {
                layoutManager = FlexboxLayoutManager(context).apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.FLEX_START
                }
                adapter = InterestsAdapter(interestsList)
            }
        }
    }

    private fun initializeGroups() {
        binding.groups.text =
            getString(
                if (event?.neighborhoods?.size == 1) R.string.associated_group else
                    R.string.associated_groups
            )
        if (event?.neighborhoods?.isEmpty() == true) {
            binding.groups.visibility = View.GONE
            binding.rvGroups.visibility = View.GONE
        } else {
            with(binding.rvGroups) {
                layoutManager = LinearLayoutManager(context)
                adapter = event?.neighborhoods?.let { AboutEventGroupListAdapter(it) }
            }
        }
    }

    private fun handleMetaData(tags: Tags?) {
        interestsList.clear()
        val eventsInterests = event?.interests
        tags?.interests?.forEach { interest ->
            if (eventsInterests?.contains(interest.id) == true) interest.name?.let { it ->
                interestsList.add(
                    it
                )
            }
        }
        binding.interests.adapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val latitude = event?.location?.latitude ?: 0.0
        val longitude = event?.location?.longitude ?: 0.0
        val latLong = LatLng(latitude, longitude)
        googleMap.addMarker(
            MarkerOptions().position(latLong)
        )
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(latitude, longitude)).zoom(ZOOM).build()
        googleMap.animateCamera(
            CameraUpdateFactory
                .newCameraPosition(cameraPosition)
        )
        googleMap.setOnMapClickListener {
            openMap()
        }
    }
}