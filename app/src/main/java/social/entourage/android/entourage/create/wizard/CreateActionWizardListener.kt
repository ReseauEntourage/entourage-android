package social.entourage.android.entourage.create.wizard

/**
 * Created by Mihai Ionescu on 05/10/2018.
 */
interface CreateActionWizardListener {
    fun createActionWizardPreviousStep(currentStep: Int)
    fun createActionWizardNextStep(currentStep: Int, option: Int)
}