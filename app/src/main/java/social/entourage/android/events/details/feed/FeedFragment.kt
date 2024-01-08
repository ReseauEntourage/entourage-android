package social.entourage.android.events.details.feed

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.gson.Gson
import kotlinx.android.synthetic.main.new_fragment_feed.view.arrow
import kotlinx.android.synthetic.main.new_fragment_feed.view.subtitle
import kotlinx.android.synthetic.main.new_fragment_feed_event.view.tv_more
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.Status
import social.entourage.android.api.model.Tags
import social.entourage.android.api.model.toEventUi
import social.entourage.android.comment.CommentsListAdapter
import social.entourage.android.comment.PostAdapter
import social.entourage.android.databinding.NewFragmentFeedEventBinding
import social.entourage.android.events.EventsPresenter
import social.entourage.android.events.details.SettingsModalFragment
import social.entourage.android.groups.details.feed.CallbackReportFragment
import social.entourage.android.groups.details.feed.GroupMembersPhotosAdapter
import social.entourage.android.groups.details.members.MembersType
import social.entourage.android.language.LanguageManager
import social.entourage.android.profile.myProfile.InterestsAdapter
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.report.ReportModalFragment
import social.entourage.android.report.ReportTypes
import social.entourage.android.tools.calculateIfEventPassed
import social.entourage.android.tools.image_viewer.ImageDialogActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.px
import social.entourage.android.tools.utils.underline
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

class FeedFragment : Fragment(), CallbackReportFragment {

