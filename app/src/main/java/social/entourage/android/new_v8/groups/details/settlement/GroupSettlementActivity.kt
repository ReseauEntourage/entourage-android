package social.entourage.android.new_v8.groups.details.settlement

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.new_activity_group_settlement.*
import social.entourage.android.R
import social.entourage.android.databinding.NewActivityGroupSettlementBinding
import social.entourage.android.new_v8.models.Settlement

class GroupSettlementActivity : AppCompatActivity() {
    private var settlementList: MutableList<Settlement> = ArrayList()
    lateinit var binding: NewActivityGroupSettlementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.new_activity_group_settlement
        )
        populateList()
        initializeGroups()
        handleBackButton()
    }


    private fun initializeGroups() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = SettlementListAdapter(settlementList)
        }
    }

    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            finish()
        }
    }

    private fun populateList() {
        val value = Settlement(
            "Titre de la règle",
            "Je n’insulterai pas , pas d’incitation à la haine ni de harcèlement.Je n’insulterai pas , pas d’incitation à la haine ni de harcèlement. "
        )
        settlementList =
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