package social.entourage.android.actions.detail

import android.os.Bundle
import android.text.TextUtils
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.R
import social.entourage.android.databinding.NewActivityActionDetailBinding
import social.entourage.android.report.ReportModalFragment
import social.entourage.android.report.ReportTypes
import social.entourage.android.tools.utils.Const

//Use to hide report button when loading detail action if canceled
interface OnDetailActionReceive {
    fun hideIconReport()
    fun updateTitle(title:String?)
}

class ActionDetailActivity : AppCompatActivity(), OnDetailActionReceive {

    private lateinit var binding: NewActivityActionDetailBinding

    private var isActionMine = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.new_activity_action_detail
        )

        val id = intent.getIntExtra(Const.ACTION_ID, 0)
        val title = intent.getStringExtra(Const.ACTION_TITLE)
        val isDemand = intent.getBooleanExtra(Const.IS_ACTION_DEMAND,false)
        isActionMine = intent.getBooleanExtra(Const.IS_ACTION_MINE,false)

        val bundle = Bundle().apply {
            putInt(Const.ACTION_ID, id)
            putBoolean(Const.IS_ACTION_DEMAND,isDemand)
            putBoolean(Const.IS_ACTION_MINE,isActionMine)
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navGraph = navHostFragment.navController.navInflater.inflate(R.navigation.action_detail)
        navGraph.setStartDestination(R.id.action_detail)

        navHostFragment.navController.setGraph(navGraph,bundle)

        val _title = if (isDemand) getString(R.string.action_name_Demand) else getString(R.string.action_name_Contrib)
        setSettingsIcon(_title)

        binding.header.iconBack.setOnClickListener {
            finish()
        }

        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }
        )

        handleReportPost(id,isDemand)
    }

    private fun setSettingsIcon(title:String?) {
        binding.header.iconSettings.isVisible = !isActionMine
        binding.header.iconSettings.setImageResource(R.drawable.new_report_group)

        binding.header.headerTitle.maxLines = 2
        binding.header.headerTitle.ellipsize = TextUtils.TruncateAt.END
        binding.header.title = title
    }

    private fun handleReport(id: Int, type: ReportTypes) {
        val reportGroupBottomDialogFragment =
            ReportModalFragment.newInstance(id, id, type,isActionMine,false, false)
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

    override fun hideIconReport() {
        binding.header.iconSettings.isVisible = false
    }

    override fun updateTitle(title: String?) {
        binding.header.title = title
    }
}