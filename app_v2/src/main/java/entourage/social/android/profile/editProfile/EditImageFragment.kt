package entourage.social.android.profile.editProfile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import entourage.social.android.R
import entourage.social.android.databinding.FragmentEditImageBinding
import entourage.social.android.databinding.FragmentEditInterestsBinding

class EditImageFragment : Fragment() {


    private var _binding: FragmentEditImageBinding? = null
    val binding: FragmentEditImageBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireContext())
            .load(R.drawable.profile).circleCrop()
            .into(binding.imageProfile)
        setBackButton()

    }

    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener { findNavController().popBackStack() }
    }
}