package social.entourage.android.view.CountryCodePicker;

public class Country {
    private String iso;
    private String phoneCode;
    private String name;

    public Country(String iso, String phoneCode, String name) {
        this.iso = iso;
        this.phoneCode = phoneCode;
        this.name = name;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * If country have query word in name or name code or phone code, this will return true.
     */
    boolean isEligibleForQuery(String query) {
        query = query.toLowerCase();
        return getName().toLowerCase().contains(query)
                || getIso().toLowerCase().contains(query)
                || getPhoneCode().toLowerCase().contains(query);
    }
}
