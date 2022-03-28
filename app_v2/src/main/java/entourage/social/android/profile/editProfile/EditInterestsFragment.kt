package entourage.social.android.profile.editProfile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import entourage.social.android.R
import entourage.social.android.databinding.FragmentEditInterestsBinding
import entourage.social.android.databinding.FragmentMyProfileBinding
import entourage.social.android.profile.models.Interest
import entourage.social.android.profile.myProfile.InterestsAdapter

class EditInterestsFragment : Fragment() {


    private var _binding: FragmentEditInterestsBinding? = null
    val binding: FragmentEditInterestsBinding get() = _binding!!

    private val interests = listOf(
        Interest(R.drawable.sport, "Sport", true),
        Interest(R.drawable.cooking, "Cuisine", true),
        Interest(R.drawable.wellbeing, "Bien-être", false),
        Interest(R.drawable.nature, "Nature", true),
        Interest(R.drawable.animals, "Animaux", true),
        Interest(R.drawable.games, "Jeux", true),
        Interest(R.drawable.art, "Art et culture", true),
        Interest(R.drawable.drawing, "Activités manuelles", true),
        Interest(R.drawable.others, "Divers", true),
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditInterestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeInterests()
        setBackButton()
    }


    private fun initializeInterests() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = InterestsListAdapter(interests)
        }
    }


    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener { findNavController().popBackStack() }
    }
}