package social.entourage.android.groups.details.feed

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.new_fragment_feed.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Group
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.Tags
import social.entourage.android.comment.PostAdapter
import social.entourage.android.databinding.NewFragmentFeedBinding
import social.entourage.android.events.create.CreateEventActivity
import social.entourage.android.groups.GroupModel
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.details.GroupDetailsFragment
import social.entourage.android.groups.details.members.MembersType
import social.entourage.android.profile.myProfile.InterestsAdapter
import social.entourage.android.report.ReportModalFragment
import social.entourage.android.report.ReportTypes
import social.entourage.android.tools.image_viewer.ImageDialogActivity
import social.entourage.android.tools.image_viewer.ImageDialogFragment
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.utils.px
import uk.co.markormesher.android_fab.SpeedDialMenuAdapter
import uk.co.markormesher.android_fab.SpeedDialMenuItem
import kotlin.math.abs


const val rotationDegree = 135F

class FeedFragment : Fragment() {

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
    private var page: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupId = args.groupID
        myId = EntourageApplication.me(activity)?.id
        groupPresenter.getGroup(groupId)
        groupPresenter.getGroup.observe(viewLifecycleOwner, ::handleResponseGetGroup)
        groupPresenter.getAllPosts.observe(viewLifecycleOwner, ::handleResponseGetGroupPosts)
        groupPresenter.hasUserJoinedGroup.observe(viewLifecycleOwner, ::handleJoinResponse)
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
    }

    override fun onResume() {
        super.onResume()
        loadPosts()
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

    private fun handleResponseGetGroupPosts(allPosts: MutableList<Post>?) {
        binding.swipeRefresh.isRefreshing = false
        newPostsList.clear()
        oldPostsList.clear()
        allPosts?.let {
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
            group = it
            updateView()
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
                //.placeholder(R.drawable.new_group_illu)
                //.error(R.drawable.new_group_illu)
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
            if (!(group?.member == true)) groupPresenter.joinGroup(groupId)
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

    private fun showWelcomeMessage(){
        var message = getString(R.string.welcome_message_placeholder)
        var title = getString(R.string.welcome_message_title)
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
            adapter = group?.futureEvents?.let { GroupEventsAdapter(it) }
        }
    }

    private fun initializePosts() {
        binding.postsNewRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PostAdapter(
                newPostsList,
                ::openCommentPage,
                ::openReportFragment,
                ::openImageFragment
            )
        }
        binding.postsOldRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PostAdapter(
                oldPostsList,
                ::openCommentPage,
                ::openReportFragment,
                ::openImageFragment
            )
        }
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
    private fun openReportFragment(postId:Int) {
        val reportGroupBottomDialogFragment =
            group?.id?.let {
                ReportModalFragment.newInstance(
                    postId,
                    it, ReportTypes.REPORT_POST
                )
            }
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
                        groupId, name,
                        members_count,
                        address?.displayAddress,
                        interests,
                        description,
                        members,
                        member,
                        EntourageApplication.me(activity)?.id == admin?.id
                    )
                }
            }

            GroupDetailsFragment.newInstance(groupUI)
                .show(parentFragmentManager, GroupDetailsFragment.TAG)
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

    private fun handleAboutButton() {
        binding.more.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_FEED_MORE_DESCRIPTION
            )
            group?.let {
                groupUI = GroupModel(
                    groupId,
                    it.name,
                    it.members_count,
                    it.address?.displayAddress,
                    it.interests,
                    it.description,
                    it.members,
                    it.member,
                    EntourageApplication.me(activity)?.id == it.admin?.id
                )
            }
            val action = FeedFragmentDirections.actionGroupFeedToGroupAbout(groupUI)
            findNavController().navigate(action)
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
}