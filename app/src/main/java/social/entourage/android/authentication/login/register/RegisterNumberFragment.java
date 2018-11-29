package social.entourage.android.authentication.login.register;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import social.entourage.android.EntourageEvents;
import social.entourage.android.view.CountryCodePicker.CountryCodePicker;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.tools.Utils;


public class RegisterNumberFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.RegisterNumber";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.register_number_ccp)
    CountryCodePicker countryCodePicker;

    @BindView(R.id.register_number_phone_number)
    EditText phoneNumberEditText;

    @BindView(R.id.register_number_next_button)
    Button nextButton;

    private OnRegisterUserListener mListener;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public RegisterNumberFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_number, container, false);
        ButterKnife.bind(this, view);
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_30_2);

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
        String phoneNumber = Utils.checkPhoneNumberFormat(countryCodePicker.getSelectedCountryCodeWithPlus(), phoneNumberEditText.getText().toString());
        if (phoneNumber == null) {
            Toast.makeText(getActivity(), R.string.login_text_invalid_format, Toast.LENGTH_SHORT).show();
        }
        else {
            nextButton.setEnabled(false);
            // Save the phone
            mListener.registerSavePhoneNumber(phoneNumber);
        }
    }

    public void savedPhoneNumber(boolean success) {
        nextButton.setEnabled(true);
    }

}
