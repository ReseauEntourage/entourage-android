package social.entourage.android.api.model.map;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
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

    private HashMap<Date, String> historic;

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

    public HashMap<Date, String> getHistoric() {
        return historic;
    }

    public void setHistoric(HashMap<Date, String> historic) {
        this.historic = historic;
    }

    public void updateCoordinates(LatLng location) {
        this.coordinates.add(location);
    }

    public void updateHistoric(Date time, String street) {
        this.historic.put(time, street);
    }
}
