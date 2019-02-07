package social.entourage.android.api.model.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.Date;

import social.entourage.android.api.model.TimestampedObject;

@SuppressWarnings("unused")
public class Poi extends TimestampedObject implements Serializable, ClusterItem {

    private static final long serialVersionUID = 7508582427596761716L;

    private final static String HASH_STRING_HEAD = "Poi-";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private long id;

    private String name;

    private String description;

    @SerializedName("adress")
    private String address;

    private String phone;

    private String website;

    private String email;

    private String audience;

    @SerializedName("category_id")
    private int categoryId;

    private double longitude;

    private double latitude;

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    // ----------------------------------
    // Timestamp methods
    // ----------------------------------


    @Override
    public Date getTimestamp() {
        return null;
    }

    @Override
    public int getType() {
        return TimestampedObject.GUIDE_POI;
    }

    @Override
    public String hashString() {
        return HASH_STRING_HEAD + id;
    }

}
