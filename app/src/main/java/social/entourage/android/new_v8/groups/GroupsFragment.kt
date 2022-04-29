package social.entourage.android.new_v8.groups

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentDonationsBinding
import social.entourage.android.databinding.NewFragmentGroupsBinding
import social.entourage.android.new_v8.profile.ProfileActivity


class GroupsFragment : Fragment() {
    private var _binding: NewFragmentGroupsBinding? = null
    val binding: NewFragmentGroupsBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createGroup()
    }


    private fun createGroup() {
        binding.createGroup.setOnClickListener {
            startActivity(
                Intent(context, CreateGroupActivity::class.java)
            )
        }

    }
}