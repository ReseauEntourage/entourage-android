package social.entourage.android.onboarding.onboard

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.Html
import android.text.InputFilter
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.phone.SmsRetriever
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
    private var callback: OnboardingStartCallback? = null
    private var countDownTimer: CountDownTimer? = null
    private var timeOut = TIME_BEFORE_CALL
    private var phoneNumber: String? = null
    private var country: Country? = null

    private val smsConsentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
                val extras = intent.extras ?: return
                val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT) ?: return
                startSmsConsent.launch(consentIntent)
            }
        }
    }

    private val startSmsConsent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE).orEmpty()
                val code = Regex("""\b(\d{6})\b""").find(message)?.groupValues?.get(1)
                if (!code.isNullOrBlank()) fillOtp(code)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            phoneNumber = it.getString(ARG_PHONE)
            country = it.get(ARG_COUNTRY) as? Country
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingPhase2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callback?.updateButtonNext(false)
        setupViews()
        setupOtp()
        activateTimer()
        AnalyticsEvents.logEvent(AnalyticsEvents.Onboard_code)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    view.hideKeyboard()
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(
            requireContext(),
            smsConsentReceiver,
            IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        SmsRetriever.getClient(requireContext()).startSmsUserConsent(null)
    }

    override fun onStop() {
        super.onStop()
        runCatching { requireContext().unregisterReceiver(smsConsentReceiver) }
    }

    // ——— Timer / Retry UI ————————————————————————————————————————————

    private fun activateTimer() {
        cancelTimer()
        timeOut = TIME_BEFORE_CALL
        setRetryWaiting(remaining = timeOut)

        countDownTimer = object : CountDownTimer(600000L, 1000L) {
            override fun onFinish() {
                cancelTimer()
                setRetryEnabled(true)
            }

            override fun onTick(ms: Long) {
                if (isDetached) return
                timeOut -= 1
                if (timeOut <= 0) {
                    setRetryEnabled(true)
                    cancelTimer()
                } else {
                    setRetryWaiting(timeOut)
                }
            }
        }
        countDownTimer?.start()
    }

    private fun setRetryWaiting(remaining: Int) {
        binding.cardRetry.isVisible = true
        val time = if (remaining < 10) "00:0$remaining" else "00:$remaining"
        binding.tvRetryTitle.text = getString(R.string.onboard_sms_wait_countdown, time)
        binding.tvRetryLink.isEnabled = false
        binding.tvRetryLink.alpha = .4f
        binding.tvRetryLink.paintFlags =
            binding.tvRetryLink.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
    }

    private fun setRetryEnabled(enabled: Boolean) {
        binding.cardRetry.isVisible = true
        binding.tvRetryTitle.text = getString(R.string.onboard_sms_wait_title)
        binding.tvRetryLink.isEnabled = enabled
        binding.tvRetryLink.alpha = if (enabled) 1f else .4f
        if (enabled) {
            binding.tvRetryLink.paintFlags =
                binding.tvRetryLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
    }

    fun cancelTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    // ——— Lifecycle hooks —————————————————————————————————————————————

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingStartCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
        cancelTimer()
    }

    // ——— UI Wiring ————————————————————————————————————————————————

    private fun setupViews() {
        // Fermer le clavier quand on tappe en dehors
        binding.layoutMain.setOnTouchListener { v, _ ->
            v.hideKeyboard()
            v.performClick()
            true
        }

        binding.uiOnboardBtHelp.paintFlags =
            binding.uiOnboardBtHelp.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        // Lien "Renvoyer le code"
        binding.tvRetryLink.setOnClickListener {
            if (binding.tvRetryLink.isEnabled) {
                callback?.requestNewCode()
                activateTimer()
            }
        }

        // Bouton d'aide → email
        binding.uiOnboardBtHelp.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:") }
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.contact_email)))
            try {
                startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                EntSnackbar.make(
                    binding.layoutMain,
                    R.string.error_no_email,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        // Texte explicatif
        binding.uiOnboardCodeTvDescription.text = getString(R.string.onboard_sms_sub)

        // ————————————————————
        // FORMATAGE DU NUMÉRO
        // ————————————————————
        val rawDigits = phoneNumber.orEmpty().filter { it.isDigit() }

        // On normalise → toujours 0XXXXXXXXX
        val normalized = when {
            rawDigits.startsWith("0") -> rawDigits
            rawDigits.startsWith("33") && rawDigits.length > 2 -> "0" + rawDigits.drop(2)
            phoneNumber?.startsWith("+33") == true && rawDigits.length > 2 -> "0" + rawDigits.drop(2)
            rawDigits.length == 9 -> "0$rawDigits"
            else -> rawDigits
        }

        // On limite à 10 chiffres max, puis espace toutes les 2
        val formatted = normalized
            .take(10)
            .chunked(2)
            .joinToString(" ")

        binding.uiOnboardCodeTvPhone.text = formatted

        // Modifier le numéro → retour étape précédente
        binding.uiOnboardCodeTvPhoneMod.setOnClickListener { callback?.goPreviousManually() }

        // CGU cliquables
        val text = getString(R.string.terms_and_conditions_html)
        @Suppress("DEPRECATION")
        binding.tvConditionGenerales.text =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
            else
                Html.fromHtml(text)
        binding.tvConditionGenerales.movementMethod = LinkMovementMethod.getInstance()
        binding.tvConditionGenerales.linksClickable = true
        binding.tvConditionGenerales.movementMethod = LinkMovementMethod.getInstance()
    }


    private fun setupOtp() {
        val fields = listOf(
            binding.etCode1, binding.etCode2, binding.etCode3,
            binding.etCode4, binding.etCode5, binding.etCode6
        )
        fields.forEach { it.filters = arrayOf(InputFilter.LengthFilter(1)) }
        fields.forEach { it.removeHintOnFocus("0") }

        fun currentCode(): String = fields.joinToString("") { it.text?.toString().orEmpty() }

        fun updateState() {
            val code = currentCode()
            if (code.length == 6) {
                callback?.validatePasscode(code)
                callback?.updateButtonNext(true)
                fields.last().clearFocus()
                binding.layoutMain.hideKeyboard()
            } else {
                callback?.validatePasscode(null)
                callback?.updateButtonNext(false)
            }
        }

        fun focusNext(from: Int) { if (from in 0..4) fields[from + 1].requestFocus() }
        fun focusPrev(from: Int) { if (from in 1..5) fields[from - 1].requestFocus() }

        fun digitsOnly(s: String) = s.filter { it.isDigit() }.take(6)

        fun setChars(start: Int, chars: String) {
            val d = digitsOnly(chars)
            if (d.isEmpty()) return
            var i = start
            var j = 0
            while (i < 6 && j < d.length) {
                fields[i].setText(d[j].toString())
                i++; j++
            }
            if (currentCode().length < 6 && i < 6) fields[i].requestFocus()
            updateState()
        }

        fun distributeFrom(startIndex: Int, payload: String) = setChars(startIndex, payload)

        fields.forEachIndexed { index, editText ->
            editText.imeOptions =
                if (index == 5) EditorInfo.IME_ACTION_DONE else EditorInfo.IME_ACTION_NEXT
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    editText.text.isNullOrEmpty()
                ) {
                    focusPrev(index); true
                } else false
            }
            editText.doOnTextChanged { text, _, _, _ ->
                if (!text.isNullOrEmpty()) focusNext(index)
                updateState()
            }
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val v = s?.toString().orEmpty()
                    if (v.length > 1) distributeFrom(index, v)
                }
            })
        }

        binding.etCode1.setOnPasteListener { distributeFrom(0, it) }
        binding.etCode2.setOnPasteListener { distributeFrom(1, it) }
        binding.etCode3.setOnPasteListener { distributeFrom(2, it) }
        binding.etCode4.setOnPasteListener { distributeFrom(3, it) }
        binding.etCode5.setOnPasteListener { distributeFrom(4, it) }
        binding.etCode6.setOnPasteListener { distributeFrom(5, it) }
    }

    private fun fillOtp(code: String) {
        val digits = code.take(6).padEnd(6, ' ').toCharArray()
        val fields = listOf(
            binding.etCode1, binding.etCode2, binding.etCode3,
            binding.etCode4, binding.etCode5, binding.etCode6
        )
        fields.forEachIndexed { i, e -> if (digits[i].isDigit()) e.setText(digits[i].toString()) }
        fields.last().clearFocus()
        binding.layoutMain.hideKeyboard()
        callback?.validatePasscode(fields.joinToString("") { it.text?.toString().orEmpty() })
        callback?.updateButtonNext(true)
    }

    companion object {
        @JvmStatic
        fun newInstance(phone: String?, country: Country?) =
            OnboardingPhase2Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PHONE, phone)
                    putSerializable(ARG_COUNTRY, country)
                }
            }
    }
}

private fun EditText.setOnPasteListener(block: (String) -> Unit) {
    setOnCreateContextMenuListener { menu, _, _ ->
        val pasteId = android.R.id.paste
        menu.findItem(pasteId)?.setOnMenuItemClickListener {
            post {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val text =
                    clipboard.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
                if (text.isNotBlank()) block(text)
            }
            true
        }
    }
}

private fun EditText.removeHintOnFocus(placeholder: CharSequence = "0") {
    // Hint visible quand pas focus
    hint = placeholder
    setOnFocusChangeListener { _, hasFocus ->
        hint = if (hasFocus) "" else placeholder
    }
}
