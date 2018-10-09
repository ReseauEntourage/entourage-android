package social.entourage.android.map.entourage.create;

import android.support.v4.app.DialogFragment;
import android.util.Log;

import social.entourage.android.EntourageComponent;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.map.entourage.create.wizard.CreateActionWizardListener;
import social.entourage.android.map.entourage.create.wizard.CreateActionWizardPage1Fragment;
import social.entourage.android.map.entourage.create.wizard.CreateActionWizardPage2Fragment;
import social.entourage.android.map.entourage.create.wizard.CreateActionWizardPage3Fragment;

/**
 *
 */
public class CreateEntourageFragment extends BaseCreateEntourageFragment implements CreateActionWizardListener {

    // ----------------------------------
    // Constants
    // ----------------------------------

    // ----------------------------------
    // Attributes
    // ----------------------------------

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public CreateEntourageFragment() {
        // Required empty public constructor
    }

    @Override
    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerCreateEntourageComponent.builder()
                .entourageComponent(entourageComponent)
                .createEntourageModule(new CreateEntourageModule(this))
                .build()
                .inject(this);
    }

    // ----------------------------------
    // Entourage create methods
    // ----------------------------------

    @Override
    protected void createEntourage() {
        if (entourageCategory != null && Entourage.TYPE_DEMAND.equalsIgnoreCase(entourageCategory.getEntourageType())) {
            // for DEMAND events, we need to show a wizard
            if (getFragmentManager() != null) {
                CreateActionWizardPage1Fragment createActionWizardPage1Fragment = new CreateActionWizardPage1Fragment();
                createActionWizardPage1Fragment.setListener(this);
                createActionWizardPage1Fragment.show(getFragmentManager(), CreateActionWizardPage1Fragment.TAG);
            }
        } else {
            super.createEntourage();
        }
    }

    @Override
    protected void postEntourageCreated(final Entourage entourage) {
        //Hide the wizard pages
        if (getFragmentManager() != null) {
            DialogFragment fragment1 = (DialogFragment) getFragmentManager().findFragmentByTag(CreateActionWizardPage1Fragment.TAG);
            if (fragment1 != null) fragment1.dismiss();
            DialogFragment fragment2 = (DialogFragment) getFragmentManager().findFragmentByTag(CreateActionWizardPage2Fragment.TAG);
            if (fragment2 != null) fragment2.dismiss();
            DialogFragment fragment3 = (DialogFragment) getFragmentManager().findFragmentByTag(CreateActionWizardPage3Fragment.TAG);
            if (fragment3 != null) fragment3.dismiss();
        }
        //let the super handle the rest
        super.postEntourageCreated(entourage);
    }

    // ----------------------------------
    // CreateActionWizardListener
    // ----------------------------------


    @Override
    public void createActionWizardPreviousStep(final int currentStep) {
        if (currentStep == 1) isSaving = false;
    }

    @Override
    public void createActionWizardNextStep(final int currentStep, final int option) {
        switch (currentStep) {
            case 1:
                handleStep1(option);
                break;
            case 2:
                handleStep2(option);
                break;
            case 3:
                handleStep3(option);
                break;
            default:
                Log.d("CREATE ACTION WIZARD", "Invalid step "+currentStep);
                break;
        }
    }

    private void handleStep1(int option) {
        switch (option) {
            case 1:
                if (getFragmentManager() != null) {
                    CreateActionWizardPage2Fragment createActionWizardPage2Fragment = new CreateActionWizardPage2Fragment();
                    createActionWizardPage2Fragment.setListener(this);
                    createActionWizardPage2Fragment.show(getFragmentManager(), CreateActionWizardPage2Fragment.TAG);
                }
                break;
            case 2:
                super.createEntourage();
                break;
            case 3:
                super.createEntourage();
                break;
        }
    }

    private void handleStep2(int option) {
        switch (option) {
            case 1:
                super.createEntourage();
                break;
            case 2:
                if (getFragmentManager() != null) {
                    CreateActionWizardPage3Fragment createActionWizardPage3Fragment = new CreateActionWizardPage3Fragment();
                    createActionWizardPage3Fragment.setListener(this);
                    createActionWizardPage3Fragment.show(getFragmentManager(), CreateActionWizardPage2Fragment.TAG);
                }
                break;
        }
    }

    private void handleStep3(int option) {
        switch (option) {
            case 1:
                status = FeedItem.STATUS_SUSPENDED;
                super.createEntourage();
                status = FeedItem.STATUS_OPEN;
                break;
        }
    }
}
