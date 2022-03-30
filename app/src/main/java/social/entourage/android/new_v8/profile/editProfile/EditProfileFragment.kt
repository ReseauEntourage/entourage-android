package social.entourage.android.new_v8.profile.editProfile

import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentEditProfileBinding
import social.entourage.android.new_v8.profile.ProfileActivity
import social.entourage.android.user.*
import social.entourage.android.user.edit.photo.ChoosePhotoFragment
import social.entourage.android.user.edit.photo.PhotoChooseInterface
import java.io.File


class EditProfileFragment : Fragment(), EditProfileCallback {

    private var _binding: NewFragmentEditProfileBinding? = null
    val binding: NewFragmentEditProfileBinding get() = _binding!!
    private var mListener: PhotoChooseInterface? = null
    private val paddingRight = 20
    private val paddingRightLimit = 60
    private val progressLimit = 96

    private lateinit var avatarUploadPresenter: AvatarUploadPresenter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUserView()
        initializeSeekBar()
        onEditInterests()
        onEditImage()
        onEditActionZone()
        initializeDescriptionCounter()
        setBackButton()
        avatarUploadPresenter = AvatarUploadPresenter(
            (activity as AvatarUploadView),
            PrepareAvatarUploadRepository(
                EntourageApplication.get().components.userRequest
            ), AvatarUploadRepository(EntourageApplication.get().components.okHttpClient),
            (activity as ProfileActivity).profilePresenter as AvatarUpdatePresenter
        )

        if (context is PhotoChooseInterface) {
            mListener = requireContext() as PhotoChooseInterface
        }
    }

    private fun setProgressThumb(progress: Int) {
        binding.seekBarLayout.tvTrickleIndicator.text =
            String.format(
                getString(R.string.progress_km),
                progress.toString()
            )
        val bounds: Rect = binding.seekBarLayout.seekbar.thumb.dirtyBounds
        val paddingRight = if (progress > progressLimit) paddingRightLimit else paddingRight
        binding.seekBarLayout.tvTrickleIndicator.x =
            (binding.seekBarLayout.seekbar.left + bounds.left - paddingRight).toFloat()
    }

    private fun initializeSeekBar() {
        binding.seekBarLayout.seekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val progressValue = if (progress == 0) 1 else progress
                setProgressThumb(progressValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initializeDescriptionCounter() {
        binding.description.counter.text = String.format(
            getString(R.string.description_counter),
            binding.description.content.text?.length.toString()
        )
        binding.description.content.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.description.counter.text = String.format(
                    getString(R.string.description_counter),
                    s.length.toString()
                )
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun onEditInterests() {
        binding.interests.layout.setOnClickListener {
            findNavController().navigate(R.id.action_edit_profile_fragment_to_edit_profile_interest_fragment)
        }
    }

    private fun onEditImage() {
        binding.editImage.setOnClickListener {
            val photoFragment = ChoosePhotoFragment.newInstance()
            photoFragment.editProfileCallback = this
            photoFragment.show(parentFragmentManager, ChoosePhotoFragment.TAG)
        }
    }

    private fun onEditActionZone() {
        binding.cityAction.layout.setOnClickListener {
            findNavController().navigate(R.id.action_edit_profile_fragment_to_edit_action_zone_fragment)
        }
    }

    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun updateUserView() {
        val user = EntourageApplication.me(activity) ?: return
        binding.firstname.content.setText(user.firstName)
        binding.lastname.content.setText(user.lastName)
        binding.description.content.setText(user.about)
        binding.birthday.content.setText(user.birthday)
        binding.phone.content.setText(user.phone)
        binding.phone.content.setText(user.phone)
        binding.email.content.setText(user.email)
        binding.cityAction.content.text = user.address?.displayAddress
        binding.seekBarLayout.seekbar.progress = user.travelDistance ?: 0
        binding.seekBarLayout.tvTrickleIndicator.text = user.travelDistance.toString()
        user.avatarURL?.let { avatarURL ->
            Glide.with(this)
                .load(Uri.parse(avatarURL))
                .placeholder(R.drawable.ic_user_photo_small)
                .circleCrop()
                .into(binding.imageProfile)
        } ?: run {
            binding.imageProfile.setImageResource(R.drawable.ic_user_photo_small)
        }
    }

    override fun updateUserPhoto(imageUri: Uri?) {
        imageUri?.path?.let { path ->
            Glide.with(this)
                .load(path)
                .placeholder(R.drawable.ic_user_photo_small)
                .circleCrop()
                .into(binding.imageProfile)
            //Upload the photo to Amazon S3
            avatarUploadPresenter.uploadPhoto(File(path))
        }

    }
}

interface EditProfileCallback {
    fun updateUserPhoto(imageUri: Uri?)
}