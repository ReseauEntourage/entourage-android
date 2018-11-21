package social.entourage.android.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import social.entourage.android.R;

/**
 * Custom class for partner logo with a transparent background if no drawable is set
 * Created by mihaiionescu on 30/01/2017.
 */

public class PartnerLogoImageView extends android.support.v7.widget.AppCompatImageView {

    public PartnerLogoImageView(final Context context) {
        super(context);
    }

    public PartnerLogoImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public PartnerLogoImageView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageDrawable(final Drawable drawable) {
        super.setImageDrawable(drawable);

        if (drawable == null) {
            setBackgroundResource(R.color.partner_logo_transparent);
        } else {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_partner_logo));
        }
    }
}
