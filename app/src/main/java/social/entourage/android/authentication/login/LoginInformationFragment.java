package social.entourage.android.authentication.login;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;

public class LoginInformationFragment extends EntourageDialogFragment {

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

    @BindView(R.id.login_button_close_info)
    ImageButton closeButton;

    @BindView(R.id.login_button_facebook)
    ImageButton facebookButton;

    @BindView(R.id.login_button_twitter)
    ImageButton twitterButton;

    @BindView(R.id.login_edit_email_ask_more)
    EditText emailEditText;

    @BindView(R.id.login_button_newsletter)
    Button newsletterButton;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------


    public LoginInformationFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View toReturn = inflater.inflate(R.layout.fragment_login_information, container, false);
        ButterKnife.bind(this, toReturn);
        return toReturn;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof OnEntourageInformationFragmentFinish)) {
            throw new ClassCastException(activity.toString()  + " must implement OnEntourageInformationFragmentFinish");
        }
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
        OnEntourageInformationFragmentFinish fragmentFinish = getOnInformationFragmentFinish();
        if (fragmentFinish != null) {
            fragmentFinish.closeEntourageInformationFragment();
        }
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
                EntourageEvents.logEvent(EntourageEvents.EVENT_NEWSLETTER_INSCRIPTION_OK);
            } else {
                Toast.makeText(getActivity(), R.string.login_text_newsletter_fail, Toast.LENGTH_SHORT).show();
                EntourageEvents.logEvent(EntourageEvents.EVENT_NEWSLETTER_INSCRIPTION_FAILED);
            }
            OnEntourageInformationFragmentFinish fragmentFinish = getOnInformationFragmentFinish();
            if (fragmentFinish != null) {
                fragmentFinish.closeEntourageInformationFragment();
            }
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
