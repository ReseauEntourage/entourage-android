package social.entourage.android.new_v8.user

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.databinding.NewFragmentUserProfileBinding
import social.entourage.android.new_v8.profile.myProfile.InterestsAdapter

class UserProfileFragment : Fragment() {


    private var _binding: NewFragmentUserProfileBinding? = null
    val binding: NewFragmentUserProfileBinding get() = _binding!!
    private val userPresenter: UserPresenter by lazy { UserPresenter() }
    private lateinit var user: User

    private var interestsList: ArrayList<String> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPresenter.getUser(2889)
        userPresenter.isGetUserSuccess.observe(requireActivity(), ::handleResponse)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    private fun handleResponse(success: Boolean) {
        if (success) updateView()
        else Toast.makeText(requireActivity(), R.string.user_retrieval_error, Toast.LENGTH_SHORT)
            .show()

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
/*
    private fun handleMetaData(tags: Tags?) {
        interestsList.clear()
        val userInterests = user.interests
        tags?.interests?.forEach { interest ->
            if (userInterests.contains(interest.id)) interest.name?.let { it -> interestsList.add(it) }
        }
        binding.interests.adapter?.notifyDataSetChanged()
    }
 */


    private fun updateView() {
        userPresenter.user.value?.let { user = it }
        with(binding) {
            name.text = user.displayName
            description.text = user.about
            events.content.text = user.stats?.eventsCount.toString()
            contribution.content.text = user.stats?.contribCreationCount.toString()
            user.avatarURL.let {
                Glide.with(requireActivity())
                    .load(Uri.parse(it))
                    .circleCrop()
                    .into(imageUser)
            }
        }
    }

}