package com.octo.entourage.map;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.octo.entourage.EntourageActivity;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * Created by RPR on 25/03/15.
 */
public class MapActivity extends EntourageActivity {

    @Inject
    MapPresenter presenter;

    @Override
    protected List<Object> getScopedModules() {
        return Arrays.<Object>asList(new MapModule(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}