package social.entourage.android.guide

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_guide_hub.*
import social.entourage.android.Constants
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.tools.log.AnalyticsEvents


class GuideHubFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_guide_hub, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = EntourageApplication.me(activity)

        user?.let { it ->
            if (it.isUserTypeAlone) {
                ui_layout_cell_3?.visibility = View.GONE
                ui_layout_cell_4?.visibility = View.GONE
            }
        }

        ui_layout_cell_1?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_SHOWGDS)
            val intent = Intent(activity,GDSMainActivity::class.java)

            startActivity(intent)
        }

        ui_layout_cell_2?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_WEBORIENTATION)
            (activity as MainActivity).showWebViewForLinkId(Constants.SLUG_HUB_LINK_1)
        }
        ui_layout_cell_3?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_WEBGUIDE)
            (activity as MainActivity).showWebViewForLinkId(Constants.SLUG_HUB_LINK_2)
        }
        ui_layout_cell_4?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_WEBATELIER)
            (activity as MainActivity).showWebViewForLinkId(Constants.SLUG_HUB_LINK_3)
        }
        ui_layout_cell_5?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_WEBFAQ)
            (activity as MainActivity).showWebViewForLinkId(Constants.SLUG_HUB_LINK_FAQ)
        }

        ui_image_3.shapeAppearanceModel = ui_image_3.shapeAppearanceModel.withCornerSize(32f).toBuilder().build()
    }

    companion object {
        const val TAG = "social.entourage.android.guideHub"
    }
}