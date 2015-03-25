package com.octo.entourage.api;

import com.octo.entourage.api.model.map.Encounter;

import java.util.List;

/**
 * Created by RPR on 25/03/15.
 */
public class MapResponse {
    private List<Encounter> encounters;

    public List<Encounter> getEncounters() {
        return encounters;
    }

    public void setEncounters(List<Encounter> encounters) {
        this.encounters = encounters;
    }
}
