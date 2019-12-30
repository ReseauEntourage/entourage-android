package social.entourage.android.api;

import java.util.List;

import social.entourage.android.api.model.map.Category;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.guide.Poi;

public class PoiResponse {
    private List<Encounter> encounters;
    private List<Poi> pois;
    private List<Category> categories;

    public List<Encounter> getEncounters() {
        return encounters;
    }

    public void setEncounters(List<Encounter> encounters) {
        this.encounters = encounters;
    }

    public List<Poi> getPois() {
        return pois;
    }

    public void setPois(List<Poi> pois) {
        this.pois = pois;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}
