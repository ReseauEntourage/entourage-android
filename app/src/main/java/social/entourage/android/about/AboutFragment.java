package social.entourage.android.about;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.BuildConfig;
import social.entourage.android.Constants;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;

public class AboutFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = AboutFragment.class.getSimpleName();

    private static final String RATE_URL = "market://details?id=";
    private static final String FACEBOOK_URL = "https://www.facebook.com/EntourageReseauCivique";
    private static final String TERMS_URL = "https://www.entourage.social/cgu/index.html";
    private static final String WEBSITE_URL = "https://www.entourage.social";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.about_version)
    TextView versionTextView;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View toReturn = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, toReturn);

        return toReturn;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populate();
    }

    private void populate() {
        versionTextView.setText(getString(R.string.about_version_format, BuildConfig.VERSION_NAME));
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------

    @OnClick(R.id.about_close_button)
    protected void onCloseButton() {
        dismiss();
    }

    @OnClick(R.id.about_rate_us_layout)
    protected void onRateUsClicked() {
        EntourageEvents.logEvent(Constants.EVENT_ABOUT_RATING);

        Uri uri = Uri.parse("market://details?id=" + this.getActivity().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + this.getActivity().getPackageName())));
        }
    }

    @OnClick(R.id.about_facebook_layout)
    protected void onFacebookClicked() {
        EntourageEvents.logEvent(Constants.EVENT_ABOUT_FACEBOOK);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOK_URL));
        try {
            startActivity(browserIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getContext(), R.string.no_browser_error, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.about_conditions_layout)
    protected void onTermsClicked() {
        EntourageEvents.logEvent(Constants.EVENT_ABOUT_CGU);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_URL));
        try {
            startActivity(browserIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getContext(), R.string.no_browser_error, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.about_website_layout)
    protected void onWebsiteClicked() {
        EntourageEvents.logEvent(Constants.EVENT_ABOUT_WEBSITE);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL));
        try {
            startActivity(browserIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getContext(), R.string.no_browser_error, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.about_email_layout)
    protected void onEmailClicked() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        String[] addresses = {Constants.EMAIL_CONTACT};
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), R.string.error_no_email, Toast.LENGTH_SHORT).show();
        }
    }
}
