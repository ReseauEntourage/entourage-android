package entourage.social.android.profile.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import entourage.social.android.databinding.FragmentHelpAboutBinding

class HelpAboutFragment : Fragment() {

    private var _binding: FragmentHelpAboutBinding? = null
    val binding: FragmentHelpAboutBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        setBackButton()
    }

    private fun initializeView() {
        binding.licence.divider.visibility = View.GONE
    }

    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener { findNavController().popBackStack() }
    }


}