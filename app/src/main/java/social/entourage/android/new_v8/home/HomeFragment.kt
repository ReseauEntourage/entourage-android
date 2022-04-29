package social.entourage.android.new_v8.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import social.entourage.android.databinding.NewFragmentHomeBinding
import social.entourage.android.new_v8.profile.ProfileActivity

class HomeFragment : Fragment() {
    private var _binding: NewFragmentHomeBinding? = null
    val binding: NewFragmentHomeBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProfile()
    }

    private fun setProfile() {
        binding.myProfile.setOnClickListener {
            startActivity(
                Intent(context, ProfileActivity::class.java)
            )
        }
    }
}