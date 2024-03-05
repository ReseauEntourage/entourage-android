package social.entourage.android.actions.list.me

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.R
import social.entourage.android.databinding.ActivityMyActionsListBinding

class MyActionsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyActionsListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyActionsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.setGraph(R.navigation.action_detail)

        setBackButton()
    }

    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener {
            finish()
        }
    }
}