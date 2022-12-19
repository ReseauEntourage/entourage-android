package social.entourage.android.old_v7.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_pop_info_create_entourage.*
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.old_v7.home.expert.HomeExpertFragment

private const val ARG_TITLE = "title"
private const val ARG_SUBTITLE = "subtitle"

class PopInfoCreateEntourageFragment : BaseDialogFragment() {
    private var title: String? = null
    private var subtitle: String? = null

    var homeFragment: HomeExpertFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE)
            subtitle = it.getString(ARG_SUBTITLE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pop_info_create_entourage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        ui_bt_close_pop_creeate?.setOnClickListener {
            homeFragment?.closePopAndGo()
        }

        ui_tv_title_pop?.text = title
        ui_tv_subtitle?.text = subtitle
    }

    override val slideStyle: Int
        get() = R.style.CustomDialogFragmentFade

    companion object {
        const val TAG = "social.entourage.android.home.popinfocreateentourage"

        @JvmStatic
        fun newInstance(title: String, subtitle: String) =
                PopInfoCreateEntourageFragment().apply {
                    this.isCancelable = false
                    arguments = Bundle().apply {
                        putString(ARG_TITLE, title)
                        putString(ARG_SUBTITLE, subtitle)
                    }
                }
    }
}