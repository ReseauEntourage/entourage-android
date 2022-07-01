package social.entourage.android.new_v8

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentTestBinding
import social.entourage.android.new_v8.profile.ProfileFragmentDirections


class TestFragment : Fragment() {

    private var _binding: NewFragmentTestBinding? = null
    val binding: NewFragmentTestBinding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeAssociationButton()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun directions(id: Int) {
        val direction =
            TestFragmentDirections.actionTestFragmentToUserFragment(id)
        findNavController().navigate(direction)
    }

    private fun initializeAssociationButton() {
        binding.user.setOnClickListener {
            directions(3193)
        }

        binding.userAllInformation.setOnClickListener {
            directions(3556)
        }

        binding.userEmpty.setOnClickListener {
            directions(3673)
        }

        binding.userNoPartner.setOnClickListener {
            directions(3802)
        }

        binding.createGroup.setOnClickListener {
            findNavController().navigate(R.id.action_test_fragment_to_create_group_fragment)
        }

    }
}