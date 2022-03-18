package entourage.social.android.profile.myProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import entourage.social.android.databinding.FragmentMyProfileBinding


class MyProfileFragment : Fragment() {
    private var _binding: FragmentMyProfileBinding? = null
    val binding: FragmentMyProfileBinding get() = _binding!!

    private val interests = listOf(
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
        binding.city.divider.visibility = View.GONE
    }

}