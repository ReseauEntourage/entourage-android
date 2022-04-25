package social.entourage.android.new_v8.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import social.entourage.android.BuildConfig
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.tools.view.WebViewFragment
import social.entourage.android.user.AvatarUploadView
import social.entourage.android.user.edit.photo.ChoosePhotoFragment


class ProfileActivity : AppCompatActivity(), AvatarUploadView {

    val profilePresenter: ProfilePresenter by lazy { ProfilePresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_profile)
        initializeMetaData()
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

    fun showWebViewForLinkId(linkId: String, shareMessageRes: Int = 0) {
        val link = getLink(linkId)
        showWebView(link, shareMessageRes)
    }

    private fun showWebView(url: String, shareMessageRes: Int = 0) {
        if (shareMessageRes != 0 || !WebViewFragment.launchURL(this, url, shareMessageRes)) {
            WebViewFragment.newInstance(url, shareMessageRes)
                .show(supportFragmentManager, WebViewFragment.TAG)
        }
    }

    fun getLink(linkId: String): String {
        return getString(R.string.redirect_link_no_token_format, BuildConfig.ENTOURAGE_URL, linkId)
    }

    private fun initializeMetaData() {
        if (MetaDataRepository.metaData.value == null) MetaDataRepository.getMetaData()
        if (MetaDataRepository.groupImages.value == null) MetaDataRepository.getGroupImages()
    }

    override fun onUploadError() {
        showErrorToast()
    }

    private fun showErrorToast() {
        Toast.makeText(this, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT)
            .show()
    }
}