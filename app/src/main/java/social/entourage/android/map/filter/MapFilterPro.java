package social.entourage.android.map.filter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.map.entourage.category.EntourageCategory;
import social.entourage.android.map.entourage.category.EntourageCategoryManager;

/**
 * Created by mihaiionescu on 27/10/16.
 */

public class MapFilterPro extends MapFilter implements Serializable {

    private static final long serialVersionUID = 790172804791850743L;

    private static MapFilter ourInstance = new MapFilterPro();

    public static MapFilter getInstance() {
        return ourInstance;
    }

    protected MapFilterPro() {
        tourTypeMedical = true;
        tourTypeSocial = true;
        tourTypeDistributive = true;

        entourageTypeDemand = false;
        entourageTypeContribution = false;

        showTours = true;
    }

}
