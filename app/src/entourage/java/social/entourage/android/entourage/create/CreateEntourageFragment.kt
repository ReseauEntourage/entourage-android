package social.entourage.android.entourage.create

import androidx.fragment.app.DialogFragment
import social.entourage.android.EntourageComponent
import social.entourage.android.api.model.map.Entourage
import social.entourage.android.entourage.create.CreateEntourageJoinTypeFragment.CreateEntourageJoinTypeListener
import social.entourage.android.entourage.create.wizard.CreateActionWizardListener
import social.entourage.android.entourage.create.wizard.CreateActionWizardPage1Fragment
import social.entourage.android.entourage.create.wizard.CreateActionWizardPage2Fragment
import social.entourage.android.entourage.create.wizard.CreateActionWizardPage3Fragment
import timber.log.Timber

/**
 *
 */
class CreateEntourageFragment : BaseCreateEntourageFragment(), CreateActionWizardListener, CreateEntourageJoinTypeListener {
    // ----------------------------------
    // Constants
    // ----------------------------------
    // ----------------------------------
    // Attributes
    // ----------------------------------
    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun setupComponent(entourageComponent: EntourageComponent) {
        DaggerCreateEntourageComponent.builder()
                .entourageComponent(entourageComponent)
                .createEntourageModule(CreateEntourageModule(this))
                .build()
                .inject(this)
    }

    // ----------------------------------
    // Entourage create/edit methods
    // ----------------------------------
    override fun createEntourage() {
        if (entourageCategory != null) {
            if (Entourage.TYPE_DEMAND.equals(entourageCategory.entourageType, ignoreCase = true)) { // for DEMAND events, we need to show a wizard
                showCreateActionWizard()
                return
            } else if (Entourage.TYPE_CONTRIBUTION.equals(entourageCategory.entourageType, ignoreCase = true)) { // for CONTRIBUTION events, we need to show the join request type screen
                showCreateEntourageJoinFragment()
                return
            }
        }
        super.createEntourage()
    }

    override fun postEntourageCreated(entourage: Entourage) {
        hideExtraScreens()
        super.postEntourageCreated(entourage)
    }

    override fun saveEditedEntourage() { // if the user changed the type, we need to show the wizard or the join type screens
        if (entourageCategory != null) {
            if (Entourage.TYPE_CONTRIBUTION.equals(entourageCategory.entourageType, ignoreCase = true)) { // for CONTRIBUTION events, we need to show the join request type screen
                showCreateEntourageJoinFragment()
                return
            } else if (!Entourage.TYPE_DEMAND.equals(editedEntourage.entourageType, ignoreCase = true)) { // for DEMAND, we show the wizard only if the type of the edited action has changed
                showCreateActionWizard()
                return
            }
        }
        super.saveEditedEntourage()
    }

    override fun postEntourageSaved(entourage: Entourage) {
        hideExtraScreens()
        super.postEntourageSaved(entourage)
    }

    private fun hideExtraScreens() {
        try { //Hide the wizard pages
            (parentFragmentManager.findFragmentByTag(CreateActionWizardPage1Fragment.TAG) as DialogFragment?)?.dismiss()
            (parentFragmentManager.findFragmentByTag(CreateActionWizardPage2Fragment.TAG) as DialogFragment?)?.dismiss()
            (parentFragmentManager.findFragmentByTag(CreateActionWizardPage3Fragment.TAG) as DialogFragment?)?.dismiss()
            //Hide the join type fragment
            (parentFragmentManager.findFragmentByTag(CreateEntourageJoinTypeFragment.TAG) as DialogFragment?)?.dismiss()
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    // ----------------------------------
    // CreateActionWizard
    // ----------------------------------
    private fun showCreateActionWizard() {
        joinRequestTypePublic = false
        try {
            val createActionWizardPage1Fragment = CreateActionWizardPage1Fragment()
            createActionWizardPage1Fragment.setListener(this)
            createActionWizardPage1Fragment.show(parentFragmentManager, CreateActionWizardPage1Fragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    override fun createActionWizardPreviousStep(currentStep: Int) {
        if (currentStep == 1) isSaving = false
    }

    override fun createActionWizardNextStep(currentStep: Int, option: Int) {
        when (currentStep) {
            1 -> handleStep1(option)
            2 -> handleStep2(option)
            3 -> handleStep3(option)
            else -> Timber.e("Invalid step %s", currentStep)
        }
    }

    private fun handleStep1(option: Int) {
        when (option) {
            1 -> try {
                val createActionWizardPage2Fragment = CreateActionWizardPage2Fragment()
                createActionWizardPage2Fragment.setListener(this)
                createActionWizardPage2Fragment.show(parentFragmentManager, CreateActionWizardPage2Fragment.TAG)
            } catch (e: IllegalStateException) {
                Timber.w(e)
            }
            2, 3 -> if (editedEntourage != null) {
                super.saveEditedEntourage()
            } else {
                super.createEntourage()
            }
        }
    }

    private fun handleStep2(option: Int) {
        when (option) {
            1 -> if (editedEntourage != null) {
                super.saveEditedEntourage()
            } else {
                super.createEntourage()
            }
            2 -> try {
                val createActionWizardPage3Fragment = CreateActionWizardPage3Fragment()
                createActionWizardPage3Fragment.setListener(this)
                createActionWizardPage3Fragment.show(parentFragmentManager, CreateActionWizardPage3Fragment.TAG)
            } catch (e: IllegalStateException) {
                Timber.w(e)
            }
        }
    }

    private fun handleStep3(option: Int) {
        if (option == 1) {
            if (editedEntourage != null) {
                super.saveEditedEntourage()
            } else {
                recipientConsentObtained = false
                super.createEntourage()
                recipientConsentObtained = true
            }
        }
    }

    // ----------------------------------
    // CreateEntourageJoinType
    // ----------------------------------
    private fun showCreateEntourageJoinFragment() {
        try {
            val createEntourageJoinTypeFragment = CreateEntourageJoinTypeFragment()
            createEntourageJoinTypeFragment.setListener(this)
            createEntourageJoinTypeFragment.show(parentFragmentManager, CreateEntourageJoinTypeFragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    override fun createEntourageWithJoinTypePublic(joinType: Boolean) {
        joinRequestTypePublic = joinType
        if (editedEntourage != null) {
            super.saveEditedEntourage()
        } else {
            super.createEntourage()
        }
    }
}