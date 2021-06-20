package social.entourage.android.newsfeed.v2

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.fragment_home_neo_tour_send.*
import kotlinx.android.synthetic.main.fragment_home_neo_tour_send.ui_bt_back
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.HomeTourArea
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.api.TourAreaApi
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.isValidEmail
import social.entourage.android.tools.log.AnalyticsEvents

private const val ARG_PARAM1 = "param1"

class HomeNeoTourSendFragment : Fragment() {

    var tourArea: HomeTourArea?= null
    var hasEmail = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tourArea = it.getSerializable(ARG_PARAM1) as? HomeTourArea
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_neo_tour_send, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui_bt_back?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.onBackPressed()
        }

        ui_button_send_tour?.setOnClickListener {
            if (!hasEmail) {
                if(ui_et_email_tour?.text?.toString()?.isValidEmail() == false) {
                    AlertDialog.Builder(requireContext())
                            .setMessage(getString(R.string.home_neo_tour_send_pop_email_error))
                            .setCancelable(false)
                            .setPositiveButton("ok") { _dialog: DialogInterface?, _: Int -> _dialog?.dismiss()  }
                            .create()
                            .show()
                    return@setOnClickListener
                }
                sendEmailUpdate(ui_et_email_tour?.text.toString())
                return@setOnClickListener
            }
            sendTourRequest()
        }

        ui_layout_main?.setOnTouchListener { _view, _ ->
            _view.hideKeyboard()
            _view.performClick()
            true
        }

        EntourageApplication.get().me()?.email?.let {
            hasEmail = true
            ui_view_email?.visibility = View.INVISIBLE
        }

        ui_home_tour_send_title?.text = String.format(getString(R.string.home_neo_tour_send_title),tourArea?.areaName)
        ui_tv_tour_send_description?.text = String.format(getString(R.string.home_neo_tour_send_description),tourArea?.areaName)

        ui_view_valid_ok?.visibility = View.GONE

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    //Network
    fun sendTourRequest() {
        tourArea?.let { it ->
            val tagAnalytic = String.format(AnalyticsEvents.ACTION_NEOFEEDFIRST_Send_TourCity,it.postalCode)
            AnalyticsEvents.logEvent(tagAnalytic)
            TourAreaApi.getInstance().sendTourAreaRequest(it.areaId) { isOk, error ->
                if (isOk) {
                    ui_view_email?.visibility = View.INVISIBLE
                    ui_view_valid_ok?.visibility = View.VISIBLE
                    ui_button_send_tour?.visibility = View.INVISIBLE
                }
            }
        }
    }

    fun sendEmailUpdate(email:String) {
        OnboardingAPI.getInstance().updateUser(email) { _, _ ->
            sendTourRequest()
        }
    }

    companion object {
        const val TAG = "social.entourage.android.home.neo.tour.send"
        @JvmStatic
        fun newInstance(tourArea:HomeTourArea) =
                HomeNeoTourSendFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_PARAM1,tourArea)
                    }
                }
    }
}