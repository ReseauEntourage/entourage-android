package social.entourage.android.actions.detail

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.BuildConfig
import social.entourage.android.R
import social.entourage.android.actions.ActionsPresenter
import social.entourage.android.api.model.Action
import social.entourage.android.databinding.ActivityActionDetailBinding
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.tools.utils.Const

//Use to hide report button when loading detail action if canceled
interface OnDetailActionReceive {
    fun hideIconReport()
    fun updateTitle(title:String?)
}

class ActionDetailActivity : AppCompatActivity(), OnDetailActionReceive {

    private lateinit var binding: ActivityActionDetailBinding
    private lateinit var actionsPresenter: ActionsPresenter
    private var shareContent = ""
    private var shareTitle = ""

    private var isActionMine = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionsPresenter = ViewModelProvider(this).get(ActionsPresenter::class.java)

        binding = ActivityActionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra(Const.ACTION_ID, 0)
        val title = intent.getStringExtra(Const.ACTION_TITLE)
        val isDemand = intent.getBooleanExtra(Const.IS_ACTION_DEMAND,false)
        isActionMine = intent.getBooleanExtra(Const.IS_ACTION_MINE,false)

        val bundle = Bundle().apply {
            putInt(Const.ACTION_ID, id)
            putString(Const.ACTION_TITLE, title)
            putBoolean(Const.IS_ACTION_DEMAND,isDemand)
            putBoolean(Const.IS_ACTION_MINE,isActionMine)
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navGraph = navHostFragment.navController.navInflater.inflate(R.navigation.action_detail)
        navGraph.setStartDestination(R.id.action_detail)

        navHostFragment.navController.setGraph(navGraph,bundle)

        val actionTitle = if (isDemand) getString(R.string.action_name_Demand) else getString(R.string.action_name_Contrib)
        setSettingsIcon(actionTitle)

        binding.header.headerIconBack.setOnClickListener {
            finish()
        }

        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }
        )
        actionsPresenter.getAction.observe(this, ::handleResponseGetDetail)

        handleShareButton()

        updatePaddingTopForEdgeToEdge(binding.actionDetailHeaderLayout)

    }

    private fun handleResponseGetDetail(action: Action?) {
        if(action != null){
            shareContent = action.title + "\n\n" + createDeepURL(action)
        }
    }

    private fun createDeepURL(action:Action):String{
        val deepLinksHostName = BuildConfig.DEEP_LINKS_URL
        var actionPath: String
        if(action.isDemand()){
            actionPath = "solicitations/"
            shareTitle = getString(R.string.share_title_demande)
        }else{
            actionPath = "contributions/"
            shareTitle = getString(R.string.share_title_contrib)
        }
        return "https://" + deepLinksHostName + "/app/" + actionPath + action.uuid_v2
    }


    private fun setSettingsIcon(title:String?) {
        binding.header.headerIconSettings.isVisible = true
        binding.header.headerIconSettings.setImageResource(R.drawable.share_icon)
        val whiteColor = ContextCompat.getColor(this, R.color.white)
        binding.header.headerIconSettings.imageTintList = ColorStateList.valueOf(whiteColor)
        binding.header.headerTitle.maxLines = 2
        binding.header.headerTitle.ellipsize = TextUtils.TruncateAt.END
        binding.header.title = title
    }


    private fun handleShareButton(){

        binding.header.headerIconSettings.setOnClickListener {
            val shareUrl = shareContent
            if(shareUrl.contains("contributions")){
                AnalyticsEvents.logEvent(AnalyticsEvents.CONTRIB_SHARED)
            }else{
                AnalyticsEvents.logEvent(AnalyticsEvents.SOLICITATION_SHARED)
            }
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareTitle + "\n" + shareUrl)

            }
            //TODO translate this
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_url_by)))
        }
    }



    override fun hideIconReport() {
        binding.header.headerIconSettings.isVisible = false
    }

    override fun updateTitle(title: String?) {
        binding.header.title = title
    }
}