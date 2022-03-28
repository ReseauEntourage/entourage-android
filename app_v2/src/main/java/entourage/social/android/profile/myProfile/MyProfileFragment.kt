package entourage.social.android.profile.myProfile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
<<<<<<< Updated upstream:app_v2/src/main/java/entourage/social/android/profile/myProfile/MyProfileFragment.kt
import entourage.social.android.databinding.FragmentMyProfileBinding
=======
import kotlinx.android.synthetic.main.layout_mainprofile.*
import kotlinx.android.synthetic.main.new_fragment_my_profile.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentMyProfileBinding
>>>>>>> Stashed changes:app/src/main/java/social/entourage/android/new_v8/profile/myProfile/MyProfileFragment.kt


class MyProfileFragment : Fragment() {
    private var _binding: FragmentMyProfileBinding? = null
    val binding: FragmentMyProfileBinding get() = _binding!!

    private val interestsList = listOf(
        "sport", "menuiserie", "jeux de société", "musique", "foot", "musique"
    )

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
        initializeView()
        initializeInterests()
        binding.seekBarLayout.seekbar.setOnTouchListener { _, _ -> true }
        updateUserView()
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
        val user = EntourageApplication.me(activity) ?: return
        binding.name.text = user.displayName
        binding.description.text = user.about
        binding.association.association_name.text = user.organization?.name
        binding.phone.content.text = user.phone
        binding.birthday.content.text = user.birthday
        binding.email.content.text = user.email
        binding.seekBarLayout.seekbar.progress = user.travelDistance ?: 0
        binding.seekBarLayout.tvTrickleIndicator.text = user.travelDistance.toString()
        binding.association.association_avatar?.let { logoView ->
            user.organization?.largeLogoUrl?.let { partnerURL ->
                Glide.with(this)
                    .load(Uri.parse(partnerURL))
                    .circleCrop()
                    .into(logoView)
            } ?: run {
                logoView.setImageDrawable(null)
            }
        }
    }
}