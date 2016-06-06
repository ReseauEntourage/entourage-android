package social.entourage.android.map.entourage;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Entourage;

public class EntourageDisclaimerFragment extends DialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage.android.entourage.disclaimer";

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @Bind(R.id.entourage_disclaimer_checkbox)
    CheckBox acceptCheckbox;

    private String entourageType;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    private OnFragmentInteractionListener mListener;

    public EntourageDisclaimerFragment() {
        // Required empty public constructor
    }

    public static EntourageDisclaimerFragment newInstance(String entourageType) {
        EntourageDisclaimerFragment fragment = new EntourageDisclaimerFragment();
        Bundle args = new Bundle();
        args.putString(CreateEntourageFragment.KEY_ENTOURAGE_TYPE, entourageType);
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
        if (acceptCheckbox.isChecked()) {
            //inform the listener that the user accepted the CGU
            mListener.onEntourageDisclaimerAccepted(this, entourageType);
        }
        else {
            Toast.makeText(getActivity(), R.string.entourage_disclaimer_error_notaccepted, Toast.LENGTH_SHORT).show();
        }
    }

    // ----------------------------------
    // Listener
    // ----------------------------------

    public interface OnFragmentInteractionListener {

        void onEntourageDisclaimerAccepted(EntourageDisclaimerFragment fragment, String entourageType);
    }
}
