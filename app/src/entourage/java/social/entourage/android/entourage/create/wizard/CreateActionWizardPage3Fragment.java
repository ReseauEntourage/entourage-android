package social.entourage.android.entourage.create.wizard;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.MainActivity;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.base.EntourageLinkMovementMethod;
import social.entourage.android.tools.Utils;

/**
 * Create Action Wizard Page 3 {@link EntourageDialogFragment} subclass.
 */
public class CreateActionWizardPage3Fragment extends EntourageDialogFragment {

    // ----------------------------------
    // Attributes
    // ----------------------------------

    public static final String TAG = CreateActionWizardPage3Fragment.class.getSimpleName();

    private static final int STEP = 3;

    private CreateActionWizardListener listener;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public CreateActionWizardPage3Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_action_wizard_page3, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null && getView() != null) {
            TextView disclaimerTextView = getView().findViewById(R.id.create_action_wizard_disclaimer);
            if (disclaimerTextView != null) {
                MainActivity activity = (MainActivity)getActivity();
                String text = getString(R.string.create_action_wizard_disclaimer, activity.getLink(Constants.CHARTE_LINK_ID));
                disclaimerTextView.setText(Utils.fromHtml(text));
                disclaimerTextView.setMovementMethod(EntourageLinkMovementMethod.getInstance());
            }
        }
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

    @OnClick(R.id.create_action_wizard_p3_option1_button)
    protected void onOption1Clicked() {
        if (listener != null) {
            listener.createActionWizardNextStep(STEP, 1);
        }
    }

}
