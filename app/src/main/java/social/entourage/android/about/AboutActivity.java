package social.entourage.android.about;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.BuildConfig;
import social.entourage.android.R;

public class AboutActivity extends AppCompatActivity {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final String RATE_URL = "";
    private static final String FACEBOOK_URL = "http://facebook.com";
    private static final String TERMS_URL = "";
    private static final String WEBSITE_URL = "";
    private static final String EMAIL_TO = "contact@entourage.social";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Bind(R.id.about_version)
    TextView versionTextView;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        populate();
    }

    private void populate() {
        versionTextView.setText(BuildConfig.VERSION_NAME);
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------

    @OnClick(R.id.about_close_button)
    protected void onCloseButton() {
        finish();
    }

    @OnClick(R.id.about_rate_us_layout)
    protected void onRateUsClicked() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(RATE_URL));
        startActivity(browserIntent);
    }

    @OnClick(R.id.about_facebook_layout)
    protected void onFacebookClicked() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOK_URL));
        startActivity(browserIntent);
    }

    @OnClick(R.id.about_conditions_layout)
    protected void onTermsClicked() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_URL));
        startActivity(browserIntent);
    }

    @OnClick(R.id.about_website_layout)
    protected void onWebsiteClicked() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL));
        startActivity(browserIntent);
    }

    @OnClick(R.id.about_email_layout)
    protected void onEmailClicked() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        String[] addresses = {EMAIL_TO};
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
