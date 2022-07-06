package social.entourage.android.new_v8.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.R
import social.entourage.android.new_v8.utils.Const

class UserProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_user_profile)


        val id = intent.getIntExtra(Const.USER_ID, Const.DEFAULT_VALUE)
        val bundle = Bundle().apply {
            putInt(Const.USER_ID, id)
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.setGraph(
            R.navigation.user,
            bundle
        )

    }
}