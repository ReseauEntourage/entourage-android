package social.entourage.android.authentication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Source : https://github.com/fsilvestremorais/android-complex-preferences/blob/master/ComplexPreferences/src/br/com/kots/mob/complex/preferences/ComplexPreferences.java
 */
public class ComplexPreferences {

    private static ComplexPreferences complexPreferences;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static Gson GSON = new Gson();

    @SuppressLint("CommitPrefEdits")
    private ComplexPreferences(Context context, String namePreferences, int mode) {
        if (TextUtils.isEmpty(namePreferences)) {
            namePreferences = "complex_preferences";
        }
        preferences = context.getSharedPreferences(namePreferences, mode);
        editor = preferences.edit();
    }

    public static ComplexPreferences getComplexPreferences(Context context, String namePreferences, int mode) {
        if (complexPreferences == null) {
            complexPreferences = new ComplexPreferences(context, namePreferences, mode);
        }
        return complexPreferences;
    }

    public void putObject(String key, Object object) {
        if(object == null) {
            editor.putString(key, "");
        }
        if(TextUtils.isEmpty(key)) {
            throw new IllegalArgumentException("key is empty or null");
        }
        editor.putString(key, GSON.toJson(object));
    }

    public void commit() {
        editor.commit();
    }

    public <T> T getObject(String key, Class<T> a) {
        String gson = preferences.getString(key, null);
        if (gson == null) {
            return null;
        } else {
            try{
                return GSON.fromJson(gson, a);
            } catch (Exception e) {
                throw new IllegalArgumentException("Object storage with key " + key + " is instanceof other class");
            }
        }
    }

    public <T> T getObject(String key, Type t) {
        String gson = preferences.getString(key, null);
        if (gson == null) {
            return null;
        } else {
            try{
                return GSON.fromJson(gson, t);
            } catch (Exception e) {
                throw new IllegalArgumentException("Object storage with key " + key + " is instanceof other class: "+e.getLocalizedMessage());
            }
        }
    }
}