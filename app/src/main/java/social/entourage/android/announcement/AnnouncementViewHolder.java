package social.entourage.android.announcement;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import social.entourage.android.R;
import social.entourage.android.api.model.Partner;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.Announcement;
import social.entourage.android.api.model.map.TourAuthor;
import social.entourage.android.base.BaseCardViewHolder;
import social.entourage.android.tools.CropCircleTransformation;
import social.entourage.android.view.PartnerLogoImageView;

/**
 * View Holder for the announcement card
 * Created by Mihai Ionescu on 02/11/2017.
 */

public class AnnouncementViewHolder extends BaseCardViewHolder implements Target {

    // ----------------------------------
    // Attributes
    // ----------------------------------

    //Views
    private TextView title;
    private ImageView photoView;
    private PartnerLogoImageView partnerLogoView;
    private TextView body;
    private View actLayout;
    private Button actButton;

    //Announcement related attributes
    private String actUrl;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public AnnouncementViewHolder(final View view) {
        super(view);
    }

    // ----------------------------------
    // BaseViewHolder implementation
    // ----------------------------------

    @Override
    protected void bindFields() {
        title = (TextView)itemView.findViewById(R.id.announcement_card_title);
        photoView = (ImageView)itemView.findViewById(R.id.announcement_card_photo);
        partnerLogoView = (PartnerLogoImageView)itemView.findViewById(R.id.announcement_card_partner_logo);
        body = (TextView)itemView.findViewById(R.id.announcement_card_body);
        actLayout = itemView.findViewById(R.id.announcement_card_act_layout);
        actButton = (Button)itemView.findViewById(R.id.announcement_card_button_act);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (actUrl == null) return;
                Intent actIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(actUrl));
                try {
                    itemView.getContext().startActivity(actIntent);
                } catch (Exception ex) {
                    Toast.makeText(itemView.getContext(), R.string.no_browser_error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        actButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (actUrl == null) return;
                Intent actIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(actUrl));
                try {
                    itemView.getContext().startActivity(actIntent);
                } catch (Exception ex) {
                    Toast.makeText(itemView.getContext(), R.string.no_browser_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void populate(final TimestampedObject data) {
        populate((Announcement)data);
    }

    private void populate(Announcement announcement) {
        if (announcement == null) return;
        //title
        title.setText(announcement.getTitle());
        String iconUrl = announcement.getIconUrl();
        if (iconUrl != null) {
            Picasso.with(itemView.getContext())
                    .load(Uri.parse(iconUrl))
                    .noPlaceholder()
                    .into(this);
        } else {
            title.setCompoundDrawables(null, null, null, null);
        }

        //author
        TourAuthor author = announcement.getAuthor();
        if (author == null) {
            if (photoView != null) {
                photoView.setImageResource(R.drawable.ic_user_photo_small);
            }
            if (partnerLogoView != null) {
                partnerLogoView.setImageDrawable(null);
            }
        } else {
            //author photo
            if (photoView != null) {
                String avatarURLAsString = author.getAvatarURLAsString();
                if (avatarURLAsString != null) {
                    Picasso.with(itemView.getContext())
                            .load(Uri.parse(avatarURLAsString))
                            .placeholder(R.drawable.ic_user_photo_small)
                            .transform(new CropCircleTransformation())
                            .into(photoView);
                } else {
                    photoView.setImageResource(R.drawable.ic_user_photo_small);
                }
            }
            // Partner logo
            if (partnerLogoView != null) {
                Partner partner = author.getPartner();
                if (partner != null) {
                    String partnerLogoURL = partner.getSmallLogoUrl();
                    if (partnerLogoURL != null) {
                        Picasso.with(itemView.getContext())
                                .load(Uri.parse(partnerLogoURL))
                                .placeholder(R.drawable.partner_placeholder)
                                .transform(new CropCircleTransformation())
                                .into(partnerLogoView);
                    } else {
                        partnerLogoView.setImageDrawable(null);
                    }
                } else {
                    partnerLogoView.setImageDrawable(null);
                }
            }
        }

        //body
        body.setText(announcement.getBody());

        //act button
        String action = announcement.getAction();
        actLayout.setVisibility(action != null ? View.VISIBLE : View.GONE);
        actButton.setText(action);
        actUrl = announcement.getUrl();
    }

    public static int getLayoutResource() {
        return R.layout.layout_card_announcement;
    }

    // ----------------------------------
    // Picasso Target implementation
    // ----------------------------------

    @Override
    public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
        int targetWidth = itemView.getResources().getDimensionPixelOffset(R.dimen.announcement_icon_width);
        int targetHeight = itemView.getResources().getDimensionPixelOffset(R.dimen.announcement_icon_height);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(Resources.getSystem(), Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false));
        title.setCompoundDrawablesWithIntrinsicBounds(bitmapDrawable, null, null, null);
    }

    @Override
    public void onBitmapFailed(final Drawable errorDrawable) {

    }

    @Override
    public void onPrepareLoad(final Drawable placeHolderDrawable) {
        title.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

}
