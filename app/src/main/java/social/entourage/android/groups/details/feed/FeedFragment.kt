package social.entourage.android.groups.details.feed

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.google.android.play.core.review.ReviewManagerFactory
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.Group
import social.entourage.android.api.model.GroupUtils
import social.entourage.android.api.model.Interest
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.Survey
import social.entourage.android.api.model.Tags
import social.entourage.android.comment.PostAdapter
import social.entourage.android.comment.ReactionInterface
import social.entourage.android.comment.SurveyInteractionListener
import social.entourage.android.databinding.FragmentFeedBinding
import social.entourage.android.events.create.CreateEventActivity
import social.entourage.android.events.details.feed.CreatePostEventActivity
import social.entourage.android.groups.GroupModel
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.details.GroupDetailsFragment
import social.entourage.android.groups.details.members.MembersType
import social.entourage.android.homev2.HomeEventAdapter
import social.entourage.android.members.MembersActivity
import social.entourage.android.profile.myProfile.InterestsAdapter
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.report.ReportModalFragment
import social.entourage.android.report.ReportTypes
import social.entourage.android.survey.CreateSurveyActivity
import social.entourage.android.survey.ResponseSurveyActivity
import social.entourage.android.survey.SurveyPresenter
import social.entourage.android.tools.image_viewer.ImageDialogActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.utils.CustomTypefaceSpan
import social.entourage.android.tools.utils.Utils.enableCopyOnLongClick
import social.entourage.android.tools.utils.VibrationUtil
import social.entourage.android.tools.utils.px
import timber.log.Timber
import kotlin.math.abs


const val rotationDegree = 135F

