package social.entourage.android.map.entourage.my.filter;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.R;

/**
 * MyEntourages Filter Fragment
 */
public class MyEntouragesFilterFragment extends EntourageDialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage_android.MyEntouragesFilterFragment";

    // ----------------------------------
    // Attributes
    // ----------------------------------

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public MyEntouragesFilterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_my_entourages_filter, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------

    @OnClick(R.id.myentourages_filter_back_button)
    void onBackClicked() {
        dismiss();
    }

}
