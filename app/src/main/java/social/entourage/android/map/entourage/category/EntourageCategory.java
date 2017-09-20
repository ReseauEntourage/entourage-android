package social.entourage.android.map.entourage.category;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Mihai Ionescu on 20/09/2017.
 */

public class EntourageCategory {

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @SerializedName("entourage_type")
    private String entourageType;

    private String category;

    private String title;

    @SerializedName("title_example")
    private String titleExample;

    @SerializedName("description_example")
    private String descriptionExample;

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------


    public String getEntourageType() {
        return entourageType;
    }

    public void setEntourageType(final String entourageType) {
        this.entourageType = entourageType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getTitleExample() {
        return titleExample;
    }

    public void setTitleExample(final String titleExample) {
        this.titleExample = titleExample;
    }

    public String getDescriptionExample() {
        return descriptionExample;
    }

    public void setDescriptionExample(final String descriptionExample) {
        this.descriptionExample = descriptionExample;
    }
}
