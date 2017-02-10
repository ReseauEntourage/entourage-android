package social.entourage.android.map.tour.information.members;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import social.entourage.android.api.model.Partner;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.BaseCardViewHolder;

import social.entourage.android.R;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.CropCircleTransformation;
import social.entourage.android.view.PartnerLogoImageView;

/**
 * Created by mihaiionescu on 23/05/16.
 */
public class MemberCardViewHolder extends BaseCardViewHolder {

    private int userId = 0;

    private ImageView mMemberPhoto;
    private PartnerLogoImageView mPartnerLogo;
    private TextView mMemberName;

    public MemberCardViewHolder(final View view) {
        super(view);
    }



    @Override
    protected void bindFields() {
        mMemberPhoto = (ImageView) itemView.findViewById(R.id.tic_member_photo);
        mPartnerLogo = (PartnerLogoImageView) itemView.findViewById(R.id.tic_member_partner_logo);
        mMemberName = (TextView) itemView.findViewById(R.id.tic_member_name);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (userId == 0) return;
                BusProvider.getInstance().post(new Events.OnUserViewRequestedEvent(userId));
            }
        });
    }

    @Override
    public void populate(final TimestampedObject data) {
        this.populate((TourUser)data);
    }

    public void populate(TourUser tourUser) {
        this.userId = tourUser.getUserId();
        String avatarURL = tourUser.getAvatarURLAsString();
        if (avatarURL != null) {
            Picasso.with(itemView.getContext()).load(Uri.parse(avatarURL))
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
                Picasso.with(itemView.getContext())
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
    }

    public static int getLayoutResource() {
        return R.layout.tour_information_member_card;
    }
}
