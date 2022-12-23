package social.entourage.android.new_v8.association

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.R
import social.entourage.android.new_v8.utils.Const

class PartnerDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partner_detail)

        val id = intent.getIntExtra(Const.PARTNER_ID, Const.DEFAULT_VALUE)
        val isFromNotif = intent.getBooleanExtra(Const.IS_FROM_NOTIF,false)
        val bundle = Bundle().apply {
            putInt(Const.PARTNER_ID, id)
            putBoolean(Const.IS_FROM_NOTIF,isFromNotif)
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        var navGraph = navController.navInflater.inflate(R.navigation.profile)
        navGraph.setStartDestination(R.id.association_fragment)

        navController.setGraph(navGraph,bundle)
    }
}