package social.entourage.android.involvement;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
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

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public GetInvolvedFragment() {
        // Required empty public constructor
    }

    public static GetInvolvedFragment newInstance() {
        GetInvolvedFragment fragment = new GetInvolvedFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_get_involved, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseButton() {
        dismiss();
    }

}
