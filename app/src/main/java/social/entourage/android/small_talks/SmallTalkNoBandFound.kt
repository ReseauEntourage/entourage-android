package social.entourage.android.small_talks

import android.os.Bundle
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivitySmallTalkNoBandFoundBinding

class SmallTalkNoBandFound: BaseActivity() {

    private lateinit var binding:ActivitySmallTalkNoBandFoundBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmallTalkNoBandFoundBinding.inflate(layoutInflater)
        initView()
        setContentView(binding.root)
    }

    private fun initView() {
        binding.buttonWait.setOnClickListener {
            finish()
        }
    }
}