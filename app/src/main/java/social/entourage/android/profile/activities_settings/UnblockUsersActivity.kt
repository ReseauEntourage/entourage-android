package social.entourage.android.profile.activities_settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.api.model.UserBlockedUser
import social.entourage.android.databinding.ActivityUnblockUsersBinding
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.profile.settings.OnItemCheckListener
import social.entourage.android.profile.settings.ProfilFullViewModel
import social.entourage.android.profile.settings.UnblockUsersListAdapter
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.tools.utils.CustomAlertDialog

class UnblockUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnblockUsersBinding
    private lateinit var profilFullViewModel: ProfilFullViewModel

    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }
    private var blockedUsers: MutableList<UserBlockedUser> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnblockUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updatePaddingTopForEdgeToEdge(binding.header.hbsLayout)
        binding.header.hbsView.isVisible = false
        profilFullViewModel = ViewModelProvider(this).get(ProfilFullViewModel::class.java)
        initializeRecyclerView()
        handleCloseButton()
        setValidateButton()

        // Observers
        discussionsPresenter.getBlockedUsers.observe(this, ::handleResponseBlocked)
        discussionsPresenter.hasUserUnblock.observe(this, ::handleResponseUnblock)

        // Charge la liste des utilisateurs bloqués
        discussionsPresenter.getBlockedUsers()
    }

    /**
     * Callback déclenché après déblocage réussi (ou non).
     */
    private fun handleResponseUnblock(isOk: Boolean) {
        showPopValidateUnBlockUser()
    }

    /**
     * Affiche un popup de confirmation une fois le(s) utilisateur(s) débloqués.
     */
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

        val title = String.format(
            getString(R.string.params_unblock_user_pop_validate_title),
            username
        )

        val desc = if (count > 1) {
            getString(R.string.params_unblock_users_pop_validate_subtitle)
        } else {
            getString(R.string.params_unblock_user_pop_validate_subtitle)
        }

        CustomAlertDialog.showOnlyOneButton(
            this,
            title,
            desc,
            getString(R.string.params_unblock_user_pop_validate_bt)
        ) {
            // On recharge la liste des utilisateurs bloqués
            discussionsPresenter.getBlockedUsers()
        }
    }

    /**
     * Callback de réception de la liste des utilisateurs bloqués.
     */
    private fun handleResponseBlocked(blockedUsers: MutableList<UserBlockedUser>?) {
        this.blockedUsers.clear()
        blockedUsers?.let { this.blockedUsers.addAll(it) }
        binding.recyclerView.adapter?.notifyDataSetChanged()

        // On gère l'affichage en fonction de la liste
        if (blockedUsers?.isEmpty() == true) {
            binding.title.isVisible = false
            binding.recyclerView.isVisible = false
            binding.uiLayoutEmpty.isVisible = true
            binding.validate.button.isEnabled = false
            binding.validate.button.alpha = 0.3f
        } else {
            binding.title.isVisible = true
            binding.recyclerView.isVisible = true
            binding.uiLayoutEmpty.isVisible = false
            binding.validate.button.isEnabled = true
            binding.validate.button.alpha = 1f
        }
    }

    /**
     * Initialise le RecyclerView avec l'adaptateur et son layoutManager.
     */
    private fun initializeRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@UnblockUsersActivity)
            adapter = UnblockUsersListAdapter(blockedUsers, object : OnItemCheckListener {
                override fun onItemCheck(position: Int) {
                    blockedUsers[position].isChecked = !blockedUsers[position].isChecked
                }
            })
        }
    }

    /**
     * Gère le clic sur le bouton de validation (déblocage).
     */
    private fun setValidateButton() {
        binding.validate.button.setOnClickListener {
            showPopValidateUnblock()
        }
    }

    /**
     * Affiche le popup de confirmation avant de débloquer les utilisateurs sélectionnés.
     */
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

        // Si aucun utilisateur n'est sélectionné, on affiche un Toast d'erreur
        if (userIds.isEmpty()) {
            Toast.makeText(
                this,
                R.string.settingsUnblockUser_error,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val desc = String.format(
            getString(R.string.params_unblock_user_pop_message),
            username
        )
        val title = if (userIds.size > 1) {
            getString(R.string.params_unblock_users_pop_title)
        } else {
            getString(R.string.params_unblock_user_pop_title)
        }

        CustomAlertDialog.showButtonClickedWithCrossClose(
            this,
            title,
            desc,
            getString(R.string.params_unblock_user_pop_bt_cancel),
            getString(R.string.params_unblock_user_pop_bt_unblock),
            showCross = false,
            onNo = {},
            onYes = {
                discussionsPresenter.unblockUsers(userIds)
            }
        )
    }

    /**
     * Gère le bouton de fermeture (icône croix).
     * Dans une activity, on peut simplement faire un finish().
     */
    private fun handleCloseButton() {
        binding.header.hbsIconCross.setOnClickListener {
            profilFullViewModel.updateProfile()
            finish()
        }
    }
}
