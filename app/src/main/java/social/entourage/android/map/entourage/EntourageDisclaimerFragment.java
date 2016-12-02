package social.entourage.android.map.entourage;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.base.EntourageDialogFragment;

public class EntourageDisclaimerFragment extends EntourageDialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage.android.entourage.disclaimer";

    private static final String KEY_IS_PRO = "social.entourage.android.KEY_IS_PRO";

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @Bind(R.id.entourage_disclaimer_text)
    TextView disclaimerTextView;

    private String entourageType;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    private OnFragmentInteractionListener mListener;

    public EntourageDisclaimerFragment() {
        // Required empty public constructor
    }

    public static EntourageDisclaimerFragment newInstance(String entourageType, boolean isPro) {
        EntourageDisclaimerFragment fragment = new EntourageDisclaimerFragment();
        Bundle args = new Bundle();
        args.putString(CreateEntourageFragment.KEY_ENTOURAGE_TYPE, entourageType);
        args.putBoolean(EntourageDisclaimerFragment.KEY_IS_PRO, isPro);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_entourage_disclaimer, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            entourageType = args.getString(CreateEntourageFragment.KEY_ENTOURAGE_TYPE, Entourage.TYPE_CONTRIBUTION);
            boolean isPro = args.getBoolean(EntourageDisclaimerFragment.KEY_IS_PRO, false);

            disclaimerTextView.setMovementMethod(LinkMovementMethod.getInstance());
            String disclaimer = "";
            if (isPro) {
                disclaimer = getString(R.string.entourage_disclaimer_text, getString(R.string.disclaimer_link_pro));

            } else {
                disclaimer = getString(R.string.entourage_disclaimer_text, getString(R.string.disclaimer_link_public));
            }
            disclaimerTextView.setText(Html.fromHtml(disclaimer));
        }
    }

    @Override
    public void onAttach(Context context) {
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentSlide;
    }

    // ----------------------------------
    // Button handling
    // ----------------------------------

    @OnClick(R.id.entourage_disclaimer_close_button)
    protected void onCloseClicked() {
        dismiss();
    }

    @OnClick(R.id.entourage_disclaimer_ok_button)
    protected void onOkClicked() {
        //MI: No longer need the checkbox
        /*
        if (acceptCheckbox.isChecked()) {
            //inform the listener that the user accepted the CGU
            mListener.onEntourageDisclaimerAccepted(this, entourageType);
        }
        else {
            Toast.makeText(getActivity(), R.string.entourage_disclaimer_error_notaccepted, Toast.LENGTH_SHORT).show();
        }
        */

        //inform the listener that the user accepted the CGU
        mListener.onEntourageDisclaimerAccepted(this, entourageType);
    }

    // ----------------------------------
    // Listener
    // ----------------------------------

    public interface OnFragmentInteractionListener {

        void onEntourageDisclaimerAccepted(EntourageDisclaimerFragment fragment, String entourageType);
    }
}
