package social.entourage.android.onboarding

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_onboarding_passcode.*
import social.entourage.android.R
import social.entourage.android.tools.hideKeyboard


private const val ARG_PHONE = "phone"

class OnboardingPasscodeFragment : Fragment() {
    private val TIME_BEFORE_CALL = 60
    private var temporaryPhone: String? = null

    private var callback:OnboardingCallback? = null

    private var countDownTimer:CountDownTimer? = null
    private var timeOut = TIME_BEFORE_CALL

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            temporaryPhone = it.getString(ARG_PHONE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_passcode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        callback?.upadteButtonNext(false)

        setupViews()

        activateTimer()
    }

    fun activateTimer() {
        cancelTimer()
        timeOut = TIME_BEFORE_CALL
        countDownTimer = object  : CountDownTimer(600000 ,1000L) {
            override fun onFinish() {
                cancelTimer()
            }

            override fun onTick(p0: Long) {
                timeOut = timeOut - 1
            }
        }

        countDownTimer?.start()
    }

    fun cancelTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
        cancelTimer()
    }

    //**********//**********//**********
    // methods
    //**********//**********//**********

    fun setupViews() {
        onboard_passcode_mainlayout?.setOnTouchListener { view, motionEvent ->
            view.hideKeyboard()
            checkInputs()
            true
        }

        ui_onboard_bt_code_retry?.setOnClickListener {
            if (timeOut > 0) {
                AlertDialog.Builder(requireActivity())
                        .setTitle(R.string.attention_pop_title)
                        .setMessage(String.format(getString(R.string.onboard_sms_pop_alert),timeOut))
                        .setPositiveButton("OK") { dialog, which -> }
                        .create()
                        .show()
            }
            else {
                callback?.requestNewCode()
                activateTimer()
            }
        }

        setupEditText(ui_onboard_code_et_1)
        setupEditText(ui_onboard_code_et_2)
        setupEditText(ui_onboard_code_et_3)
        setupEditText(ui_onboard_code_et_4)
        setupEditText(ui_onboard_code_et_5)
        setupEditText(ui_onboard_code_et_6)

        ui_onboard_code_tv_description.text = String.format(getString(R.string.onboard_sms_sub),temporaryPhone?.takeLast(2))
    }

    fun setupEditText(_editText: EditText?) {
        _editText?.setOnFocusChangeListener { view, b ->
            if (b) {
                _editText.setText("")
            }
        }

        _editText?.setFilters(arrayOf<InputFilter>(InputFilter.LengthFilter(1)))

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                           after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                       count: Int) {
                val text = activity?.getCurrentFocus() as? EditText?

                if (text != null && text.length() > 0) {
                    changeEditTextFocus(text)
                }
            }
        }
        _editText?.addTextChangedListener(textWatcher)
    }

    fun changeEditTextFocus(_editText: EditText?) {
        when(_editText) {
            ui_onboard_code_et_1 -> {
                ui_onboard_passcode_line_1.setBackgroundColor(resources.getColor(R.color.accent))
                ui_onboard_code_et_2?.requestFocus()
            }
            ui_onboard_code_et_2 -> {
                ui_onboard_passcode_line_2.setBackgroundColor(resources.getColor(R.color.accent))
                ui_onboard_code_et_3?.requestFocus()
            }
            ui_onboard_code_et_3 -> {
                ui_onboard_passcode_line_3.setBackgroundColor(resources.getColor(R.color.accent))
                ui_onboard_code_et_4?.requestFocus()
            }
            ui_onboard_code_et_4 -> {
                ui_onboard_passcode_line_4.setBackgroundColor(resources.getColor(R.color.accent))
                ui_onboard_code_et_5?.requestFocus()
            }
            ui_onboard_code_et_5 -> {
                ui_onboard_passcode_line_5.setBackgroundColor(resources.getColor(R.color.accent))
                ui_onboard_code_et_6?.requestFocus()
            }
            ui_onboard_code_et_6 -> {
                ui_onboard_passcode_line_6.setBackgroundColor(resources.getColor(R.color.accent))
                 onboard_passcode_mainlayout.hideKeyboard()
//                val imm: InputMethodManager? = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
//                imm?.hideSoftInputFromWindow(ui_onboard_code_et_6.getWindowToken(), 0)
                checkInputs()
            }
        }
    }

    fun checkInputs() {
        val input1 = ui_onboard_code_et_1?.text?.length ?:0 == 1
        val input2 = ui_onboard_code_et_2?.text?.length ?:0 == 1
        val input3 = ui_onboard_code_et_3?.text?.length ?:0 == 1
        val input4 = ui_onboard_code_et_4?.text?.length ?:0 == 1
        val input5 = ui_onboard_code_et_5?.text?.length ?:0 == 1
        val input6 = ui_onboard_code_et_6?.text?.length ?:0 == 1

        val isInputsOk = input1 == input2 == input3 == input4 == input5 == input6
        val tempCode = ui_onboard_code_et_1.text.toString() + ui_onboard_code_et_2.text.toString() +
                ui_onboard_code_et_3.text.toString() + ui_onboard_code_et_4.text.toString() +
                ui_onboard_code_et_5.text.toString() + ui_onboard_code_et_6.text.toString()
        callback?.validatePasscode(tempCode)
        callback?.upadteButtonNext(isInputsOk)
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        @JvmStatic
        fun newInstance(temporaryPhone: String?) =
                OnboardingPasscodeFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PHONE, temporaryPhone)
                    }
                }
    }
}
