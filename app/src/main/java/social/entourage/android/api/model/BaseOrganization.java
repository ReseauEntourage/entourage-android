package social.entourage.android.api.model;

import android.content.Context;

import androidx.annotation.StringRes;

import java.util.Comparator;

/**
 * Created by mihaiionescu on 01/02/2017.
 */

public abstract class BaseOrganization {

    public static final int TYPE_ORGANIZATION = 0;
    public static final int TYPE_PARTNER = 1;

    public abstract int getType();

    public abstract String getTypeAsString(Context contet);

    public abstract String getName();

    public abstract String getSmallLogoUrl();

    public abstract String getLargeLogoUrl();

    public static class CustomComparator implements Comparator<BaseOrganization> {

        @Override
        public int compare(final BaseOrganization organization1, final BaseOrganization organization2) {
            String name1 = organization1.getName();
            if (name1 == null) return -1;
            String name2 = organization2.getName();
            if (name2 == null) return 1;
            return name1.compareTo(name2);
        }
    }

}
