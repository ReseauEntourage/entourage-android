package social.entourage.android.events.details.feed

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.actions.create.CreateActionActivity
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.Status
import social.entourage.android.api.model.Survey
import social.entourage.android.api.model.Tags
import social.entourage.android.api.model.toEventUi
import social.entourage.android.comment.PostAdapter
import social.entourage.android.comment.ReactionInterface
import social.entourage.android.comment.SurveyInteractionListener
import social.entourage.android.databinding.FragmentFeedEventBinding
import social.entourage.android.events.EventsPresenter
import social.entourage.android.events.create.CreateEventActivity
import social.entourage.android.events.details.SettingsModalFragment
import social.entourage.android.groups.details.feed.CallbackReportFragment
import social.entourage.android.groups.details.feed.FeedFragment
import social.entourage.android.groups.details.feed.GroupMembersPhotosAdapter
import social.entourage.android.groups.details.feed.rotationDegree
import social.entourage.android.groups.details.members.MembersType
import social.entourage.android.language.LanguageManager
import social.entourage.android.members.MembersActivity
import social.entourage.android.profile.myProfile.InterestsAdapter
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.report.ReportModalFragment
import social.entourage.android.report.ReportTypes
import social.entourage.android.survey.CreateSurveyActivity
import social.entourage.android.survey.ResponseSurveyActivity
import social.entourage.android.survey.SurveyPresenter
import social.entourage.android.tools.calculateIfEventPassed
import social.entourage.android.tools.image_viewer.ImageDialogActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.Utils.enableCopyOnLongClick
import social.entourage.android.tools.utils.px
import social.entourage.android.tools.utils.underline
import java.text.SimpleDateFormat
import kotlin.math.abs
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.testing.FakeReviewManager
import timber.log.Timber


