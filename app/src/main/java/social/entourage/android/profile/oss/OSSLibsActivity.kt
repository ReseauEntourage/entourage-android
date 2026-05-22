package social.entourage.android.profile.oss

import android.os.Bundle
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.util.withContext
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityOssLibsBinding

class OSSLibsActivity : BaseActivity() {
    private lateinit var binding: ActivityOssLibsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOssLibsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.header.headerHelpIconCross.setOnClickListener {
            finish()
        }

        val libs = Libs.Builder().withContext(this).build()

        val fragment = LibsBuilder()
            .withLibs(libs)
            .withAboutAppName(getString(R.string.app_name))
            .withAboutVersionShown(true)
            .withAboutVersionShownName(false)
            .withAboutVersionShownCode(false)
            .withAboutIconShown(aboutShowIcon = true)
            .withAboutMinimalDesign(true)
            .withEdgeToEdge(true)
            .withActivityTitle(getString(R.string.about_oss_licenses))
            .withLicenseShown(false)
            .withVersionShown(false)
            .supportFragment()

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }
}
