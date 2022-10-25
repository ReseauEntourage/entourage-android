package social.entourage.android.new_v8.actions.list.me

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.R
import social.entourage.android.databinding.NewActivityMyActionsListBinding

class MyActionsListActivity : AppCompatActivity() {

    private lateinit var binding: NewActivityMyActionsListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.new_activity_my_actions_list
        )

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