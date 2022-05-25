package social.entourage.android.new_v8.groups.details.feed

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentAboutGroupBinding
import social.entourage.android.databinding.NewFragmentFeedBinding
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.groups.details.SettingsModalFragment
import social.entourage.android.new_v8.models.GroupUiModel
import social.entourage.android.new_v8.profile.myProfile.InterestsAdapter
import social.entourage.android.new_v8.utils.Utils

class AboutGroupFragment : Fragment() {

    private var _binding: NewFragmentAboutGroupBinding? = null
    val binding: NewFragmentAboutGroupBinding get() = _binding!!
    var group: GroupUiModel? = null
    private var interestsList: ArrayList<String> = ArrayList()
    private val args: AboutGroupFragmentArgs by navArgs()
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentAboutGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        group = args.group
        setView()
        handleSettingsButton()
        handleJoinButton()
        handleBackButton()
        groupPresenter.hasUserJoinedGroup.observe(requireActivity(), ::handleJoinResponse)
        groupPresenter.hasUserLeftGroup.observe(requireActivity(), ::handleJoinResponse)
    }

    private fun setView() {
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        binding.header.iconSettings.visibility = View.VISIBLE
        with(binding) {
            groupName.text = group?.name
            groupMembersNumberLocation.text = String.format(
                getString(R.string.members_location),
                group?.members_count,
                group?.address?.displayAddress
            )
            initializeMembersPhotos()
            groupDescription.text = group?.description
            initializeInterests()
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
            getString(if (group?.member == true) R.string.member else R.string.join)
        val textColor = ContextCompat.getColor(
            requireContext(),
            if (group?.member == true) R.color.orange else R.color.white
        )
        val background = ResourcesCompat.getDrawable(
            resources,
            if (group?.member == true) R.drawable.new_bg_rounded_button_orange_stroke else R.drawable.new_bg_rounded_button_orange_fill,
            null
        )
        val rightDrawable = ResourcesCompat.getDrawable(
            resources,
            if (group?.member == true) R.drawable.new_check else R.drawable.new_plus_white,
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


    private fun initializeMembersPhotos() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = group?.members?.let { GroupMembersPhotosAdapter(it) }
        }
    }

    private fun handleSettingsButton() {
        binding.header.iconSettings.setOnClickListener {
            group?.let { group ->
                SettingsModalFragment.newInstance(group)
                    .show(parentFragmentManager, SettingsModalFragment.TAG)
            }
        }
    }

    private fun initializeInterests() {
        if (interestsList.isEmpty()) binding.interests.visibility = View.GONE
        else {
            binding.interests.apply {
                val layoutManagerFlex = FlexboxLayoutManager(context)
                layoutManagerFlex.flexDirection = FlexDirection.ROW
                layoutManagerFlex.justifyContent = JustifyContent.CENTER
                layoutManager = layoutManagerFlex
                adapter = InterestsAdapter(interestsList)
            }
        }
    }

    private fun handleMetaData(tags: Tags?) {
        interestsList.clear()
        val groupInterests = group?.interests
        tags?.interests?.forEach { interest ->
            if (groupInterests?.contains(interest.id) == true) interest.name?.let { it ->
                interestsList.add(
                    it
                )
            }
        }
        binding.interests.adapter?.notifyDataSetChanged()
    }

    private fun handleJoinResponse(hasJoined: Boolean) {
        if (hasJoined) {
            group?.let {
                it.member = !it.member
            }
            updateButtonJoin()
        }
    }

    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleJoinButton() {
        binding.join.setOnClickListener {
            if (group?.member == true) {
                Utils.showAlertDialogButtonClicked(
                    requireView(),
                    getString(R.string.leave_group),
                    getString(R.string.leave_group_dialog_content),
                    getString(R.string.exit)
                ) {
                    group?.let {
                        it.id?.let { id -> groupPresenter.leaveGroup(id) }
                    }
                }
            } else {
                group?.let {
                    it.id?.let { id -> groupPresenter.joinGroup(id) }
                }
            }
        }
    }
}