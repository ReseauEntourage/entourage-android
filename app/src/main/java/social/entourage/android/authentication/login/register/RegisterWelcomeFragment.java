package social.entourage.android.authentication.login.register;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;


public class RegisterWelcomeFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.RegisterWelcome";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private OnRegisterUserListener mListener;

    @BindView(R.id.register_welcome_description)
    TextView descriptionTextView;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public RegisterWelcomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_welcome, container, false);
        ButterKnife.bind(this, view);

        FlurryAgent.logEvent(Constants.EVENT_SCREEN_30_1);

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialiseView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRegisterUserListener) {
            mListener = (OnRegisterUserListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRegisterUserListener");
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

    @OnClick(R.id.register_welcome_back_button)
    protected void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.register_welcome_signin_button)
    protected void onSigninClicked() {
        mListener.registerShowSignIn();
        dismiss();
    }

    @OnClick(R.id.register_welcome_start_button)
    protected void onStartClicked() {
        RegisterNumberFragment registerNumberFragment = new RegisterNumberFragment();
        registerNumberFragment.show(getFragmentManager(), RegisterNumberFragment.TAG);
    }

    // ----------------------------------
    // Private Methods
    // ----------------------------------

    private void initialiseView() {

        descriptionTextView.setMovementMethod(LinkMovementMethod.getInstance());

    }

}
