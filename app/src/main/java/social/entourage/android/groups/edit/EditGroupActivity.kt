package social.entourage.android.groups.edit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.R
import social.entourage.android.tools.utils.Const

class EditGroupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_group)
        val id = intent.getIntExtra(Const.GROUP_ID, Const.DEFAULT_VALUE)
        val bundle = Bundle().apply {
            putInt(Const.GROUP_ID, id)
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.edit_group_nav_host_fragment) as NavHostFragment
        navHostFragment.navController.setGraph(
            R.navigation.edit_group,
            bundle
        )
    }
}