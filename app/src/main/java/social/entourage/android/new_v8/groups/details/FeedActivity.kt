package social.entourage.android.new_v8.groups.details

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.R
import social.entourage.android.new_v8.utils.Const

class FeedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        val id = intent.getIntExtra(Const.GROUP_ID, -1)
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