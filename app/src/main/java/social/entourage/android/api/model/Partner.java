package social.entourage.android.api.model;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

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

    @SerializedName("user_role_title")
    private String userRoleTitle;

    @SerializedName("postal_code")
    private String postal_code;

    private boolean isCreation;

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

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    public boolean isCreation() {
        return isCreation;
    }

    public void setCreation(boolean creation) {
        isCreation = creation;
    }

    public String getUserRoleTitle() {
        return userRoleTitle;
    }

    public void setUserRoleTitle(String userRoleTitle) {
        this.userRoleTitle = userRoleTitle;
    }

    @Override
    public int getType() {
        return TYPE_PARTNER;
    }

    @Override
    public String getTypeAsString(Context context) {
        return userRoleTitle;//context.getString(R.string.member_type_partner);
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public boolean isSame(Partner partner) {
        if (partner == null) return false;
        if (id != partner.id) return false;
        if (smallLogoUrl != null) {
            return smallLogoUrl.equals(partner.smallLogoUrl);
        } else {
            return partner.smallLogoUrl == null;
        }
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
