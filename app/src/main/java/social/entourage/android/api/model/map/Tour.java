package social.entourage.android.api.model.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class Tour implements Serializable {

    private long id;

    @SerializedName("tour_type")
    private String  tourType = "social";

    private transient Date date;

    private transient List<LatLng> coordinates;

    private transient HashMap<Date, String> steps;

    public Tour() {
        this.coordinates = new ArrayList<>();
        this.steps = new HashMap<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTourType() {
        return tourType;
    }

    public void setTourType(String tourType) {
        this.tourType = tourType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<LatLng> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<LatLng> coordinates) {
        this.coordinates = coordinates;
    }

    public HashMap<Date, String> getSteps() {
        return steps;
    }

    public void updateCoordinates(LatLng location) {
        this.coordinates.add(location);
    }

    public void updateSteps(Date time, String step) {
        this.steps.put(time, step);
    }

}
