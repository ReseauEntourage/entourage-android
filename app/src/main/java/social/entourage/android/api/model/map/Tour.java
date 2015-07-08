package social.entourage.android.api.model.map;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by NTE on 06/07/15.
 */
public class Tour implements Serializable {

    private long id;

    private Date date;

    private List<LatLng> coordinates;

    private HashMap<Date, String> steps;

    public Tour() {
        this.coordinates = new ArrayList<LatLng>();
        this.steps = new HashMap<Date, String>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public void setSteps(HashMap<Date, String> steps) {
        this.steps = steps;
    }

    public void updateCoordinates(LatLng location) {
        this.coordinates.add(location);
    }

    public void updateSteps(Date time, String step) {
        this.steps.put(time, step);
    }
}
