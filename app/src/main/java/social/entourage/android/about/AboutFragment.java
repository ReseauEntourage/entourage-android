package social.entourage.android.about;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.snackbar.Snackbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import social.entourage.android.BuildConfig;
import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.view.EntourageSnackbar;

public class AboutFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = AboutFragment.class.getSimpleName();

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Nullable
    @BindView(R.id.about_version)
    TextView versionTextView;

    @BindView(R.id.about_logo)
    ImageView logoView;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View toReturn = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, toReturn);

        return toReturn;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populate();
    }

    private void populate() {
        if (versionTextView != null) {
            versionTextView.setText(getString(R.string.about_version_format, BuildConfig.VERSION_NAME));
        }
        logoView.setOnLongClickListener(this::handleLongPress);
    }

    private boolean handleLongPress(View view) {
        if(EntourageApplication.get().clearFeedStorage() && getView()!=null){
            EntourageSnackbar.INSTANCE.make(getView(), R.string.about_clearing_entourage_cache, Snackbar.LENGTH_SHORT).show();
        }
        return true;
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseButton() {
        dismiss();
    }

    @Optional
    @OnClick(R.id.about_version_layout)
    protected void onVersionClicked() {
        Uri uri = Uri.parse(getString(R.string.market_url, this.getActivity().getPackageName()));
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
                    Uri.parse(getString(R.string.playstore_url,this.getActivity().getPackageName()))));
        }
    }

    @Optional
    @OnClick(R.id.about_conditions_layout)
    protected void onTermsClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ABOUT_CGU);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_url)));
        try {
            startActivity(browserIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getContext(), R.string.no_browser_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Optional
    @OnClick(R.id.about_website_layout)
    protected void onWebsiteClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ABOUT_WEBSITE);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_url)));
        try {
            startActivity(browserIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getContext(), R.string.no_browser_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Optional
    @OnClick(R.id.about_email_layout)
    protected void onEmailClicked() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        String[] addresses = {getString(R.string.contact_email)};
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), R.string.error_no_email, Toast.LENGTH_SHORT).show();
        }
    }

    @Optional
    @OnClick(R.id.about_oss_licenses)
    protected void onOSSLicensesClicked() {
        OssLicensesMenuActivity.setActivityTitle(getString(R.string.about_oss_licenses));
        startActivity(new Intent(getContext(), OssLicensesMenuActivity.class));
    }

    @Optional
    @OnClick(R.id.faq_website_layout)
    protected void onFAQClicked() {
        if (getActivity() != null && getActivity() instanceof DrawerActivity) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_FAQ);
            DrawerActivity drawerActivity = (DrawerActivity) getActivity();
            drawerActivity.showWebViewForLinkId(Constants.FAQ_LINK_ID);
        }
    }

    @Optional
    @OnClick(R.id.about_tutorial_layout)
    protected void onTutorialClicked() {
        if (getActivity() != null && getActivity() instanceof DrawerActivity) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ABOUT_TUTORIAL);
            DrawerActivity drawerActivity = (DrawerActivity) getActivity();
            drawerActivity.showTutorial(true);
        }
    }

    @Optional
    @OnClick(R.id.about_privacy_layout)
    protected void onPrivacyClicked() {
        if (getActivity() != null && getActivity() instanceof DrawerActivity) {
            DrawerActivity drawerActivity = (DrawerActivity) getActivity();
            drawerActivity.showWebViewForLinkId(Constants.PRIVACY_LINK_ID);
        }
    }

    @Optional
    @OnClick(R.id.about_suggestion_layout)
    protected void onSuggestionClicked() {
        if (getActivity() != null && getActivity() instanceof DrawerActivity) {
            DrawerActivity drawerActivity = (DrawerActivity) getActivity();
            drawerActivity.showWebView(drawerActivity.getLink(Constants.SUGGESTION_ID));
        }
    }

    @Optional
    @OnClick(R.id.about_feedback_layout)
    protected void onFeedbackClicked() {
        if (getActivity() != null && getActivity() instanceof DrawerActivity) {
            DrawerActivity drawerActivity = (DrawerActivity) getActivity();
            drawerActivity.showWebView(drawerActivity.getLink(Constants.FEEDBACK_ID));
        }
    }
}
