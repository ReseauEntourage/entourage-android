package social.entourage.android.old_v7.entourage.create.wizard

/**
 * Created by Mihai Ionescu on 05/10/2018.
 */
interface CreateActionWizardListener {
    fun createActionWizardPreviousStep(currentStep: Int)
    fun createActionWizardNextStep(currentStep: Int, option: Int)
}