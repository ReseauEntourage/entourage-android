package social.entourage.android.map.filter;

import java.io.Serializable;

/**
 * Created by Mihai Ionescu on 04/07/2018.
 */
public class MapFilterPFP extends MapFilter implements Serializable {

    private static final long serialVersionUID = 1562838744560618668L;

    private static MapFilter ourInstance = new MapFilterPFP();

    public static MapFilter getInstance() {
        return ourInstance;
    }

    @Override
    public String getTypes() {
        return null;
    }
}
