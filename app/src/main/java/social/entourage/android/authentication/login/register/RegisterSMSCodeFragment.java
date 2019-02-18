package social.entourage.android.authentication.login.register;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;

public class RegisterSMSCodeFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.RegisterSMSCode";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private OnRegisterUserListener mListener;

    @BindView(R.id.register_smscode_code)
    EditText codeEditText;

    @BindView(R.id.register_smscode_email)
    TextView emailTextView;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public RegisterSMSCodeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_smscode, container, false);
        ButterKnife.bind(this, view);
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_30_3);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnRegisterUserListener) {
            mListener = (OnRegisterUserListener) context;
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

    // ----------------------------------
    // Click handlers
    // ----------------------------------

    @OnClick(R.id.register_smscode_back_button)
    protected void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.register_smscode_validate_button)
    protected void onValidateClicked() {
        if (!checkValidLocalSMSCode()) {
            Toast.makeText(getActivity(), R.string.registration_smscode_error_code, Toast.LENGTH_SHORT).show();
        } else {
            mListener.registerCheckCode(codeEditText.getText().toString());
        }
    }

    @OnClick(R.id.register_smscode_lost_code)
    protected void onLostCodeClicked() {
        // Resend the code
        mListener.registerResendCode();
    }

    @OnClick(R.id.register_smscode_description)
    protected void onResendByEmailViewClicked() {
        emailTextView.setVisibility(View.VISIBLE);
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private boolean checkValidLocalSMSCode() {
        String code = codeEditText.getText().toString();
        return code.trim().length() != 0;
    }

}
