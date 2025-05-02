package social.entourage.android.groups.details.feed

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Const.DEFAULT_VALUE

class FeedActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        val id = intent.getIntExtra(Const.GROUP_ID, DEFAULT_VALUE)
        val bundle = Bundle().apply {
            putInt(Const.GROUP_ID, id)
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.setGraph(
            R.navigation.groups_feed,
            bundle
        )
    }
}