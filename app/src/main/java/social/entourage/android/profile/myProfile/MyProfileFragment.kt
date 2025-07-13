package social.entourage.android.profile.myProfile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags
import social.entourage.android.api.model.User
import social.entourage.android.databinding.FragmentMyProfileBinding
import social.entourage.android.language.LanguageManager
import social.entourage.android.profile.ProfileFragmentDirections
import social.entourage.android.tools.log.AnalyticsEvents
import java.text.SimpleDateFormat

class MyProfileFragment : Fragment() {
    private lateinit var _binding: FragmentMyProfileBinding
    val binding: FragmentMyProfileBinding get() = _binding
    private lateinit var user: User

    private var interestsList: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = EntourageApplication.me(activity) ?: return
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        updateUserView()
        initializeView()
        initializeInterests()
        initializeAssociationButton()
        AnalyticsEvents.logEvent(AnalyticsEvents.Profile_view_profile)
    }

    private fun handleMetaData(tags: Tags?) {
        interestsList.clear()
        val userInterests = user.interests
        tags?.interests?.forEach { interest ->
            if (userInterests.contains(interest.id)) interest.name?.let { interestsList.add(it) }
        }
        binding.interests.adapter?.notifyDataSetChanged()
    }

    private fun initializeInterests() {
        if (interestsList.isEmpty()) binding.interests.visibility = View.GONE
        else {
            binding.interests.visibility = View.VISIBLE
            binding.interests.apply {
                val layoutManagerFlex = FlexboxLayoutManager(context)
                layoutManagerFlex.flexDirection = FlexDirection.ROW
                layoutManagerFlex.justifyContent = JustifyContent.CENTER
                layoutManager = layoutManagerFlex
                adapter = InterestsAdapter(interestsList)
            }
        }
    }

    private fun initializeView() {
        binding.city.profileCityDivider.visibility = View.INVISIBLE
    }

    private fun updateUserView() {
        if(isAdded){
            with(binding) {
                user.about?.let {
                    if (it.isNotEmpty()) {
                        description.visibility = View.VISIBLE
                        description.text = it
                    }
                }
                user.phone?.let {
                    if (it.isNotEmpty()) {
                        phone.root.visibility = View.VISIBLE
                        phone.profileItemContent.text = it
                    }
                }
                user.birthday?.let {
                    if (it.isNotEmpty()) {
                        birthday.root.visibility = View.VISIBLE
                        birthday.profileItemContent.text = it
                    }else {
                        birthday.root.visibility = View.VISIBLE
                        birthday.profileItemContent.hint = getString(R.string.placeholder_birthday_my_profile)
                    }
                }
                if (user.birthday == null){
                    birthday.root.visibility = View.VISIBLE
                    birthday.profileItemContent.hint = getString(R.string.placeholder_birthday_my_profile)
                }
                user.email?.let {
                    if (it.isNotEmpty()) {
                        email.root.visibility = View.VISIBLE
                        email.profileItemContent.text = it
                    }else {
                        email.root.visibility = View.VISIBLE
                        email.profileItemContent.hint = getString(R.string.placeholder_email_my_profile)
                    }
                }
                if (user.birthday == null){
                    email.root.visibility = View.VISIBLE
                    email.profileItemContent.hint = getString(R.string.placeholder_email_my_profile)
                }
                user.address?.displayAddress?.let {
                    if (it.isNotEmpty()) {
                        city.root.visibility = View.VISIBLE
                        city.profileCityContent.text = it
                    }
                }
                user.travelDistance?.let {
                    city.within.text = getString(R.string.progress_km, it)
                }
                user.stats?.let {
                    contribContent.text = it.neighborhoodsCount.toString()
                    eventContent.text = it.outingsCount.toString()


                }
                user.roles?.let {
                    if (it.contains("ambassador")) ambassador.visibility = View.VISIBLE
                }
                user.createdAt?.let {createdAt ->
                    val locale = LanguageManager.getLocaleFromPreferences(requireContext())
                    joined.profileJoinedDate.text = SimpleDateFormat(
                        requireContext().getString(R.string.profile_date_format),
                        locale
                    ).format(
                        createdAt
                    )
                }
                user.partner?.let {
                    association.visibility = View.VISIBLE
                    associationName.text = it.name
                    it.smallLogoUrl.let { logo ->
                        Glide.with(requireActivity())
                            .load(Uri.parse(logo))
                            .circleCrop()
                            .into(associationAvatar)
                    }
                }
            }
        }
    }

    private fun initializeAssociationButton() {
        binding.association.setOnClickListener {
            user.partner?.id?.toInt()
                ?.let {
                    val direction =
                        ProfileFragmentDirections.actionProfileFragmentToAssociationFragment(it,false)
                    findNavController().navigate(direction)
                }
        }

    }
}