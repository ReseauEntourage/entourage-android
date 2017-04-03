package social.entourage.android.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Stats implements Serializable {

    private static final long serialVersionUID = -6285877183504670635L;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @SerializedName("tour_count")
    private int tourCount;

    @SerializedName("encounter_count")
    private int encounterCount;

    @SerializedName("entourage_count")
    private int entourageCount;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public Stats(int tourCount, int encounterCount, int entourageCount) {
        this.tourCount = tourCount;
        this.encounterCount = encounterCount;
        this.entourageCount = entourageCount;
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public int getTourCount() {
        return tourCount;
    }

    public int getEncounterCount() {
        return encounterCount;
    }

    public void setTourCount(int tourCount) {
        this.tourCount = tourCount;
    }

    public void setEncounterCount(int encounterCount) {
        this.encounterCount = encounterCount;
    }

    public int getEntourageCount() {
        return entourageCount + tourCount;
    }

    public void setEntourageCount(final int entourageCount) {
        this.entourageCount = entourageCount;
    }

}
