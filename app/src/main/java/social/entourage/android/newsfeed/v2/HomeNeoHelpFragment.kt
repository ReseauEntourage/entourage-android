package social.entourage.android.newsfeed.v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_home_neo_help.*
import social.entourage.android.R
import social.entourage.android.base.BackPressable
import social.entourage.android.tools.Utils


class HomeNeoHelpFragment : Fragment(),BackPressable {

    override fun onBackPressed(): Boolean {
        requireActivity().supportFragmentManager.popBackStack(TAG,0)

        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_neo_help, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val colorId = ContextCompat.getColor(requireContext(), R.color.accent)
        ui_home_neo_help_title?.text = Utils.formatTextWithBoldSpanAndColor(colorId,true,getString(R.string.home_neo_help_title),getString(R.string.home_neo_help_title_bold))


        ui_bt_back?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.onBackPressed()
        }

        ui_layout_button_help_1?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.goStreet()
        }
        ui_layout_button_help_2?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.goTourStart()
        }
        ui_layout_button_help_3?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.showActions(false)
        }
    }

    companion object {
        const val TAG = "social.entourage.android.home.neo.help"
    }
}