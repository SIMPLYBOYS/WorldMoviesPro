package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.florent37.materialviewpager.worldmovies.ui.SearchActivity;
import com.moxun.tagcloudlib.view.TagsAdapter;

import java.util.List;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;

/**
 * Created by aaron on 2016/12/21.
 */

public class TextTagsAdapter extends TagsAdapter {
    private List<String> dataSet;
    private Activity activity;
    private int lastSelectedPosition = 0;

    public TextTagsAdapter(List<String> data, Activity activity) {
        dataSet = data;
        this.activity = activity;
//        Collections.addAll(dataSet, data);
    }

    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public View getView(final Context context, final int position, ViewGroup parent) {
        TextView tv = new TextView(context);
        tv.setText(dataSet.get(position));
//        tv.setText("No." + position);
        tv.setGravity(Gravity.CENTER);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LOGD("Click", "Tag " + position + " clicked.");
//                Toast.makeText(context, "Tag " + position + " clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity, SearchActivity.class);
                intent.putExtra(SearchManager.QUERY, dataSet.get(position));
                intent.putExtra("lastSelectedPosition", lastSelectedPosition);
                intent.putExtra("lauchBy", "main");
                ActivityCompat.startActivity(activity, intent, null);
            }
        });
        tv.setTextColor(Color.WHITE);
        return tv;
    }

    @Override
    public Object getItem(int position) {
        return dataSet.get(position);
    }

    @Override
    public int getPopularity(int position) {
        return position % 7;
    }

    @Override
    public void onThemeColorChanged(View view, int themeColor) {
        view.setBackgroundColor(themeColor);
    }
}
