package social.entourage.android.entourage.create.wizard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.entourage.fragment_create_action_wizard_page2.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.Constants
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.base.EntourageLinkMovementMethod
import social.entourage.android.tools.Utils

/**
 * Create Action Wizard Page 2 [EntourageDialogFragment] subclass.
 */
class CreateActionWizardPage2Fragment : EntourageDialogFragment() {
    private var listener: CreateActionWizardListener? = null

    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_action_wizard_page2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity != null && getView() != null) {
            val mainActivity = activity as MainActivity
            val text = getString(R.string.create_action_wizard_disclaimer, mainActivity.getLink(Constants.CHARTE_LINK_ID))
            create_action_wizard_disclaimer?.text = Utils.fromHtml(text)
            create_action_wizard_disclaimer?.movementMethod = EntourageLinkMovementMethod.getInstance()
        }
        title_close_button?.setOnClickListener { onCloseClicked()        }
        create_action_wizard_p2_option1_button?.setOnClickListener {onOption1Clicked()}
        create_action_wizard_p2_option2_button?.setOnClickListener {onOption2Clicked()}
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

    private fun onOption2Clicked() {
        listener?.createActionWizardNextStep(STEP, 2)
    }

    companion object {
        // ----------------------------------
        // Attributes
        // ----------------------------------
        val TAG = CreateActionWizardPage2Fragment::class.java.simpleName
        private const val STEP = 2
    }
}