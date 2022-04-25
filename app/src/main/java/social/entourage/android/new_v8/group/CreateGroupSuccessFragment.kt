package social.entourage.android.new_v8.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import social.entourage.android.databinding.NewFragmentCreateGroupSuccessBinding


class CreateGroupSuccessFragment : Fragment() {

    private var _binding: NewFragmentCreateGroupSuccessBinding? = null
    val binding: NewFragmentCreateGroupSuccessBinding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handlePassButton()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = NewFragmentCreateGroupSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun handlePassButton() {
        binding.previous.setOnClickListener {
            activity?.finish()
        }
    }
}