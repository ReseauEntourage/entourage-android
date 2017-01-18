package social.entourage.android.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mihaiionescu on 17/01/2017.
 */

public class Partner implements Serializable {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private long id;

    private String name;

    @SerializedName("small_logo_url")
    private String smallLogoUrl;

    @SerializedName("large_logo_url")
    private String largeLogoUrl;

    @SerializedName("default")
    private boolean isDefault;

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------


    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(final boolean aDefault) {
        isDefault = aDefault;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getLargeLogoUrl() {
        return largeLogoUrl;
    }

    public void setLargeLogoUrl(final String largeLogoUrl) {
        this.largeLogoUrl = largeLogoUrl;
    }

    public String getSmallLogoUrl() {
        return smallLogoUrl;
    }

    public void setSmallLogoUrl(final String smallLogoUrl) {
        this.smallLogoUrl = smallLogoUrl;
    }

    // ----------------------------------
    // WRAPPERS
    // ----------------------------------

    public static class PartnersWrapper {

        @SerializedName("partners")
        private List<Partner> partners;

        public List<Partner> getPartners() {
            return partners;
        }

        public void setPartners(final List<Partner> partners) {
            this.partners = partners;
        }

    }

}
