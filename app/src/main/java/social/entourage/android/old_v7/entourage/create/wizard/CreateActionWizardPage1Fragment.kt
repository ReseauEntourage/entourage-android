package social.entourage.android.old_v7.entourage.create.wizard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_create_action_wizard_page1.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment

/**
 * Create Action Wizard Page 1 [BaseDialogFragment] subclass.
 */
class CreateActionWizardPage1Fragment : BaseDialogFragment() {

    private var listener: CreateActionWizardListener? = null

    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_action_wizard_page1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title_close_button?.setOnClickListener { onCloseClicked() }

        create_action_wizard_p1_option1_button?.setOnClickListener {onOption1Clicked()}
        create_action_wizard_p1_option2_button?.setOnClickListener {onOption2Clicked()}
        create_action_wizard_p1_option3_button?.setOnClickListener {onOption3Clicked()}
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

    private fun onOption3Clicked() {
        listener?.createActionWizardNextStep(STEP, 3)
    }

    companion object {
        // ----------------------------------
        // Attributes
        // ----------------------------------
        val TAG: String? = CreateActionWizardPage1Fragment::class.java.simpleName
        private const val STEP = 1
    }
}