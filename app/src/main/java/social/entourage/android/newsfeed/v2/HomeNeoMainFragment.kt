package social.entourage.android.newsfeed.v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_home_neo_main.*
import social.entourage.android.R
import social.entourage.android.tools.Utils

class HomeNeoMainFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_neo_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val colorId = ContextCompat.getColor(requireContext(), R.color.accent)
        ui_home_neo_start_title?.text = Utils.formatTextWithBoldSpanAndColor(colorId,true,getString(R.string.home_neo_title),getString(R.string.home_neo_title_bold))

        ui_layout_button_neo_1?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.goHelp()
        }

        ui_layout_button_neo_2?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.goActions()
        }
    }
}