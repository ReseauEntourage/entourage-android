package social.entourage.android.map.encounter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;

public class EncounterDisclaimerFragment extends DialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage.android.entourage.prodisclaimer";

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @BindView(R.id.encounter_disclaimer_checkbox)
    CheckBox acceptCheckbox;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    private OnFragmentInteractionListener mListener;

    public EncounterDisclaimerFragment() {
        // Required empty public constructor
    }

    public static EncounterDisclaimerFragment newInstance() {
        return new EncounterDisclaimerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_encounter_disclaimer, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getDialog() != null && getDialog().getWindow() != null && getDialog().getWindow().getAttributes() != null) {
            getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentSlide;
        }
    }

    // ----------------------------------
    // Button handling
    // ----------------------------------

    @OnClick(R.id.encounter_disclaimer_close_button)
    protected void onCloseClicked() {
        dismiss();
    }

    @OnClick(R.id.encounter_disclaimer_ok_button)
    protected void onOkClicked() {
        if (acceptCheckbox.isChecked()) {
            //inform the listener that the user accepted the CGU
            mListener.onEncounterDisclaimerAccepted(this);
        }
        else {
            Toast.makeText(getActivity(), R.string.encounter_disclaimer_error_notaccepted, Toast.LENGTH_SHORT).show();
        }
    }

    // ----------------------------------
    // Listener
    // ----------------------------------

    public interface OnFragmentInteractionListener {

        void onEncounterDisclaimerAccepted(EncounterDisclaimerFragment fragment);
    }
}
