package social.entourage.android.groups.edit

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Group
import social.entourage.android.api.model.Image
import social.entourage.android.api.model.Interest
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentEditGroupBinding
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.choosePhoto.ChooseGalleryPhotoModalFragment
import social.entourage.android.groups.choosePhoto.ImagesType
import social.entourage.android.groups.details.feed.FeedFragmentArgs
import social.entourage.android.profile.editProfile.InterestsListAdapter
import social.entourage.android.profile.editProfile.OnItemCheckListener
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.px
import social.entourage.android.tools.utils.trimEnd

class EditGroupFragment : Fragment() {

    private var _binding: NewFragmentEditGroupBinding? = null
    val binding: NewFragmentEditGroupBinding get() = _binding!!
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private lateinit var group: Group
    private var interestsList: MutableList<Interest> = mutableListOf()
    private var selectedInterestIdList: MutableList<String> = mutableListOf()
    private var selectedImage: Image? = null
    private val args: FeedFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupPresenter.getGroup(args.groupID)
        groupPresenter.getGroup.observe(viewLifecycleOwner, ::handleResponseGetGroup)
        groupPresenter.isGroupUpdated.observe(viewLifecycleOwner, ::handleResponseUpdateGroup)
        initializeView()
        onFragmentResult()
    }

    private fun handleResponseUpdateGroup(isGroupUpdated: Boolean) {
        if (isGroupUpdated) {
            Utils.showToast(requireContext(), getString(R.string.group_updated))
            back()
            RefreshController.shouldRefreshFragment = true
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
        AnalyticsEvents.logEvent(
            AnalyticsEvents.VIEW_GROUP_OPTION_EDITION
        )

        ViewCompat.setOnApplyWindowInsetsListener(binding.header.layout) { view, windowInsets ->
            // Get the insets for the statusBars() type:
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(
                top = insets.top
            )
            // Return the original insets so they aren’t consumed
            windowInsets
        }
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
            group.imageUrl?.let {
                Glide.with(requireActivity())
                    .load(Uri.parse(it))
                    .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                    .placeholder(R.drawable.placeholder_user)
                    .into(stepThree.addPhoto)
            }
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
            handleEditTextChangedTextListener(stepOne.groupName)
            handleEditTextChangedTextListener(stepOne.groupDescription)
            stepThree.groupPhotoTitle.title.text = getString(R.string.edit_photo)
            stepThree.groupPhotoTitle.mandatory.visibility = View.GONE
            stepThree.groupPhotoLabel.visibility = View.GONE
            stepThree.addPhoto.setOnClickListener {
                ChooseGalleryPhotoModalFragment.newInstance(ImagesType.GROUPS)
                    .show(parentFragmentManager, ChooseGalleryPhotoModalFragment.TAG)
            }
            header.iconBack.setOnClickListener {
                back()
            }
        }
        handleSaveButton()
    }

    private fun handleEditTextChangedTextListener(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                handleSaveButtonState()
            }

            override fun afterTextChanged(s: Editable) {}
        })
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
            selectedImage = bundle.getParcelable(Const.CHOOSE_PHOTO_PATH)
            selectedImage?.imageUrl.let { imageUrl ->
                binding.stepThree.addPhoto.visibility = View.VISIBLE
                Glide.with(requireActivity())
                    .load(Uri.parse(imageUrl))
                    .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                    .into(binding.stepThree.addPhoto)
            }
        }
    }

    private fun checkName(): Boolean {
        with(binding) {
            return if (isGroupNameValid() && isGroupDescriptionValid()) {
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
        val isActive = isInterestsListValid() && isGroupNameValid() && isGroupDescriptionValid()
        val background = ContextCompat.getDrawable(
            requireContext(),
            if (isActive) R.drawable.new_rounded_button_orange else R.drawable.new_bg_rounded_inactive_button_light_orange
        )
        binding.validate.button.background = background
    }

    private fun isGroupNameValid(): Boolean {
        return binding.stepOne.groupName.text.length >= Const.GROUP_NAME_MIN_LENGTH && binding.stepOne.groupName.text.isNotBlank()
    }

    private fun isGroupDescriptionValid(): Boolean {
        return binding.stepOne.groupDescription.text.length >= Const.GROUP_DESCRIPTION_MIN_LENGTH && binding.stepOne.groupDescription.text.isNotBlank()
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
                groupPresenter.updateGroup(args.groupID, group)
            }
        }
    }
}