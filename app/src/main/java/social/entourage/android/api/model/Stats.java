package social.entourage.android.api.model;

import com.google.gson.annotations.SerializedName;

public class Stats {

    @SerializedName("tour_count")
    private int tourCount;

    @SerializedName("encounter_count")
    private int encounterCount;

    public Stats(int tourCount, int encounterCount) {
        this.tourCount = tourCount;
        this.encounterCount = encounterCount;
    }

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
}
