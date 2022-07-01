package social.entourage.android.new_v8.profile.editProfile

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
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
        setBackButton()
        updateUserView()
    }

    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun updateUserView() {
        val user = EntourageApplication.me(activity) ?: return
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
}