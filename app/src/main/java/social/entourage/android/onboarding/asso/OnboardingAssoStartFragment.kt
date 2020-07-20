package social.entourage.android.onboarding.asso

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_onboarding_asso_info.*
import kotlinx.android.synthetic.main.fragment_onboarding_asso_start.*
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.R
import social.entourage.android.tools.Utils

private const val ARG_PARAM1 = "param1"

class OnboardingAssoStartFragment : Fragment() {
    private var isStart = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isStart = it.getBoolean(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (isStart) {
            return inflater.inflate(R.layout.fragment_onboarding_asso_start, container, false)
        }
        return inflater.inflate(R.layout.fragment_onboarding_asso_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isStart) {
            val color = ContextCompat.getColor(requireContext(),R.color.pre_onboard_orange)
            val _txt = getString(R.string.onboard_asso_start_description)
            val _txtBold = getString(R.string.onboard_asso_start_description_bold)
            ui_onboard_asso_start_tv_description?.text = Utils.formatTextWithBoldSpanAndColor(color,true,_txt,_txtBold)
            EntourageEvents.logEvent(EntourageEvents.EVENT_VIEW_ONBOARDING_PRO_STORIES)
        }
        else {
            val color = ContextCompat.getColor(requireContext(),R.color.onboard_black_36)
            val _txt = getString(R.string.onboard_asso_info_1)
            val _txtBold = getString(R.string.onboard_asso_info_1_bold)
            ui_onboard_asso_info_tv_1?.text = Utils.formatTextWithBoldSpanAndColor(color,true,_txt,_txtBold)
            val _txt2 = getString(R.string.onboard_asso_info_2)
            val _txtBold2 = getString(R.string.onboard_asso_info_2_bold)
            ui_onboard_asso_info_tv_2?.text = Utils.formatTextWithBoldSpanAndColor(color,true,_txt2,_txtBold2)
            val _txt3 = getString(R.string.onboard_asso_info_3)
            val _txtBold3 = getString(R.string.onboard_asso_info_3_bold)
            ui_onboard_asso_info_tv_3?.text = Utils.formatTextWithBoldSpanAndColor(color,true,_txt3,_txtBold3)
            EntourageEvents.logEvent(EntourageEvents.EVENT_VIEW_ONBOARDING_PRO_FEATURES)
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(isStart:Boolean) =
                OnboardingAssoStartFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean(ARG_PARAM1, isStart)
                    }
                }
    }
}