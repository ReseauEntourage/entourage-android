package social.entourage.android.new_v8.association

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentAssociationProfileBinding
import social.entourage.android.databinding.NewFragmentMyProfileBinding


class AssociationProfile : Fragment() {

    private var _binding: NewFragmentAssociationProfileBinding? = null
    val binding: NewFragmentAssociationProfileBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentAssociationProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBackButton()
    }

    private fun setBackButton() {
        binding.iconBack.setOnClickListener { findNavController().popBackStack() }
    }

}