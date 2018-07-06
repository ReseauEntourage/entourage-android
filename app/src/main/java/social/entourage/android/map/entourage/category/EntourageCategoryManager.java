package social.entourage.android.map.entourage.category;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.model.map.BaseEntourage;
import social.entourage.android.api.model.map.Entourage;

/**
 * Created by Mihai Ionescu on 20/09/2017.
 */

public class EntourageCategoryManager {

    // ----------------------------------
    // Attributes
    // ----------------------------------

    private List<String> entourageTypes = new ArrayList<>();
    private HashMap<String, List<EntourageCategory>> entourageCategoriesHashMap = new HashMap<>();

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
        List<EntourageCategory> entourageCategories = reader.constructUsingGson(listType);
        if (entourageCategories == null) {
            entourageCategories = new ArrayList<>();
        }
        // To preserve the required order, we add the know types in the required order and the other types will get added after them
        entourageTypes.add(Entourage.TYPE_DEMAND);
        entourageTypes.add(Entourage.TYPE_CONTRIBUTION);
        // Construct the hashmap
        entourageCategoriesHashMap.put(Entourage.TYPE_DEMAND, new ArrayList<EntourageCategory>());
        entourageCategoriesHashMap.put(Entourage.TYPE_CONTRIBUTION, new ArrayList<EntourageCategory>());
        for (EntourageCategory category:entourageCategories) {
            String key = category.getEntourageType();
            List<EntourageCategory> list = entourageCategoriesHashMap.get(key);
            if (list == null) {
                entourageTypes.add(key);
                list = new ArrayList<>();
                entourageCategoriesHashMap.put(key, list);
            }
            list.add(category);
        }
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------


    public List<String> getEntourageTypes() {
        return entourageTypes;
    }

    public HashMap<String, List<EntourageCategory>> getEntourageCategories() {
        return entourageCategoriesHashMap;
    }

    // ----------------------------------
    // Public methods
    // ----------------------------------

    public List<EntourageCategory> getEntourageCategoriesForType(String categoryType) {
        return entourageCategoriesHashMap.get(categoryType);
    }

    public EntourageCategory findCategory(BaseEntourage entourage) {
        if (entourage == null) return null;
        return findCategory(entourage.getEntourageType(), entourage.getCategory());
    }

    public EntourageCategory findCategory(String entourageType, String entourageCategory) {
        if (entourageCategory == null) entourageCategory = "other";
        List<EntourageCategory> list = entourageCategoriesHashMap.get(entourageType);
        if (list != null) {
            for (EntourageCategory category:list) {
                if (category.getCategory() != null) {
                    if (category.getCategory().equalsIgnoreCase(entourageCategory)) {
                        return category;
                    }
                }
            }
        }
        return null;
    }

}
