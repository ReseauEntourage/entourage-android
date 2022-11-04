package social.entourage.android.new_v8.actions.create

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.R
import social.entourage.android.new_v8.utils.Const

class CreateActionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_create_action)

        val isDemand = intent.getBooleanExtra(Const.IS_ACTION_DEMAND,false)

        val bundle = Bundle().apply {
            putBoolean(Const.IS_ACTION_DEMAND, isDemand)
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.create_group_nav_host_fragment) as NavHostFragment
        navHostFragment.navController.setGraph(
            R.navigation.create_action,
            bundle
        )
    }
}