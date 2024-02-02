package social.entourage.android.groups.details.feed

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.new_fragment_feed.view.arrow
import kotlinx.android.synthetic.main.new_fragment_feed.view.empty_state_events_subtitle
import kotlinx.android.synthetic.main.new_fragment_feed.view.subtitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Group
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.Tags
import social.entourage.android.api.model.notification.Reaction
import social.entourage.android.comment.PostAdapter
import social.entourage.android.comment.ReactionInterface
import social.entourage.android.databinding.NewFragmentFeedBinding
import social.entourage.android.events.create.CreateEventActivity
import social.entourage.android.groups.GroupModel
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.details.GroupDetailsFragment
import social.entourage.android.groups.details.members.MembersFragment
import social.entourage.android.groups.details.members.MembersType
import social.entourage.android.homev2.HomeEventAdapter
import social.entourage.android.profile.myProfile.InterestsAdapter
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.report.ReportModalFragment
import social.entourage.android.report.ReportTypes
import social.entourage.android.tools.image_viewer.ImageDialogActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.utils.Utils.enableCopyOnLongClick
import social.entourage.android.tools.utils.px
import uk.co.markormesher.android_fab.SpeedDialMenuAdapter
import uk.co.markormesher.android_fab.SpeedDialMenuItem
import kotlin.math.abs


const val rotationDegree = 135F

class FeedFragment : Fragment(),CallbackReportFragment, ReactionInterface {

    private var _binding: NewFragmentFeedBinding? = null
    val binding: NewFragmentFeedBinding get() = _binding!!
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private var interestsList: ArrayList<String> = ArrayList()
    private var groupId = -1
    private var group: Group? = null
    private lateinit var groupUI: GroupModel
    private var myId: Int? = null
    private val args: FeedFragmentArgs by navArgs()

    private val speedDialMenuAdapter = object : SpeedDialMenuAdapter() {
        override fun getCount(): Int = 2
        override fun getMenuItem(context: Context, position: Int): SpeedDialMenuItem =
            when (position) {
                0 -> SpeedDialMenuItem(
                    context,
                    R.drawable.new_create_post,
                    getString(R.string.create_post)
                )
                1 -> SpeedDialMenuItem(
                    context,
                    R.drawable.new_create_event,
                    getString(R.string.create_event)
                )
                else -> SpeedDialMenuItem(
                    context,
                    R.drawable.new_create_event,
                    getString(R.string.create_event)
                )
            }



        override fun onMenuItemClick(position: Int): Boolean {
            when (position) {
                0 -> {
                    createAPost()
                }
                1 -> {
                    AnalyticsEvents.logEvent(
                        AnalyticsEvents.ACTION_GROUP_FEED_NEW_EVENT
                    )
                    val intent = Intent(context, CreateEventActivity::class.java)
                    intent.putExtra(Const.GROUP_ID, groupId)
                    startActivityForResult(intent,0)
                }
                else -> {
                    AnalyticsEvents.logEvent(
                        AnalyticsEvents.ACTION_GROUP_FEED_PLUS_CLOSE
                    )
                }
            }
            return true
        }

        override fun onPrepareItemLabel(context: Context, position: Int, label: TextView) {
            TextViewCompat.setTextAppearance(label, R.style.left_courant_bold_black)
        }

        override fun onPrepareItemCard(context: Context, position: Int, card: View) {
            card.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.new_bg_circle_orange
            )
        }

