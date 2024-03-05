package social.entourage.android.guide

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import social.entourage.android.Constants
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.databinding.FragmentGuideHubBinding
import social.entourage.android.tools.log.AnalyticsEvents

class GuideHubFragment : Fragment() {

    private lateinit var binding : FragmentGuideHubBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentGuideHubBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = EntourageApplication.me(activity)

        user?.let { it ->
            if (it.isUserTypeAlone) {
                binding.uiLayoutCell3?.visibility = View.GONE
                binding.uiLayoutCell4?.visibility = View.GONE
            }
        }

        binding.uiLayoutCell1?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_SHOWGDS)
            val intent = Intent(activity,GDSMainActivity::class.java)

            startActivity(intent)
        }

        binding.uiLayoutCell2?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_WEBORIENTATION)
            (activity as MainActivity).showWebViewForLinkId(Constants.SLUG_HUB_LINK_1)
        }
        binding.uiLayoutCell3?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_WEBGUIDE)
            (activity as MainActivity).showWebViewForLinkId(Constants.SLUG_HUB_LINK_2)
        }
        binding.uiLayoutCell4?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_WEBATELIER)
            (activity as MainActivity).showWebViewForLinkId(Constants.SLUG_HUB_LINK_3)
        }
        binding.uiLayoutCell5?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_WEBFAQ)
            (activity as MainActivity).showWebViewForLinkId(Constants.SLUG_HUB_LINK_FAQ)
        }

        binding.uiImage3.shapeAppearanceModel = binding.uiImage3.shapeAppearanceModel.withCornerSize(32f).toBuilder().build()
    }

    companion object {
        const val TAG = "social.entourage.android.guideHub"
    }
}