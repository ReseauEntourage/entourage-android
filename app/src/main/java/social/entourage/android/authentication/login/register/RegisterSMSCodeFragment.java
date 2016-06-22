package social.entourage.android.authentication.login.register;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
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

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public RegisterSMSCodeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_smscode, container, false);
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

    @OnClick(R.id.register_smscode_back_button)
    protected void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.register_smscode_validate_button)
    protected void onValidateClicked() {

    }
}
