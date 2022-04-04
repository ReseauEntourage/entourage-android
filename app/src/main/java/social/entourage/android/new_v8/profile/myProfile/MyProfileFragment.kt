package social.entourage.android.new_v8.profile.myProfile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import social.entourage.android.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.new_fragment_my_profile.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags
import social.entourage.android.api.model.User
import social.entourage.android.databinding.NewFragmentMyProfileBinding


class MyProfileFragment : Fragment() {
    private var _binding: NewFragmentMyProfileBinding? = null
    val binding: NewFragmentMyProfileBinding get() = _binding!!
    private lateinit var user: User

    private var interestsList: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentMyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = EntourageApplication.me(activity) ?: return
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        updateUserView()
        initializeView()
        initializeInterests()
        binding.seekBarLayout.seekbar.setOnTouchListener { _, _ -> true }
    }


    private fun handleMetaData(tags: Tags?) {
        interestsList.clear()
        val userInterests = user.interests
        tags?.interests?.forEach { interest ->
            if (userInterests.contains(interest.id)) interest.name?.let { it -> interestsList.add(it) }
        }
        binding.interests.adapter?.notifyDataSetChanged()
    }


    private fun initializeInterests() {
        binding.interests.apply {
            val layoutManagerFlex = FlexboxLayoutManager(context)
            layoutManagerFlex.flexDirection = FlexDirection.ROW
            layoutManagerFlex.justifyContent = JustifyContent.CENTER
            layoutManager = layoutManagerFlex
            adapter = InterestsAdapter(interestsList)
        }
    }

    private fun initializeView() {
        binding.ambassador.setOnClickListener {
            findNavController().navigate(R.id.action_profile_fragment_to_user_fragment)
        }
    }


    private fun updateUserView() {
        with(binding) {
            name.text = user.displayName
            description.text = user.about
            phone.content.text = user.phone
            birthday.content.text = user.birthday
            email.content.text = user.email
            city.content.text = user.address?.displayAddress
            seekBarLayout.seekbar.progress = user.travelDistance ?: 0
            seekBarLayout.tvTrickleIndicator.text = user.travelDistance.toString()
            description.text = user.about
            user.stats?.let {
                contribution.content.text = it.contribCreationCount.toString()
                events.content.text = it.eventsCount.toString()
            }
            user.roles?.let {
                if (it.contains("ambassador")) ambassador.visibility = View.VISIBLE
                else ambassador.visibility = View.GONE
            }
            user.partner?.let {
                association.association_name.text = it.name
                it.smallLogoUrl.let { logo ->
                    Glide.with(requireActivity())
                        .load(Uri.parse(logo))
                        .circleCrop()
                        .into(associationAvatar)
                }
            } ?: run {
                association.visibility = View.GONE
            }
        }
    }
}