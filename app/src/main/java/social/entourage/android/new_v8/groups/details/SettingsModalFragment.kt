package social.entourage.android.new_v8.groups.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentSettingsModalBinding
import social.entourage.android.new_v8.models.Group
import social.entourage.android.new_v8.profile.myProfile.InterestsAdapter
import social.entourage.android.new_v8.user.ReportUserModalFragment
import social.entourage.android.new_v8.utils.Const
import timber.log.Timber

class SettingsModalFragment : BottomSheetDialogFragment() {

    private var _binding: NewFragmentSettingsModalBinding? = null
    val binding: NewFragmentSettingsModalBinding get() = _binding!!
    private var group: GroupUiModel? = null
    private var interestsList: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentSettingsModalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getGroupInformation()
        handleCloseButton()
        updateView()
    }


    private fun updateView() {
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        binding.settlement.divider.visibility = View.GONE
        group?.let {
            binding.groupName.text = it.name
            binding.groupMembersNumberLocation.text = String.format(
                getString(R.string.members_location),
                it.members_count,
                it.address?.displayAddress
            )
            initializeInterests()
        }
    }

    private fun handleCloseButton() {
        binding.header.iconBack.setOnClickListener {
            dismiss()
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

    private fun handleMetaData(tags: Tags?) {
        interestsList.clear()
        group?.let {
            val groupInterests = it.interests
            tags?.interests?.forEach { interest ->
                if (groupInterests.contains(interest.id)) interest.name?.let { it ->
                    interestsList.add(
                        it
                    )
                }
            }
        }
        binding.interests.adapter?.notifyDataSetChanged()
    }

    private fun getGroupInformation() {
        group = arguments?.getParcelable(Const.GROUP_UI)

    }

    companion object {
        const val TAG = "SettingsModalFragment"
        fun newInstance(group: GroupUiModel): SettingsModalFragment {
            val fragment = SettingsModalFragment()
            val args = Bundle()
            args.putParcelable(Const.GROUP_UI, group)
            fragment.arguments = args
            return fragment
        }
    }
}