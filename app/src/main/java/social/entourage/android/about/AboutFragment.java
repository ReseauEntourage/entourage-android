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
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.carousel.CarouselFragment;

public class AboutFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = AboutFragment.class.getSimpleName();

    private static final String TERMS_URL = "https://www.entourage.social/cgu/index.html";
    private static final String WEBSITE_URL = "https://www.entourage.social";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

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

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseButton() {
        dismiss();
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

    @OnClick(R.id.faq_website_layout)
    protected void onFAQClicked() {
        if (getActivity() != null && getActivity() instanceof DrawerActivity) {
            EntourageEvents.logEvent(Constants.EVENT_MENU_FAQ);
            DrawerActivity drawerActivity = (DrawerActivity) getActivity();
            Intent userGuideIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(drawerActivity.getLink(Constants.FAQ_LINK_ID)));
            try {
                startActivity(userGuideIntent);
            } catch (Exception ex) {
                Toast.makeText(getContext(), R.string.no_browser_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.about_tutorial_layout)
    protected void onTutorialClicked() {
        CarouselFragment carouselFragment = new CarouselFragment();
        carouselFragment.show(getFragmentManager(), CarouselFragment.TAG);
    }
}
