package social.entourage.android.entourage.create.wizard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_create_action_wizard_page3.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.Constants
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.tools.EntLinkMovementMethod
import social.entourage.android.tools.Utils

/**
 * Create Action Wizard Page 3 [BaseDialogFragment] subclass.
 */
class CreateActionWizardPage3Fragment : BaseDialogFragment() {
    private var listener: CreateActionWizardListener? = null

    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_action_wizard_page3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity != null && getView() != null) {
            val activity = activity as MainActivity?
            val text = getString(R.string.create_action_wizard_disclaimer, activity?.getLink(Constants.CHARTE_LINK_ID))
            create_action_wizard_disclaimer?.text = Utils.fromHtml(text)
            create_action_wizard_disclaimer?.movementMethod = EntLinkMovementMethod
        }
        title_close_button?.setOnClickListener {onCloseClicked() }
        create_action_wizard_p3_option1_button?.setOnClickListener { onOption1Clicked() }
    }

    fun setListener(listener: CreateActionWizardListener?) {
        this.listener = listener
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------
    fun onCloseClicked() {
        listener?.createActionWizardPreviousStep(STEP)
        dismiss()
    }

    private fun onOption1Clicked() {
        listener?.createActionWizardNextStep(STEP, 1)
    }

    companion object {
        // ----------------------------------
        // Attributes
        // ----------------------------------
        val TAG = CreateActionWizardPage3Fragment::class.java.simpleName
        private const val STEP = 3
    }
}