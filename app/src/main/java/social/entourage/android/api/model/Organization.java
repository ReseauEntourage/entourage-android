package social.entourage.android.api.model;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import social.entourage.android.R;

public class Organization extends BaseOrganization implements Serializable {

    private static final long serialVersionUID = 987327362138435972L;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private String name;

    private String description;

    private String phone;

    private String address;

    @SerializedName("logo_url")
    private String logoUrl;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public Organization(String name, String description, String phone, String address) {
        this.name = name;
        this.description = description;
        this.phone = phone;
        this.address = address;
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(final String logoUrl) {
        this.logoUrl = logoUrl;
    }

    @Override
    public String getLargeLogoUrl() {
        return logoUrl;
    }

    @Override
    public String getSmallLogoUrl() {
        return logoUrl;
    }

    @Override
    public int getType() {
        return TYPE_ORGANIZATION;
    }

    @Override
    public String getTypeAsString(Context context) {
        return context.getString(R.string.member_type_organization);
    }
}
