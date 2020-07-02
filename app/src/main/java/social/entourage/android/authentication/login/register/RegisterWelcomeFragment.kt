package social.entourage.android.authentication.login.register

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_register_welcome.*
import social.entourage.android.Constants
import social.entourage.android.EntourageActivity
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.base.EntourageLinkMovementMethod
import social.entourage.android.tools.Utils.fromHtml
import timber.log.Timber

class RegisterWelcomeFragment  : EntourageDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var mListener: OnRegisterUserListener? = null

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_30_1)
        return inflater.inflate(R.layout.fragment_register_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseView()
        register_welcome_back_button?.setOnClickListener { onBackClicked() }
        register_welcome_signin_button?.setOnClickListener { onSigninClicked() }
        register_welcome_start_button?.setOnClickListener { onStartClicked() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnRegisterUserListener) {
            context
        } else {
            throw RuntimeException(context.toString()
                    + " must implement OnRegisterUserListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    //Hack temporaire (en attendant la nouvelle version de l'onboarding)
    @JvmField
    var isFromChoice = false
    var isShowLogin = false
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (isFromChoice) {
            mListener?.registerClosePop(isShowLogin)
        }
    }

    // ----------------------------------
    // Click handlers
    // ----------------------------------
    fun onBackClicked() {
        dismiss()
    }

    fun onSigninClicked() {
        mListener?.registerShowSignIn()
        isShowLogin = true
        dismiss()
    }

    fun onStartClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_WELCOME_CONTINUE)
        if (mListener?.registerStart() == true) {
            try {
                RegisterNumberFragment().show(parentFragmentManager, RegisterNumberFragment.TAG)
            } catch (e: IllegalStateException) {
                Timber.w(e)
            }
        } else {
            dismiss()
        }
    }

    // ----------------------------------
    // Private Methods
    // ----------------------------------
    private fun initialiseView() {
        if (activity != null && activity is EntourageActivity) {
            (activity as EntourageActivity?)?.let {
                val termsLink = it.getLink(Constants.TERMS_LINK_ID)
                val privacyLink = it.getLink(Constants.PRIVACY_LINK_ID)
                val text = getString(R.string.registration_welcome_privacy, termsLink, privacyLink)
                register_welcome_privacy?.text = fromHtml(text)
            }
        }
        register_welcome_privacy?.movementMethod = EntourageLinkMovementMethod
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.RegisterWelcome"
    }
}