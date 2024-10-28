package social.entourage.android.home.pedago

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.R
import social.entourage.android.api.model.Pedago
import social.entourage.android.home.HomePresenter
import social.entourage.android.tools.utils.Const

class PedagoDetailActivity : AppCompatActivity() {

    var id:Int = 0
    var htmlContent = ""
    var isFromNotif = false
    private val pedagoPresenter: PedagoPresenter by lazy { PedagoPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedago_detail)

        id = intent.getIntExtra(Const.ID, Const.DEFAULT_VALUE)
        val htmlContent = Companion.getHtmlContent()
        //htmlContent = intent.getStringExtra(Const.HTML_CONTENT).toString()
        isFromNotif = intent.getBooleanExtra(Const.IS_FROM_NOTIF,false)
        pedagoPresenter.pedagolSingle.observe(this, ::handlePedago)
        pedagoPresenter.hasNoReturn.observe(this, ::handlePedago)
    }

    override fun onResume() {
        super.onResume()
        if(hashId != ""){
            pedagoPresenter.getPedagogicalResource(hashId)
        }else{
            pedagoPresenter.getPedagogicalResource(id.toString())
        }
    }

    fun showFragment(){
        val bundle = Bundle().apply {
            PedagoDetailActivity.setHtmlContent(htmlContent)
            putInt(Const.ID, id)
            //putString(Const.HTML_CONTENT,htmlContent)
            putBoolean(Const.IS_FROM_NOTIF,isFromNotif)
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        var navGraph = navController.navInflater.inflate(R.navigation.pedagogical_content)
        navGraph.setStartDestination(R.id.pedagogical_details_fragment)


        navController.setGraph(navGraph,bundle)
    }

    fun handlePedago(pedago: Pedago){
        if(pedago != null){
            id = pedago.id!!
            htmlContent = pedago.html!!
            isFromNotif = true
        }
        showFragment()
    }

    fun handlePedago(hasNoReturn: Boolean){
        if(hasNoReturn){
            Toast.makeText(this,"Aucune ressource trouvée",Toast.LENGTH_SHORT).show()
            this.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hashId = ""
    }

    companion object {
        var hashId = ""
        private var htmlContent: String = ""
        fun setHtmlContent(content: String) {
            htmlContent = content
        }
        fun getHtmlContent(): String {
            return htmlContent
        }

    }
}