package social.entourage.android.map.entourage.category;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import social.entourage.android.R;
import social.entourage.android.api.model.map.Entourage;

/**
 * Created by Mihai Ionescu on 20/09/2017.
 */

public class EntourageCategory implements Serializable{

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @SerializedName("entourage_type")
    private String entourageType;

    @SerializedName("display_category")
    private String category;

    @SerializedName("display_category_title")
    private String title;

    @SerializedName("title_example")
    private String titleExample;

    @SerializedName("description_example")
    private String descriptionExample;

    private boolean isDefault = false;

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

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(final boolean aDefault) {
        isDefault = aDefault;
    }

    // ----------------------------------
    // Helper methods
    // ----------------------------------

    public @DrawableRes int getIconRes() {
        if ("social".equalsIgnoreCase(category)) {
            return R.drawable.ic_entourage_category_friendly_time;
        }
        if ("event".equalsIgnoreCase(category)) {
            return R.drawable.ic_entourage_category_event;
        }
        if ("mat_help".equalsIgnoreCase(category)) {
            return R.drawable.ic_entourage_category_sweater;
        }
        if ("resource".equalsIgnoreCase(category)) {
            return R.drawable.ic_entourage_category_washing_machine;
        }
        if ("info".equalsIgnoreCase(category)) {
            if (Entourage.TYPE_CONTRIBUTION.equalsIgnoreCase(entourageType)) {
                return R.drawable.ic_entourage_category_info_chat;
            }
            return R.drawable.ic_entourage_category_question_chat;
        }
        if ("skill".equalsIgnoreCase(category)) {
            return R.drawable.ic_entourage_category_skill;
        }
        if ("other".equalsIgnoreCase(category)) {
            return R.drawable.ic_entourage_category_more;
        }
        return R.drawable.ic_entourage_category_more;
    }

    public @ColorRes int getTypeColorRes() {
        if (Entourage.TYPE_CONTRIBUTION.equalsIgnoreCase(entourageType)) {
            return R.color.bright_blue;
        }
        if (Entourage.TYPE_DEMAND.equalsIgnoreCase(entourageType)) {
            return R.color.accent;
        }
        return R.color.accent;
    }

    public static @StringRes int getEntourageTypeDescription(String entourageType) {
        if (Entourage.TYPE_CONTRIBUTION.equalsIgnoreCase(entourageType)) {
            return R.string.entourage_category_type_contribution_label;
        }
        if (Entourage.TYPE_DEMAND.equalsIgnoreCase(entourageType)) {
            return R.string.entourage_category_type_demand_label;
        }
        return R.string.entourage_category_type_demand_label;
    }

    public String getKey() {
        String fullCategory = "";
        if (entourageType != null) fullCategory = entourageType;
        if (category != null) {
            if (fullCategory.length() > 0) fullCategory = fullCategory + "_";
            fullCategory = fullCategory + category;
        }
        return fullCategory;
    }
}
