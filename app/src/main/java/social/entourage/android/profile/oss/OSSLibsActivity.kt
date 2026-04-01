@file:Suppress("DEPRECATION", "UNCHECKED_CAST", "OVERRIDE_DEPRECATION", "DEPRECATION_ERROR")
package social.entourage.android.profile.oss

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mikepenz.aboutlibraries.LibsBuilder
import social.entourage.android.R

class OSSLibsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oss_libs)

        val fragment = LibsBuilder()
            .withAboutAppName(getString(R.string.app_name))
            .withAboutVersionShown(true)
            .withAboutVersionShownName(false)
            .withAboutVersionShownCode(false)
            .withAboutIconShown(aboutShowIcon = true)
            .withAboutMinimalDesign(true)
//            .withAboutDescription(getString(R.string.about_oss_licenses))

            .withEdgeToEdge(true)
            .withActivityTitle(getString(R.string.about_oss_licenses))

            .withLicenseShown(false)
            .withVersionShown(false)
            .supportFragment()

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }
}