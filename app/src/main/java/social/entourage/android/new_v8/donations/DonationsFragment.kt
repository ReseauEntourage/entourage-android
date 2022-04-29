package social.entourage.android.new_v8.donations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import social.entourage.android.databinding.NewFragmentDonationsBinding


class DonationsFragment : Fragment() {

    private var _binding: NewFragmentDonationsBinding? = null
    val binding: NewFragmentDonationsBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentDonationsBinding.inflate(inflater, container, false)
        return binding.root
    }
}