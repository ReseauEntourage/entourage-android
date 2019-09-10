package social.entourage.android.entourage;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.base.EntourageDialogFragment;

public class EntourageDisclaimerFragment extends EntourageDialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage.android.entourage.disclaimer";

    private static final String KEY_GROUP_TYPE = "social.entourage.android.KEY_GROUP_TYPE";

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @BindView(R.id.entourage_disclaimer_text_chart)
    TextView disclaimerTextView;

    @BindView(R.id.entourage_disclaimer_switch)
    SwitchCompat disclaimerSwitch;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    private OnFragmentInteractionListener mListener;

    public EntourageDisclaimerFragment() {
        // Required empty public constructor
    }

    public static EntourageDisclaimerFragment newInstance(String groupType) {
        EntourageDisclaimerFragment fragment = new EntourageDisclaimerFragment();
        Bundle args = new Bundle();
        args.putString(KEY_GROUP_TYPE, groupType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        String groupType = null;
        if (getArguments() != null) {
            groupType = getArguments().getString(KEY_GROUP_TYPE, null);
        }
        View view = inflater.inflate(
                Entourage.TYPE_OUTING.equalsIgnoreCase(groupType) ? R.layout.fragment_outing_disclaimer : R.layout.fragment_entourage_disclaimer,
                container,
                false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        disclaimerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_DISCLAIMER_LINK);
                String disclaimerURL = getString(R.string.disclaimer_link_public);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(disclaimerURL));
                try {
                    startActivity(browserIntent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), R.string.no_browser_error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        disclaimerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_DISCLAIMER_ACCEPT);
                    // trigger the accept after a delay
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(EntourageDisclaimerFragment.this::onOkClicked, 1000);
                }
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    protected int getSlideStyle() {
        return R.style.CustomDialogFragmentSlide;
    }

    // ----------------------------------
    // Button handling
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_DISCLAIMER_CLOSE);
        dismiss();
    }

    @OnClick(R.id.entourage_disclaimer_ok_button)
    protected void onOkClicked() {
        if (disclaimerSwitch.isChecked()) {
            //inform the listener that the user accepted the CGU
            if (mListener != null) {
                mListener.onEntourageDisclaimerAccepted(this);
            }
        } else {
            Toast.makeText(getActivity(), R.string.entourage_disclaimer_error_notaccepted, Toast.LENGTH_SHORT).show();
        }
    }

    // ----------------------------------
    // Listener
    // ----------------------------------

    public interface OnFragmentInteractionListener {

        void onEntourageDisclaimerAccepted(EntourageDisclaimerFragment fragment);
    }
}
