package social.entourage.android.map.entourage.create.wizard;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;

/**
 * Create Action Wizard Page 1 {@link Fragment} subclass.
 */
public class CreateActionWizardPage1Fragment extends EntourageDialogFragment {

    // ----------------------------------
    // Attributes
    // ----------------------------------

    public static final String TAG = CreateActionWizardPage1Fragment.class.getSimpleName();

    private static final int STEP = 1;

    private CreateActionWizardListener listener;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public CreateActionWizardPage1Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_action_wizard_page1, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    public void setListener(final CreateActionWizardListener listener) {
        this.listener = listener;
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseClicked() {
        if (listener != null) listener.createActionWizardPreviousStep(STEP);
        dismiss();
    }

    @OnClick(R.id.create_action_wizard_p1_option1_button)
    protected void onOption1Clicked() {
        if (listener != null) {
            listener.createActionWizardNextStep(STEP, 1);
        }
    }

    @OnClick(R.id.create_action_wizard_p1_option2_button)
    protected void onOption2Clicked() {
        if (listener != null) {
            listener.createActionWizardNextStep(STEP, 2);
        }
    }

    @OnClick(R.id.create_action_wizard_p1_option3_button)
    protected void onOption3Clicked() {
        if (listener != null) {
            listener.createActionWizardNextStep(STEP, 3);
        }
    }

}
