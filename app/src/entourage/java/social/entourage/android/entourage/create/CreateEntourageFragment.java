package social.entourage.android.entourage.create;

import androidx.fragment.app.DialogFragment;

import social.entourage.android.EntourageComponent;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.entourage.create.wizard.CreateActionWizardListener;
import social.entourage.android.entourage.create.wizard.CreateActionWizardPage1Fragment;
import social.entourage.android.entourage.create.wizard.CreateActionWizardPage2Fragment;
import social.entourage.android.entourage.create.wizard.CreateActionWizardPage3Fragment;
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
                return;
            } else if (Entourage.TYPE_CONTRIBUTION.equalsIgnoreCase(entourageCategory.getEntourageType())) {
                // for CONTRIBUTION events, we need to show the join request type screen
                showCreateEntourageJoinFragment();
                return;
            }
        }
        super.createEntourage();
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
            } else  if (!Entourage.TYPE_DEMAND.equalsIgnoreCase(editedEntourage.getEntourageType())) {
                // for DEMAND, we show the wizard only if the type of the edited action has changed
                showCreateActionWizard();
                return;
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
        try {
            //Hide the wizard pages
            DialogFragment fragment1 = (DialogFragment) getParentFragmentManager().findFragmentByTag(CreateActionWizardPage1Fragment.Companion.getTAG());
            if (fragment1 != null) fragment1.dismiss();
            DialogFragment fragment2 = (DialogFragment) getParentFragmentManager().findFragmentByTag(CreateActionWizardPage2Fragment.Companion.getTAG());
            if (fragment2 != null) fragment2.dismiss();
            DialogFragment fragment3 = (DialogFragment) getParentFragmentManager().findFragmentByTag(CreateActionWizardPage3Fragment.Companion.getTAG());
            if (fragment3 != null) fragment3.dismiss();
            //Hide the join type fragment
            DialogFragment joinTypeFragment = (DialogFragment) getParentFragmentManager().findFragmentByTag(CreateEntourageJoinTypeFragment.Companion.getTAG());
            if (joinTypeFragment != null) joinTypeFragment.dismiss();
        } catch(IllegalStateException e) {
            Timber.w(e);
        }
    }

    // ----------------------------------
    // CreateActionWizard
    // ----------------------------------

    private void showCreateActionWizard() {
        joinRequestTypePublic = false;
        try {
            CreateActionWizardPage1Fragment createActionWizardPage1Fragment = new CreateActionWizardPage1Fragment();
            createActionWizardPage1Fragment.setListener(this);
            createActionWizardPage1Fragment.show(getParentFragmentManager(), CreateActionWizardPage1Fragment.Companion.getTAG());
        } catch(IllegalStateException e) {
            Timber.w(e);
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
                Timber.e("Invalid step %s", currentStep);
                break;
        }
    }

    private void handleStep1(int option) {
        switch (option) {
            case 1:
                try {
                    CreateActionWizardPage2Fragment createActionWizardPage2Fragment = new CreateActionWizardPage2Fragment();
                    createActionWizardPage2Fragment.setListener(this);
                    createActionWizardPage2Fragment.show(getParentFragmentManager(), CreateActionWizardPage2Fragment.Companion.getTAG());
                } catch(IllegalStateException e) {
                    Timber.w(e);
                }
                break;
            case 2:
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
                try {
                    CreateActionWizardPage3Fragment createActionWizardPage3Fragment = new CreateActionWizardPage3Fragment();
                    createActionWizardPage3Fragment.setListener(this);
                    createActionWizardPage3Fragment.show(getParentFragmentManager(), CreateActionWizardPage3Fragment.Companion.getTAG());
                } catch(IllegalStateException e) {
                    Timber.w(e);
                }
                break;
        }
    }

    private void handleStep3(int option) {
        if (option == 1) {
            if (editedEntourage != null) {
                super.saveEditedEntourage();
            } else {
                recipientConsentObtained = false;
                super.createEntourage();
                recipientConsentObtained = true;
            }
        }
    }

    // ----------------------------------
    // CreateEntourageJoinType
    // ----------------------------------

    private void showCreateEntourageJoinFragment() {
       try {
            CreateEntourageJoinTypeFragment createEntourageJoinTypeFragment = new CreateEntourageJoinTypeFragment();
            createEntourageJoinTypeFragment.setListener(this);
            createEntourageJoinTypeFragment.show(getParentFragmentManager(), CreateEntourageJoinTypeFragment.Companion.getTAG());
        } catch(IllegalStateException e) {
           Timber.w(e);
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
