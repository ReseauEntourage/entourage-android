package social.entourage.android.user;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import social.entourage.android.R;
import social.entourage.android.api.model.BaseOrganization;

/**
 * Created by mihaiionescu on 24/03/16.
 */
public class UserOrganizationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class OrganizationViewHolder extends RecyclerView.ViewHolder {

        public TextView mOrganizationName;
        public ImageView mOrganizationLogo;
        public TextView mOrganizationType;
        public View mSeparator;

        public OrganizationViewHolder(View v) {
            super(v);
            mOrganizationName = (TextView) v.findViewById(R.id.organization_name);
            mOrganizationType = (TextView) v.findViewById(R.id.organization_type);
            mOrganizationLogo = (ImageView) v.findViewById(R.id.organization_logo);
            mSeparator = v.findViewById(R.id.organization_separator);
        }

    }

    private List<BaseOrganization> organizationList;

    public UserOrganizationsAdapter(List<BaseOrganization> organizationList) {
        this.organizationList = organizationList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_profile_organization, parent, false);
        return new OrganizationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        BaseOrganization organization = organizationList.get(position);
        OrganizationViewHolder organizationViewHolder = (OrganizationViewHolder) holder;

        organizationViewHolder.mOrganizationName.setText(organization.getName());

        organizationViewHolder.mOrganizationType.setText(organization.getTypeAsResourceId());

        String organizationLogo = organization.getLargeLogoUrl();
        if (organizationLogo != null) {
            Picasso.with(holder.itemView.getContext())
                    .load(Uri.parse(organizationLogo))
                    .placeholder(null)
                    .into(organizationViewHolder.mOrganizationLogo);
        }
        else {
            organizationViewHolder.mOrganizationLogo.setImageDrawable(null);
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
