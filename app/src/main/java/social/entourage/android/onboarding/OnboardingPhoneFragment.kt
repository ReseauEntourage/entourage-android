package social.entourage.android.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_onboarding_phone.*
import social.entourage.android.R
import social.entourage.android.tools.Logger
import social.entourage.android.tools.hideKeyboard


private const val ARG_FIRSTNAME = "firstname"
private const val ARG_COUNTRYCODE = "countrycode"
private const val ARG_PHONE = "phone"


class OnboardingPhoneFragment : Fragment() {
    private val minimumPhoneCharacters = 9

    private var firstname: String? = null
    private var countryCode: String? = null
    private var phone: String? = null

    private var callback:OnboardingCallback? = null

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            firstname = it.getString(ARG_FIRSTNAME)
            countryCode = it.getString(ARG_COUNTRYCODE)
            phone = it.getString(ARG_PHONE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_phone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getActivity()?.getWindow()?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        if (phone?.length ?: 0  >= minimumPhoneCharacters) {
            callback?.upadteButtonNext(true)
        }
        else {
            callback?.upadteButtonNext(false)
        }

        setupViews()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    //**********//**********//**********
    // Methods
    //**********//**********//**********

    fun setupViews() {
        ui_onboard_phone_tv_title.text = String.format(getString(R.string.onboard_phone_title),firstname)
        ui_onboard_phone_et_phone.setText(phone)

        ui_onboard_phone_et_phone?.setOnFocusChangeListener { _view, b ->
            checkAndUpdate()
        }

        ui_onboard_phone_et_phone?.setOnEditorActionListener { _, event, _ ->
            Logger("ON key listener $event -- ${EditorInfo.IME_ACTION_DONE}")
            if (event == EditorInfo.IME_ACTION_DONE) {
                checkAndUpdate()
            }
            false
        }

        onboard_phone_mainlayout?.setOnTouchListener { view, motionEvent ->
            view.hideKeyboard()
            true
        }
    }

    fun checkAndUpdate() {
        if (ui_onboard_phone_et_phone.text?.length ?: 0  >= minimumPhoneCharacters) {
            phone = ui_onboard_phone_et_phone?.text.toString()
            callback?.upadteButtonNext(true)
            val countryCode = ui_onboard_phone_ccp_code?.getSelectedCountryCodeWithPlus()
            callback?.validatePhoneNumber(countryCode,ui_onboard_phone_et_phone.text.toString())
        }
        else {
            callback?.upadteButtonNext(false)
            callback?.validatePhoneNumber(null,null)
        }
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        @JvmStatic
        fun newInstance(firstname: String?, countryCode: String?, phone:String?) =
                OnboardingPhoneFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_FIRSTNAME, firstname)
                        putString(ARG_COUNTRYCODE, countryCode)
                        putString(ARG_PHONE, phone)
                    }
                }
    }
}
