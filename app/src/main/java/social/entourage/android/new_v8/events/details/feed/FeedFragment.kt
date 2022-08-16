package social.entourage.android.new_v8.events.details.feed

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.new_fragment_feed.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentFeedEventBinding
import social.entourage.android.new_v8.events.EventsPresenter
import social.entourage.android.new_v8.events.details.SettingsModalFragment
import social.entourage.android.new_v8.groups.details.feed.GroupMembersPhotosAdapter
import social.entourage.android.new_v8.groups.details.members.MembersType
import social.entourage.android.new_v8.models.Events
import social.entourage.android.new_v8.models.SettingUiModel
import social.entourage.android.new_v8.models.toEventUi
import social.entourage.android.new_v8.profile.myProfile.InterestsAdapter
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.new_v8.utils.underline
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class FeedFragment : Fragment() {

    private var _binding: NewFragmentFeedEventBinding? = null
    val binding: NewFragmentFeedEventBinding get() = _binding!!

    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }
    private var interestsList: ArrayList<String> = ArrayList()
    private var eventId = Const.DEFAULT_VALUE
    private lateinit var eventUI: SettingUiModel
    private lateinit var event: Events
    private var myId: Int? = null
    private val args: FeedFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentFeedEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventId = args.eventID
        myId = EntourageApplication.me(activity)?.id
        handleImageViewAnimation()
        eventPresenter.getEvent(eventId)
        eventPresenter.getEvent.observe(viewLifecycleOwner, ::handleResponseGetEvent)
        eventPresenter.isUserParticipating.observe(viewLifecycleOwner, ::handleParticipateResponse)
        handleMembersButton()
        handleParticipateButton()
        handleBackButton()
        handleSettingsButton()
        handleAboutButton()
    }

    private fun handleResponseGetEvent(getEvent: Events?) {
        getEvent?.let {
            event = it
            updateView()
        }
    }


    private fun handleImageViewAnimation() {
        binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val res: Float =
                abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            binding.toolbarLayout.alpha = 1f - res
            Timber.e(res.toString())
            binding.eventImageToolbar.alpha = res
            binding.eventNameToolbar.alpha = res
        }
    }

    private fun openGoogleMaps() {
        if (event.online != true) {
            binding.location.root.setOnClickListener {
                val geoUri =
                    String.format(getString(R.string.geoUri), event.metadata?.displayAddress)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
                startActivity(intent)
            }
        }
    }

    private fun updateView() {
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        with(binding) {
            eventName.text = event.title
            eventNameToolbar.text = event.title
            eventMembersNumberLocation.text = String.format(
                getString(R.string.members_number),
                event.membersCount,
            )
            event.metadata?.placeLimit?.let {
                placesLimit.root.visibility = View.VISIBLE
                placesLimit.content.text = String.format(
                    getString(R.string.places_numbers),
                    it,
                )
            }
            event.metadata?.startsAt?.let {
                binding.dateStartsAt.content.text = SimpleDateFormat(
                    context?.getString(R.string.feed_event_date),
                    Locale.FRANCE
                ).format(
                    it
                )
            }
            event.metadata?.startsAt?.let {
                binding.time.content.text = SimpleDateFormat(
                    context?.getString(R.string.feed_event_time),
                    Locale.FRANCE
                ).format(
                    it
                )
            }
            initializeMembersPhotos()
            if (event.member) {
                more.visibility = View.VISIBLE
                join.visibility = View.GONE
                toKnow.visibility = View.GONE
                eventDescription.visibility = View.GONE
            } else {
                join.visibility = View.VISIBLE
                toKnow.visibility = View.VISIBLE
                eventDescription.visibility = View.VISIBLE
                eventDescription.text = event.description
                more.visibility = View.GONE
                initializeInterests()
            }

            binding.location.icon = AppCompatResources.getDrawable(
                requireContext(),
                if (event.online == true) R.drawable.new_web else R.drawable.new_location_event
            )

            (if (event.online == true) event.eventUrl else event.metadata?.displayAddress)?.let {
                binding.location.content.underline(
                    it
                )
            }
            event.metadata?.landscapeUrl?.let {
                Glide.with(requireActivity())
                    .load(Uri.parse(it))
                    .centerCrop()
                    .into(eventImage)
            }
        }
        updateButtonJoin()
        handleCreatePostButton()
        openGoogleMaps()
    }

    private fun handleBackButton() {
        binding.iconBack.setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun handleSettingsButton() {
        binding.iconSettings.setOnClickListener {
            with(event) {
                eventUI = SettingUiModel(
                    id, title,
                    membersCount,
                    metadata?.displayAddress,
                    interests,
                    description,
                    members,
                    member,
                    EntourageApplication.me(context)?.id == author?.userID,
                    recurrence
                )
                SettingsModalFragment.newInstance(eventUI)
                    .show(parentFragmentManager, SettingsModalFragment.TAG)
            }
        }
    }


    private fun handleAboutButton() {
        binding.more.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_FEED_MORE_DESCRIPTION
            )
            val eventUI = event.toEventUi(requireContext())
            val action =
                FeedFragmentDirections.actionEventFeedToEventAbout(
                    eventUI
                )
            findNavController().navigate(action)
        }
    }


    private fun handleParticipateButton() {
        binding.join.setOnClickListener {
            if (!event.member) eventPresenter.participate(eventId)
        }
    }

    private fun handleMembersButton() {
        binding.members.setOnClickListener {
            val action =
                FeedFragmentDirections.actionEventFeedToMembers(eventId, MembersType.EVENT)
            findNavController().navigate(action)
        }
    }

    private fun handleParticipateResponse(isParticipating: Boolean) {
        if (isParticipating) {
            event.member = !event.member
            updateButtonJoin()
            handleCreatePostButton()
            if (event.metadata?.placeLimit != null) {
                showLimitPlacePopUp()
            } else {
                Utils.showAddToCalendarPopUp(requireContext(), event.toEventUi(requireContext()))
            }
        }
    }

    private fun showLimitPlacePopUp() {
        Utils.showAlertDialogButtonClicked(
            requireContext(),
            getString(R.string.event_limited_places_title),
            getString(R.string.event_limited_places_subtitle),
            getString(R.string.button_OK), onYes = null
        )
    }

    private fun handleCreatePostButton() {
        if (event.member) {
            binding.createPost.show()
            binding.postsLayoutEmptyState.subtitle.visibility = View.VISIBLE
            binding.postsLayoutEmptyState.arrow.visibility = View.VISIBLE
        } else {
            binding.createPost.hide(true)
            binding.postsLayoutEmptyState.subtitle.visibility = View.GONE
            binding.postsLayoutEmptyState.arrow.visibility = View.GONE
        }
    }

    private fun handleMetaData(tags: Tags?) {
        interestsList.clear()
        val groupInterests = event.interests
        tags?.interests?.forEach { interest ->
            if (groupInterests.contains(interest.id)) interest.name?.let { it ->
                interestsList.add(
                    it
                )
            }
        }
        binding.interests.adapter?.notifyDataSetChanged()
    }

    private fun initializeMembersPhotos() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = event.members?.let { GroupMembersPhotosAdapter(it) }
        }
    }

    private fun initializeInterests() {
        if (interestsList.isEmpty()) binding.interests.visibility = View.GONE
        else {
            binding.interests.visibility = View.VISIBLE
            binding.interests.apply {
                val layoutManagerFlex = FlexboxLayoutManager(context)
                layoutManagerFlex.flexDirection = FlexDirection.ROW
                layoutManagerFlex.justifyContent = JustifyContent.FLEX_START
                layoutManager = layoutManagerFlex
                adapter = InterestsAdapter(interestsList)
            }
        }
    }

    private fun updateButtonJoin() {
        val label =
            getString(if (event.member) R.string.participating else R.string.participate)
        val textColor = ContextCompat.getColor(
            requireContext(),
            if (event.member) R.color.orange else R.color.white
        )
        val background = ResourcesCompat.getDrawable(
            resources,
            if (event.member) R.drawable.new_bg_rounded_button_orange_stroke else R.drawable.new_bg_rounded_button_orange_fill,
            null
        )
        val rightDrawable = ResourcesCompat.getDrawable(
            resources,
            if (event.member) R.drawable.new_check else R.drawable.new_plus_white,
            null
        )
        binding.join.text = label
        binding.join.setTextColor(textColor)
        binding.join.background = background
        binding.join.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            rightDrawable,
            null
        )
    }
}