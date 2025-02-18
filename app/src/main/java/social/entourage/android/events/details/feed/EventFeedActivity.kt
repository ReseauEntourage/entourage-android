package social.entourage.android.events.details.feed

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.tools.utils.Const

class EventFeedActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_event)
        val id = intent.getIntExtra(Const.EVENT_ID, Const.DEFAULT_VALUE)
        val bundle = Bundle().apply {
            putInt(Const.EVENT_ID, id)
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.setGraph(
            R.navigation.events_feed,
            bundle
        )
    }
}