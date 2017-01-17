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

    @SerializedName("logo_url")
    private String logoUrl;

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

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(final String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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
