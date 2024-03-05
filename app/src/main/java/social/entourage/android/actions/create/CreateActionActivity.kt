package social.entourage.android.actions.create

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.R
import social.entourage.android.api.model.Action
import social.entourage.android.tools.utils.Const

class CreateActionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_action)

        val isDemand = intent.getBooleanExtra(Const.IS_ACTION_DEMAND,false)
        val action = intent.getSerializableExtra(Const.ACTION_OBJ) as? Action
        val bundle = Bundle().apply {
            putBoolean(Const.IS_ACTION_DEMAND, isDemand)
            putSerializable(Const.ACTION_OBJ, action)
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.create_group_nav_host_fragment) as NavHostFragment
        navHostFragment.navController.setGraph(
            R.navigation.create_action,
            bundle
        )
    }
}