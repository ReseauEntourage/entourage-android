package social.entourage.android.welcome

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.util.Log
import social.entourage.android.R
import social.entourage.android.api.model.Group
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityLayoutWelcomeTwoBinding
import social.entourage.android.groups.GroupPresenter

class WelcomeTwoActivity: BaseActivity() {

    private lateinit var binding: ActivityLayoutWelcomeTwoBinding
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutWelcomeTwoBinding.inflate(layoutInflater)
        groupPresenter.getGroup.observe(this, ::handleResponseGetGroup)
        setThirdTV()
        getGroup()
        setContentView(binding.root)
    }

    fun getGroup(){
        groupPresenter.getInitialGroup()
    }
    private fun handleResponseGetGroup(getGroup: Group?) {
        getGroup?.let {
            var titleString = it.name
            if(titleString!!.contains("Groupe") || titleString!!.contains("groupe")){
                binding.tvTextOne.text = String.format(getString(R.string.welcome_two_text_one_corrected), it.name)
            }else{
                binding.tvTextOne.text = String.format(getString(R.string.welcome_two_text_one), it.name)
            }
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


}