package social.entourage.android.groups.details.rules

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.api.model.Rules
import social.entourage.android.databinding.ActivityGroupRulesBinding
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const

class GroupRulesActivity : AppCompatActivity() {
    private var rulesList: MutableList<Rules> = ArrayList()
    lateinit var binding: ActivityGroupRulesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnalyticsEvents.logEvent(
            AnalyticsEvents.VIEW_GROUP_OPTION_RULES)
        binding = ActivityGroupRulesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ruleType = intent.getStringExtra(Const.RULES_TYPE)

        populateList(ruleType)
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
        binding.header.headerIconBack.setOnClickListener {
            finish()
        }
    }

    private fun populateList(ruleType:String?) {
        var cgus = ArrayList<Rules>()
        var rule_description = ""
        var rule_title = ""
        when(ruleType) {
            Const.RULES_GROUP -> {
                rule_description = getString(R.string.group_params_cgu_description)
                rule_title = getString(R.string.group_params_cgu_title)
                val line1 = Rules(getString(R.string.neighborhood_CGU_1_title),getString(R.string.neighborhood_CGU_1))
                val line2 = Rules(getString(R.string.neighborhood_CGU_2_title),getString(R.string.neighborhood_CGU_2))
                val line3 = Rules(getString(R.string.neighborhood_CGU_3_title),getString(R.string.neighborhood_CGU_3))
                val line4 = Rules(getString(R.string.neighborhood_CGU_4_title),getString(R.string.neighborhood_CGU_4))
                val line5 = Rules(getString(R.string.neighborhood_CGU_5_title),getString(R.string.neighborhood_CGU_5))
                cgus = arrayListOf(line1,line2,line3,line4,line5)
            }
            Const.RULES_EVENT -> {
                rule_description = getString(R.string.event_params_cgu_description)
                rule_title = getString(R.string.event_params_cgu_title)
                val line1 = Rules(getString(R.string.event_CGU_1_title),getString(R.string.event_CGU_1))
                val line2 = Rules(getString(R.string.event_CGU_2_title),getString(R.string.event_CGU_2))
                val line3 = Rules(getString(R.string.event_CGU_3_title),getString(R.string.event_CGU_3))
                val line4 = Rules(getString(R.string.event_CGU_4_title),getString(R.string.event_CGU_4))
                cgus = arrayListOf(line1,line2,line3,line4)
            }
            Const.RULES_ACTION ->  {
                rule_description = getString(R.string.action_params_cgu_description)
                rule_title = getString(R.string.action_params_cgu_title)
                val line1 = Rules(getString(R.string.action_CGU_1_title),getString(R.string.action_CGU_1))
                val line2 = Rules(getString(R.string.action_CGU_2_title),getString(R.string.action_CGU_2))
                val line3 = Rules(getString(R.string.action_CGU_3_title),getString(R.string.action_CGU_3))
                val line4 = Rules(getString(R.string.action_CGU_4_title),getString(R.string.action_CGU_4))
                val line5 = Rules(getString(R.string.action_CGU_5_title),getString(R.string.action_CGU_5))
                cgus = arrayListOf(line1,line2,line3,line4,line5)
            }
        }
        rulesList = cgus
        binding.recyclerView.adapter?.notifyDataSetChanged()
        binding.ruleDescription.isVisible = rule_description.isNotEmpty()
        binding.ruleDescription.text = rule_description
        binding.header.title = rule_title
    }

}