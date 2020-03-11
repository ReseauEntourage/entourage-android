package social.entourage.android.involvement;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.BuildConfig;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;

/**
 * Get Involved Menu Fragment
 */
public class GetInvolvedFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = GetInvolvedFragment.class.getSimpleName();

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.get_involved_version)
    TextView versionTextView;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public GetInvolvedFragment() {
        // Required empty public constructor
    }

    public static GetInvolvedFragment newInstance() {
        return new GetInvolvedFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_get_involved, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populate();
    }

    private void populate() {
        versionTextView.setText(getString(R.string.about_version_format, if (BuildConfig.DEBUG) BuildConfig.VERSION_DEBUG_NAME else BuildConfig.VERSION_NAME));
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseButton() {
        dismiss();
    }

    @OnClick(R.id.get_involved_rate_us_layout)
    protected void onRateUsClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ABOUT_RATING);

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

    @OnClick(R.id.get_involved_facebook_layout)
    protected void onFacebookClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ABOUT_FACEBOOK);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.facebook_url)));
        try {
            startActivity(browserIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getContext(), R.string.no_browser_error, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.get_involved_instagram_layout)
    protected void onInstaClicked() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.instagram_url)));
        try {
            startActivity(browserIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getContext(), R.string.no_browser_error, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.get_involved_twitter_layout)
    protected void onTwitterClicked() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.twitter_url)));
        try {
            startActivity(browserIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getContext(), R.string.no_browser_error, Toast.LENGTH_SHORT).show();
        }
    }

}
