package social.entourage.android.new_v8

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import social.entourage.android.R

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment_new_activity_main
        ) as NavHostFragment
        navController = navHostFragment.navController

        // Setup the bottom navigation view with navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView.itemIconTintList = null
        bottomNavigationView.setupWithNavController(navController)
    }
}