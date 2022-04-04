package social.entourage.android.new_v8.profile.myProfile

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
import social.entourage.android.databinding.NewFragmentMyProfileBinding


class MyProfileFragment : Fragment() {
    private var _binding: NewFragmentMyProfileBinding? = null
    val binding: NewFragmentMyProfileBinding get() = _binding!!

    private val interests = listOf(
        "sport", "menuiserie", "jeux de société", "musique", "foot", "musique"
    )

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
        initializeView()
        initializeInterests()
        binding.seekBarLayout.seekbar.setOnTouchListener { _, _ -> true }
    }


    private fun initializeInterests() {
        binding.interests.apply {
            val layoutManagerFlex = FlexboxLayoutManager(context)
            layoutManagerFlex.flexDirection = FlexDirection.ROW
            layoutManagerFlex.justifyContent = JustifyContent.CENTER
            layoutManager = layoutManagerFlex
            adapter = InterestsAdapter(interests)
        }
    }

    private fun initializeView() {
        binding.ambassador.setOnClickListener {
            findNavController().navigate(R.id.action_profile_fragment_to_user_fragment)
        }

    }
}