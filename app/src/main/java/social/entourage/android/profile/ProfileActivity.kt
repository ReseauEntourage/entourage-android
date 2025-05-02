package social.entourage.android.profile

import android.os.Bundle
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.base.BaseSecuredActivity
import social.entourage.android.databinding.ActivityProfileBinding
import social.entourage.android.tools.utils.Const
import social.entourage.android.user.AvatarUploadView
import social.entourage.android.user.edit.photo.ChooseProfilePhotoFragment

class ProfileActivity : BaseSecuredActivity(), AvatarUploadView {

    val profilePresenter: ProfilePresenter by lazy { ProfilePresenter() }
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                    ChooseProfilePhotoFragment.TAG
                ) as ChooseProfilePhotoFragment?
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

    override fun onStop() {
        super.onStop()
        MainActivity.instance?.recreate()
    }

}