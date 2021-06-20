package social.entourage.android.newsfeed.v2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home_help_report.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment

/**
 * Created on 3/26/21.
 */
class HomeHelpReporterFragment: BaseDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_help_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title_close_button?.setOnClickListener {
            dismiss()
        }

        ui_home_report_bt?.setOnClickListener {
            val url = "https://entourage-asso.typeform.com/to/xVBzfaVd"
            val activity = requireActivity() as? MainActivity ?: return@setOnClickListener
            activity.showWebView(url)
        }
    }

    companion object {
        val TAG: String? = HomeHelpReporterFragment::class.java.simpleName
    }
}