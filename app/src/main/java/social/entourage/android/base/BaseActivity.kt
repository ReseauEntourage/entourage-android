package social.entourage.android.base

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageComponent
import social.entourage.android.R
import social.entourage.android.tools.view.WebViewFragment
import social.entourage.android.tools.view.WebViewFragment.Companion.newInstance

/**
 * Base activity which set up a scoped graph and inject it
 */
abstract class BaseActivity : AppCompatActivity() {
    private var progressDialog: ProgressDialog? = null
    val entApp: EntourageApplication?
        get()= (application as? EntourageApplication)

    override fun onCreate(savedInstanceState: Bundle?) {
        entApp?.onActivityCreated(this)
        super.onCreate(savedInstanceState)
        setupComponent(EntourageApplication.get(this).components)
    }

    override fun onDestroy() {
        entApp?.onActivityDestroyed(this)
        super.onDestroy()
    }

    fun showProgressDialog(resId: Int) {
        if (progressDialog?.isShowing == true) {
            progressDialog?.setTitle(resId)
        } else {
            progressDialog = ProgressDialog(this).apply {
                if (resId != 0) {
                    this.setTitle(resId)
                }
                this.setCancelable(false)
                this.setCanceledOnTouchOutside(false)
                this.isIndeterminate = true
                this.show()
            }
        }
    }

    fun dismissProgressDialog() {
        if (progressDialog?.isShowing == true) {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }

    protected open fun setupComponent(entourageComponent: EntourageComponent?) {}
    protected fun showKeyboard(view: View) {
        view.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    protected fun hideKeyboard() {
        val view = this.currentFocus ?: return
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager? ?: return
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showWebView(url: String?) {
        val webViewFragment = newInstance(url)
        webViewFragment.show(supportFragmentManager, WebViewFragment.TAG)
    }

    fun showWebViewForLinkId(linkId: String) {
        val link = getLink(linkId)
        showWebView(link)
    }

    open fun getLink(linkId: String): String {
        return getString(R.string.redirect_link_no_token_format, BuildConfig.ENTOURAGE_URL, linkId)
    }
}