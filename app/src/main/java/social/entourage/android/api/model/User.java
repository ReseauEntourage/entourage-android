package social.entourage.android.api.model;

import androidx.collection.ArrayMap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import social.entourage.android.EntourageApplication;
import social.entourage.android.api.model.map.TourAuthor;
import social.entourage.android.api.model.map.UserMembership;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class User implements Serializable {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final long serialVersionUID = 6066574628451729752L;

    public static final String KEY_USER_ID = "social.entourage.android.KEY_USER_ID";
    public static final String KEY_USER = "social.entourage.android.KEY_USER";

    public static final String TYPE_PUBLIC = "public";
    public static final String TYPE_PRO = "pro";

    private static final String USER_ROLE_ETHICS_CHARTER_SIGNED = "ethics_charter_signed";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final int id;

    private String email;

    @Expose(serialize = false)
    private String phone;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("display_name")
    private final String displayName;

    @Expose(serialize = false)
    private final String token;

    @Expose(serialize = false)
    private final Stats stats;

    @Expose(serialize = false)
    private final Organization organization;

    private Partner partner;

    @SerializedName("avatar_url")
    private String avatarURL;

    @Expose(serialize = false)
    private String smsCode;

    @SerializedName("user_type")
    private String type = TYPE_PRO;

    private String about;

    private ArrayList<String> roles;

    private ArrayList<UserMembership.MembershipList> memberships;

    private UserConversation conversation;

    private Address address;

    @Expose(serialize = false)
    private boolean entourageDisclaimerShown = false;

    @Expose(serialize = false)
    private boolean encounterDisclaimerShown = false;

    @Expose(serialize = false, deserialize = false)
    private boolean isOnboardingUser = false;

    @Expose(serialize = false, deserialize = false)
    public boolean editActionZoneShown = false;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public User() {
        this.id = 0;
        this.email = "";
        this.displayName = "";
        this.stats = null;
        this.organization = null;
        this.partner = null;
        this.token = null;
        this.avatarURL = null;
    }

    private User(final int id, final String email, final String displayName, final Stats stats, final Organization organization, final String token, final String avatarURL) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.stats = stats;
        this.organization = organization;
        this.partner = null;
        this.token = token;
        this.avatarURL = avatarURL;
    }

    public User clone() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (User) ois.readObject();
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDisplayName() {
        return displayName == null ? "" : displayName;
    }

    public String getFirstName() {
        return firstName == null ? "" : firstName;
    }

    public String getLastName() {
        return lastName == null ? "" : lastName;
    }

    public String getToken() {
        return token;
    }

    public Stats getStats() {
        return stats;
    }

    public Organization getOrganization() {
        return organization;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(final Partner partner) {
        this.partner = partner;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(final String smsCode) {
        this.smsCode = smsCode;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(final String about) {
        this.about = about;
    }

    public ArrayList<String> getRoles() {
        return roles;
    }

    public ArrayList<UserMembership.MembershipList> getAllMemberships() {
        return memberships;
    }

    public ArrayList<UserMembership> getMemberships(String type) {
        if (memberships == null || type == null) return new ArrayList<>();
        for (UserMembership.MembershipList membershipList:
             memberships) {
            if (type.equalsIgnoreCase(membershipList.getType())) {
                return membershipList.getList();
            }
        }
        return new ArrayList<>();
    }

    public void setMemberships(final ArrayList<UserMembership.MembershipList> memberships) {
        this.memberships = memberships;
    }

    public UserConversation getConversation() {
        return conversation;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    // ----------------------------------
    // Other methods
    // ----------------------------------

    public boolean isEntourageDisclaimerShown() {
        return entourageDisclaimerShown;
    }

    public void setEntourageDisclaimerShown(final boolean entourageDisclaimerShown) {
        this.entourageDisclaimerShown = entourageDisclaimerShown;
    }

    public boolean isEncounterDisclaimerShown() {
        return encounterDisclaimerShown;
    }

    public void setEncounterDisclaimerShown(final boolean encounterDisclaimerShown) {
        this.encounterDisclaimerShown = encounterDisclaimerShown;
    }

    public boolean isOnboardingUser() {
        return isOnboardingUser;
    }

    public void setOnboardingUser(final boolean onboardingUser) {
        isOnboardingUser = onboardingUser;
    }

    public boolean isEditActionZoneShown() {
        return editActionZoneShown;
    }

    public void setEditActionZoneShown(final boolean isEditActionZoneShown) {
        editActionZoneShown = isEditActionZoneShown;
    }

    public void incrementTours() {
        if (stats != null) {
            stats.setTourCount(stats.getTourCount() + 1);
        }
    }

    public void incrementEncouters() {
        if (stats != null) {
            stats.setEncounterCount(stats.getEncounterCount() + 1);
        }
    }

    public static String decodeURL(String encodedURL) {
        if (encodedURL == null) {
            return null;
        }
        return encodedURL.replace('\u0026', '&');
    }

    public boolean isPro() {
        return EntourageApplication.isEntourageApp() && TYPE_PRO.equals(type);
    }

    public String getLocationAccessString() {
        return isPro() ? ACCESS_FINE_LOCATION : ACCESS_COARSE_LOCATION;
    }

    public TourAuthor asTourAuthor() {
        return new TourAuthor(avatarURL, id, displayName, partner);
    }

    public ArrayMap<String, Object> getArrayMapForUpdate() {
        ArrayMap<String, Object> userMap = new ArrayMap<>();
        userMap.put("first_name", firstName);
        userMap.put("last_name", lastName);
        if (email != null) {
            userMap.put("email", email);
        }
        if (smsCode != null) {
            userMap.put("sms_code", smsCode);
        }
        if (about != null) {
            userMap.put("about", about);
        }
        return userMap;
    }

    public boolean hasSignedEthicsCharter() {
        return roles != null && roles.contains(USER_ROLE_ETHICS_CHARTER_SIGNED);
    }

    // ----------------------------------
    // BUILDER
    // ----------------------------------

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private int id;
        private String email, displayName, token, avatarURL;
        private Stats stats;
        private Organization organization;

        public Builder() {
        }

        public Builder withId(final int id) {
            this.id = id;
            return this;
        }

        public Builder withEmail(final String email) {
            this.email = email;
            return this;
        }

        public Builder withDisplayName(final String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder withStats(final Stats stats) {
            this.stats = stats;
            return this;
        }

        public Builder withOrganization(final Organization organization) {
            this.organization = organization;
            return this;
        }

        public Builder withToken(final String token) {
            this.token = token;
            return this;
        }

        public Builder withAvatarURL(final String avatarURL) {
            this.avatarURL = avatarURL;
            return this;
        }

        public User build() {
            if (id == -1) {
                return null;
            }
            if (email == null) {
                return null;
            }
            if (displayName == null) {
                return null;
            }
            if (stats == null) {
                return null;
            }
            if (organization == null) {
                return null;
            }
            if (token == null) {
                return null;
            }
            return new User(id, email, displayName, stats, organization, token, avatarURL);
        }
    }

    // ----------------------------------
    // User Conversation
    // ----------------------------------

    public static class UserConversation implements Serializable {

        private static final long serialVersionUID = -2040655965839688879L;

        @SerializedName("uuid")
        private String UUID;

        public String getUUID() {
            return UUID;
        }
    }

    // ----------------------------------
    // User Address
    // ----------------------------------

    public static class Address implements Serializable {

        private static final long serialVersionUID = 5727042444482111831L;

        private double latitude;

        private double longitude;

        @SerializedName("display_address")
        private String displayAddress;

        @SerializedName("google_place_id")
        private String googlePlaceId;

        public Address(String googlePlaceId) {
            this.googlePlaceId = googlePlaceId;
        }

        public Address(double latitude, double longitude, String displayAddress) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.displayAddress = displayAddress;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(final double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(final double longitude) {
            this.longitude = longitude;
        }

        public String getDisplayAddress() {
            return displayAddress;
        }

        public void setDisplayAddress(final String displayAddress) {
            this.displayAddress = displayAddress;
        }

        public String getGooglePlaceId() {
            return googlePlaceId;
        }

        public void setGooglePlaceId(final String googlePlaceId) {
            this.googlePlaceId = googlePlaceId;
        }
    }

    // ----------------------------------
    // WRAPPERS
    // ----------------------------------

    public static class UserWrapper {

        private User user;

        public UserWrapper(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    public static class AddressWrapper {

        private Address address;

        public AddressWrapper(Address address) {
            this.address = address;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(final Address address) {
            this.address = address;
        }
    }
}
