package social.entourage.android.new_v8.user

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.new_fragment_my_profile.view.*
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.User
import social.entourage.android.databinding.NewFragmentUserProfileBinding
import social.entourage.android.new_v8.profile.myProfile.InterestsAdapter

class UserProfileFragment : Fragment() {


    private var _binding: NewFragmentUserProfileBinding? = null
    val binding: NewFragmentUserProfileBinding get() = _binding!!
    private val userPresenter: UserPresenter by lazy { UserPresenter() }
    private lateinit var user: User
    private var interestsList: ArrayList<String> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO add user id to the call
        userPresenter.getUser(2889)
        initializeInterests()
        setBackButton()
        userPresenter.isGetUserSuccess.observe(requireActivity(), ::handleResponse)
        onReportUser()
    }


    private fun handleResponse(success: Boolean) {
        if (success) updateView()
        else Toast.makeText(requireActivity(), R.string.user_retrieval_error, Toast.LENGTH_SHORT)
            .show()

    }

    private fun onReportUser() {
        val reportUserBottomDialogFragment = ReportUserModalFragment.newInstance()
        binding.report.setOnClickListener {
            reportUserBottomDialogFragment.show(parentFragmentManager, ReportUserModalFragment.TAG)
        }
    }

    private fun initializeInterests() {
        with(binding.interests) {
            val layoutManagerFlex = FlexboxLayoutManager(context)
            layoutManagerFlex.flexDirection = FlexDirection.ROW
            layoutManagerFlex.justifyContent = JustifyContent.CENTER
            layoutManager = layoutManagerFlex
            adapter = InterestsAdapter(interestsList)
        }
    }

    private fun handleMetaData() {
        interestsList.clear()
        val userInterests = user.interests
        MetaDataRepository.metaData.value?.interests?.forEach { interest ->
            if (userInterests.contains(interest.id)) interest.name?.let { it -> interestsList.add(it) }
        }
        binding.interests.adapter?.notifyDataSetChanged()
    }


    private fun updateView() {
        userPresenter.user.value?.let { user = it }
        handleMetaData()
        with(binding) {
            name.text = user.displayName
            description.text = user.about
            user.stats?.let {
                contribution.content.text = it.contribCreationCount.toString()
                events.content.text = it.eventsCount.toString()
            }
            user.roles?.let {
                if (it.contains("ambassador")) pins.ambassador.visibility = View.VISIBLE
                else pins.ambassador.visibility = View.GONE
            }
            user.partner?.let {
                pins.association.association_name.text = it.name
                it.smallLogoUrl.let { logo ->
                    Glide.with(requireActivity())
                        .load(Uri.parse(logo))
                        .circleCrop()
                        .into(pins.associationAvatar)
                }
            } ?: run {
                pins.association.visibility = View.GONE
            }
            user.avatarURL.let {
                Glide.with(requireActivity())
                    .load(Uri.parse(it))
                    .circleCrop()
                    .into(imageUser)
            }
        }
    }

    private fun setBackButton() {
        binding.iconBack.setOnClickListener { findNavController().popBackStack() }
    }

}