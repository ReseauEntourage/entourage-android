package social.entourage.android.onboarding

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_onboarding_names.*
import social.entourage.android.R
import social.entourage.android.tools.Logger
import social.entourage.android.tools.hideKeyboard

private const val ARG_FIRSTNAME = "firstname"
private const val ARG_LASTNAME = "lastname"

class OnboardingNamesFragment : Fragment() {
    private val minChars = 2

    private var firstname: String? = null
    private var lastname: String? = null

    private var callback:OnboardingCallback? = null
    private var isAllreadyCall = false

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            firstname = it.getString(ARG_FIRSTNAME)
            lastname = it.getString(ARG_LASTNAME)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_names, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        callback?.upadteButtonNext(checkAndValidateInput())

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
    // methods
    //**********//**********//**********

    fun setupViews() {
        onboard_names_mainlayout?.setOnTouchListener { view, motionEvent ->
            view.hideKeyboard()
            true
        }

        ui_onboard_names_et_firstname?.setOnFocusChangeListener { view, b ->
            if (!b) updateButtonNext(false)
        }

        ui_onboard_names_et_lastname?.setOnFocusChangeListener { view, b ->
            Logger("Call from Focus change")
            if (!b && !isAllreadyCall) updateButtonNext(false)
            if (b) isAllreadyCall = false
        }

        ui_onboard_names_et_lastname.setOnEditorActionListener { _, event, _ ->
            Logger("ON key listener $event -- ${EditorInfo.IME_ACTION_DONE}")
            if (event == EditorInfo.IME_ACTION_DONE) {
                Logger("Call from Editor Action")
                isAllreadyCall = true
                updateButtonNext(true)
            }
            false
        }

        ui_onboard_names_et_firstname.setText(firstname)
        ui_onboard_names_et_lastname.setText(lastname)
    }

    fun updateButtonNext(isValidate:Boolean) {
        if (checkAndValidateInput()) {
            callback?.upadteButtonNext(true)
            callback?.validateNames(ui_onboard_names_et_firstname?.text?.toString(),ui_onboard_names_et_lastname?.text?.toString(),isValidate)
        }
        else {
            callback?.upadteButtonNext(false)
            callback?.validateNames(null,null,false)
        }
    }

    fun checkAndValidateInput() : Boolean {
        if (ui_onboard_names_et_firstname?.text?.length ?:0 >= minChars  && ui_onboard_names_et_lastname?.text?.length ?:0 >= minChars) {
            return  true
        }
        return false
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        @JvmStatic
        fun newInstance(firstname: String?, lastname: String?) =
                OnboardingNamesFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_FIRSTNAME, firstname)
                        putString(ARG_LASTNAME, lastname)
                    }
                }
    }
}
