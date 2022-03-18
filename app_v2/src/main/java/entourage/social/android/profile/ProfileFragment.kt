package entourage.social.android.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import entourage.social.android.R
import entourage.social.android.databinding.FragmentProfileBinding
import entourage.social.android.profile.myProfile.MyProfileFragment
import entourage.social.android.profile.settings.SettingsFragment


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    val binding: FragmentProfileBinding get() = _binding!!
    private var myProfileFragment: Fragment? = null
    private var settingsFragment: Fragment? = null
    private var allTabs: TabLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeTab()
        // getAllWidgets();
        //bindWidgetsWithAnEvent();
        //setupTabLayout();
    }

    private fun getAllWidgets() {
        allTabs = binding.tabLayout
    }

    private fun setupTabLayout() {
        myProfileFragment = MyProfileFragment()
        settingsFragment = SettingsFragment()
        allTabs?.newTab()?.setText("ONE")?.let { allTabs?.addTab(it, true) }
        allTabs?.newTab()?.setText("TWO")?.let { allTabs?.addTab(it) }
    }

    private fun bindWidgetsWithAnEvent() {
        allTabs?.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                setCurrentTabFragment(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setCurrentTabFragment(tabPosition: Int) {
        when (tabPosition) {
            0 -> myProfileFragment?.let { replaceFragment(it) }
            1 -> settingsFragment?.let { replaceFragment(it) }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        /*activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.frame_container, fragment)
            ?.setTransition(FragmentTransaction.TRANSIT_NONE)?.commit()*/
    }

    private fun initializeTab() {
        val viewPager = binding.viewPager
        val adapter = ViewPagerAdapter(parentFragmentManager, lifecycle)
        viewPager.adapter = adapter
        val tabLayout = binding.tabLayout
        val tabs = arrayOf(
            requireContext().getString(R.string.my_profile),
            requireContext().getString(R.string.settings)
        )
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()
    }
}