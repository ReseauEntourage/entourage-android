package social.entourage.android.newsfeed.v2

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

class HomeNeoActionFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_neo_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui_bt_back?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.onBackPressed()
        }

        ui_home_action_button_1?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.goDetailActions()
        }
        ui_home_action_button_2?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.goDetailEvents()
        }

        ui_home_action_button_3?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.createAction2(BaseEntourage.GROUPTYPE_ACTION,
                    BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION,EntourageCategory.CATEGORY_MATHELP)
            //Faire un don materiel -- "contrib - mat_help
        }
        ui_home_action_button_4?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.createAction2(BaseEntourage.GROUPTYPE_ACTION,
                    BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION, EntourageCategory.CATEGORY_RESOURCE)
            //Offrir un service ---contrib - resource
        }
        ui_home_action_button_5?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.createAction2(BaseEntourage.GROUPTYPE_ACTION,
                    BaseEntourage.GROUPTYPE_ACTION_DEMAND, EntourageCategory.CATEGORY_MATHELP)
            //Un don matériel --- ask_for_help - mat_help
        }
        ui_home_action_button_6?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.createAction2(BaseEntourage.GROUPTYPE_ACTION,
                    BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION, EntourageCategory.CATEGORY_SOCIAL)
            //Partager un repas, un café - contrib - social
        }
        ui_button_show_help?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.showWebLink(Constants.SLUG_HOME_ACTION_HELP)
        }
    }
    companion object {
        const val TAG = "social.entourage.android.home.neo.actions"
    }
}