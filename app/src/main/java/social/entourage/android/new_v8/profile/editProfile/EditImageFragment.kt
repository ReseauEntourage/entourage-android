package social.entourage.android.new_v8.profile.editProfile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentEditImageBinding

class EditImageFragment : Fragment() {


    private var _binding: NewFragmentEditImageBinding? = null
    val binding: NewFragmentEditImageBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = NewFragmentEditImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireContext())
            .load(R.drawable.new_profile).circleCrop()
            .into(binding.imageProfile)
        setBackButton()

    }

    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener { findNavController().popBackStack() }
    }
}