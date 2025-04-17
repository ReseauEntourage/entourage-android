package social.entourage.android.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.request.UserResponse
import social.entourage.android.databinding.NewFragmentProfileBinding
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge

class ProfileFragment : Fragment() {

    private var _binding: NewFragmentProfileBinding? = null
    val binding: NewFragmentProfileBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentProfileBinding.inflate(inflater, container, false)

        updatePaddingTopForEdgeToEdge(binding.profileHeader)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeTab()
        initializeEditButton()
        getUser()
        handleBackButton()
        val activityName = activity?.javaClass?.name
    }

    private fun initializeTab() {
        val viewPager = binding.viewPager
        val adapter = ProfileViewPagerAdapter(childFragmentManager, lifecycle)
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
        if(isAdded && view != null){
            binding.editProfile.setOnClickListener {
                try {
                    it.isEnabled = false
                    AnalyticsEvents.logEvent(AnalyticsEvents.Profile_action_modify)
                    findNavController().navigate(R.id.action_profile_fragment_to_edit_profile_fragment)
                    it.isEnabled = true

                }catch(e:Exception){

                }
            }
        }
    }

    private fun handleBackButton() {
        binding.iconBack.setOnClickListener {
            requireActivity().finish() // termine l'activité actuelle
        }
    }

    fun getUser() {
        val user = EntourageApplication.me(activity) ?: return
        val userRequest = EntourageApplication.get().apiModule.userRequest
        userRequest.getUser(user.id.toString()).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    response.body()?.user?.let {
                        EntourageApplication.get().authenticationController.saveUser(
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
        if(isAdded){
            val user = EntourageApplication.me(activity) ?: return
            with(binding) {
                name.text = user.displayName
                imageProfile.let { photoView ->
                    user.avatarURL?.let { avatarURL ->
                        Glide.with(requireActivity())
                            .load(avatarURL)
                            .placeholder(R.drawable.placeholder_user)
                            .error(R.drawable.placeholder_user)
                            .circleCrop()
                            .into(photoView)
                    } ?: run {
                        photoView.setImageResource(R.drawable.placeholder_user)
                    }
                }
            }
        }
    }
}