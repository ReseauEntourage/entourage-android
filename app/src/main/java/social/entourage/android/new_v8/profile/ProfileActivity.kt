package social.entourage.android.new_v8.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.BuildConfig
import social.entourage.android.R
import social.entourage.android.base.BaseSecuredActivity
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.tools.view.WebViewFragment
import social.entourage.android.user.AvatarUploadView
import social.entourage.android.user.edit.photo.ChoosePhotoFragment


class ProfileActivity : BaseSecuredActivity(), AvatarUploadView {

    val profilePresenter: ProfilePresenter by lazy { ProfilePresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_profile)
        val goToEditProfile = intent.getBooleanExtra(Const.GO_TO_EDIT_PROFILE, false)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.profile)
        graph.setStartDestination(if (goToEditProfile) R.id.edit_profile_fragment else R.id.profile_fragment)
        val navController = navHostFragment.navController
        navController.setGraph(graph, intent.extras)
        profilePresenter.isPhotoSuccess.observe(this, ::handlePhotoResponse)
    }

    private fun handlePhotoResponse(isSuccess: Boolean) {
        if (isSuccess) {
            val photoChooseSourceFragment =
                supportFragmentManager.findFragmentByTag(
                    ChoosePhotoFragment.TAG
                ) as ChoosePhotoFragment?
            photoChooseSourceFragment?.dismiss()
        } else {
            showErrorToast()
        }
    }


    override fun onUploadError() {
        showErrorToast()
    }

    private fun showErrorToast() {
        Toast.makeText(this, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT)
            .show()
    }
}