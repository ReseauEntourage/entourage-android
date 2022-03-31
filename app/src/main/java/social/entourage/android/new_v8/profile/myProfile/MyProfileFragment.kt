package social.entourage.android.new_v8.profile.myProfile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
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
        binding.city.divider.visibility = View.INVISIBLE
    }

    private fun updateUserView() {
        binding.name.text = user.displayName
        binding.description.text = user.about
        binding.phone.content.text = user.phone
        binding.birthday.content.text = user.birthday
        binding.email.content.text = user.email
        binding.city.content.text = user.address?.displayAddress
        binding.seekBarLayout.seekbar.progress = user.travelDistance ?: 0
        binding.seekBarLayout.tvTrickleIndicator.text = user.travelDistance.toString()
        binding.description.text = user.about
        //interestsList = user.myInterest

        user.stats?.let {
            binding.contribution.content.text = it.contribCreationCount.toString()
            binding.events.content.text = it.eventsCount.toString()
        }
        user.roles?.let {
            if (it.contains("ambassador")) binding.ambassador.visibility = View.VISIBLE
            else binding.ambassador.visibility = View.GONE
        }
        user.organization?.let {
            binding.association.association_name.text = it.name
            it.largeLogoUrl.let { logo ->
                Glide.with(this)
                    .load(Uri.parse(logo))
                    .circleCrop()
                    .into(binding.associationAvatar)
            }
        } ?: run {
            binding.association.visibility = View.GONE
        }
    }
}