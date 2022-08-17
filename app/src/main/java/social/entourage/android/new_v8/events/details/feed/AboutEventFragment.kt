package social.entourage.android.new_v8.events.details.feed

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.util.Util
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentAboutEventBinding
import social.entourage.android.new_v8.events.EventsPresenter
import social.entourage.android.new_v8.events.create.Recurrence
import social.entourage.android.new_v8.events.details.feed.AboutEventFragmentDirections.actionEventAboutToEventMembers
import social.entourage.android.new_v8.groups.details.feed.GroupMembersPhotosAdapter
import social.entourage.android.new_v8.groups.details.members.MembersType
import social.entourage.android.new_v8.models.EventUiModel
import social.entourage.android.new_v8.profile.myProfile.InterestsAdapter
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.new_v8.utils.underline
import java.text.SimpleDateFormat
import java.util.*

const val ZOOM = 15f

class AboutEventFragment : Fragment(), OnMapReadyCallback {

    private var _binding: NewFragmentAboutEventBinding? = null
    val binding: NewFragmentAboutEventBinding get() = _binding!!
    var event: EventUiModel? = null
    private var interestsList: ArrayList<String> = ArrayList()
    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }
    private var mMap: MapView? = null


    private val args: AboutEventFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentAboutEventBinding.inflate(inflater, container, false)
        mMap = binding.mapView
        mMap?.onCreate(savedInstanceState)
        mMap?.getMapAsync(this)
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
    }

    private fun setView() {
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        with(binding) {
            eventName.text = event?.name
            eventMembersNumberLocation.text = String.format(
                getString(R.string.members_number),
                event?.members_count
            )
            binding.location.icon = AppCompatResources.getDrawable(
                requireContext(),
                if (event?.online == true) R.drawable.new_web else R.drawable.new_location
            )

            (if (event?.online == true) event?.eventUrl else event?.metadata?.displayAddress)?.let {
                binding.location.content.underline(
                    it
                )
            }
            binding.placesLimit.root.isVisible = event?.metadata?.placeLimit != null
            binding.placesLimit.content.text =
                String.format(getString(R.string.limited_places), event?.metadata?.placeLimit)

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
                binding.dateStartsAt.content.text =
                    String.format(
                        getString(R.string.date_recurrence_event),
                        SimpleDateFormat(
                            context?.getString(R.string.feed_event_date),
                            Locale.FRANCE
                        ).format(
                            it
                        ), recurrence
                    )
            }
            event?.metadata?.let {
                binding.time.content.text =
                    String.format(
                        getString(R.string.start_and_end_time_event),
                        it.startsAt?.let { it1 ->
                            SimpleDateFormat(
                                context?.getString(R.string.feed_event_time),
                                Locale.FRANCE
                            ).format(
                                it1
                            )
                        }, it.endsAt?.let { it1 ->
                            SimpleDateFormat(
                                context?.getString(R.string.feed_event_time),
                                Locale.FRANCE
                            ).format(
                                it1
                            )
                        }
                    )
            }
            if (event?.updatedAt != null) {
                updatedDate.text = String.format(
                    getString(R.string.updated_at), event?.updatedAt?.let {
                        SimpleDateFormat(
                            context?.getString(R.string.feed_event_date),
                            Locale.FRANCE
                        ).format(
                            it
                        )
                    }
                )
            } else {
                updatedDate.text = String.format(
                    getString(R.string.created_at), event?.createdAt?.let {
                        SimpleDateFormat(
                            context?.getString(R.string.feed_event_date),
                            Locale.FRANCE
                        ).format(
                            it
                        )
                    }
                )
            }
            initializeMembersPhotos()
            eventDescription.text = event?.description
            initializeInterests()
            initializeGroups()
        }
        updateButtonJoin()
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
        startActivity(intent)
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
                    startActivity(browserIntent)
                }
            }
        }
    }


    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleJoinButton() {
        binding.join.setOnClickListener {
            if (event?.member == true) {
                Utils.showAlertDialogButtonClicked(
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
            rightDrawable = ResourcesCompat.getDrawable(resources, R.drawable.new_plus_white, null)
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
        mMap?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMap?.onPause()
    }

    override fun onStart() {
        super.onStart()
        mMap?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mMap?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMap?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMap?.onLowMemory()
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