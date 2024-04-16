package social.entourage.android.enhanced_onboarding.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import social.entourage.android.R
import social.entourage.android.databinding.FragmentOnboardingCongratsFragmentBinding
import social.entourage.android.databinding.FragmentOnboardingInterestsLayoutBinding
import social.entourage.android.enhanced_onboarding.OnboardingViewModel

class OnboardingCongratsFragment: Fragment() {

    private lateinit var binding: FragmentOnboardingCongratsFragmentBinding
    private lateinit var viewModel: OnboardingViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOnboardingCongratsFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(OnboardingViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lottieAnimation.setAnimation(R.raw.congrats_animation)
        binding.lottieAnimation.playAnimation()
        binding.buttonStart.setOnClickListener {
            viewModel.registerAndQuit()
            Log.wtf("wtf", "should quit")
        }
    }
}