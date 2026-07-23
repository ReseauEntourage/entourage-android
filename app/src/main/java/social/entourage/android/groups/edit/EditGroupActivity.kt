package social.entourage.android.groups.edit

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.R
import social.entourage.android.databinding.ActivityEditGroupBinding
import social.entourage.android.tools.updatePaddingBottomForEdgeToEdge
import social.entourage.android.tools.utils.Const

class EditGroupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditGroupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updatePaddingBottomForEdgeToEdge(binding.root)
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