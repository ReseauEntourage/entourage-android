package entourage.social.android.profile.editProfile

import android.graphics.Rect
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
import entourage.social.android.R
import entourage.social.android.databinding.NewFragmentEditProfileBinding


class EditProfileFragment : Fragment() {

    private var _binding: NewFragmentEditProfileBinding? = null
    val binding: NewFragmentEditProfileBinding get() = _binding!!
    private val paddingRight = 20
    private val paddingRightLimit = 60
    private val progressLimit = 96


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeSeekBar()
        onEditInterests()
        onEditImage()
        onEditActionZone()
        initializeDescriptionCounter()
        Glide.with(requireContext())
            .load(R.drawable.new_profile).circleCrop()
            .into(binding.imageProfile)
        setBackButton()
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
            findNavController().navigate(R.id.action_edit_profile_fragment_to_edit_profile_image_fragment)
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
}