package social.entourage.android.api.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.List;

import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.Tour;

/**
 * Created by mihaiionescu on 05/05/16.
 */
public class Newsfeed {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String STATUS_ALL = "all";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_CLOSED = "closed";

    static final String TYPE = "type";
    static final String DATA = "data";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    String type;

    Object data;

    // ----------------------------------
    // GETTERS AND SETTERS
    // ----------------------------------

    public Object getData() {
        return data;
    }

    public void setData(final Object data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public long getId() {
        long id = 0;
        if (data != null) {
            if (data instanceof Tour) {
                id = ((Tour)data).getId();
            }
            else if (data instanceof Entourage) {
                id = ((Entourage)data).getId();
            }
        }
        return id;
    }


    // ----------------------------------
    // WRAPPERS
    // ----------------------------------

    public static class NewsfeedWrapper {

        @SerializedName("feeds")
        private List<Newsfeed> newsfeed;

        public List<Newsfeed> getNewsfeed() {
            return newsfeed;
        }

        public void setNewsfeed(final List<Newsfeed> newsfeed) {
            this.newsfeed = newsfeed;
        }
    }

    public static class NewsfeedJsonAdapter implements JsonDeserializer<Newsfeed> {
        @Override
        public Newsfeed deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                    .create();
            Newsfeed newsfeed = new Newsfeed();
            JsonObject jsonObject = json.getAsJsonObject();
            try {
                String type = jsonObject.get(TYPE).getAsString();
                newsfeed.type = type;
                if (type != null) {
                    if (type.equals(Tour.NEWSFEED_TYPE)) {
                        Tour tour = gson.fromJson(jsonObject.get(DATA), Tour.class);
                        newsfeed.data = tour;
                    }
                    else if (type.equals(Entourage.NEWSFEED_TYPE)) {
                        Entourage entourage = gson.fromJson(jsonObject.get(DATA), Entourage.class);
                        newsfeed.data = entourage;
                    }

                }
            } catch (Exception e) {
                Log.d("NewsfeedAdapter", e.toString());
            }

            return newsfeed;
        }
    }
}
