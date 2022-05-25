package social.entourage.android.new_v8.groups.details.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.appbar.AppBarLayout
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentFeedBinding
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.groups.details.SettingsModalFragment
import social.entourage.android.new_v8.models.GroupUiModel
import social.entourage.android.new_v8.models.Group
import social.entourage.android.new_v8.profile.myProfile.InterestsAdapter
import timber.log.Timber
import kotlin.math.abs

class FeedFragment : Fragment() {

    private var _binding: NewFragmentFeedBinding? = null
    val binding: NewFragmentFeedBinding get() = _binding!!
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private var interestsList: ArrayList<String> = ArrayList()
    private var groupId = -1
    private lateinit var group: Group
    private lateinit var groupUI: GroupUiModel
    private var myId: Int? = null
    private val args: FeedFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupId = args.groupID
        myId = EntourageApplication.me(activity)?.id
        groupPresenter.getGroup(groupId)
        groupPresenter.getGroup.observe(viewLifecycleOwner, ::handleResponseGetGroup)
        groupPresenter.hasUserJoinedGroup.observe(requireActivity(), ::handleJoinResponse)
        handleFollowButton()
        handleBackButton()
        handleSettingsButton()
        handleImageViewAnimation()
        handleMembersButton()
        handleAboutButton()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }


    private fun handleResponseGetGroup(getGroup: Group?) {
        getGroup?.let {
            group = it
            updateView()
        }

    }

    private fun handleImageViewAnimation() {
        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val res: Float =
                abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            binding.toolbarLayout.alpha = 1f - res
            Timber.e(res.toString())
            binding.groupImageToolbar.alpha = res
            binding.groupNameToolbar.alpha = res
        })
    }

    private fun updateView() {
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        with(binding) {
            groupName.text = group.name
            groupNameToolbar.text = group.name
            groupMembersNumberLocation.text = String.format(
                getString(R.string.members_location),
                group.members_count,
                group.address?.displayAddress
            )
            initializeMembersPhotos()
            if (group.member) {
                more.visibility = View.VISIBLE
            } else {
                join.visibility = View.VISIBLE
                toKnow.visibility = View.VISIBLE
                groupDescription.visibility = View.VISIBLE
                groupDescription.text = group.description
                initializeInterests()
            }
            /*
            Glide.with(requireActivity())
                .load(Uri.parse(group.imageUrl))
                .centerCrop()
                .into(groupImage)
             */
        }
        updateButtonJoin()
    }

    private fun updateButtonJoin() {
        val label =
            getString(if (group.member) R.string.member else R.string.join)
        val textColor = ContextCompat.getColor(
            requireContext(),
            if (group.member) R.color.orange else R.color.white
        )
        val background = ResourcesCompat.getDrawable(
            resources,
            if (group.member) R.drawable.new_bg_rounded_button_orange_stroke else R.drawable.new_bg_rounded_button_orange_fill,
            null
        )
        val rightDrawable = ResourcesCompat.getDrawable(
            resources,
            if (group.member) R.drawable.new_check else R.drawable.new_plus_white,
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
            if (!group.member) groupPresenter.joinGroup(groupId)
        }
    }


    private fun handleJoinResponse(hasJoined: Boolean) {
        if (hasJoined) {
            group.member = !group.member
            updateButtonJoin()
        }
    }

    private fun initializeMembersPhotos() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = group.members?.let { GroupMembersPhotosAdapter(it) }
        }
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

    private fun handleBackButton() {
        binding.iconBack.setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun handleSettingsButton() {
        binding.iconSettings.setOnClickListener {
            groupUI = GroupUiModel(
                groupId,
                group.name,
                group.members_count,
                group.address,
                group.interests,
                group.description,
                group.members,
                group.member,
                EntourageApplication.me(activity)?.id == group.admin?.id
            )
            SettingsModalFragment.newInstance(groupUI)
                .show(parentFragmentManager, SettingsModalFragment.TAG)
        }
    }

    private fun handleMembersButton() {
        binding.members.setOnClickListener {
            val action = FeedFragmentDirections.actionGroupFeedToGroupMembers(groupId)
            findNavController().navigate(action)
        }
    }

    private fun handleAboutButton() {
        binding.more.setOnClickListener {
            groupUI = GroupUiModel(
                groupId,
                group.name,
                group.members_count,
                group.address,
                group.interests,
                group.description,
                group.members,
                group.member,
                EntourageApplication.me(activity)?.id == group.admin?.id
            )
            val action = FeedFragmentDirections.actionGroupFeedToGroupAbout(groupUI)
            findNavController().navigate(action)
        }
    }

    private fun handleMetaData(tags: Tags?) {
        interestsList.clear()
        val groupInterests = group.interests
        tags?.interests?.forEach { interest ->
            if (groupInterests.contains(interest.id)) interest.name?.let { it ->
                interestsList.add(
                    it
                )
            }
        }
        binding.interests.adapter?.notifyDataSetChanged()
    }
}