class FeedFragment : Fragment(),CallbackReportFragment, ReactionInterface,
    SurveyInteractionListener {

    private var _binding: FragmentFeedBinding? = null
    val binding: FragmentFeedBinding get() = _binding!!
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private var interestsList: ArrayList<String> = ArrayList()
    private var groupId = -1
    private var group: Group? = null
    private lateinit var groupUI: GroupModel
    private var myId: Int? = null
    private val args: FeedFragmentArgs by navArgs()

    private var isLoading = false
    private var page:Int = 0
    private val ITEM_PER_PAGE = 10
    private var hasShownWelcomeMessage = false
    private var surveyPresenter: SurveyPresenter = SurveyPresenter()

    private var newPostsList: MutableList<Post> = ArrayList()
    private var oldPostsList: MutableList<Post> = ArrayList()
    private var allPostsList: MutableList<Post> = ArrayList()
    private var memberList: MutableList<EntourageUser> = mutableListOf()
    private var dernierClicTime: Long = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupId = args.groupID
        myId = EntourageApplication.me(activity)?.id

        getPrincipalMember()
        //groupPresenter.getMembers.observe(viewLifecycleOwner, ::handleResponseGetGroupMembers)
        groupPresenter.getGroup.observe(viewLifecycleOwner, ::handleResponseGetGroup)
        groupPresenter.getAllPosts.observe(viewLifecycleOwner, ::handleResponseGetGroupPosts)
        groupPresenter.hasUserJoinedGroup.observe(viewLifecycleOwner, ::handleJoinResponse)
        groupPresenter.hasUserLeftGroup.observe(viewLifecycleOwner, ::handleLeftResponse)
        groupPresenter.isPostDeleted.observe(requireActivity(), ::handleDeletedResponse)
        groupPresenter.haveReacted.observe(requireActivity(), ::handleReactionGroupPost)
        surveyPresenter.isSurveyVoted.observe(requireActivity(), ::handleSurveyPostResponse)
        handleFollowButton()
        handleBackButton()
        handleSettingsButton()
        handleImageViewAnimation()
        handleMembersButton()
        handleGroupEventsButton()
        handleAboutButton()
        handleSwipeRefresh()
        onFragmentResult()

        binding.createPost.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GROUP_FEED_PLUS)
        }

        setupNestedScrollViewScrollListener()
        createPost()
        loadPosts()
    }


    override fun onResume() {
        super.onResume()
        if(isFromCreation) {
            isFromCreation = false
            page = 0
            oldPostsList.clear()
            newPostsList.clear()
            allPostsList.clear()
            isLoading = true
            loadPosts()
        }
        val fromWelcomeActivity = activity?.intent?.getBooleanExtra("fromWelcomeActivity", false)
        if (fromWelcomeActivity == true) {
            createAPost()
            activity?.intent= Intent(activity, FeedActivity::class.java) // Réinitialise l'intent

        }
        binding.createPost.close()
        binding.overlayView.visibility = View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(
            AnalyticsEvents.VIEW_GROUP_FEED_SHOW
        )
        return binding.root
    }

    private fun setupNestedScrollViewScrollListener() {
        binding.nestSvFeedFragment.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > 0) {
                // Ici, tu peux notifier ton RecyclerView adapter de cacher les éléments layoutReactions
                hideReactionsInRecyclerView()
                if (!binding.nestSvFeedFragment.canScrollVertically(1) && !isLoading) {
                    isLoading = true

                    binding.progressBar.visibility = View.VISIBLE
                    loadPosts()
                }
            }
        })
    }

    private fun getPrincipalMember(){
//        groupPresenter.getGroupMembers(groupId)
        groupPresenter.getGroup(groupId)


    }

    private fun handleResponseGetGroupMembers(allMembers: MutableList<EntourageUser>?) {
        memberList.addAll(allMembers ?: emptyList())
        groupPresenter.getGroup(groupId)

    }

    private fun hideReactionsInRecyclerView() {
        hideReactionsInView(binding.postsNewRecyclerview)
        hideReactionsInView(binding.postsOldRecyclerview)
    }

    private fun hideReactionsInView(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

        // Vérifie que les positions visibles sont valides
        if (firstVisiblePosition == -1 || lastVisiblePosition == -1) return

        // Parcours toutes les cellules visibles
        for (i in firstVisiblePosition..lastVisiblePosition) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? PostAdapter.ViewHolder
            viewHolder?.binding?.layoutReactions?.visibility = View.GONE
        }
    }

    private fun setupMoreTextView() {
        val description = group?.description ?: return
        val limit = 150
        val isTruncated = description.length > limit
        val truncatedText = if (isTruncated) description.substring(0, limit) + "..." else description
        val spannable = SpannableStringBuilder()
        val regularFont = ResourcesCompat.getFont(requireContext(), R.font.nunitosans_regular)

        // Ajouter le texte tronqué ou complet en gris avec police Nunitosans Regular
        spannable.append(truncatedText)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.grey)),
            0,
            truncatedText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            CustomTypefaceSpan(regularFont),
            0,
            truncatedText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvKnowMore.text = spannable
        binding.tvKnowMore.visibility = View.VISIBLE
        binding.btnMoreDesc.paintFlags = binding.btnMoreDesc.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        binding.btnMoreDesc.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_SEE_MORE_DESC
            )
            if (binding.tagsContainer.visibility == View.GONE) {
                // Afficher tout le texte et les tags
                binding.btnMoreDesc.text = getString(R.string.see_less)
                val fullSpannable = SpannableStringBuilder(description)
                fullSpannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.grey)),
                    0,
                    description.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                fullSpannable.setSpan(
                    CustomTypefaceSpan(regularFont),
                    0,
                    description.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                binding.tvKnowMore.text = fullSpannable
                addTags() // Afficher les tags
                binding.tagsContainer.visibility = View.VISIBLE
            } else {
                // Revenir au texte tronqué et masquer les tags
                binding.btnMoreDesc.text = getString(R.string.see_more)
                binding.tvKnowMore.text = spannable
                binding.tagsContainer.visibility = View.GONE
            }
        }
        if(!isTruncated){
            addTags()
            binding.btnMoreDesc.visibility = View.GONE
            binding.tagsContainer.visibility = View.VISIBLE
        }
    }
    private fun addTags() {
        val interests = group?.interests ?: return
        val context = requireContext()
        val flexboxLayout = binding.tagsContainer
        flexboxLayout.removeAllViews() // Nettoyer les tags existants
        Timber.wtf("wtf interest $interests")
        interests.forEach { interest ->
            val tagView = LayoutInflater.from(context).inflate(R.layout.tag_item_layout, flexboxLayout, false)
            val tagTextView = tagView.findViewById<TextView>(R.id.tv_tag_home_v2_event_item)

            // Configure le texte et l'icône
            tagTextView.text = GroupUtils.showTagTranslated(context, interest).replaceFirstChar { it.uppercaseChar() }

//            when (interest) {
//                Interest.animals -> tagImageView.setImageResource(R.drawable.new_animals)
//                Interest.wellBeing -> tagImageView.setImageResource(R.drawable.new_wellbeing)
//                Interest.cooking -> tagImageView.setImageResource(R.drawable.new_cooking)
//                Interest.culture -> tagImageView.setImageResource(R.drawable.new_art)
//                Interest.games -> tagImageView.setImageResource(R.drawable.new_games)
//                Interest.nature -> tagImageView.setImageResource(R.drawable.new_nature)
//                Interest.sport -> tagImageView.setImageResource(R.drawable.new_sport)
//                Interest.activities -> tagImageView.setImageResource(R.drawable.new_drawing)
//                Interest.marauding -> tagImageView.setImageResource(R.drawable.new_encounters)
//                else -> tagImageView.setImageResource(R.drawable.new_others)
//            }
            flexboxLayout.addView(tagView)
        }
        Timber.wtf("wtf tag addend : " + flexboxLayout.childCount)
        Timber.wtf("wtf tag visibility : " + flexboxLayout.visibility)
        Timber.wtf("wtf tag height : " + flexboxLayout.height)
        flexboxLayout.visibility = View.VISIBLE
    }




    private fun handleResponseGetGroupPosts(allPosts: MutableList<Post>?) {
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
            //allPosts?.let { newPostsList.addAll(it) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                newPostsList.removeIf { post ->
                    post.status == "deleted"
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                oldPostsList.removeIf { post ->
                    post.status == "deleted"
                }
            }
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
            if (group?.national == true) {
                group?.address?.displayAddress = ""
            }
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
    private fun handleSurveyPostResponse(success: Boolean) {
        if(isAdded){
            if (success){
                //showToast("Réponse enregistrée !")

            }else{
                showToast("Erreur lors de l'envoi du vote")
            }
        }
    }

    private fun handleCreatePostButton() {
        if (group?.member == true) {
            binding.createPost.show()
            binding.emptyStateEventsSubtitle.visibility = View.VISIBLE
            binding.subtitle.visibility = View.VISIBLE
            binding.arrow.visibility = View.VISIBLE
        } else {
            binding.emptyStateEventsSubtitle.visibility = View.GONE
            binding.subtitle.visibility = View.GONE
            binding.arrow.visibility = View.GONE
        }
    }

    private fun handleImageViewAnimation() {
        binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val res: Float =
                abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            binding.toolbarLayout.alpha = 1f - res
            binding.groupImageToolbar.alpha = res
            binding.groupNameToolbar.alpha = res

        }
    }

    private fun createAPost(){
        isFromCreation = true
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
                getString(R.string.members_location_temporary),
                group?.address?.displayAddress
            )
            initializeMembersPhotos()
            more.visibility = View.VISIBLE
            binding.btnShare.visibility = View.VISIBLE
            join.visibility = View.GONE
            VibrationUtil.vibrate(requireContext())
            toKnow.visibility = View.GONE
            groupDescription.visibility = View.GONE
            if (group?.member == true) {
                join.visibility = View.GONE

            } else {
                join.visibility = View.VISIBLE

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

            // Appelle la méthode pour configurer "en savoir plus"
            setupMoreTextView()
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
                groupPresenter.getGroup(groupId)
                showWelcomeMessage()
            }
        }
    }
    private fun handleLeftResponse(hasJoined: Boolean) {
        hasShownWelcomeMessage = false
        group?.let {
            if (hasJoined) {
                group?.member = !it.member
                updateButtonJoin()
                groupPresenter.getGroup(groupId)
                handleCreatePostButton()

            }
        }
    }

    private fun showWelcomeMessage(){
        if(hasShownWelcomeMessage){
            return
        }
        hasShownWelcomeMessage = true
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
            this,
            newPostsList,
            this.group?.member,
            ::openCommentPage,
            ::openReportFragment,
            ::openImageFragment,
            memberList
        )
        (binding.postsNewRecyclerview.adapter as? PostAdapter)?.initiateList()


        binding.postsOldRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.postsOldRecyclerview.adapter = PostAdapter(
            requireContext(),
            this,
            this,
            oldPostsList,
            this.group?.member,
            ::openCommentPage,
            ::openReportFragment,
            ::openImageFragment,
            memberList
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
        val fromLang = post?.contentTranslations?.fromLang
        if (fromLang != null) {
            DataLanguageStock.updatePostLanguage(fromLang)
        }
        val description = allPostsList.find { it.id == postId }?.content ?: ""
        val reportGroupBottomDialogFragment =
            group?.id?.let {
                ReportModalFragment.newInstance(
                    postId,
                    it, ReportTypes.REPORT_POST,isFrome
                ,false, isOneToOne = false, contentCopied = description)
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

    private fun loadPosts() {
        page++
        groupPresenter.getGroupPosts(groupId, page, ITEM_PER_PAGE)

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
            val intent = Intent(context, MembersActivity::class.java).apply {
                // Passage des arguments nécessaires
                putExtra("ID", groupId) // Assure-toi que 'groupId' est un Int
                putExtra("TYPE", MembersType.GROUP.code) // Utilise 'code' pour passer l'enum comme un Int
            }
            startActivity(intent)
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
        binding.btnShare.setOnClickListener {
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
            if (groupInterests.contains(interest.id)) interest.name?.let {
                interestsList.add(
                    it
                )
            }
        }
    }

    private fun onFragmentResult() {
        setFragmentResultListener(Const.REQUEST_KEY_SHOULD_REFRESH) { _, bundle ->
            val shouldRefresh = bundle.getBoolean(Const.SHOULD_REFRESH)
            if (shouldRefresh) groupPresenter.getGroup(groupId)
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
        requestInAppReview(requireContext())
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
        MembersActivity.isFromReact = true
        MembersActivity.postId = post.id!!
        AnalyticsEvents.logEvent(
            AnalyticsEvents.ACTION_GROUP_FEED_MORE_MEMBERS
        )
        val intent = Intent(context, MembersActivity::class.java).apply {
            // Passage des arguments nécessaires
            putExtra("ID", groupId) // Assure-toi que 'groupId' est un Int
            putExtra("TYPE", MembersType.GROUP.code) // Utilise 'code' pour passer l'enum comme un Int
        }
        startActivity(intent)
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
    companion object {
        var isFromCreation = false
    }

    override fun onSurveyOptionClicked(postId: Int, surveyResponse: MutableList<Boolean>) {
        val tempsActuel = System.currentTimeMillis()
        if (tempsActuel - dernierClicTime > 50) { // Délai d'1 seconde (1000 millisecondes)
            dernierClicTime = tempsActuel
            surveyPresenter.postSurveyResponseGroup(groupId, postId, surveyResponse)
        } else {
            // Affiche un petit message pour calmer les ardeurs de l'utilisateur trop rapide
            Toast.makeText(requireContext(), "Veuillez patienter une seconde avant de relancer un vote", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDeleteSurveyClick(postId: Int, surveyResponse: MutableList<Boolean>) {
        Toast.makeText(requireContext(), "Survey option deleted", Toast.LENGTH_SHORT).show()
    }

    override fun showParticipantWhoVote(survey: Survey, postId: Int, question:String) {
        val intent = Intent(context, ResponseSurveyActivity::class.java).apply {
            ResponseSurveyActivity.survey = survey
            ResponseSurveyActivity.isGroup = true
            ResponseSurveyActivity.itemId = groupId
            ResponseSurveyActivity.postId = postId
            ResponseSurveyActivity.question = question
        }
        startActivity(intent)
    }


    private fun createPost() {

        val speedDialView: SpeedDialView = binding.createPost

        speedDialView.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_create_event, R.drawable.ic_group_feed_two)
                .setFabBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange))
                .setFabImageTintColor(ContextCompat.getColor(requireContext(), R.color.white))
                .setLabel(getString(R.string.create_event))
                .setLabelColor(ContextCompat.getColor(requireContext(), R.color.white))
                .setLabelBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange))
                .create()
        )
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
                R.id.fab_create_event -> {
                    AnalyticsEvents.logEvent(
                        AnalyticsEvents.ACTION_GROUP_FEED_NEW_EVENT
                    )
                    val intent = Intent(context, CreateEventActivity::class.java)
                    intent.putExtra(Const.GROUP_ID, groupId)
                    startActivityForResult(intent,0)
                    true
                }
                R.id.fab_create_post -> {
                    createAPost()
                    requestInAppReview(requireContext())
                    true
                }
                R.id.fab_create_survey -> {
                    AnalyticsEvents.logEvent(
                        AnalyticsEvents.Clic_Group_Create_Poll
                    )
                    requestInAppReview(requireContext())
                    val intent = Intent(context, CreateSurveyActivity::class.java)
                    isFromCreation = true
                    intent.putExtra(Const.GROUP_ID, groupId)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        speedDialView.setOnChangeListener(object : SpeedDialView.OnChangeListener {
            override fun onMainActionSelected(): Boolean {
                // Vous pouvez ici ajouter une action sur le bouton principal
                return false // Retourner false pour garder le comportement par défaut
            }

            override fun onToggleChanged(isOpen: Boolean) {
                // Gérer la visibilité de l'overlayView
                binding.overlayView.visibility = if (isOpen) View.VISIBLE else View.GONE
            }
        })
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

}

 interface CallbackReportFragment{
    fun onSuppressPost(id: Int)
    fun onTranslatePost(id:Int)
}