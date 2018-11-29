package social.entourage.android.map.entourage;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

/**
 * Offers options when closing an entourage
 * Use the {@link EntourageCloseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EntourageCloseFragment extends DialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = EntourageCloseFragment.class.getSimpleName();

    // ----------------------------------
    // Attributes
    // ----------------------------------

    private FeedItem feedItem;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------



    public EntourageCloseFragment() {
        // Required empty public constructor
    }

    public static EntourageCloseFragment newInstance(FeedItem feedItem) {
        EntourageCloseFragment fragment = new EntourageCloseFragment();
        Bundle args = new Bundle();
        args.putSerializable(FeedItem.KEY_FEEDITEM, feedItem);
        fragment.setArguments(args);
        return fragment;
    }

    public void show(FragmentManager fragmentManager, String tag, Context context) {
        show(fragmentManager, tag);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            feedItem = (FeedItem)getArguments().getSerializable(FeedItem.KEY_FEEDITEM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_entourage_close, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getDialog() != null && getDialog().getWindow() != null && getDialog().getWindow().getAttributes() != null) {
            getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentSlide;
        }
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------

    @OnClick({R.id.entourage_close_close_button, R.id.entourage_close_cancel_button})
    protected void onCloseClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_CLOSE_POPUP_CANCEL);
        dismiss();
    }

    @OnClick(R.id.entourage_close_success_button)
    protected void onSuccessClicked() {
        BusProvider.getInstance().post(new Events.OnFeedItemCloseRequestEvent(feedItem, false, true));
        showEmail(R.string.entourage_close_email_title_success);
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_CLOSE_POPUP_SUCCESS);
        dismiss();
    }

    @OnClick(R.id.entourage_close_failed_button)
    protected void onFailedClicked() {
        BusProvider.getInstance().post(new Events.OnFeedItemCloseRequestEvent(feedItem, false, false));
        showEmail(R.string.entourage_close_email_title_failed);
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_CLOSE_POPUP_FAILURE);
        dismiss();
    }

    @OnClick(R.id.entourage_close_help_button)
    protected void onHelpClicked() {
        showEmail(R.string.entourage_close_email_title_help);
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_CLOSE_POPUP_HELP);
        dismiss();
    }

    // ----------------------------------
    // INNER METHODS
    // ----------------------------------

    private boolean showEmail(@StringRes int emailSubjectFormat) {
        if (feedItem == null) return false;
        // Build the email intent
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        // Set the email to
        String[] addresses = {getString(R.string.contact_email)};
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        // Set the subject
        String title = feedItem.getTitle();
        if (title == null) title = "";
        String emailSubject = getString(emailSubjectFormat, title);
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Start the intent
            startActivity(intent);
        } else {
            // No Email clients
            Toast.makeText(getContext(), R.string.error_no_email, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

}
