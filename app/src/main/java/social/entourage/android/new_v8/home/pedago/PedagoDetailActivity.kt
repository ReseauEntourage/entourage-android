package social.entourage.android.new_v8.home.pedago

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.R
import social.entourage.android.new_v8.utils.Const

class PedagoDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedago_detail)

        val id = intent.getIntExtra(Const.ID, Const.DEFAULT_VALUE)
        val htmlContent = intent.getStringExtra(Const.HTML_CONTENT)
        val isFromNotif = intent.getBooleanExtra(Const.IS_FROM_NOTIF,false)

        val bundle = Bundle().apply {
            putInt(Const.ID, id)
            putString(Const.HTML_CONTENT,htmlContent)
            putBoolean(Const.IS_FROM_NOTIF,isFromNotif)
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        var navGraph = navController.navInflater.inflate(R.navigation.pedagogical_content)
        navGraph.setStartDestination(R.id.pedagogical_details_fragment)

        navController.setGraph(navGraph,bundle)
    }
}