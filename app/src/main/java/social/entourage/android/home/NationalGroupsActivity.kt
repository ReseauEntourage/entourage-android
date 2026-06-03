package social.entourage.android.home

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.api.model.Group
import social.entourage.android.databinding.ActivityNationalGroupsBinding
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.details.feed.GroupFeedActivity
import social.entourage.android.tools.utils.Const
import timber.log.Timber

class NationalGroupsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNationalGroupsBinding
    private lateinit var adapter: NationalGroupsAdapter
    private lateinit var groupPresenter: GroupPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNationalGroupsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        // Load data after observers are set up
        loadNationalGroups()
    }

    private fun setupToolbar() {
        Timber.wtf("wtf init")

        binding.btnBack.setOnClickListener {
            Timber.wtf("wtf clicked")
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = NationalGroupsAdapter(emptyList(), { group, position ->
            handleGroupJoinLeave(group, position)
        }, { group ->
            onGroupItemClick(group)
        })
        binding.groupsRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.groupsRecyclerview.adapter = adapter
    }

    private fun setupObservers() {
        groupPresenter = ViewModelProvider(this).get(GroupPresenter::class.java)

        groupPresenter.getAllGroups.observe(this) { groups ->
            groups?.let {
                adapter.updateGroups(it)
            }
        }

        groupPresenter.hasUserJoinedGroup.observe(this) { success ->
            if (success == true) {
                // Mettre à jour localement le statut du groupe au lieu de recharger
                setResult(RESULT_OK)
            }
        }

        groupPresenter.hasUserLeftGroup.observe(this) { success ->
            if (success == true) {
                // Mettre à jour localement le statut du groupe au lieu de recharger
            }
        }
    }

    private fun loadNationalGroups() {
        groupPresenter.getNationalGroups()
    }

    private fun handleGroupJoinLeave(group: Group, position: Int) {
        if (group.member) {
            // Leave group
            group.id?.let { groupId ->
                groupPresenter.leaveGroup(groupId)
                // Mettre à jour localement le statut immédiatement
                adapter.updateGroupJoinedStatus(groupId, false)
            }
        } else {
            // Join group
            group.id?.let { groupId ->
                groupPresenter.joinGroup(groupId)
                // Mettre à jour localement le statut immédiatement
                adapter.updateGroupJoinedStatus(groupId, true)
            }
        }
    }

    private fun onGroupItemClick(group: Group) {
        // Navigate to group feed activity
        group.id?.let { groupId ->
            val intent = Intent(this, GroupFeedActivity::class.java).apply {
                putExtra(Const.GROUP_ID, groupId)
            }
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}