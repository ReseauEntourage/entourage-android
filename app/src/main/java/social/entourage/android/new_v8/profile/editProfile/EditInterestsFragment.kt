package social.entourage.android.new_v8.profile.editProfile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentEditInterestsBinding
import social.entourage.android.new_v8.profile.models.Interest

class EditInterestsFragment : Fragment() {


    private var _binding: NewFragmentEditInterestsBinding? = null
    val binding: NewFragmentEditInterestsBinding get() = _binding!!

    private val interests = listOf(
        Interest(R.drawable.new_sport, "Sport", true),
        Interest(R.drawable.new_cooking, "Cuisine", true),
        Interest(R.drawable.new_wellbeing, "Bien-être", false),
        Interest(R.drawable.new_nature, "Nature", true),
        Interest(R.drawable.new_animals, "Animaux", true),
        Interest(R.drawable.new_games, "Jeux", true),
        Interest(R.drawable.new_art, "Art et culture", true),
        Interest(R.drawable.new_drawing, "Activités manuelles", true),
        Interest(R.drawable.new_others, "Divers", true),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentEditInterestsBinding.inflate(inflater, container, false)
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