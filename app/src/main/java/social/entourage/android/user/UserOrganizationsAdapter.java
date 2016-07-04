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
import social.entourage.android.api.model.Organization;

/**
 * Created by mihaiionescu on 24/03/16.
 */
public class UserOrganizationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class OrganizationViewHolder extends RecyclerView.ViewHolder {

        public TextView mOrganizationName;
        public ImageView mOrganizationLogo;

        public OrganizationViewHolder(View v) {
            super(v);
            mOrganizationName = (TextView) v.findViewById(R.id.organization_name);
            mOrganizationLogo = (ImageView) v.findViewById(R.id.organization_logo);
        }

    }

    private List<Organization> organizationList;

    public UserOrganizationsAdapter(List<Organization> organizationList) {
        this.organizationList = organizationList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_profile_organization, parent, false);
        return new OrganizationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Organization organization = organizationList.get(position);
        OrganizationViewHolder organizationViewHolder = (OrganizationViewHolder) holder;
        organizationViewHolder.mOrganizationName.setText(organization.getName());

        String organizationLogo = organization.getLogoUrl();
        if (organizationLogo != null) {
            Picasso.with(holder.itemView.getContext())
                    .load(Uri.parse(organizationLogo))
                    .into(organizationViewHolder.mOrganizationLogo);
        }
    }

    @Override
    public int getItemCount() {
        if (organizationList == null) return 0;
        return organizationList.size();
    }
}
