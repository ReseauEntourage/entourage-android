package social.entourage.android.api.model;

public class Organization {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private String name;

    private String description;

    private String phone;

    private String address;

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
}
