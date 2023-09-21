package social.entourage.android.homev2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import social.entourage.android.databinding.FragmentHomeV2LayoutBinding
import social.entourage.android.events.EventsPresenter
import social.entourage.android.home.HomePresenter

class HomeV2Fragment: Fragment() {

    //VAR
    private lateinit var binding:FragmentHomeV2LayoutBinding
    private lateinit var homePresenter:HomePresenter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeV2LayoutBinding.inflate(layoutInflater)
        homePresenter = ViewModelProvider(requireActivity()).get(HomePresenter::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    
}