package social.entourage.android.enhanced_onboarding.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.FragmentOnboardingPresentationFragmentBinding
import social.entourage.android.enhanced_onboarding.OnboardingViewModel
import social.entourage.android.tools.log.AnalyticsEvents

class OnboardingPresentationFragment: Fragment() {

    private lateinit var binding: FragmentOnboardingPresentationFragmentBinding
    private lateinit var viewModel: OnboardingViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentOnboardingPresentationFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(OnboardingViewModel::class.java)
        initView()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.toggleBtnBack(false)
    }

     fun initView() {
        val myName = EntourageApplication.get().me()?.firstName
        binding.tvTitle.text = String.format(getString(R.string.onboarding_presentation_title), myName)
        binding.buttonStart.setOnClickListener {
            viewModel.setOnboardingSecondStep(true)
        }
         binding.buttonConfigureLater.setOnClickListener {
             AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_welcome_config_later_clic)
             requireActivity().finish()
         }
         binding.buttonStart.setOnClickListener {
             AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_welcome_next_clic)
             viewModel.setOnboardingSecondStep(true)
         }
         binding.buttonConfigureLater.setOnClickListener {
             requireActivity().finish()
         }
    }

}