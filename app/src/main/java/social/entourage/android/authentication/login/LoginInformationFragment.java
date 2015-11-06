package social.entourage.android.authentication.login;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;

public class LoginInformationFragment extends DialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final String FACEBOOK = "https://www.facebook.com/EntourageReseauCivique";
    private final String TWITTER = "https://twitter.com/R_Entour";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    LoginInformationPresenter presenter;

    @Bind(R.id.login_button_close_info)
    ImageButton closeButton;

    @Bind(R.id.login_button_facebook)
    ImageButton facebookButton;

    @Bind(R.id.login_button_twitter)
    ImageButton twitterButton;

    @Bind(R.id.login_edit_email_ask_more)
    EditText emailEditText;

    @Bind(R.id.login_button_newsletter)
    Button newsletterButton;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------


    public LoginInformationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View toReturn = inflater.inflate(R.layout.fragment_login_information, container, false);
        ButterKnife.bind(this, toReturn);
        return toReturn;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerLoginInformationComponent.builder()
                .entourageComponent(entourageComponent)
                .loginInformationModule(new LoginInformationModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof OnEntourageInformationFragmentFinish)) {
            throw new ClassCastException(activity.toString()  + " must implement OnEntourageInformationFragmentFinish");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentSlide;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void startLoader() {
        closeButton.setClickable(false);
        newsletterButton.setText(R.string.button_loading);
        newsletterButton.setEnabled(false);
    }

    public void resetNewsletterButton() {
        closeButton.setClickable(true);
        newsletterButton.setText(R.string.login_button_newsletter);
        newsletterButton.setEnabled(true);
    }

    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------

    @OnClick(R.id.login_button_close_info)
    void closeFragment() {
        getOnInformationFragmentFinish().closeEntourageInformationFragment();
    }

    @OnClick(R.id.login_button_facebook)
    void facebookButton() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOK));
        startActivity(browserIntent);
    }

    @OnClick(R.id.login_button_twitter)
    void twitterButton() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TWITTER));
        startActivity(browserIntent);
    }

    @OnClick(R.id.login_button_newsletter)
    void newsletterButton() {
        presenter.subscribeToNewsletter(emailEditText.getText().toString(), true);
        emailEditText.setText("");
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void newsletterResult(boolean success) {
        resetNewsletterButton();
        if (getActivity() != null) {
            if (success) {
                Toast.makeText(getActivity(), R.string.login_text_newsletter_success, Toast.LENGTH_SHORT).show();
                FlurryAgent.logEvent(Constants.EVENT_NEWSLETTER_INSCRIPTION_OK);
            } else {
                Toast.makeText(getActivity(), R.string.login_text_newsletter_fail, Toast.LENGTH_SHORT).show();
                FlurryAgent.logEvent(Constants.EVENT_NEWSLETTER_INSCRIPTION_FAILED);
            }
            getOnInformationFragmentFinish().closeEntourageInformationFragment();
        }
    }

    private OnEntourageInformationFragmentFinish getOnInformationFragmentFinish() {
        final Activity activity = getActivity();
        return activity != null ? (OnEntourageInformationFragmentFinish) activity : null;
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public interface OnEntourageInformationFragmentFinish {
        void closeEntourageInformationFragment();
    }
}
