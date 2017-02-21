package social.entourage.android.partner;


import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.tools.CropCircleTransformation;
import social.entourage.android.view.PartnerLogoImageView;

/**
 * Fragment that displays the details of a partner organisation
 */
public class PartnerFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.partner_fragment";

    private static final String KEY_PARTNER_ID = "social.entourage.android.partner_id";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.partner_view_logo)
    PartnerLogoImageView logoImageView;

    private long partnerId;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public PartnerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param partnerId Partner ID.
     * @return A new instance of fragment PartnerFragment.
     */
    public static PartnerFragment newInstance(long partnerId) {
        PartnerFragment fragment = new PartnerFragment();
        Bundle args = new Bundle();
        args.putLong(KEY_PARTNER_ID, partnerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            partnerId = getArguments().getLong(KEY_PARTNER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_partner, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        configureView();
    }

    @OnClick(R.id.partner_view_close_button)
    void onCloseClicked() {
        dismiss();
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void configureView() {
        // Check for valid activity
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        // For the moment populate with hardcoded values related to ATD
        /*
        Picasso.with(getContext())
                .load(Uri.parse("https://s3-eu-west-1.amazonaws.com/entourage-ressources/atd-large.png"))
                .placeholder(R.drawable.partner_placeholder)
                .transform(new CropCircleTransformation())
                .into(logoImageView);
                */
    }

}
