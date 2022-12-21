package social.entourage.android.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.fragment_inputs_names.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.CustomProgressDialog

class InputNamesFragment : BaseDialogFragment() {
    private val minChars = 2

    private var firstname = ""
    private var lastname = ""

    var isAllreadySend = false

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inputs_names, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
    }

    //**********//**********//**********
    // methods
    //**********//**********//**********

    fun setupViews() {
        onboard_names_mainlayout?.setOnTouchListener { view, motionEvent ->
            view.hideKeyboard()
            view.performClick()
            true
        }

        ui_onboard_names_et_lastname?.setOnEditorActionListener { _, event, _ ->
            if (event == EditorInfo.IME_ACTION_DONE) {
                checkAndValidateInput()
            }
            false
        }

        ui_bt_validate?.setOnClickListener {
            checkAndValidateInput()
        }
        ui_onboard_names_et_firstname?.setText(firstname)
        ui_onboard_names_et_lastname?.setText(lastname)
    }

    fun sendUpdate() {
        if (isAllreadySend) return

        val dialog = CustomProgressDialog(requireContext())

        dialog.show(R.string.onboard_waiting_dialog)
        isAllreadySend = true
        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_ADD_USERNAME_SUBMIT)

        OnboardingAPI.getInstance().updateUserNames(firstname,lastname) { isOK, userResponse ->
            isAllreadySend = false
            dialog.dismiss()
            if (isOK && userResponse != null) {
                EntourageApplication.get().authenticationController.saveUser(userResponse.user)
            }
            dismiss()
        }
    }

    fun checkAndValidateInput() {
        if (ui_onboard_names_et_firstname?.text?.length ?:0 >= minChars  && ui_onboard_names_et_lastname?.text?.length ?:0 >= minChars) {
            this.firstname = ui_onboard_names_et_firstname?.text.toString()
            this.lastname = ui_onboard_names_et_lastname?.text.toString()
            sendUpdate()
        }
        else {
            val message = String.format(getString(R.string.input_names_error),minChars)
            AlertDialog.Builder(requireContext())
                    .setTitle("")
                    .setMessage(message)
                    .setPositiveButton(R.string.button_OK) { dialog, which ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
        }
    }
}
