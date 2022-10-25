package social.entourage.android.new_v8.actions.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.R
import social.entourage.android.databinding.NewActivityActionDetailBinding
import social.entourage.android.new_v8.report.ReportModalFragment
import social.entourage.android.new_v8.report.ReportTypes
import social.entourage.android.new_v8.utils.Const

class ActionDetailActivity : AppCompatActivity() {

    private lateinit var binding: NewActivityActionDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.new_activity_action_detail
        )

        val id = intent.getIntExtra(Const.ACTION_ID, 0)
        val title = intent.getStringExtra(Const.ACTION_TITLE)
        val isDemand = intent.getBooleanExtra(Const.IS_ACTION_DEMAND,false)

        val bundle = Bundle().apply {
            putInt(Const.ACTION_ID, id)
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navGraph = navHostFragment.navController.navInflater.inflate(R.navigation.action_detail)
        navGraph.setStartDestination(R.id.action_detail)

        navHostFragment.navController.setGraph(navGraph,bundle)
        setSettingsIcon(title)
        handleBackButton()
        handleReportPost(id,isDemand)
    }


    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            finish()
        }
    }

    private fun setSettingsIcon(title:String?) {
        binding.header.iconSettings.isVisible = true
        binding.header.iconSettings.setImageResource(R.drawable.new_report_group)

        binding.header.title = title
    }

    private fun handleReport(id: Int, type: ReportTypes) {
        val reportGroupBottomDialogFragment =
            ReportModalFragment.newInstance(id, id, type)
        reportGroupBottomDialogFragment.show(
            supportFragmentManager,
            ReportModalFragment.TAG
        )
    }

    private fun handleReportPost(id: Int, isDemand:Boolean) {
        binding.header.iconSettings.setOnClickListener {
            val _type = if (isDemand) ReportTypes.REPORT_DEMAND else ReportTypes.REPORT_CONTRIB
            handleReport(id, _type)
        }
    }
}