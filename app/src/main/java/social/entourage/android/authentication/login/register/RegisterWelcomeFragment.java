package social.entourage.android.authentication.login.register;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.base.EntourageLinkMovementMethod;
import social.entourage.android.tools.Utils;
import timber.log.Timber;

import static social.entourage.android.EntourageApplication.isPfpApp;


public class RegisterWelcomeFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.RegisterWelcome";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private OnRegisterUserListener mListener;

    @BindView(R.id.register_welcome_privacy)
    TextView privacyTextView;

    @BindView(R.id.register_welcome_logo)
    ImageView logoImageView;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public RegisterWelcomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_welcome, container, false);
        ButterKnife.bind(this, view);

        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_30_1);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialiseView();
    }

    @Override
    public void onAttach(@NonNull Context context) {
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
        EntourageEvents.logEvent(EntourageEvents.EVENT_WELCOME_CONTINUE);
        if (mListener.registerStart()) {
            try {
                RegisterNumberFragment registerNumberFragment = new RegisterNumberFragment();
                registerNumberFragment.show(getFragmentManager(), RegisterNumberFragment.TAG);
            } catch(IllegalStateException e) {
                Timber.w(e);
            }
        } else {
            dismiss();
        }
    }

    // ----------------------------------
    // Private Methods
    // ----------------------------------

    private void initialiseView() {

        if (getActivity() != null && getActivity() instanceof EntourageActivity) {
            String termsLink = ((EntourageActivity)getActivity()).getLink(Constants.TERMS_LINK_ID);
            String privacyLink = ((EntourageActivity)getActivity()).getLink(Constants.PRIVACY_LINK_ID);
            String text = getString(R.string.registration_welcome_privacy, termsLink, privacyLink);
            privacyTextView.setText(Utils.fromHtml(text));
        }

        if (isPfpApp()) {
            logoImageView.setVisibility(View.INVISIBLE);
        }

        privacyTextView.setMovementMethod(EntourageLinkMovementMethod.getInstance());

    }

}
