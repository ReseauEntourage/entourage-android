package social.entourage.android.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import social.entourage.android.R;

/**
 * Created by mihaiionescu on 17/01/2017.
 */

public class Partner extends BaseOrganization implements Serializable {

    private static final long serialVersionUID = -8314100695611710517L;

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

    private String description;

    private String phone;

    private String address;

    private String email;

    @SerializedName("website_url")
    private String websiteUrl;

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

    public String getDescription() {
        return description;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    @Override
    public int getType() {
        return TYPE_PARTNER;
    }

    @Override
    public int getTypeAsResourceId() {
        return R.string.member_type_partner;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public boolean isSame(Partner partner) {
        if (partner == null) return false;
        if (id != partner.id) return false;
        if (smallLogoUrl != null) {
            if (!smallLogoUrl.equals(partner.smallLogoUrl)) return false;
        } else {
            if (partner.smallLogoUrl != null) return false;
        }
        return true;
    }

    // ----------------------------------
    // WRAPPERS
    // ----------------------------------

    public static class PartnerWrapper {

        @SerializedName("partner")
        private Partner partner;

        public PartnerWrapper(final Partner partner) {
            this.partner = partner;
        }

        public Partner getPartner() {
            return partner;
        }

        public void setPartner(final Partner partner) {
            this.partner = partner;
        }

    }

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
