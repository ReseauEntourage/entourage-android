package social.entourage.android.new_v8.groups

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import social.entourage.android.databinding.NewFragmentGroupsBinding
import social.entourage.android.new_v8.groups.edit.EditGroupActivity


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
        editGroup()
    }


    private fun createGroup() {
        binding.createGroup.setOnClickListener {
            startActivity(
                Intent(context, CreateGroupActivity::class.java)
            )
        }
    }


    private fun editGroup() {
        binding.editGroup.setOnClickListener {
            startActivity(
                Intent(context, EditGroupActivity::class.java)
            )
        }

    }
}