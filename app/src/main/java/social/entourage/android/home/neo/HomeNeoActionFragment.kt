package social.entourage.android.home.neo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home_neo_action.*
import kotlinx.android.synthetic.main.fragment_home_neo_action.ui_bt_back
import social.entourage.android.Constants
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.home.HomeFragment
import social.entourage.android.tools.log.AnalyticsEvents

class HomeNeoActionFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_neo_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui_bt_back?.setOnClickListener {
            (parentFragment as? HomeFragment)?.onBackPressed()
        }

        ui_home_action_button_1?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEOFEEDACT_Needs)
            (parentFragment as? HomeFragment)?.goDetailActions()
        }
        ui_home_action_button_2?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEOFEEDACT_Events)
            (parentFragment as? HomeFragment)?.goDetailEvents()
        }

        ui_home_action_button_3?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEOFEEDACT_OfferMaterial)
            (parentFragment as? HomeFragment)?.createAction2(BaseEntourage.GROUPTYPE_ACTION,
                    BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION,EntourageCategory.CATEGORY_MATHELP,
                    AnalyticsEvents.ACTION_NEOFEEDACT_NameMaterial)
            //Faire un don materiel -- "contrib - mat_help
        }
        ui_home_action_button_4?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEOFEEDACT_OfferService)
            (parentFragment as? HomeFragment)?.createAction2(BaseEntourage.GROUPTYPE_ACTION,
                    BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION, EntourageCategory.CATEGORY_RESOURCE,
                    AnalyticsEvents.ACTION_NEOFEEDACT_NameService)
            //Offrir un service ---contrib - resource
        }
        ui_home_action_button_5?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEOFEEDACT_RelayNeed)
            (parentFragment as? HomeFragment)?.createAction2(BaseEntourage.GROUPTYPE_ACTION,
                    BaseEntourage.GROUPTYPE_ACTION_DEMAND, EntourageCategory.CATEGORY_MATHELP,
                    AnalyticsEvents.ACTION_NEOFEEDACT_NameNeeds)
            //Un don matériel --- ask_for_help - mat_help
        }
        ui_home_action_button_6?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEOFEEDACT_Coffee)
            (parentFragment as? HomeFragment)?.createAction2(BaseEntourage.GROUPTYPE_ACTION,
                    BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION, EntourageCategory.CATEGORY_SOCIAL,
                    AnalyticsEvents.ACTION_NEOFEEDACT_NameCoffee)
            //Partager un repas, un café - contrib - social
        }
        ui_button_show_help?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEOFEEDACT_How1Step)
            (parentFragment as? HomeFragment)?.showWebLink(Constants.SLUG_HOME_ACTION_HELP)
        }
    }
    companion object {
        const val TAG = "social.entourage.android.home.neo.actions"
    }
}