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
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.new_fragment_my_profile.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags
import social.entourage.android.api.model.User
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentMyProfileBinding
import social.entourage.android.new_v8.profile.ProfileFragmentDirections


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
        initializeAssociationButton()
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
        binding.city.divider.visibility = View.INVISIBLE
    }

    private fun updateUserView() {
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
                    phone.content.text = it
                }
            }
            user.birthday?.let {
                if (it.isNotEmpty()) {
                    birthday.root.visibility = View.VISIBLE
                    birthday.content.text = it
                }
            }
            user.email?.let {
                if (it.isNotEmpty()) {
                    email.root.visibility = View.VISIBLE
                    email.content.text = it
                }
            }
            user.address?.displayAddress?.let {
                if (it.isNotEmpty()) {
                    city.root.visibility = View.VISIBLE
                    city.content.text = it
                }
            }
            user.travelDistance?.let {
                city.within.text = String.format(getString(R.string.progress_km), it)
            }
            user.stats?.let {
                contribution.content.text = it.contribCreationCount.toString()
                events.content.text = it.eventsCount.toString()
            }
            user.roles?.let {
                if (it.contains("ambassador")) ambassador.visibility = View.VISIBLE
            }
            user.partner?.let {
                association.visibility = View.VISIBLE
                association.association_name.text = it.name
                it.smallLogoUrl.let { logo ->
                    Glide.with(requireActivity())
                        .load(Uri.parse(logo))
                        .circleCrop()
                        .into(associationAvatar)
                }
            }
        }
    }

    private fun initializeAssociationButton() {
        binding.association.setOnClickListener {
            user.partner?.id?.toInt()
                ?.let {
                    val direction =
                        ProfileFragmentDirections.actionProfileFragmentToAssociationFragment(it)
                    findNavController().navigate(direction)
                }
        }

    }
}