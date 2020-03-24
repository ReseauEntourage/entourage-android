package social.entourage.android.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_get_involved.*
import social.entourage.android.*
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.android.synthetic.main.fragment_get_involved.about_version_description
import kotlinx.android.synthetic.pfp.fragment_about.*

class VoisinageAboutFragment : AboutFragment() {
    // ----------------------------------
// LIFECYCLE
// ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun populate() {
        super.populate()
        about_version_description?.text = getString(R.string.about_version_format, BuildConfig.VERSION_FULL_NAME )
        about_version_layout?.setOnClickListener { onVersionClicked() }
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------
    private fun onVersionClicked() {
        if (activity == null) {
            return
        }
        val uri = Uri.parse(getString(R.string.market_url, requireActivity().packageName))
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.playstore_url, requireActivity().packageName))))
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        @JvmField
        val TAG = VoisinageAboutFragment::class.java.simpleName
    }
}