package social.entourage.android.new_v8.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags
import social.entourage.android.api.request.MetaDataResponse
import social.entourage.android.api.request.UserResponse
import social.entourage.android.databinding.NewFragmentProfileBinding
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingStartActivity
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber


class ProfileFragment : Fragment() {

    private var _binding: NewFragmentProfileBinding? = null
    val binding: NewFragmentProfileBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeTab()
        initializeEditButton()
        Glide.with(requireContext())
            .load(R.drawable.new_profile).circleCrop()
            .into(binding.imageProfile)
        getUser()

        binding.imageProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profile_fragment_to_create_group_fragment)
        }
    }

    private fun initializeTab() {
        val viewPager = binding.viewPager
        val adapter = ViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter
        val tabLayout = binding.tabLayout
        val tabs = arrayOf(
            requireContext().getString(R.string.my_profile),
            requireContext().getString(R.string.settings)
        )
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()
    }

    private fun initializeEditButton() {
        binding.editProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profile_fragment_to_edit_profile_fragment)
        }
    }

    fun getUser() {
        val user = EntourageApplication.me(activity) ?: return
        val userRequest = EntourageApplication.get().components.userRequest
        userRequest.getUser(user.id).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    response.body()?.user?.let {
                        EntourageApplication.get().components.authenticationController.saveUser(
                            it
                        )
                    }
                }
                updateUserView()
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                updateUserView()
            }
        })
    }

    private fun updateUserView() {
        val user = EntourageApplication.me(activity) ?: return
        binding.imageProfile.let { photoView ->
            user.avatarURL?.let { avatarURL ->
                Glide.with(this)
                    .load(Uri.parse(avatarURL))
                    .placeholder(R.drawable.ic_user_photo_small)
                    .circleCrop()
                    .into(photoView)
            } ?: run {
                photoView.setImageResource(R.drawable.ic_user_photo_small)
            }
        }
    }
}