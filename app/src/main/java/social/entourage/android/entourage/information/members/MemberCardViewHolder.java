package social.entourage.android.entourage.information.members;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import social.entourage.android.api.model.Partner;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.BaseCardViewHolder;

import social.entourage.android.R;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.CropCircleTransformation;
import social.entourage.android.user.role.RoleView;
import social.entourage.android.user.role.UserRole;
import social.entourage.android.user.role.UserRolesFactory;
import social.entourage.android.view.PartnerLogoImageView;

import java.util.ArrayList;

/**
 * Created by mihaiionescu on 23/05/16.
 */
public class MemberCardViewHolder extends BaseCardViewHolder {

    private int userId = 0;

    private ImageView mMemberPhoto;
    private PartnerLogoImageView mPartnerLogo;
    private TextView mMemberName;
    private LinearLayout mMemberTags;

    public MemberCardViewHolder(final View view) {
        super(view);
    }



    @Override
    protected void bindFields() {
        mMemberPhoto = itemView.findViewById(R.id.tic_member_photo);
        mPartnerLogo = itemView.findViewById(R.id.tic_member_partner_logo);
        mMemberName = itemView.findViewById(R.id.tic_member_name);
        mMemberTags = itemView.findViewById(R.id.tic_member_tags);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (userId == 0) return;
                BusProvider.INSTANCE.getInstance().post(new Events.OnUserViewRequestedEvent(userId));
            }
        });
    }

    @Override
    public void populate(@NotNull final TimestampedObject data) {
        this.populate((TourUser)data);
    }

    public void populate(TourUser tourUser) {
        this.userId = tourUser.getUserId();
        String avatarURL = tourUser.getAvatarURLAsString();
        if (avatarURL != null) {
            Picasso.get().load(Uri.parse(avatarURL))
                    .placeholder(R.drawable.ic_user_photo_small)
                    .transform(new CropCircleTransformation())
                    .into(mMemberPhoto);
        } else {
            mMemberPhoto.setImageResource(R.drawable.ic_user_photo_small);
        }
        // Partner logo
        Partner partner = tourUser.getPartner();
        if (partner != null) {
            String partnerLogoURL = partner.getSmallLogoUrl();
            if (partnerLogoURL != null) {
                Picasso.get()
                        .load(Uri.parse(partnerLogoURL))
                        .placeholder(R.drawable.partner_placeholder)
                        .transform(new CropCircleTransformation())
                        .into(mPartnerLogo);
            }
            else {
                mPartnerLogo.setImageDrawable(null);
            }
        } else {
            mPartnerLogo.setImageDrawable(null);
        }

        mMemberName.setText(tourUser.getDisplayName());

        ArrayList<String> roles = new ArrayList<>();
        if (tourUser.getGroupRole() != null) {
            roles.add(tourUser.getGroupRole());
        }
        for (String role: tourUser.getCommunityRoles()) {
            if (!role.equals(tourUser.getGroupRole())) {
                roles.add(role);
            }
        }

        mMemberTags.removeAllViews();
        for (String role: roles) {
            UserRole userRole = UserRolesFactory.INSTANCE.findByName(role);
            if (userRole == null || !userRole.isVisible()) {
                continue;
            }
            RoleView roleView = new RoleView(itemView.getContext());
            roleView.setRole(userRole);
            mMemberTags.addView(roleView);
        }
    }

    public static int getLayoutResource() {
        return R.layout.tour_information_member_card;
    }
}
