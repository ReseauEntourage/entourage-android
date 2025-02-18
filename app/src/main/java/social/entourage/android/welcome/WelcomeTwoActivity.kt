package social.entourage.android.welcome

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.Group
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityLayoutWelcomeTwoBinding
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.details.feed.GroupFeedActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const

class WelcomeTwoActivity: BaseActivity() {

    private lateinit var binding: ActivityLayoutWelcomeTwoBinding
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    var isGroupExisting = false
    var group:Group? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.updateLanguage()
        super.onCreate(savedInstanceState)
        AnalyticsEvents.logEvent(AnalyticsEvents.View_WelcomeOfferHelp_Day2)
        binding = ActivityLayoutWelcomeTwoBinding.inflate(layoutInflater)
        groupPresenter.getGroup.observe(this, ::handleResponseGetGroup)
        setThirdTV()
        getGroup()
        handleButton()
        handleCloseButton()
        setContentView(binding.root)
    }
    fun getGroup(){
        groupPresenter.getDefaultGroup()
    }
    private fun handleResponseGetGroup(getGroup: Group?) {
        getGroup?.let {
            isGroupExisting = true
            var titleString = it.name
            this.group = it
            if(titleString!!.contains("Groupe") || titleString!!.contains("groupe")){
                binding.tvTextOne.text = String.format(getString(R.string.welcome_two_text_one_corrected), it.name)
            }else{
                binding.tvTextOne.text = String.format(getString(R.string.welcome_two_text_one), it.name)
            }
        }
        if(!isGroupExisting){
            binding.tvTextOne.text = getString(R.string.welcome_two_text_one_no_group)
        }
    }

    private fun handleButton(){
        binding.buttonSayHello.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_WelcomeOfferHelp_Day2)
            if(isGroupExisting){
                val intent = Intent(this, GroupFeedActivity::class.java)
                intent.putExtra(Const.GROUP_ID,
                    this.group?.id,)
                intent.putExtra("fromWelcomeActivity", true)
                startActivity(intent)
                this.finish()
            }else{
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("fromWelcomeActivity", true)
                intent.putExtra("goMyGroup", true)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
                this.finish()
            }
        }
    }

    private fun handleCloseButton(){
        binding.btnClose.setOnClickListener {
            super.finish()
        }
    }


    fun setThirdTV(){

        val bulletPointList = listOf(
            getString(R.string.welcome_two_text_pin_one),
            getString(R.string.welcome_two_text_pin_two),
            getString(R.string.welcome_two_text_pin_three),
        )

        val bulletSpannableString = bulletPointList.joinTo(SpannableStringBuilder(), "\n") { line ->
            SpannableStringBuilder(line).apply {
                setSpan(BulletSpan(20), 0, line.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }

        binding.tvTextThree.text = bulletSpannableString
    }

    @Deprecated("Deprecated in kt 1.9.0")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        this.startActivity(intent)
    }
}

