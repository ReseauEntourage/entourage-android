package social.entourage.android.new_v8.groups.edit

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.GroupImage
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentEditGroupBinding
import social.entourage.android.new_v8.groups.choosePhoto.ChoosePhotoModalFragment
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.models.Group
import social.entourage.android.new_v8.models.Interest
import social.entourage.android.new_v8.profile.editProfile.InterestsListAdapter
import social.entourage.android.new_v8.profile.editProfile.OnItemCheckListener
import social.entourage.android.new_v8.user.ReportUserModalFragment
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.new_v8.utils.px
import social.entourage.android.new_v8.utils.trimEnd

class EditGroupFragment : Fragment() {

    private var _binding: NewFragmentEditGroupBinding? = null
    val binding: NewFragmentEditGroupBinding get() = _binding!!
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private lateinit var group: Group
    private var interestsList: MutableList<Interest> = mutableListOf()
    private var selectedInterestIdList: MutableList<String> = mutableListOf()
    private var selectedImage: GroupImage? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupPresenter.getGroup(81)
        groupPresenter.getGroup.observe(viewLifecycleOwner, ::handleResponseGetGroup)
        groupPresenter.isGroupUpdated.observe(viewLifecycleOwner, ::handleResponseUpdateGroup)
        initializeView()
        onFragmentResult()
    }

    private fun handleResponseUpdateGroup(isGroupUpdated: Boolean) {
        if (isGroupUpdated) {
            Utils.showToast(requireContext(), getString(R.string.group_updated))
            back()
        } else {
            Utils.showToast(requireContext(), getString(R.string.group_error_updated))
        }
    }

    private fun back() {
        requireActivity().finish()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentEditGroupBinding.inflate(inflater, container, false)
        return binding.root
    }


    private fun handleResponseGetGroup(getGroup: Group?) {
        getGroup?.let {
            group = it
            updateView()
        }
    }

    private fun updateView() {
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        initializeInterests()
        with(binding) {
            stepOne.groupName.setText(group.name)
            stepOne.groupDescription.setText(group.description)
            stepThree.groupMessageWelcome.setText(group.welcomeMessage)
            stepThree.addPhotoLayout.visibility = View.GONE
            stepThree.addPhoto.visibility = View.VISIBLE
            Glide.with(requireActivity())
                .load(Uri.parse(group.imageUrl))
                .transform(CenterCrop(), RoundedCorners(14.px))
                .into(stepThree.addPhoto)
        }
    }

    private fun initializeView() {
        with(binding) {
            stepOne.counter.text = String.format(
                getString(R.string.description_counter),
                stepOne.groupDescription.text?.length.toString()
            )
            stepOne.groupDescription.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    stepOne.counter.text = String.format(
                        getString(R.string.description_counter),
                        s.length.toString()
                    )
                }

                override fun afterTextChanged(s: Editable) {}
            })
            stepOne.groupName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    handleSaveButtonState()
                }

                override fun afterTextChanged(s: Editable) {}
            })
            stepThree.groupPhotoTitle.title.text = getString(R.string.edit_photo)
            stepThree.groupPhotoTitle.mandatory.visibility = View.GONE
            stepThree.groupPhotoLabel.visibility = View.GONE
            stepThree.addPhoto.setOnClickListener {
                ChoosePhotoModalFragment.newInstance()
                    .show(parentFragmentManager, ReportUserModalFragment.TAG)
            }
            header.iconBack.setOnClickListener {
                back()
            }
        }
        handleSaveButton()
    }


    private fun initializeInterests() {
        binding.stepTwo.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = InterestsListAdapter(interestsList, object : OnItemCheckListener {
                override fun onItemCheck(item: Interest) {
                    item.id?.let { selectedInterestIdList.add(it) }
                    handleSaveButtonState()
                }

                override fun onItemUncheck(item: Interest) {
                    selectedInterestIdList.remove(item.id)
                    handleSaveButtonState()
                }
            }, false)
        }
    }


    private fun handleMetaData(tags: Tags?) {
        interestsList.clear()
        val userInterests = group.interests
        tags?.interests?.forEach { interest ->
            interestsList.add(
                Interest(
                    interest.id,
                    interest.name,
                    userInterests.contains(interest.id)
                )
            )
            if (userInterests.contains(interest.id)) interest.id?.let {
                selectedInterestIdList.add(it)
            }
        }
        binding.stepTwo.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun onFragmentResult() {
        setFragmentResultListener(Const.REQUEST_KEY_CHOOSE_PHOTO) { _, bundle ->
            selectedImage = bundle.getParcelable(Const.CHOOSE_PHOTO)
            selectedImage?.imageUrl.let { imageUrl ->
                binding.stepThree.addPhoto.visibility = View.VISIBLE
                Glide.with(requireActivity())
                    .load(Uri.parse(imageUrl))
                    .transform(CenterCrop(), RoundedCorners(14.px))
                    .into(binding.stepThree.addPhoto)
            }
        }
    }

    private fun checkName(): Boolean {
        with(binding) {
            return if (isGroupNameValid()) {
                stepOne.error.root.visibility = View.GONE
                true
            } else {
                stepOne.error.root.visibility = View.VISIBLE
                stepOne.error.errorMessage.text = getString(R.string.error_mandatory_fields)
                false
            }
        }
    }

    private fun checkInterestsList(): Boolean {
        with(binding) {
            return if (isInterestsListValid()) {
                stepTwo.error.root.visibility = View.GONE
                true
            } else {
                stepTwo.error.root.visibility = View.VISIBLE
                stepTwo.error.errorMessage.text = getString(R.string.error_categories_create_group)
                false
            }
        }
    }

    private fun handleSaveButtonState() {
        val isActive = isInterestsListValid() && isGroupNameValid()
        val background = ContextCompat.getDrawable(
            requireContext(),
            if (isActive) R.drawable.new_rounded_button_orange else R.drawable.new_rounded_button_light_orange
        )
        binding.validate.button.background = background
    }

    private fun isGroupNameValid(): Boolean {
        return binding.stepOne.groupName.text.length >= Const.GROUP_NAME_MIN_LENGTH
    }

    private fun isInterestsListValid(): Boolean {
        return selectedInterestIdList.isNotEmpty()
    }

    private fun handleSaveButton() {
        binding.validate.button.setOnClickListener {
            if (checkName() && checkInterestsList()) {
                val editedGroup: MutableMap<String, Any> = mutableMapOf()
                val group: ArrayMap<String, Any> = ArrayMap()
                with(binding) {
                    editedGroup["name"] = stepOne.groupName.text.trimEnd()
                    editedGroup["description"] = stepOne.groupDescription.text.trimEnd()
                    editedGroup["welcome_message"] = stepThree.groupMessageWelcome.text.trimEnd()
                    selectedImage?.let { image ->
                        editedGroup["neighborhood_image_id"] = image.id as Int
                    }
                    editedGroup["interests"] = selectedInterestIdList
                }
                group["neighborhood"] = editedGroup
                groupPresenter.updateGroup(81, group)
            }
        }
    }
}