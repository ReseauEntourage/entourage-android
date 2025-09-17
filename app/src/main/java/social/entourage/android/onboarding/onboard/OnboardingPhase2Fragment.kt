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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

import social.entourage.android.R
import social.entourage.android.databinding.FragmentOnboardingPhase2Binding
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.EntSnackbar
import social.entourage.android.tools.view.countrycodepicker.Country

private const val ARG_PHONE = "phone"
private const val ARG_COUNTRY = "couuntry"

class OnboardingPhase2Fragment : Fragment() {
    private lateinit var binding: FragmentOnboardingPhase2Binding
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
        binding = FragmentOnboardingPhase2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        callback?.updateButtonNext(false)

        setupViews()

        activateTimer()
        AnalyticsEvents.logEvent(AnalyticsEvents.Onboard_code)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                view?.hideKeyboard()
                // Tu peux ici laisser l'utilisateur quitter la vue :
                // isEnabled = false
                // requireActivity().onBackPressed()
            }
        })
    }

    private fun activateTimer() {
        cancelTimer()
        timeOut = TIME_BEFORE_CALL
        countDownTimer = object  : CountDownTimer(600000 ,1000L) {
            override fun onFinish() {
                cancelTimer()
                binding.uiOnboardBtCodeRetry?.visibility = View.VISIBLE
                binding.uiOnboardCodeRetry?.visibility = View.INVISIBLE
                binding.uiOnboardCodeTvPhoneMod?.visibility = View.VISIBLE
            }

            override fun onTick(p0: Long) {
                if(isDetached) return
                timeOut -= 1

                if (timeOut == 0) {
                    binding.uiOnboardBtCodeRetry.visibility = View.VISIBLE
                    binding.uiOnboardCodeRetry.visibility = View.INVISIBLE
                    binding.uiOnboardCodeTvPhoneMod.visibility = View.VISIBLE
                    cancelTimer()
                }
                else {
                    val _time = if (timeOut < 10) "00:0$timeOut" else "00:$timeOut"
                    val _retryTxt = String.format(getString(R.string.onboard_sms_wait_retry),_time)
                    binding.uiOnboardCodeRetry.text = _retryTxt
                }
            }
        }
        binding.uiOnboardCodeRetry?.text = String.format(getString(R.string.onboard_sms_wait_retry),"00:0$timeOut")

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
        binding.layoutMain.setOnTouchListener { view, motionEvent ->
            view.hideKeyboard()
            view.performClick()
            true
        }

        binding.uiOnboardBtCodeRetry?.setOnClickListener {
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

        binding.uiOnboardBtHelp?.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            val addresses = arrayOf(getString(R.string.contact_email))
            intent.putExtra(Intent.EXTRA_EMAIL, addresses)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                binding.layoutMain?.let { EntSnackbar.make(it, R.string.error_no_email, Snackbar.LENGTH_SHORT).show()}
            }
        }

        binding.uiOnboardCodeTvDescription?.text = getString(R.string.onboard_sms_sub)

        binding.uiOnboardCodeTvPhone?.text = country?.phoneCode + phone

        binding.uiOnboardBtCodeRetry?.visibility = View.INVISIBLE
        binding.uiOnboardCodeRetry?.visibility = View.VISIBLE

        binding.uiOnboardCodeTvPhoneMod?.visibility = View.INVISIBLE

        binding.uiOnboardCodeTvPhoneMod?.setOnClickListener {
            callback?.goPreviousManually()
        }

        val text = getString(R.string.terms_and_conditions_html)
        binding.tvConditionGenerales.text = Html.fromHtml(text)
        binding.tvConditionGenerales.movementMethod = LinkMovementMethod.getInstance()


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
        binding.uiOnboardCode?.addTextChangedListener(textWatcher)
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