package social.entourage.android.authentication.login.register;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.authentication.login.LoginPresenter;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.R;


public class RegisterNumberFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.RegisterNumber";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Bind(R.id.register_number_phone_number)
    EditText phoneNumberEditText;

    private OnRegisterUserListener mListener;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public RegisterNumberFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_number, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onAttach(Context context) {
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

    @OnClick(R.id.register_number_back_button)
    protected void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.register_number_next_button)
    protected void onNextClicked() {
        // Check the phone
        String phoneNumber = LoginPresenter.checkPhoneNumberFormat(phoneNumberEditText.getText().toString());
        if (phoneNumber == null) {
            Toast.makeText(getActivity(), R.string.login_text_invalid_format, Toast.LENGTH_SHORT).show();
        }
        else {
            // Save the phone
            mListener.registerSavePhoneNumber(phoneNumber);
        }
    }

    @OnClick(R.id.register_number_lost_code)
    protected void onLostCodeClicked() {
        // Check the phone
        String phoneNumber = LoginPresenter.checkPhoneNumberFormat(phoneNumberEditText.getText().toString());
        if (phoneNumber == null) {
            Toast.makeText(getActivity(), R.string.login_text_invalid_format, Toast.LENGTH_SHORT).show();
        }
        else {
            // Resend the code
            mListener.registerResendCode(phoneNumber);
        }
    }

}
