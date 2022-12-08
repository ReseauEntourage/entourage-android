package social.entourage.android.new_v8.profile.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentUnblockUsersBinding
import social.entourage.android.new_v8.discussions.DiscussionsPresenter
import social.entourage.android.new_v8.models.UserBlocked
import social.entourage.android.new_v8.utils.Utils

class UnblockUsersFragment : BottomSheetDialogFragment() {

    private var _binding: NewFragmentUnblockUsersBinding? = null
    val binding: NewFragmentUnblockUsersBinding get() = _binding!!

    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }

    private var blockedUsers: MutableList<UserBlocked> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentUnblockUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBackButton()
        initializeRv()
        handleCloseButton()
        setValidateButton()

        discussionsPresenter.getBlockedUsers.observe(requireActivity(),::handleResponseBlocked)
        discussionsPresenter.hasUserUnblock.observe(requireActivity(),::handleResponseUnblock)

        discussionsPresenter.getBlockedUsers()
    }

    private fun handleResponseUnblock(isOk:Boolean) {
        showPopValidateUnBlockUser()
    }

    private fun showPopValidateUnBlockUser() {
        var username = ""
        var count = 0
        for (user in blockedUsers) {
            if (user.isChecked) {
                 user.blockedUser.displayName?.let { _name ->
                    val space = if (username.isEmpty()) "" else ", "
                    username = username + space + _name
                     count += 1
                }
            }
        }

        val title = String.format(getString(R.string.params_unblock_user_pop_validate_title,username))
        val desc = if (count > 1) getString(R.string.params_unblock_users_pop_validate_subtitle) else getString(R.string.params_unblock_user_pop_validate_subtitle)
        Utils.showAlertDialogButtonClicked(
            requireContext(),
            title,
            desc,
            getString(R.string.params_unblock_user_pop_validate_bt),
            {
                discussionsPresenter.getBlockedUsers()
            },null
        )
    }

    private fun handleResponseBlocked(blockedUsers:MutableList<UserBlocked>?) {
        this.blockedUsers.clear()
        blockedUsers?.let {
            this.blockedUsers.clear()
            this.blockedUsers.addAll(it)
        }
        binding.recyclerView.adapter?.notifyDataSetChanged()

        if (blockedUsers?.size == 0) {
            binding.title.isVisible = false
            binding.recyclerView.isVisible = false
            binding.uiLayoutEmpty.isVisible = true
            binding.validate.button.isEnabled = false
            binding.validate.button.alpha = 0.3f
        }
        else {
            binding.title.isVisible = true
            binding.recyclerView.isVisible = true
            binding.uiLayoutEmpty.isVisible = false
            binding.validate.button.isEnabled = true
            binding.validate.button.alpha = 1f
        }
    }

    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun initializeRv() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = UnblockUsersListAdapter(blockedUsers, object : OnItemCheckListener {
                override fun onItemCheck(position: Int) {
                    blockedUsers[position].isChecked = !blockedUsers[position].isChecked
                }
            })
        }
    }

    private fun setValidateButton() {
        binding.validate.button.setOnClickListener {
            showPopValidateUnblock()
        }
    }

    private fun showPopValidateUnblock() {
        val userIds = ArrayList<Int>()
        var username = ""

        for (user in blockedUsers) {
            if (user.isChecked) {
                userIds.add(user.blockedUser.id)
                user.blockedUser.displayName?.let { _name ->
                    val space = if (username.isEmpty()) "" else ", "
                    username = username + space + _name
                }
            }
        }

        if (userIds.size == 0) {
            Toast.makeText(requireContext(),R.string.settingsUnblockUser_error,Toast.LENGTH_SHORT).show()
            return
        }

        val desc = String.format(getString(R.string.params_unblock_user_pop_message,username))
        val title = if (userIds.size > 1) getString(R.string.params_unblock_users_pop_title) else getString(R.string.params_unblock_user_pop_title)
        Utils.showAlertDialogButtonClickedWithCrossClose(
            requireContext(),
            title,
            desc,
            getString(R.string.params_unblock_user_pop_bt_cancel),
            getString(R.string.params_unblock_user_pop_bt_unblock), showCross = false, onNo = {}, onYes = {
                discussionsPresenter.unblockUsers(userIds)
            }
        )
    }

    private fun handleCloseButton() {
        binding.header.iconBack.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        const val TAG = "UnblockUsersFragment"
        fun newInstance(): UnblockUsersFragment {
            return UnblockUsersFragment()
        }
    }
}