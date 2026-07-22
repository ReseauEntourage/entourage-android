package social.entourage.android.profile.oss

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.mikepenz.aboutlibraries.LibsBuilder
import social.entourage.android.R
import social.entourage.android.databinding.ActivityOssLibsBinding
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge

class OSSLibsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOssLibsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOssLibsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updatePaddingTopForEdgeToEdge(binding.root)

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