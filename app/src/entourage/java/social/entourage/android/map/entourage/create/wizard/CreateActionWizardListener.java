package social.entourage.android.map.entourage.create.wizard;

/**
 * Created by Mihai Ionescu on 05/10/2018.
 */
public interface CreateActionWizardListener {

    void createActionWizardPreviousStep(int currentStep);
    void createActionWizardNextStep(int currentStep, int option);

}
