package social.entourage.android.newsfeed.v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_home_neo_main.*
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.tools.Utils
import social.entourage.android.tools.log.AnalyticsEvents

class HomeNeoMainFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_neo_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val colorId = ContextCompat.getColor(requireContext(), R.color.accent)
        ui_home_neo_start_title?.text = Utils.formatTextWithBoldSpanAndColor(colorId,true, getString(R.string.home_neo_title), getString(R.string.home_neo_title_bold))

        ui_layout_button_neo_1?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.goHelp()
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEOFEED_FirstStep)
        }

        ui_layout_button_neo_2?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.goActions()
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEOFEED_ActNow)
        }

        ui_tv_action_change_mode?.setOnClickListener {
            (activity as? MainActivity)?.showProfileTab()
        }

        checkProfile()

        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_START_NeoFeed)
    }

    fun checkProfile() {
        val isAlreadyInfoNeo = EntourageApplication.get().sharedPreferences.getBoolean(EntourageApplication.KEY_HOME_IS_ALREADYINFO_NEO,false)

        if (!isAlreadyInfoNeo) {
            EntourageApplication.get().sharedPreferences.edit()
                    .putBoolean(EntourageApplication.KEY_HOME_IS_ALREADYINFO_NEO,true).apply()

            EntourageApplication.me(activity)?.let { user ->
                if (user.isUserTypeNeighbour && !user.isEngaged) {
                    AlertDialog.Builder(requireContext())
                            .setTitle(R.string.home_neo_pop_info_title)
                            .setMessage(R.string.home_neo_pop_info_message)
                            .setNegativeButton(R.string.home_neo_pop_info_button_ok) { _,_ ->}
                            .setPositiveButton(R.string.home_neo_pop_info_button_profil) { dialog, _ ->
                                dialog.dismiss()
                                (activity as? MainActivity)?.showProfileTab()
                            }
                            .create()
                            .show()
                }
            }
        }
    }
}