        override fun fabRotationDegrees(): Float = rotationDegree
    }

    private var newPostsList: MutableList<Post> = ArrayList()
    private var oldPostsList: MutableList<Post> = ArrayList()
    private var allPostsList: MutableList<Post> = ArrayList()
    private var page: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupId = args.groupID
        myId = EntourageApplication.me(activity)?.id
        groupPresenter.getGroup(groupId)
        groupPresenter.getGroup.observe(viewLifecycleOwner, ::handleResponseGetGroup)
        groupPresenter.getAllPosts.observe(viewLifecycleOwner, ::handleResponseGetGroupPosts)
        groupPresenter.hasUserJoinedGroup.observe(viewLifecycleOwner, ::handleJoinResponse)
        groupPresenter.hasUserLeftGroup.observe(viewLifecycleOwner, ::handleLeftResponse)
        groupPresenter.isPostDeleted.observe(requireActivity(), ::handleDeletedResponse)
        groupPresenter.haveReacted.observe(requireActivity(), ::handleReactionGroupPost)

        handleFollowButton()
        handleBackButton()
        handleSettingsButton()
        handleImageViewAnimation()
        handleMembersButton()
        handleGroupEventsButton()
        handleAboutButton()
        handleSwipeRefresh()
        onFragmentResult()
        binding.createPost.setContentCoverColour(0xeeffffff.toInt())
        binding.createPost.speedDialMenuAdapter = speedDialMenuAdapter
        binding.createPost.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GROUP_FEED_PLUS)
        }
        binding.createPost.setContentCoverColour(
            ContextCompat.getColor(
                requireContext(),
                R.color.light_beige_96
            )
        )
        setupNestedScrollViewScrollListener()
    }


    override fun onResume() {
        super.onResume()
        loadPosts()
        val fromWelcomeActivity = activity?.intent?.getBooleanExtra("fromWelcomeActivity", false)
        if (fromWelcomeActivity == true) {
            createAPost()
            activity?.intent= Intent(activity, FeedActivity::class.java) // Réinitialise l'intent

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = NewFragmentFeedBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(
            AnalyticsEvents.VIEW_GROUP_FEED_SHOW
        )
        return binding.root
    }

    private fun setupNestedScrollViewScrollListener() {
        binding.nestSvFeedFragment.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY > 0) {
                Log.wtf("NestedScroll", "Scrolling in NestedScrollView")
                // Ici, tu peux notifier ton RecyclerView adapter de cacher les éléments layoutReactions
                hideReactionsInRecyclerView()
            }
        })
    }

    private fun hideReactionsInRecyclerView() {
        hideReactionsInView(binding.postsNewRecyclerview)
        hideReactionsInView(binding.postsOldRecyclerview)
    }

    private fun hideReactionsInView(recyclerView: RecyclerView) {
        val firstVisiblePosition = (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: return
        val lastVisiblePosition = (recyclerView.layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition() ?: return

        // Parcours toutes les cellules visibles
        for (i in firstVisiblePosition..lastVisiblePosition) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? PostAdapter.ViewHolder
            viewHolder?.binding?.layoutReactions?.visibility = View.GONE
        }
    }
    private fun handleResponseGetGroupPosts(allPosts: MutableList<Post>?) {
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
        //allPosts?.let { newPostsList.addAll(it) }
        if (newPostsList.isEmpty() && oldPostsList.isEmpty()) {
            binding.postsLayoutEmptyState.visibility = View.VISIBLE
            binding.postsNewRecyclerview.visibility = View.GONE
            binding.postsOldRecyclerview.visibility = View.GONE
        }
        if (newPostsList.isNotEmpty()) {
            binding.postsNew.root.visibility = View.VISIBLE
            binding.postsNewRecyclerview.visibility = View.VISIBLE
            binding.postsLayoutEmptyState.visibility = View.GONE
            binding.postsNewRecyclerview.adapter?.notifyDataSetChanged()
        } else {
            binding.postsNew.root.visibility = View.GONE
            binding.postsNewRecyclerview.visibility = View.GONE
        }

        if (oldPostsList.isNotEmpty()) {
            if (newPostsList.isNotEmpty()) binding.postsOld.root.visibility = View.VISIBLE
            else binding.postsOld.root.visibility = View.GONE
            binding.postsOldRecyclerview.visibility = View.VISIBLE
            binding.postsLayoutEmptyState.visibility = View.GONE
            binding.postsOldRecyclerview.adapter?.notifyDataSetChanged()
        } else {
            binding.postsOldRecyclerview.visibility = View.GONE
        }
    }


    private fun handleSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            groupPresenter.getGroup(groupId)
            page = 0
            loadPosts()
        }
    }

    private fun handleResponseGetGroup(getGroup: Group?) {
        getGroup?.let {
            groupId = it.id!!
            group = it
            updateView()
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

    private fun handleCreatePostButton() {
        if (group?.member == true) {
            binding.createPost.show()
            binding.eventsLayoutEmptyState.empty_state_events_subtitle.visibility = View.VISIBLE
            binding.postsLayoutEmptyState.subtitle.visibility = View.VISIBLE
            binding.postsLayoutEmptyState.arrow.visibility = View.VISIBLE
        } else {
            binding.createPost.hide(true)
            binding.eventsLayoutEmptyState.empty_state_events_subtitle.visibility = View.GONE
            binding.postsLayoutEmptyState.subtitle.visibility = View.GONE
            binding.postsLayoutEmptyState.arrow.visibility = View.GONE
        }
    }

    private fun handleImageViewAnimation() {
        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val res: Float =
                abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            binding.toolbarLayout.alpha = 1f - res
            binding.groupImageToolbar.alpha = res
            binding.groupNameToolbar.alpha = res

        })
    }

    private fun createAPost(){
        AnalyticsEvents.logEvent(
            AnalyticsEvents.ACTION_GROUP_FEED_NEW_POST
        )
        val intent = Intent(context, CreatePostGroupActivity::class.java)
        intent.putExtra(Const.ID, groupId)
        startActivityForResult(intent, 0)
    }

    private fun updateView() {

        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        with(binding) {
            groupDescription.enableCopyOnLongClick(requireContext())
            groupName.text = group?.name
            groupNameToolbar.text = group?.name
            groupMembersNumberLocation.text = String.format(
                getString(R.string.members_location),
                group?.members_count,
                group?.address?.displayAddress
            )
            initializeMembersPhotos()
            if (group?.member == true) {
                more.visibility = View.VISIBLE
                join.visibility = View.GONE
                toKnow.visibility = View.GONE
                groupDescription.visibility = View.GONE
            } else {
                join.visibility = View.VISIBLE
                toKnow.visibility = View.VISIBLE
                groupDescription.visibility = View.VISIBLE
                groupDescription.text = group?.description
                more.visibility = View.GONE
                initializeInterests()
            }
            if (group?.futureEvents?.isEmpty() == true) {
                binding.eventsLayoutEmptyState.visibility = View.VISIBLE
                binding.eventsRecyclerview.visibility = View.GONE
            } else {
                binding.eventsRecyclerview.visibility = View.VISIBLE
                binding.eventsLayoutEmptyState.visibility = View.GONE
                initializeEvents()
            }
            binding.seeMoreEvents.isVisible = group?.futureEvents?.isNotEmpty() == true
            binding.arrowEvents.isVisible = group?.futureEvents?.isNotEmpty() == true


            Glide.with(requireActivity())
                .load(group?.imageUrl)
                .error(R.drawable.new_group_illu)
                .centerCrop()
                .into(groupImage)

            Glide.with(requireActivity())
                .load(group?.imageUrl)
                .placeholder(R.drawable.new_group_illu)
                .error(R.drawable.new_group_illu)
                .transform(CenterCrop(), RoundedCorners(8.px))
                .into(groupImageToolbar)
        }

        updateButtonJoin()
        initializePosts()
        handleCreatePostButton()
    }

    private fun updateButtonJoin() {
        val isMember = group?.member == true
        val label =
            getString(if (isMember) R.string.member else R.string.join)
        val textColor = ContextCompat.getColor(
            requireContext(),
            if (isMember) R.color.orange else R.color.white
        )
        val background = ResourcesCompat.getDrawable(
            resources,
            if (isMember) R.drawable.new_bg_rounded_button_orange_stroke else R.drawable.new_bg_rounded_button_orange_fill,
            null
        )
        val rightDrawable = ResourcesCompat.getDrawable(
            resources,
            if (isMember) R.drawable.new_check else R.drawable.new_plus_white,
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

    private fun handleFollowButton() {
        binding.join.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_FEED_JOIN
            )
            if (!(group?.member == true)){
                groupPresenter.joinGroup(groupId)
            }else{
                groupPresenter.leaveGroup(groupId)
            }
        }
    }

    private fun handleJoinResponse(hasJoined: Boolean) {
        group?.let {
            if (hasJoined) {
                group?.member = !it.member
                updateButtonJoin()
                handleCreatePostButton()
                showWelcomeMessage()
            }
        }
    }
    private fun handleLeftResponse(hasJoined: Boolean) {
        group?.let {
            if (hasJoined) {
                group?.member = !it.member
                updateButtonJoin()
                handleCreatePostButton()

            }
        }
    }

    private fun showWelcomeMessage(){
        var message = getString(R.string.welcome_message_placeholder)
        val title = getString(R.string.welcome_message_title)
        if (group?.welcomeMessage?.isNotBlank() == true) message = group?.welcomeMessage.toString()
        CustomAlertDialog.showWelcomeAlert(
            requireContext(),
            title,
            message,
            getString(R.string.welcome_message_btn_title)){
                this.createAPost()
        }
    }

    private fun initializeMembersPhotos() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = group?.members?.let { GroupMembersPhotosAdapter(it) }
        }
    }

    private fun initializeEvents() {
        binding.eventsRecyclerview.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = HomeEventAdapter(context)
            group?.futureEvents?.let {
                (adapter as? HomeEventAdapter)?.resetData(it)
            }
        }
    }

    private fun initializePosts() {

        binding.postsNewRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.postsNewRecyclerview.adapter = PostAdapter(
            requireContext(),
            this,
            newPostsList,
            ::openCommentPage,
            ::openReportFragment,
            ::openImageFragment
        )
        (binding.postsNewRecyclerview.adapter as? PostAdapter)?.initiateList()


        binding.postsOldRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.postsOldRecyclerview.adapter = PostAdapter(
            requireContext(),
            this,
            oldPostsList,
            ::openCommentPage,
            ::openReportFragment,
            ::openImageFragment
        )
        (binding.postsOldRecyclerview.adapter as? PostAdapter)?.initiateList()

    }

    private fun openCommentPage(post: Post, shouldOpenKeyboard: Boolean) {
        startActivityForResult(
            Intent(context, GroupCommentActivity::class.java)
                .putExtras(
                    bundleOf(
                        Const.ID to group?.id,
                        Const.POST_ID to post.id,
                        Const.POST_AUTHOR_ID to post.user?.userId,
                        Const.SHOULD_OPEN_KEYBOARD to shouldOpenKeyboard,
                        Const.IS_MEMBER to group?.member,
                        Const.NAME to group?.name
                    )
                ), 0
        )
    }
    private fun openReportFragment(postId:Int,userId:Int) {
        if(this.group?.member == false){
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

        val meId = EntourageApplication.get().me()?.id
        val post = allPostsList.find { it.id == postId }
        val isFrome = meId == post?.user?.id?.toInt()
        Log.wtf("wtf", "isFrome $isFrome")
        val fromLang = post?.contentTranslations?.fromLang
        if (fromLang != null) {
            DataLanguageStock.updatePostLanguage(fromLang)
        }
        var description = allPostsList.find { it.id == postId }?.content ?: ""
        val reportGroupBottomDialogFragment =
            group?.id?.let {
                ReportModalFragment.newInstance(
                    postId,
                    it, ReportTypes.REPORT_POST,isFrome
                ,false,false, contentCopied = description)
            }
        reportGroupBottomDialogFragment?.setCallback(this)
        reportGroupBottomDialogFragment?.show(parentFragmentManager, ReportModalFragment.TAG)

    }


    private fun openImageFragment(imageUrl:String, postId: Int) {
        val intent = Intent(requireContext(), ImageDialogActivity::class.java)
        intent.putExtra("postId", postId)
        intent.putExtra("groupId", this.group?.id)
        startActivity(intent)
    }

    private fun initializeInterests() {
        if (interestsList.isEmpty()) binding.interests.visibility = View.GONE
        else {
            binding.interests.visibility = View.VISIBLE
            binding.interests.apply {
                val layoutManagerFlex = FlexboxLayoutManager(context)
                layoutManagerFlex.flexDirection = FlexDirection.ROW
                layoutManagerFlex.justifyContent = JustifyContent.CENTER
                layoutManager = layoutManagerFlex
                adapter = InterestsAdapter(interestsList)
            }
        }
    }

    private fun loadPosts() {
        groupPresenter.getGroupPosts(groupId)
    }

    private fun handleBackButton() {
        binding.iconBack.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_FEED_BACK_ARROW
            )
            requireActivity().finish()
        }
    }

    private fun handleSettingsButton() {
        binding.iconSettings.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_FEED_OPTION
            )

            group?.let {
                with(it) {
                    groupUI = GroupModel(
                        groupId, name, nameTranslations, uuid_v2,
                        members_count,
                        address?.displayAddress,
                        interests,
                        description, descriptionTranslations,
                        members,
                        member,
                        EntourageApplication.me(activity)?.id == admin?.id
                    )
                }
                GroupDetailsFragment.newInstance(groupUI)
                    .show(parentFragmentManager, GroupDetailsFragment.TAG)
            }
        }
    }

    private fun handleMembersButton() {
        binding.members.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_FEED_MORE_MEMBERS
            )
            val action =
                FeedFragmentDirections.actionGroupFeedToGroupMembers(groupId, MembersType.GROUP)
            findNavController().navigate(action)
        }
    }

    private fun handleReactionGroupPost(reactionId: Int) {

    }

    private fun handleGroupEventsButton() {
        binding.seeMoreEvents.setOnClickListener {
            group?.name?.let { name ->
                val action =
                    FeedFragmentDirections.actionGroupFeedToGroupEventsList(
                        groupId,
                        name,
                        group?.member == true
                    )
                findNavController().navigate(action)
            }
        }
    }

    private fun createShareUrl():String{
        val deepLinksHostName = BuildConfig.DEEP_LINKS_URL
        return "https://" + deepLinksHostName + "/app/neighborhoods/" + group?.uuid_v2
    }
    private fun handleAboutButton() {
        binding.more.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_FEED_MORE_DESCRIPTION
            )
            group?.let {
                groupUI = GroupModel(
                    groupId,
                    it.name,
                    it.nameTranslations,
                    it.uuid_v2,
                    it.members_count,
                    it.address?.displayAddress,
                    it.interests,
                    it.description,
                    it.descriptionTranslations,
                    it.members,
                    it.member,
                    EntourageApplication.me(activity)?.id == it.admin?.id
                )
                val action = FeedFragmentDirections.actionGroupFeedToGroupAbout(groupUI)
                findNavController().navigate(action)
            }
        }
        binding.btnShare.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GROUPOPTION_SHARE)
            val shareTitle = getString(R.string.share_title_group)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareTitle + "\n" + group?.name + ": " + "\n" + createShareUrl())
            }
            startActivity(Intent.createChooser(shareIntent, "Partager l'URL via"))
        }
        binding.bigBtnShare.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GROUPOPTION_SHARE)
            val shareTitle = getString(R.string.share_title_group)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareTitle + "\n" + group?.name + ": " + "\n" + createShareUrl())
            }
            startActivity(Intent.createChooser(shareIntent, "Partager l'URL via"))
        }
    }

    private fun handleMetaData(tags: Tags?) {
        if (group == null) return

        interestsList.clear()
        val groupInterests = group!!.interests
        tags?.interests?.forEach { interest ->
            if (groupInterests.contains(interest.id)) interest.name?.let { it ->
                interestsList.add(
                    it
                )
            }
        }
        binding.interests.adapter?.notifyDataSetChanged()
    }

    private fun onFragmentResult() {
        setFragmentResultListener(Const.REQUEST_KEY_SHOULD_REFRESH) { _, bundle ->
            val shouldRefresh = bundle.getBoolean(Const.SHOULD_REFRESH)
            if (shouldRefresh) groupPresenter.getGroup(groupId)
        }
    }

    override fun onSuppressPost() {
        lifecycleScope.launch {
            delay(500)
            loadPosts()
            updateView()
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
        if(this.group?.member == false){
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
        groupPresenter.reactToPost(groupId,postId.id!!, reactionId)
    }

    override fun seeMemberReaction(post: Post) {
        MembersFragment.isFromReact = true
        MembersFragment.postId = post.id!!
        AnalyticsEvents.logEvent(
            AnalyticsEvents.ACTION_GROUP_FEED_MORE_MEMBERS
        )
        val action =
            FeedFragmentDirections.actionGroupFeedToGroupMembers(groupId, MembersType.GROUP)
        findNavController().navigate(action)
    }

    override fun deleteReaction(post: Post) {
        if(this.group?.member == false){
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
        groupPresenter.deleteReactToPost(groupId, post.id!!)
    }
}

 interface CallbackReportFragment{
    fun onSuppressPost()
    fun onTranslatePost(id:Int)
}