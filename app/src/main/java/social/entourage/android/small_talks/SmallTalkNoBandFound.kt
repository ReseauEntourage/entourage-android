package social.entourage.android.small_talks

import android.content.Intent
import android.os.Bundle
import android.view.View
import social.entourage.android.api.model.Events
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivitySmallTalkNoBandFoundBinding
import social.entourage.android.events.EventsFragment
import social.entourage.android.events.EventsPresenter
import social.entourage.android.events.details.feed.EventFeedActivity
import social.entourage.android.tools.utils.Const

class SmallTalkNoBandFound: BaseActivity() {

    private lateinit var binding:ActivitySmallTalkNoBandFoundBinding
    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmallTalkNoBandFoundBinding.inflate(layoutInflater)
        initView()
        eventPresenter.getEvent.observe(this, ::onEventChanged)
        setContentView(binding.root)
    }

    private fun onEventChanged(event: Events?) {
        if (event != null) {
            binding.cardEvent.visibility = View.VISIBLE
            binding.cardEvent.setOnClickListener {
                EventsFragment.isFromDetails = true
                startActivity(
                    Intent(this, EventFeedActivity::class.java).putExtra(
                        Const.EVENT_ID,
                        event.id
                    )
                )
            }
        } else {
            binding.cardEvent.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        eventPresenter.getEventSmallTalk()
    }

    private fun initView() {
        binding.buttonWait.setOnClickListener {
            finish()
        }
    }
}