package social.entourage.android.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import social.entourage.android.R
import social.entourage.android.base.BaseFragment
import social.entourage.android.home.expert.HomeExpertFragment

@Deprecated("You should use HomeExpertFragment")
class HomeFragment : BaseFragment() {
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.beginTransaction()
            .replace(R.id.ui_container, HomeExpertFragment())
            .commit()
    }

    companion object {
        const val TAG = "social.entourage.android.fragment_home"
    }
}