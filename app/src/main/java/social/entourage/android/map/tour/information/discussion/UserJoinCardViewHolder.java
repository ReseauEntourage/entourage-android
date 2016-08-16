package social.entourage.android.map.tour.information.discussion;

import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.BaseCardViewHolder;
import social.entourage.android.tools.BusProvider;

/**
 * User Card View in tour information screen
 */
public class UserJoinCardViewHolder extends BaseCardViewHolder {

    private View mPublicSection;
    private View mPrivateSection;

    private TextView mPublicUsernameView;
    private TextView mJoinStatusView;

    private ImageView mPhotoView;
    private TextView mPrivateUsernameView;
    private TextView mJoinMessage;
    private Button mAcceptButton;
    private Button mRefuseButton;

    private int userId;

    public UserJoinCardViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {

        mPublicSection = (View) itemView.findViewById(R.id.tic_public_info_section);
        mPrivateSection = (View) itemView.findViewById(R.id.tic_private_info_section);

        mPublicUsernameView = (TextView) itemView.findViewById(R.id.tic_username);
        mJoinStatusView = (TextView) itemView.findViewById(R.id.tic_join_status);

        mPhotoView = (ImageView) itemView.findViewById(R.id.tic_photo);
        mPrivateUsernameView = (TextView) itemView.findViewById(R.id.tic_private_username);
        mJoinMessage = (TextView) itemView.findViewById(R.id.tic_join_message);
        mAcceptButton = (Button) itemView.findViewById(R.id.tic_accept_button);
        mRefuseButton = (Button) itemView.findViewById(R.id.tic_refuse_button);

        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (userId == 0) return;
            }
        });

        mRefuseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (userId == 0) return;
            }
        });
    }

    @Override
    public void populate(final TimestampedObject data) {
        this.populate((TourUser)data);
    }

    public void populate(TourUser user) {

        if (user.getDisplayName() == null || user.getStatus() == null) return;

        if (user.getStatus().equals(FeedItem.JOIN_STATUS_PENDING)) {
            mPrivateSection.setVisibility(View.VISIBLE);
            mPublicSection.setVisibility(View.GONE);

            mPrivateUsernameView.setText(user.getDisplayName());

            String avatarURL = user.getAvatarURLAsString();
            if (avatarURL != null) {
                Picasso.with(itemView.getContext()).load(Uri.parse(avatarURL))
                        .transform(new CropCircleTransformation())
                        .into(mPhotoView);
            }

            mJoinMessage.setText(user.getMessage());

        } else {
            mPrivateSection.setVisibility(View.GONE);
            mPublicSection.setVisibility(View.VISIBLE);

            String displayName = user.getDisplayName();
            SpannableString spannableString = new SpannableString(user.getDisplayName() + getJoinStatus(user.getStatus()));
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(final View widget) {
                    if (userId == 0) return;
                    BusProvider.getInstance().post(new Events.OnUserViewRequestedEvent(userId));
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            spannableString.setSpan(clickableSpan, 0, displayName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            ForegroundColorSpan fcs = new ForegroundColorSpan(itemView.getResources().getColor(R.color.accent));
            spannableString.setSpan(fcs, 0, displayName.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            mJoinStatusView.setMovementMethod(LinkMovementMethod.getInstance());
            mJoinStatusView.setText(spannableString);
        }

        userId = user.getUserId();
    }

    private String getJoinStatus(String joinStatus) {
        if (joinStatus.equals(Tour.JOIN_STATUS_ACCEPTED)) {
            return itemView.getContext().getString(R.string.tour_info_text_join_accepted);
        }
        else if (joinStatus.equals(Tour.JOIN_STATUS_REJECTED)) {
            return itemView.getContext().getString(R.string.tour_info_text_join_rejected);
        }
        else if (joinStatus.equals(Tour.JOIN_STATUS_PENDING)) {
            return itemView.getContext().getString(R.string.tour_info_text_join_pending);
        }
        else {
            return "";
        }
    }

    public static int getLayoutResource() {
        return R.layout.tour_information_user_join_card_view;
    }

}
