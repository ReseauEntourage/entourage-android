package social.entourage.android.guide

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_guide_hub.*
import social.entourage.android.Constants
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.tools.log.EntourageEvents


class GuideHubFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_guide_hub, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui_layout_cell_1?.setOnClickListener {
            EntourageEvents.logEvent(EntourageEvents.ACTION_GUIDE_SHOWGDS)
            val intent = Intent(activity,GDSMainActivity::class.java)

            startActivity(intent)
        }

        ui_layout_cell_2?.setOnClickListener {
            EntourageEvents.logEvent(EntourageEvents.ACTION_GUIDE_WEBORIENTATION)
            (activity as MainActivity).showWebViewForLinkId(Constants.SLUG_HUB_LINK_1)
        }
        ui_layout_cell_3?.setOnClickListener {
            EntourageEvents.logEvent(EntourageEvents.ACTION_GUIDE_WEBGUIDE)
            (activity as MainActivity).showWebViewForLinkId(Constants.SLUG_HUB_LINK_2)
        }
        ui_layout_cell_4?.setOnClickListener {
            EntourageEvents.logEvent(EntourageEvents.ACTION_GUIDE_WEBATELIER)
            (activity as MainActivity).showWebViewForLinkId(Constants.SLUG_HUB_LINK_3)
        }
        ui_layout_cell_5?.setOnClickListener {
            EntourageEvents.logEvent(EntourageEvents.ACTION_GUIDE_WEBFAQ)
            (activity as MainActivity).showWebViewForLinkId(Constants.SLUG_HUB_LINK_FAQ)
        }
    }

    companion object {
        const val TAG = "social.entourage.android.guideHub"
    }
}