package com.octo.entourage.map;

import com.octo.entourage.EntourageActivity;

import java.util.Arrays;
import java.util.List;

/**
 * Created by RPR on 25/03/15.
 */
public class MapActivity extends EntourageActivity{
    @Override
    protected List<Object> getScopedModules() {
        return Arrays.<Object>asList(new MapModule(this));
    }
}
