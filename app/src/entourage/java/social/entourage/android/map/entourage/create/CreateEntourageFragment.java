package social.entourage.android.map.entourage.create;

import android.support.v4.app.DialogFragment;
import android.util.Log;

import social.entourage.android.EntourageComponent;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.map.entourage.create.wizard.CreateActionWizardListener;
import social.entourage.android.map.entourage.create.wizard.CreateActionWizardPage1Fragment;
import social.entourage.android.map.entourage.create.wizard.CreateActionWizardPage2Fragment;
import social.entourage.android.map.entourage.create.wizard.CreateActionWizardPage3Fragment;
import timber.log.Timber;

/**
 *
 */
public class CreateEntourageFragment extends BaseCreateEntourageFragment implements CreateActionWizardListener, CreateEntourageJoinTypeFragment.CreateEntourageJoinTypeListener {

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
    // Entourage create/edit methods
    // ----------------------------------

    @Override
    protected void createEntourage() {
        if (entourageCategory != null) {
            if (Entourage.TYPE_DEMAND.equalsIgnoreCase(entourageCategory.getEntourageType())) {
                // for DEMAND events, we need to show a wizard
                showCreateActionWizard();
            } else {
                // for CONTRIBUTION events, we need to show the join request type screen
                showCreateEntourageJoinFragment();
            }
        } else {
            super.createEntourage();
        }
    }

    @Override
    protected void postEntourageCreated(final Entourage entourage) {
        hideExtraScreens();
        super.postEntourageCreated(entourage);
    }

    @Override
    protected void saveEditedEntourage() {
        // if the user changed the type, we need to show the wizard or the join type screens
        if (entourageCategory != null) {
            if (Entourage.TYPE_CONTRIBUTION.equalsIgnoreCase(entourageCategory.getEntourageType())) {
                // for CONTRIBUTION events, we need to show the join request type screen
                showCreateEntourageJoinFragment();
                return;
            } else {
                // for DEMAND, we show the wizard only if the type of the edited action has changed
                if (!Entourage.TYPE_DEMAND.equalsIgnoreCase(editedEntourage.getEntourageType())) {
                    showCreateActionWizard();
                    return;
                }
            }
        }
        super.saveEditedEntourage();
    }

    @Override
    protected void postEntourageSaved(final Entourage entourage) {
        hideExtraScreens();
        super.postEntourageSaved(entourage);
    }

    private void hideExtraScreens() {
        if (getFragmentManager() != null) {
            //Hide the wizard pages
            DialogFragment fragment1 = (DialogFragment) getFragmentManager().findFragmentByTag(CreateActionWizardPage1Fragment.TAG);
            if (fragment1 != null) fragment1.dismiss();
            DialogFragment fragment2 = (DialogFragment) getFragmentManager().findFragmentByTag(CreateActionWizardPage2Fragment.TAG);
            if (fragment2 != null) fragment2.dismiss();
            DialogFragment fragment3 = (DialogFragment) getFragmentManager().findFragmentByTag(CreateActionWizardPage3Fragment.TAG);
            if (fragment3 != null) fragment3.dismiss();
            //Hide the join type fragment
            DialogFragment joinTypeFragment = (DialogFragment) getFragmentManager().findFragmentByTag(CreateEntourageJoinTypeFragment.TAG);
            if (joinTypeFragment != null) joinTypeFragment.dismiss();
        }
    }

    // ----------------------------------
    // CreateActionWizard
    // ----------------------------------

    private void showCreateActionWizard() {
        joinRequestTypePublic = false;
        if (getFragmentManager() != null) {
            CreateActionWizardPage1Fragment createActionWizardPage1Fragment = new CreateActionWizardPage1Fragment();
            createActionWizardPage1Fragment.setListener(this);
            createActionWizardPage1Fragment.show(getFragmentManager(), CreateActionWizardPage1Fragment.TAG);
        }
    }

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
                Timber.tag("CREATE ACTION WIZARD").e("Invalid step " + currentStep);
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
                if (editedEntourage != null) {
                    super.saveEditedEntourage();
                } else {
                    super.createEntourage();
                }
                break;
            case 3:
                if (editedEntourage != null) {
                    super.saveEditedEntourage();
                } else {
                    super.createEntourage();
                }
                break;
        }
    }

    private void handleStep2(int option) {
        switch (option) {
            case 1:
                if (editedEntourage != null) {
                    super.saveEditedEntourage();
                } else {
                    super.createEntourage();
                }
                break;
            case 2:
                if (getFragmentManager() != null) {
                    CreateActionWizardPage3Fragment createActionWizardPage3Fragment = new CreateActionWizardPage3Fragment();
                    createActionWizardPage3Fragment.setListener(this);
                    createActionWizardPage3Fragment.show(getFragmentManager(), CreateActionWizardPage3Fragment.TAG);
                }
                break;
        }
    }

    private void handleStep3(int option) {
        switch (option) {
            case 1:
                if (editedEntourage != null) {
                    super.saveEditedEntourage();
                } else {
                    recipientConsentObtained = false;
                    super.createEntourage();
                    recipientConsentObtained = true;
                }
                break;
        }
    }

    // ----------------------------------
    // CreateEntourageJoinType
    // ----------------------------------

    private void showCreateEntourageJoinFragment() {
        if (getFragmentManager() != null) {
            CreateEntourageJoinTypeFragment createEntourageJoinTypeFragment = new CreateEntourageJoinTypeFragment();
            createEntourageJoinTypeFragment.setListener(this);
            createEntourageJoinTypeFragment.show(getFragmentManager(), CreateEntourageJoinTypeFragment.TAG);
        }
    }

    @Override
    public void createEntourageWithJoinTypePublic(final boolean joinType) {
        joinRequestTypePublic = joinType;
        if (editedEntourage != null) {
            super.saveEditedEntourage();
        } else {
            super.createEntourage();
        }
    }
}
