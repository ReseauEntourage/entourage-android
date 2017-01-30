package social.entourage.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

import social.entourage.android.R;

/**
 * Custom class for partner logo with a transparent background if no drawable is set
 * Created by mihaiionescu on 30/01/2017.
 */

public class PartnerLogoImageView extends ImageView {

    public PartnerLogoImageView(final Context context) {
        super(context);
    }

    public PartnerLogoImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public PartnerLogoImageView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public PartnerLogoImageView(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setImageDrawable(final Drawable drawable) {
        super.setImageDrawable(drawable);

        if (drawable == null) {
            setBackgroundResource(R.color.partner_logo_transparent);
        } else {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.bg_partner_logo));
            } else {
                setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_partner_logo));
            }
        }
    }
}
