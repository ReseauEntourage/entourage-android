package social.entourage.android.home.pedago

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.R
import social.entourage.android.tools.utils.Const

class PedagoDetailActivity : AppCompatActivity() {

    var id:Int = 0
    var htmlContent = ""
    var isFromNotif = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedago_detail)

        id = intent.getIntExtra(Const.ID, Const.DEFAULT_VALUE)
        htmlContent = intent.getStringExtra(Const.HTML_CONTENT).toString()
        isFromNotif = intent.getBooleanExtra(Const.IS_FROM_NOTIF,false)

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