package social.entourage.android.new_v8.events.details.feed

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
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
import social.entourage.android.new_v8.groups.details.feed.CreatePostGroupActivity
import social.entourage.android.new_v8.groups.details.feed.GroupMembersPhotosAdapter
import social.entourage.android.new_v8.comment.PostAdapter
import social.entourage.android.new_v8.groups.details.members.MembersType
import social.entourage.android.new_v8.models.*
import social.entourage.android.new_v8.profile.myProfile.InterestsAdapter
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.new_v8.utils.underline
import social.entourage.android.tools.log.AnalyticsEvents
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class FeedFragment : Fragment() {

    private var _binding: NewFragmentFeedEventBinding? = null
    val binding: NewFragmentFeedEventBinding get() = _binding!!

    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }
    private var interestsList: ArrayList<String> = ArrayList()
    private var eventId = Const.DEFAULT_VALUE
    private lateinit var event: Events
    private var myId: Int? = null
    private val args: FeedFragmentArgs by navArgs()

    private var newPostsList: MutableList<Post> = mutableListOf()
    private var oldPostsList: MutableList<Post> = mutableListOf()

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
        eventPresenter.getEvent(eventId)
        eventPresenter.getEvent.observe(viewLifecycleOwner, ::handleResponseGetEvent)
        eventPresenter.isUserParticipating.observe(viewLifecycleOwner, ::handleParticipateResponse)
        eventPresenter.getAllPosts.observe(viewLifecycleOwner, ::handleResponseGetEventPosts)
        handleSwipeRefresh()
        handleMembersButton()
        fragmentResult()
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
        handleImageViewAnimation()
    }


    private fun handleImageViewAnimation() {
        binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val res: Float =
                abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            binding.toolbarLayout.alpha = 1f - res
            binding.eventImageToolbar.alpha = res
            binding.eventNameToolbar.alpha = res
            binding.participate.isVisible =
                res == 1F && !event.member && event.status == Status.OPEN
        }
    }


    private fun handleResponseGetEventPosts(allPosts: MutableList<Post>?) {
        binding.swipeRefresh.isRefreshing = false
        newPostsList.clear()
        oldPostsList.clear()
        allPosts?.let {
            it.forEach { post ->
                if (post.read == true || post.read == null) oldPostsList.add(post)
                else newPostsList.add(post)
            }
        }

        when {
            newPostsList.isEmpty() && oldPostsList.isEmpty() -> {
                binding.postsLayoutEmptyState.visibility = View.VISIBLE
                binding.postsNewRecyclerview.visibility = View.GONE
                binding.postsOldRecyclerview.visibility = View.GONE
                binding.postsNew.root.visibility = View.GONE
                binding.postsOld.root.visibility = View.GONE
            }
            newPostsList.isNotEmpty() && oldPostsList.isEmpty() -> {
                binding.postsNew.root.visibility = View.VISIBLE
                binding.postsNewRecyclerview.visibility = View.VISIBLE
                binding.postsLayoutEmptyState.visibility = View.GONE
                binding.postsOldRecyclerview.visibility = View.GONE
                binding.postsOld.root.visibility = View.GONE
                binding.postsNewRecyclerview.adapter?.notifyDataSetChanged()
            }
            oldPostsList.isNotEmpty() && newPostsList.isEmpty() -> {
                binding.postsOld.root.visibility = View.GONE
                binding.postsOldRecyclerview.visibility = View.VISIBLE
                binding.postsLayoutEmptyState.visibility = View.GONE
                binding.postsNew.root.visibility = View.GONE
                binding.postsNewRecyclerview.visibility = View.GONE
                binding.postsOldRecyclerview.adapter?.notifyDataSetChanged()
            }
            oldPostsList.isNotEmpty() && newPostsList.isNotEmpty() -> {
                binding.postsOld.root.visibility = View.VISIBLE
                binding.postsOldRecyclerview.visibility = View.VISIBLE
                binding.postsNew.root.visibility = View.VISIBLE
                binding.postsNewRecyclerview.visibility = View.VISIBLE
                binding.postsLayoutEmptyState.visibility = View.GONE
                binding.postsOldRecyclerview.adapter?.notifyDataSetChanged()
                binding.postsNewRecyclerview.adapter?.notifyDataSetChanged()
            }
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
                participate.visibility = View.GONE
                toKnow.visibility = View.GONE
                eventDescription.visibility = View.GONE
            } else {
                join.visibility = View.VISIBLE
                participate.visibility = View.VISIBLE
                toKnow.visibility = View.VISIBLE
                eventDescription.visibility = View.VISIBLE
                eventDescription.text = event.description
                more.visibility = View.GONE
                initializeInterests()
            }

            binding.location.icon = AppCompatResources.getDrawable(
                requireContext(),
                if (event.online == true) R.drawable.new_web else R.drawable.new_location
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
            canceled.isVisible = event.status == Status.CLOSED
            if (event.status == Status.CLOSED) {
                eventName.setTextColor(getColor(requireContext(), R.color.grey))
                dateStartsAt.content.setTextColor(getColor(requireContext(), R.color.grey))
                dateStartsAt.icon = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.new_calendar_grey
                )
                time.content.setTextColor(getColor(requireContext(), R.color.grey))
                time.icon = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.new_time_grey
                )
            }
        }
        updateButtonJoin()
        handleCreatePostButton()
        handleCreatePostButtonClick()
        openGoogleMaps()
        initializePosts()
    }

    private fun handleBackButton() {
        binding.iconBack.setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun handleSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadPosts()
        }
    }

    private fun loadPosts() {
        eventPresenter.getEventPosts(eventId)
    }


    private fun initializePosts() {
        binding.postsNewRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PostAdapter(
                newPostsList,
                ::openCommentPage
            )
        }
        binding.postsOldRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PostAdapter(
                oldPostsList,
                ::openCommentPage
            )
        }
    }

    private fun openCommentPage(post: Post, shouldOpenKeyboard: Boolean) {
        context?.startActivity(
            Intent(context, EventCommentActivity::class.java)
                .putExtras(
                    bundleOf(
                        Const.ID to eventId,
                        Const.POST_ID to post.id,
                        Const.POST_AUTHOR_ID to post.user?.userId,
                        Const.SHOULD_OPEN_KEYBOARD to shouldOpenKeyboard,
                        Const.IS_MEMBER to event.member,
                        Const.NAME to event.title
                    )
                )
        )
    }

    private fun fragmentResult() {
        setFragmentResultListener(Const.REQUEST_KEY_SHOULD_REFRESH) { _, bundle ->
            val shouldRefresh = bundle.getBoolean(Const.SHOULD_REFRESH)
            if (shouldRefresh) eventPresenter.getEvent(eventId)
        }
    }

    private fun handleSettingsButton() {
        binding.iconSettings.setOnClickListener {
            SettingsModalFragment.newInstance(event)
                .show(parentFragmentManager, SettingsModalFragment.TAG)

        }
    }

    override fun onResume() {
        super.onResume()
        loadPosts()
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
        binding.participate.setOnClickListener {
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
            binding.participate.hide()
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

    private fun handleCreatePostButtonClick() {
        binding.createPost.setOnClickListener {
            val intent = Intent(context, CreatePostEventActivity::class.java)
            intent.putExtra(Const.ID, eventId)
            startActivity(intent)
        }
    }

    private fun handleCreatePostButton() {
        if (event.member) {
            if (event.status == Status.OPEN) binding.createPost.show()
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