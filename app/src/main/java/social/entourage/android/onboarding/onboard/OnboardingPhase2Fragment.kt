package social.entourage.android.onboarding.onboard

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_onboarding_phase2.*
import kotlinx.android.synthetic.main.fragment_onboarding_phase2.tv_condition_generales
import social.entourage.android.R
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.EntSnackbar
import social.entourage.android.tools.view.countrycodepicker.Country

private const val ARG_PHONE = "phone"
private const val ARG_COUNTRY = "couuntry"

class OnboardingPhase2Fragment : Fragment() {
    private val TIME_BEFORE_CALL = 60
    private var callback:OnboardingStartCallback? = null
    private var countDownTimer:CountDownTimer? = null
    private var timeOut = TIME_BEFORE_CALL

    private var phone: String? = null
    private var country: Country? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            phone = it.getString(ARG_PHONE)
            country = it.get(ARG_COUNTRY) as? Country
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding_phase2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        callback?.updateButtonNext(false)

        setupViews()

        activateTimer()
        AnalyticsEvents.logEvent(AnalyticsEvents.Onboard_code)
    }

    private fun activateTimer() {
        cancelTimer()
        timeOut = TIME_BEFORE_CALL
        countDownTimer = object  : CountDownTimer(600000 ,1000L) {
            override fun onFinish() {
                cancelTimer()
                ui_onboard_bt_code_retry?.visibility = View.VISIBLE
                ui_onboard_code_retry?.visibility = View.INVISIBLE
                ui_onboard_code_tv_phone_mod?.visibility = View.VISIBLE
            }

            override fun onTick(p0: Long) {
                if(isDetached) return
                timeOut -= 1

                if (timeOut == 0) {
                    ui_onboard_bt_code_retry?.visibility = View.VISIBLE
                    ui_onboard_code_retry?.visibility = View.INVISIBLE
                    ui_onboard_code_tv_phone_mod?.visibility = View.VISIBLE
                    cancelTimer()
                }
                else {
                    val _time = if (timeOut < 10) "00:0$timeOut" else "00:$timeOut"
                    val _retryTxt = String.format(getString(R.string.onboard_sms_wait_retry),_time)
                    ui_onboard_code_retry?.text = _retryTxt
                }
            }
        }
        ui_onboard_code_retry?.text = String.format(getString(R.string.onboard_sms_wait_retry),"00:0$timeOut")

        countDownTimer?.start()
    }

    fun cancelTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingStartCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
        cancelTimer()
    }

    fun setupViews() {
        layout_main?.setOnTouchListener { view, motionEvent ->
            view.hideKeyboard()
            view.performClick()
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

        ui_onboard_bt_help?.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            val addresses = arrayOf(getString(R.string.contact_email))
            intent.putExtra(Intent.EXTRA_EMAIL, addresses)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                layout_main?.let { EntSnackbar.make(it, R.string.error_no_email, Snackbar.LENGTH_SHORT).show()}
            }
        }

        ui_onboard_code_tv_description?.text = getString(R.string.onboard_sms_sub)

        ui_onboard_code_tv_phone?.text = country?.phoneCode + phone

        ui_onboard_bt_code_retry?.visibility = View.INVISIBLE
        ui_onboard_code_retry?.visibility = View.VISIBLE

        ui_onboard_code_tv_phone_mod?.visibility = View.INVISIBLE

        ui_onboard_code_tv_phone_mod?.setOnClickListener {
            callback?.goPreviousManually()
        }
        val text = "En cliquant sur Je me connecte, vous acceptez les <a href='https://www.entourage.social/cgu/'>Conditions Générales d'Utilisation</a> et <a href='https://www.entourage.social/politique-de-confidentialite/'>Politique de Confidentialité</a> d'Entourage."
        tv_condition_generales.text = Html.fromHtml(text)
        tv_condition_generales.movementMethod = LinkMovementMethod.getInstance()

        addTextwatcher()
    }

    private fun addTextwatcher() {
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                           after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                       count: Int) {
                val text = activity?.currentFocus as? EditText?

                if ((text?.text?.length ?: 0) > 0) {
                    callback?.validatePasscode(text?.text.toString())
                    callback?.updateButtonNext(true)
                }
                else {
                    callback?.validatePasscode(null)
                    callback?.updateButtonNext(false)
                }
            }
        }
        ui_onboard_code?.addTextChangedListener(textWatcher)
    }

    companion object {
        @JvmStatic
        fun newInstance(phone: String?, country: Country?) =
            OnboardingPhase2Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PHONE, phone)
                    putSerializable(ARG_COUNTRY,country)
                }
            }
    }
}