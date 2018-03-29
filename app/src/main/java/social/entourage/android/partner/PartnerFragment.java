package social.entourage.android.partner;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.api.model.Partner;
import social.entourage.android.base.EntourageDialogFragment;

/**
 * Fragment that displays the details of a partner organisation
 */
public class PartnerFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.partner_fragment";

    private static final String KEY_PARTNER_ID = "social.entourage.android.partner_id";
    private static final String KEY_PARTNER = "social.entourage.android.partner";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.partner_view_logo)
    ImageView partnerLogoImageView;

    @BindView(R.id.partner_view_name)
    TextView partnerName;

    @BindView(R.id.partner_view_details)
    TextView partnerDescription;

    @BindView(R.id.partner_view_phone_layout)
    View partnerPhoneLayout;

    @BindView(R.id.partner_view_phone)
    TextView partnerPhone;

    @BindView(R.id.partner_view_address_layout)
    View partnerAddressLayout;

    @BindView(R.id.partner_view_address)
    TextView partnerAddress;

    @BindView(R.id.partner_view_website_layout)
    View partnerWebsiteLayout;

    @BindView(R.id.partner_view_website)
    TextView partnerWebsite;

    @BindView(R.id.partner_view_email_layout)
    View partnerEmailLayout;

    @BindView(R.id.partner_view_email)
    TextView partnerEmail;

    private long partnerId;

    private Partner partner;

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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param partner Partner object.
     * @return A new instance of fragment PartnerFragment.
     */
    public static PartnerFragment newInstance(Partner partner) {
        PartnerFragment fragment = new PartnerFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_PARTNER, partner);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            partner = (Partner)getArguments().getSerializable(KEY_PARTNER);
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

        if (partner != null) {
            configureView();
        }
    }

    @OnClick(R.id.title_close_button)
    void onCloseClicked() {
        dismiss();
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void configureView() {
        // Check for valid activity
        if (getActivity() == null || getActivity().isFinishing()) {
            Log.i("PartnerFragment", "No activity for this View");
            return;
        }

        // url
        Picasso.with(getContext())
                .load(Uri.parse(partner.getLargeLogoUrl()))
                .placeholder(R.drawable.partner_placeholder)
                .into(partnerLogoImageView);
        // name
        partnerName.setText(partner.getName());
        // description
        partnerDescription.setText(partner.getDescription());
        // phone
        String phone = partner.getPhone();
        if (phone == null || phone.length() == 0) {
            partnerPhoneLayout.setVisibility(View.GONE);
        } else {
            partnerPhoneLayout.setVisibility(View.VISIBLE);
            partnerPhone.setText(phone);
        }
        // address
        String address = partner.getAddress();
        if (address == null || address.length() == 0) {
            partnerAddressLayout.setVisibility(View.GONE);
        } else {
            partnerAddressLayout.setVisibility(View.VISIBLE);
            partnerAddress.setText(address);
        }
        // website
        String website = partner.getWebsiteUrl();
        if (website == null || website.length() == 0) {
            partnerWebsiteLayout.setVisibility(View.GONE);
        } else {
            partnerWebsiteLayout.setVisibility(View.VISIBLE);
            partnerWebsite.setText(website);
        }
        // email
        String email = partner.getEmail();
        if (email == null || email.length() == 0) {
            partnerEmailLayout.setVisibility(View.GONE);
        } else {
            partnerEmailLayout.setVisibility(View.VISIBLE);
            partnerEmail.setText(email);
        }
    }

}