    private var _binding: NewFragmentFeedEventBinding? = null
    val binding: NewFragmentFeedEventBinding get() = _binding!!

    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }
    private var interestsList: ArrayList<String> = ArrayList()
    private var eventId = Const.DEFAULT_VALUE
    private var event: Events? = null
    private var myId: Int? = null
    private val args: FeedFragmentArgs by navArgs()
    private var shouldShowPopUp = true

    private var newPostsList: MutableList<Post> = mutableListOf()
    private var oldPostsList: MutableList<Post> = mutableListOf()
    private var allPostsList: MutableList<Post> = mutableListOf()

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
        eventPresenter.isEventReported.observe(requireActivity(), ::handleDeletedResponse)
        eventPresenter.hasUserLeftEvent.observe(requireActivity(),::handleLeaveResponse)
        eventPresenter.isUserParticipating.observe(viewLifecycleOwner, ::handleParticipateResponse)
        eventPresenter.getAllPosts.observe(viewLifecycleOwner, ::handleResponseGetEventPosts)
        eventPresenter.getMembers.observe(viewLifecycleOwner, ::handleResponseGetMembers)
        handleSwipeRefresh()
        handleMembersButton()
        fragmentResult()
        handleParticipateButton()
        handleBackButton()
        handleSettingsButton()
        handleAboutButton()
        onFragmentResult()
        openLink()
        AnalyticsEvents.logEvent(AnalyticsEvents.Event_detail_main)
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
                res == 1F && event?.member == false && event?.status == Status.OPEN

            binding.eventImage.alpha = 1f - res
        }
    }

    private fun handleResponseGetEventPosts(allPosts: MutableList<Post>?) {
        binding.swipeRefresh.isRefreshing = false
        newPostsList.clear()
        oldPostsList.clear()
        allPostsList.clear()
        allPosts?.let {
            allPostsList.addAll(allPosts)
            it.forEach { post ->
                if (post.read == true || post.read == null) oldPostsList.add(post)
                else newPostsList.add(post)
            }
        }
        Log.wtf("wtf", "newPostsList: " + newPostsList.size)
        Log.wtf("wtf", "oldpostlist: " + oldPostsList.size)

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
        if (event?.online != true) {
            binding.location.root.setOnClickListener {
                val geoUri =
                    String.format(getString(R.string.geoUri), event?.metadata?.displayAddress)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
                startActivity(intent)
            }
        }
    }

    private fun updateView() {

        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        with(binding) {
            eventName.text = event?.title
            eventNameToolbar.text = event?.title

            if(event != null && event?.membersCount!! > 1){
                eventMembersNumberLocation.text = String.format(
                    getString(R.string.members_number),
                    event?.membersCount,
                )
            }else{
                eventMembersNumberLocation.text = String.format(
                    getString(R.string.members_number_singular),
                    event?.membersCount,
                )
            }
            var locale = LanguageManager.getLocaleFromPreferences(requireContext())


            event?.metadata?.placeLimit?.let {
                placesLimit.root.visibility = View.VISIBLE
                placesLimit.content.text = String.format(
                    getString(R.string.places_numbers),
                    it,
                )
            }
            event?.metadata?.startsAt?.let {
                binding.dateStartsAt.content.text = SimpleDateFormat(
                    context?.getString(R.string.feed_event_date),
                    locale
                ).format(
                    it
                )
            }
            event?.metadata?.startsAt?.let {
                binding.time.content.text = SimpleDateFormat(
                    context?.getString(R.string.feed_event_time),
                    locale
                ).format(
                    it
                )
            }
            initializeMembersPhotos()
            if (event?.member == true) {
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
                eventDescription.text = event?.description
                more.visibility = View.GONE

                initializeInterests()
            }

            binding.location.icon = AppCompatResources.getDrawable(
                requireContext(),
                if (event?.online == true) R.drawable.new_web else R.drawable.new_location
            )

            (if (event?.online == true) event?.eventUrl else event?.metadata?.displayAddress)?.let {
                binding.location.content.underline(
                    it
                )
            }
            event?.author.let {author->
                binding.organizer.content.text = String.format(getString(R.string.event_organisez_by), author?.userName)
                author?.partner.let { partner->
                    if(!partner?.name.isNullOrEmpty()){
                        binding.tvAssociation.text = String.format(getString(R.string.event_organisez_asso),partner?.name)
                        binding.tvAssociation.visibility = View.VISIBLE
                    }
                }
            }
            event?.metadata?.landscapeUrl?.let {
                Glide.with(requireActivity())
                    .load(it)
                    .error(R.drawable.new_group_illu)
                    .centerCrop()
                    .into(eventImage)

                Glide.with(requireActivity())
                    .load(it)
                    .error(R.drawable.new_group_illu)
                    .transform(CenterCrop(), RoundedCorners(5.px))
                    .into(eventImageToolbar)
            } ?: kotlin.run {
                Glide.with(requireActivity())
                    .load(R.drawable.new_group_illu)
                    .centerCrop()
                    .into(eventImage)

                Glide.with(requireActivity())
                    .load(R.drawable.new_group_illu)
                    .transform(CenterCrop(), RoundedCorners(5.px))
                    .into(eventImageToolbar)
            }

            canceled.isVisible = event?.status == Status.CLOSED
            if (event?.status == Status.CLOSED) {
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
        getPrincipalMember()
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

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun handleDeletedResponse(success: Boolean) {
        if(isAdded){
            if (success){
                showToast(getString(R.string.delete_success_send))
            }else{
                showToast(getString(R.string.delete_error_send_failed))
            }
        }
    }

    private fun loadPosts() {
        eventPresenter.getEventPosts(eventId)
    }

    private fun initializePosts() {
        binding.postsNewRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PostAdapter(
                requireContext(),
                newPostsList,
                ::openCommentPage,
                ::openReportFragment,
                ::openImageFragment
            )
            (adapter as? PostAdapter)?.initiateList()
        }
        binding.postsOldRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PostAdapter(
                requireContext(),
                oldPostsList,
                ::openCommentPage,
                ::openReportFragment,
                ::openImageFragment
            )
            (adapter as? PostAdapter)?.initiateList()
        }
    }

    private fun openCommentPage(post: Post, shouldOpenKeyboard: Boolean) {
        startActivityForResult(
            Intent(context, EventCommentActivity::class.java)
                .putExtras(
                    bundleOf(
                        Const.ID to eventId,
                        Const.POST_ID to post.id,
                        Const.POST_AUTHOR_ID to post.user?.userId,
                        Const.SHOULD_OPEN_KEYBOARD to shouldOpenKeyboard,
                        Const.IS_MEMBER to event?.member,
                        Const.NAME to event?.title
                    )
                ), 0
        )
    }

    private fun openReportFragment(postId:Int, userId:Int) {

        val reportGroupBottomDialogFragment =
            event?.id?.let {
                val meId = EntourageApplication.get().me()?.id
                val post = allPostsList.find { it.id == postId }
                val fromLang = post?.contentTranslations?.fromLang
                if (fromLang != null) {
                    DataLanguageStock.updatePostLanguage(fromLang)
                }
                val isFrome = meId == post?.user?.id?.toInt()
                Log.wtf("wtf", "isFrome $isFrome")

                var description = allPostsList.find { it.id == postId }?.content ?: ""

                ReportModalFragment.newInstance(
                    postId,
                    it, ReportTypes.REPORT_POST_EVENT, isFrome
                ,false,false, contentCopied = description)
            }
        reportGroupBottomDialogFragment?.setCallback(this)
        reportGroupBottomDialogFragment?.show(parentFragmentManager, ReportModalFragment.TAG)

    }

    private fun openImageFragment(imageUrl:String, postId: Int) {
        val intent = Intent(requireContext(), ImageDialogActivity::class.java)
        intent.putExtra("postId", postId)
        intent.putExtra("eventId", this.event?.id)
        startActivity(intent)
    }

    private fun fragmentResult() {
        setFragmentResultListener(Const.REQUEST_KEY_SHOULD_REFRESH) { _, bundle ->
            val shouldRefresh = bundle.getBoolean(Const.SHOULD_REFRESH)
            if (shouldRefresh) eventPresenter.getEvent(eventId)
        }
    }

    private fun handleSettingsButton() {
        if(isAdded){
            binding.iconSettings.setOnClickListener {
                AnalyticsEvents.logEvent(AnalyticsEvents.Event_detail_action_param)
                event?.let { event ->
                    SettingsModalFragment.newInstance(event).show(parentFragmentManager, SettingsModalFragment.TAG)
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (RefreshController.shouldRefreshEventFragment) eventPresenter.getEvent(eventId)
        loadPosts()

    }


    private fun handleAboutButton() {
        binding.more.tv_more.setOnClickListener {
            event?.let { event ->
                val eventUI = event.toEventUi(requireContext())
                val action =
                    FeedFragmentDirections.actionEventFeedToEventAbout(
                        eventUI
                    )
                findNavController().navigate(action)
            }
        }
        binding.btnShare.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_SHARED)
            val shareTitle = getString(R.string.share_title_event)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareTitle + "\n" + event?.title + ": " + "\n" + createShareUrl())
            }
            startActivity(Intent.createChooser(shareIntent, "Partager l'URL via"))
        }
        binding.bigBtnShare.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_SHARED)
            val shareTitle = getString(R.string.share_title_event)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareTitle + "\n" + event?.title + ": " + "\n" + createShareUrl())
            }
            startActivity(Intent.createChooser(shareIntent, "Partager l'URL via"))
        }
    }

    private fun createShareUrl():String{
        val deepLinksHostName = BuildConfig.DEEP_LINKS_URL
        return "https://" + deepLinksHostName + "/app/outings/" + event?.uuid_v2
    }

    fun getPrincipalMember(){
        eventPresenter.getEventMembers(eventId)

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

    private fun handleParticipateButton() {
        binding.join.setOnClickListener {
            if (event?.member==false){
                eventPresenter.participate(eventId)
            }else{
                eventPresenter.leaveEvent(eventId)
            }
        }
        binding.participate.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_detail_action_participate)
            if (event?.member==false){
                eventPresenter.participate(eventId)
            }else{
                eventPresenter.leaveEvent(eventId)
            }
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
            event?.let {event ->
                event.member = !event.member
                updateButtonJoin()
                handleCreatePostButton()
                binding.participate.hide()
                if (event.metadata?.placeLimit != null) {
                    showLimitPlacePopUp()
                } else {
                    if (shouldShowPopUp){
                        Utils.showAddToCalendarPopUp(requireContext(), event.toEventUi(requireContext()))
                    }
                    shouldShowPopUp = false
                }
            }
        }
    }
    private fun handleLeaveResponse(isParticipating: Boolean) {
        event?.let {event ->
            event.member = !event.member
            updateButtonJoin()
            handleCreatePostButton()
            binding.participate.show()
        }
    }

    private fun showLimitPlacePopUp() {
        CustomAlertDialog.showOnlyOneButton(
            requireContext(),
            getString(R.string.event_limited_places_title),
            getString(R.string.event_limited_places_subtitle),
            getString(R.string.button_OK)
        )
    }

    private fun handleCreatePostButtonClick() {
        binding.createPost.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_detail_action_post)
            val intent = Intent(context, CreatePostEventActivity::class.java)
            intent.putExtra(Const.ID, eventId)
            startActivityForResult(intent, 0)
        }
    }

    private fun handleCreatePostButton() {
        if (event?.member == false) {
            binding.createPost.hide(true)
            binding.postsLayoutEmptyState.subtitle.visibility = View.VISIBLE
            binding.postsLayoutEmptyState.arrow.visibility = View.VISIBLE
        } else {
            binding.createPost.show()
            binding.postsLayoutEmptyState.subtitle.visibility = View.GONE
            binding.postsLayoutEmptyState.arrow.visibility = View.GONE
        }
    }

    private fun handleMetaData(tags: Tags?) {
        interestsList.clear()
        event?.interests?.let { groupInterests ->
            tags?.interests?.forEach { interest ->
                if (groupInterests.contains(interest.id)) interest.name?.let {
                    interestsList.add(it)
                }
            }
        }
        binding.interests.adapter?.notifyDataSetChanged()
    }

    private fun initializeMembersPhotos() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = event?.members?.let { GroupMembersPhotosAdapter(it) }
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
            getString(if (event?.member==true) R.string.participating else R.string.participate)
        val textColor = ContextCompat.getColor(
            requireContext(),
            if (event?.member==true) R.color.orange else R.color.white
        )
        val background = ResourcesCompat.getDrawable(
            resources,
            if (event?.member==true) R.drawable.new_bg_rounded_button_orange_stroke else R.drawable.new_bg_rounded_button_orange_fill,
            null
        )
        val rightDrawable = ResourcesCompat.getDrawable(
            resources,
            if (event?.member==true) R.drawable.new_check else R.drawable.new_plus_white,
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
        if(event?.calculateIfEventPassed()==true){
            binding.join.visibility = View.GONE
        }
    }

    private fun onFragmentResult() {
        setFragmentResultListener(Const.REQUEST_KEY_SHOULD_REFRESH) { _, bundle ->
            if (bundle.getBoolean(Const.SHOULD_REFRESH)) eventPresenter.getEvent(eventId)
        }
    }

    override fun onSuppressPost() {
        lifecycleScope.launch {
            delay(500)
            loadPosts()
        }
    }

    override fun onTranslatePost(id: Int) {
        val isNewPost = newPostsList.any { it.id == id }
        val adapter = if (isNewPost) {
            binding.postsNewRecyclerview.adapter as? PostAdapter
        } else {
            binding.postsOldRecyclerview.adapter as? PostAdapter
        }
        adapter?.translateItem(id)
    }
}