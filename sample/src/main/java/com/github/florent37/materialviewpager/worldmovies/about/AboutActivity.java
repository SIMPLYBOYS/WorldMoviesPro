package com.github.florent37.materialviewpager.worldmovies.about;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.ui.BaseActivity;

/**
 * Created by aaron on 2016/2/15.
 */
public class AboutActivity extends BaseActivity {

    private Toolbar toolbar;
    private View button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Bundle extras = getIntent().getExtras();

        if (extras != null)
            Log.d("0327", "index: " + extras.getInt("index") + " location: " + extras.getString("location"));

        this.toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                afficherCacherToolbar();
            }
        });



        registerHideableHeaderView(findViewById(R.id.headerbar));
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        /*new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/1242976969049134/friendlists",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
            *//* handle the result *//*
                        Log.d("FB", "complete " + response);
                    }
                }
        ).executeAsync();*/

        // synchroniser le drawerToggle après la restauration via onRestoreInstanceState
//        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_ABOUT;
    }

    private void afficherCacherToolbar() {
        Log.d("0309", "afficherCacherToolbar");

        if(toolbar.getAlpha() == 1){

            toolbar.animate()
                    .alpha(0)
                    .translationY(-toolbar.getHeight())
                    .start();
        }
        else{
            toolbar.animate()
                    .alpha(1)
                    .translationY(0)
                    .start();
        }
    }

}