class FeedFragment : Fragment(), CallbackReportFragment, ReactionInterface,
    SurveyInteractionListener {

    private var _binding: FragmentFeedEventBinding? = null
    val binding: FragmentFeedEventBinding get() = _binding!!

    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }
    private val surveyPresenter: SurveyPresenter by lazy { SurveyPresenter() }
    private var interestsList: ArrayList<String> = ArrayList()
    private var eventId = Const.DEFAULT_VALUE
    private var event: Events? = null
    private var myId: Int? = null
    private val args: FeedFragmentArgs by navArgs()
    private var shouldShowPopUp = true
    private var isLoading = false
    private var page:Int = 1
    private val ITEM_PER_PAGE = 10


    private var newPostsList: MutableList<Post> = mutableListOf()
    private var oldPostsList: MutableList<Post> = mutableListOf()
    private var allPostsList: MutableList<Post> = mutableListOf()
    private var memberList: MutableList<EntourageUser> = mutableListOf()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedEventBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventId = args.eventID
        myId = EntourageApplication.me(activity)?.id
        eventPresenter.getEvent.observe(viewLifecycleOwner, ::handleResponseGetEvent)
        eventPresenter.isEventReported.observe(requireActivity(), ::handleDeletedResponse)
        eventPresenter.hasUserLeftEvent.observe(requireActivity(),::handleLeaveResponse)
        eventPresenter.isUserParticipating.observe(viewLifecycleOwner, ::handleParticipateResponse)
        eventPresenter.getAllPosts.observe(viewLifecycleOwner, ::handleResponseGetEventPosts)
        surveyPresenter.isSurveyVoted.observe(requireActivity(), ::handleSurveyPostResponse)
        eventPresenter.getMembers.observe(viewLifecycleOwner, ::handleResponseGetMembers)
        getPrincipalMember()

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
        setupNestedScrollViewScrollListener()

    }




    private fun handleResponseGetEvent(getEvent: Events?) {
        getEvent?.let {
            event = it
            updateView()
        }
        handleImageViewAnimation()
    }

    private fun setupNestedScrollViewScrollListener() {
        binding.scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > 0) {
                // Ici, tu peux notifier ton RecyclerView adapter de cacher les éléments layoutReactions
                hideReactionsInRecyclerView()
                if (!binding.scrollView.canScrollVertically(1) && !isLoading) {
                    isLoading = true
                    page++ // Incrémente la page pour la pagination
                    binding.progressBar.visibility = View.VISIBLE
                    loadPosts()
                }
            }
        })
    }

    private fun hideReactionsInRecyclerView() {
        hideReactionsInView(binding.postsNewRecyclerview)
        hideReactionsInView(binding.postsOldRecyclerview)
    }

    private fun hideReactionsInView(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

        if (firstVisiblePosition == -1 || lastVisiblePosition == -1) return

        // Parcours toutes les cellules visibles
        for (i in firstVisiblePosition..lastVisiblePosition) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? PostAdapter.ViewHolder
            viewHolder?.binding?.layoutReactions?.visibility = View.GONE
        }
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
        CoroutineScope(Dispatchers.Main).launch {
            binding.swipeRefresh.isRefreshing = false
            binding.progressBar.visibility = View.GONE
            isLoading = false
            allPosts?.let {
                allPostsList.addAll(allPosts)
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
            eventDescription.enableCopyOnLongClick(requireContext())
            if(event != null && event?.membersCount!! > 1){
                eventMembersNumberLocation.text = String.format(
                    getString(R.string.participant_number),
                    event?.membersCount,
                )
            }else{
                eventMembersNumberLocation.text = String.format(
                    getString(R.string.participant_number_singular),
                    event?.membersCount,
                )
            }
            val locale = LanguageManager.getLocaleFromPreferences(requireContext())


            event?.metadata?.placeLimit?.let {
                if(it == 0){
                    placesLimit.root.visibility = View.GONE
                }else{
                    placesLimit.root.visibility = View.VISIBLE
                }
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
                //binding.organizer.content.text = String.format(getString(R.string.event_organisez_by), author?.userName)
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
        createPost()
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
            page = 1
            oldPostsList.clear()
            newPostsList.clear()
            allPostsList.clear()
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
        CoroutineScope(Dispatchers.IO).launch {
            eventPresenter.getEventPosts(eventId,page,ITEM_PER_PAGE)
        }
    }

    private fun initializePosts() {

        binding.postsNewRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.postsNewRecyclerview.adapter = PostAdapter(
            requireContext(),
            this,
            this,
            newPostsList,
            this.event?.member,
            ::openCommentPage,
            ::openReportFragment,
            ::openImageFragment,
            memberList)
        (binding.postsNewRecyclerview.adapter as? PostAdapter)?.initiateList()


        binding.postsOldRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.postsOldRecyclerview.adapter = PostAdapter(
                requireContext(),
            this,
            this,
                oldPostsList,
                this.event?.member,
                ::openCommentPage,
                ::openReportFragment,
                ::openImageFragment,
                memberList)
            (binding.postsOldRecyclerview.adapter as? PostAdapter)?.initiateList()

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
                val post = allPostsList.find { post -> post.id == postId }
                val fromLang = post?.contentTranslations?.fromLang
                if (fromLang != null) {
                    DataLanguageStock.updatePostLanguage(fromLang)
                }
                val isFrome = meId == post?.user?.id?.toInt()

                val description = allPostsList.find { post1 -> post1.id == postId }?.content ?: ""

                ReportModalFragment.newInstance(
                    postId,
                    it, ReportTypes.REPORT_POST_EVENT, isFrome
                , isConv = false, isOneToOne = false, contentCopied = description)
            }
        reportGroupBottomDialogFragment?.setCallback(this)
        reportGroupBottomDialogFragment?.show(parentFragmentManager, ReportModalFragment.TAG)

    }

    private fun handleSurveyPostResponse(success: Boolean) {
        if(isAdded){
            if (success){
                
            }else{
                showToast("Erreur serveur, veuillez réessayer plus tard")
            }
        }
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
        newPostsList.clear()
        oldPostsList.clear()
        allPostsList.clear()
        loadPosts()

    }


    private fun handleAboutButton() {
        binding.tvMore.setOnClickListener {
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

    private fun getPrincipalMember(){
        eventPresenter.getEventMembers(eventId)

    }

    private fun handleResponseGetMembers(allMembers: MutableList<EntourageUser>?) {
        if (allMembers != null) {
            this.memberList.clear()
            this.memberList.addAll(allMembers)
            var numberOrganizer = 0
            var nameOrganizers = ""
            for(member in allMembers){
                Timber.wtf("wtf role " + member.groupRole + "name " + nameOrganizers)
                if(member.groupRole == "organizer"){
                    numberOrganizer += 1
                    if(numberOrganizer < 3){
                        nameOrganizers += ", " + member.displayName
                    }
                }
                if(member.id.toInt() == event?.author?.userID){
                    if(member.communityRoles?.contains("Équipe Entourage") == true || member.communityRoles?.contains("Ambassadeur") == true){
                        binding.tvAssociation.text = getString(R.string.event_organisez_entourage)
                        binding.tvAssociation.visibility = View.VISIBLE
                    }
                }
            }
            nameOrganizers.removePrefix(", ")
            if(numberOrganizer > 2 ){
                nameOrganizers += " + " + (numberOrganizer - 2).toString()
            }
            binding.organizer.content.text = String.format(getString(R.string.event_organisez_by), nameOrganizers)

        }

        eventPresenter.getEvent(eventId)
    }

    fun requestInAppReview(context: Context) {
    val manager = ReviewManagerFactory.create(context)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val flow = manager.launchReviewFlow(context as Activity, task.result)
                flow.addOnCompleteListener {
                }
            } else {
            }
        }
    }



    private fun handleParticipateButton() {
        binding.join.setOnClickListener {
            requestInAppReview(requireContext())
            val meUser = EntourageApplication.me(activity)
            Timber.wtf("wtf role " + meUser?.roles)
            if(meUser?.roles?.contains("Ambassadeur") == true){
                CustomAlertDialog.showAmbassadorWithTwoButton(requireContext(),
                    onNo = {
                        if (event?.member==false){
                            eventPresenter.joinAsOrganizer(eventId)
                        }else{
                            eventPresenter.leaveEvent(eventId)
                        }
                    }, onYes = {
                        if (event?.member==false){
                            eventPresenter.participate(eventId)
                        }else{
                            eventPresenter.leaveEvent(eventId)
                        }
                    })
            }else{
                if (event?.member==false){
                    eventPresenter.participate(eventId)
                }else{
                    eventPresenter.leaveEvent(eventId)
                }
            }
        }
        binding.participate.setOnClickListener {
            val meUser = EntourageApplication.me(activity)
            Timber.wtf("wtf", "role ? ${meUser?.roles}")
            if(meUser?.roles?.contains("Ambassadeur") == true){
                CustomAlertDialog.showAmbassadorWithTwoButton(requireContext(),
                    onNo = {
                        if (event?.member==false){
                            eventPresenter.joinAsOrganizer(eventId)
                        }else{
                            eventPresenter.leaveEvent(eventId)
                        }
                }, onYes = {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Event_detail_action_participate)
                    if (event?.member==false){
                        eventPresenter.participate(eventId)
                    }else{
                        eventPresenter.leaveEvent(eventId)
                    }
                })

            }else{
                AnalyticsEvents.logEvent(AnalyticsEvents.Event_detail_action_participate)
                if (event?.member==false){
                    eventPresenter.participate(eventId)
                }else{
                    eventPresenter.leaveEvent(eventId)
                }
            }

        }
    }

    private fun handleMembersButton() {
        binding.members.setOnClickListener {
            val intent = Intent(context, MembersActivity::class.java).apply {
                // Passage des arguments nécessaires
                putExtra("ID", eventId) // Assure-toi que 'groupId' est un Int
                putExtra("TYPE", MembersType.EVENT.code) // Utilise 'code' pour passer l'enum comme un Int
            }
            startActivity(intent)
        }
    }

    private fun handleParticipateResponse(isParticipating: Boolean) {
        if (isParticipating) {
            event?.let {event ->
                event.member = !event.member
                updateButtonJoin()
                //handleCreatePostButton()
                binding.participate.hide()
                eventPresenter.getEvent(eventId)
                if (event.metadata?.placeLimit != null && event.metadata.placeLimit != 0) {
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
            //handleCreatePostButton()
            binding.participate.show()
            eventPresenter.getEvent(eventId)
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

    private fun createPost() {
        val speedDialView: SpeedDialView = binding.createPost
        speedDialView.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_create_post, R.drawable.ic_group_feed_one)
                .setFabBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange))
                .setFabImageTintColor(ContextCompat.getColor(requireContext(), R.color.white))
                .setLabel(getString(R.string.create_post))
                .setLabelColor(ContextCompat.getColor(requireContext(), R.color.white))
                .setLabelBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange))
                .create()
        )
        speedDialView.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_create_survey, R.drawable.ic_survey_creation)
                .setFabBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange))
                .setFabImageTintColor(ContextCompat.getColor(requireContext(), R.color.white))
                .setLabel(getString(R.string.create_survey))
                .setLabelColor(ContextCompat.getColor(requireContext(), R.color.white))
                .setLabelBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange))
                .create()
        )
        speedDialView.setOnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.fab_create_post -> {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Event_detail_action_post)
                    val intent = Intent(context, CreatePostEventActivity::class.java)
                    intent.putExtra(Const.ID, eventId)
                    startActivityForResult(intent, 0)
                    true
                }
                R.id.fab_create_survey -> {
                    AnalyticsEvents.logEvent(
                        AnalyticsEvents.Clic_Event_Create_Poll
                    )
                    val intent = Intent(context, CreateSurveyActivity::class.java)
                    FeedFragment.isFromCreation = true
                    intent.putExtra(Const.EVENT_ID, eventId)
                    startActivity(intent)
                    true
                }
                else -> false
            }
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

    override fun onSuppressPost(id: Int) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            delay(300)
            val isNewPost = newPostsList.any { it.id == id }
            val adapter = if (isNewPost) {
                binding.postsNewRecyclerview.adapter as? PostAdapter
            } else {
                binding.postsOldRecyclerview.adapter as? PostAdapter
            }
            adapter?.deleteItem(id)
            page--
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

    override fun onReactionClicked(postId: Post, reactionId: Int) {
        if(this.event?.member == false){
            AlertDialog.Builder(context) // Utilise 'this' si c'est dans une activité, ou 'getActivity()' si c'est dans un fragment
                .setTitle("Attention")
                .setMessage("Vous devez rejoindre le groupe pour effectuer cette action.")
                .setPositiveButton("Retour") { dialog, which ->
                    // Code à exécuter lorsque le bouton "Retour" est cliqué.
                    // Si tu ne veux rien faire, tu peux laisser ce bloc vide.
                }
                .show()
            return
        }
        eventPresenter.reactToPost(eventId,postId.id!!, reactionId)
    }

    override fun seeMemberReaction(post: Post) {
        MembersActivity.isFromReact = true
        MembersActivity.postId = post.id!!
        val intent = Intent(context, MembersActivity::class.java).apply {
            // Passage des arguments nécessaires
            putExtra("ID", eventId) // Assure-toi que 'groupId' est un Int
            putExtra("TYPE", MembersType.EVENT.code) // Utilise 'code' pour passer l'enum comme un Int
        }
        startActivity(intent)
    }

    override fun deleteReaction(post: Post) {
        if(this.event?.member == false){
            AlertDialog.Builder(context) // Utilise 'this' si c'est dans une activité, ou 'getActivity()' si c'est dans un fragment
                .setTitle("Attention")
                .setMessage("Vous devez rejoindre le groupe pour effectuer cette action.")
                .setPositiveButton("Retour") { dialog, which ->
                    // Code à exécuter lorsque le bouton "Retour" est cliqué.
                    // Si tu ne veux rien faire, tu peux laisser ce bloc vide.
                }
                .show()
            return
        }
        eventPresenter.deleteReactToPost(eventId,post.id!!)
    }

    override fun onSurveyOptionClicked(postId: Int, surveyResponse: MutableList<Boolean>) {
        surveyPresenter.postSurveyResponseEvent(eventId, postId, surveyResponse)
    }

    override fun onDeleteSurveyClick(postId: Int, surveyResponse: MutableList<Boolean>) {
        //Toast.makeText(requireContext(), "Survey option deleted", Toast.LENGTH_SHORT).show()
    }

    override fun showParticipantWhoVote(survey: Survey, postId: Int, question:String) {
        val intent = Intent(context, ResponseSurveyActivity::class.java).apply {
            ResponseSurveyActivity.survey = survey
            ResponseSurveyActivity.isGroup = false
            ResponseSurveyActivity.itemId = eventId
            ResponseSurveyActivity.postId = postId
            ResponseSurveyActivity.question = question

        }
        startActivity(intent)
    }
}