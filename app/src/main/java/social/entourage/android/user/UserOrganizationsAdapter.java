package social.entourage.android.user;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import social.entourage.android.R;
import social.entourage.android.api.model.BaseOrganization;
import social.entourage.android.api.model.Partner;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

/**
 * Created by mihaiionescu on 24/03/16.
 */
public class UserOrganizationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class OrganizationViewHolder extends RecyclerView.ViewHolder {

        public TextView mOrganizationName;
        public ImageView mOrganizationLogo;
        public TextView mOrganizationType;
        public View mSeparator;

        public Partner partner;

        public OrganizationViewHolder(View v) {
            super(v);
            mOrganizationName = v.findViewById(R.id.organization_name);
            mOrganizationType = v.findViewById(R.id.organization_type);
            mOrganizationLogo = v.findViewById(R.id.organization_logo);
            mSeparator = v.findViewById(R.id.organization_separator);

            if (mOrganizationLogo != null) {
                mOrganizationLogo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        if (partner != null) {
                            BusProvider.INSTANCE.getInstance().post(new Events.OnPartnerViewRequestedEvent(partner));
                        }
                    }
                });
            }
        }

    }

    private List<BaseOrganization> organizationList;

    public UserOrganizationsAdapter(List<BaseOrganization> organizationList) {
        this.organizationList = organizationList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_profile_organization, parent, false);
        return new OrganizationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        BaseOrganization organization = organizationList.get(position);
        OrganizationViewHolder organizationViewHolder = (OrganizationViewHolder) holder;

        organizationViewHolder.mOrganizationName.setText(organization.getName());

        organizationViewHolder.mOrganizationType.setText(organization.getTypeAsString(organizationViewHolder.mOrganizationType.getContext()));

        String organizationLogo = organization.getLargeLogoUrl();
        if (organizationLogo != null) {
            Picasso.get()
                    .load(Uri.parse(organizationLogo))
                    .into(organizationViewHolder.mOrganizationLogo);
        }
        else {
            organizationViewHolder.mOrganizationLogo.setImageDrawable(null);
        }

        if (organization.getType() == BaseOrganization.TYPE_PARTNER) {
            organizationViewHolder.partner = (Partner)organization;
        }
        else {
            organizationViewHolder.partner = null;
        }

        organizationViewHolder.mSeparator.setVisibility(position == organizationList.size() - 1 ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        if (organizationList == null) return 0;
        return organizationList.size();
    }

    public void setOrganizationList(final List<BaseOrganization> organizationList) {
        this.organizationList = organizationList;
        notifyDataSetChanged();
    }
}
