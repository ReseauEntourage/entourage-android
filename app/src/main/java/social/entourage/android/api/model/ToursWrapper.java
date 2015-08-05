package social.entourage.android.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import social.entourage.android.api.model.map.Tour;

public class ToursWrapper {

    private List<Tour> tours;

    public List<Tour> getTours() {
        return tours;
    }

    public void setTours(List<Tour> tours) {
        this.tours = tours;
    }

}
