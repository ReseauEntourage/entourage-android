package social.entourage.android.map.entourage.category;

import android.app.Application;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import social.entourage.android.EntourageApplication;
import social.entourage.android.R;

/**
 * Created by Mihai Ionescu on 20/09/2017.
 */

public class EntourageCategoryManager {

    // ----------------------------------
    // Attributes
    // ----------------------------------

    private List<EntourageCategory> entourageCategories = new ArrayList<>();

    // ----------------------------------
    // Singleton
    // ----------------------------------

    private static final EntourageCategoryManager ourInstance = new EntourageCategoryManager();

    public static EntourageCategoryManager getInstance() {
        return ourInstance;
    }

    private EntourageCategoryManager() {
        // Load our JSON file.
        JSONResourceReader reader = new JSONResourceReader(EntourageApplication.get().getResources(), R.raw.display_categories);
        Type listType = new TypeToken<ArrayList<EntourageCategory>>(){}.getType();
        entourageCategories = reader.constructUsingGson(listType);
        if (entourageCategories == null) {
            entourageCategories = new ArrayList<>();
        }
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public List<EntourageCategory> getEntourageCategories() {
        return entourageCategories;
    }

}
