package social.entourage.android.actions.create

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentCreateActionSuccessBinding
import social.entourage.android.RefreshController
import social.entourage.android.actions.detail.ActionDetailActivity
import social.entourage.android.tools.utils.Const

class CreateActionSuccessFragment : Fragment() {

    private var _binding: NewFragmentCreateActionSuccessBinding? = null
    val binding: NewFragmentCreateActionSuccessBinding get() = _binding!!

    private val args: CreateActionSuccessFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleFinishButton()

        binding.title.text = getString(R.string.action_create_success_title,
            if (args.successIsDemand) getString(R.string.action_name_demand)
            else getString(R.string.action_name_contrib))
        binding.subtitle.text = getString(R.string.action_create_success_subtitle,
            if (args.successIsDemand) getString(R.string.action_name_demand)
            else getString(R.string.action_name_contrib))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateActionSuccessBinding.inflate(inflater, container, false)

        return binding.root
    }

    private fun handleFinishButton() {
        binding.post.setOnClickListener {
            requireActivity().finish()
            RefreshController.shouldRefreshFragment = true

        }
    }
}