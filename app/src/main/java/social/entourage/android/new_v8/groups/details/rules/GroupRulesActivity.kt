package social.entourage.android.new_v8.groups.details.rules

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.databinding.NewActivityGroupRulesBinding
import social.entourage.android.new_v8.models.Rules
import social.entourage.android.tools.log.AnalyticsEvents

class GroupRulesActivity : AppCompatActivity() {
    private var rulesList: MutableList<Rules> = ArrayList()
    lateinit var binding: NewActivityGroupRulesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnalyticsEvents.logEvent(
            AnalyticsEvents.VIEW_GROUP_OPTION_RULES)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.new_activity_group_rules
        )
        populateList()
        initializeGroups()
        handleBackButton()
    }


    private fun initializeGroups() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = RulesListAdapter(rulesList)
        }
    }

    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            finish()
        }
    }

    private fun populateList() {
        val value = Rules(
            "Titre de la règle",
            "Je n’insulterai pas , pas d’incitation à la haine ni de harcèlement.Je n’insulterai pas , pas d’incitation à la haine ni de harcèlement. "
        )
        rulesList =
            mutableListOf(
                value,
                value,
                value,
                value,
                value,
                value,
                value,
                value,
                value,
                value,
                value
            )
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

}