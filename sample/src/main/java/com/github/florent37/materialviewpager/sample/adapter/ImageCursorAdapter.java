package com.github.florent37.materialviewpager.sample.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.framework.CustomTextView;
import com.squareup.picasso.Picasso;

/**
 * Created by aaron on 2016/8/3.
 */
public class ImageCursorAdapter extends SimpleCursorAdapter {
    private Cursor c;
    private Context context;
    private String type;

    public ImageCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, String type) {
        super(context, layout, c, from, to);
        this.c = c;
        this.context = context;
        this.type = type;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        View v = view;
        String descriptionStr = "";
        String titleStr = "";
        String posterUrl = "";
        int titleCol, descriptionCol, posterCol;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.search_row, null);
        }

        if (cursor != null) {
            titleCol = cursor.getColumnIndex("filmName");
            descriptionCol = cursor.getColumnIndex("filmDescription");
            posterCol = cursor.getColumnIndex("filmPoster");
            titleStr = cursor.getString(titleCol);
            descriptionStr = cursor.getString(descriptionCol);
            posterUrl = cursor.getString(posterCol);
        }

        ImageView iv = (ImageView) v.findViewById(R.id.pic);
        Picasso.with(iv.getContext()).load(posterUrl).placeholder(R.drawable.placeholder).centerCrop().fit()
                .into(iv);
        CustomTextView title = (CustomTextView) v.findViewById(R.id.title);
        title.setText(titleStr);

        TextView description = (TextView) v.findViewById(R.id.descriptioin);
        description.setText(descriptionStr);

        if (type == "genre") {
            v.setBackgroundColor(context.getResources().getColor(R.color.material_blue_300));
            title.setTextColor(context.getResources().getColor(R.color.io15_white));
        }
        else if (type == "imdb") {
            v.setBackgroundColor(context.getResources().getColor(R.color.imdb_yellow));
            title.setTextColor(context.getResources().getColor(R.color.black_opacity_66));
            description.setTextColor(context.getResources().getColor(R.color.material_grey_600));
        } else if (type == "upcoming") {
            v.setBackgroundColor(context.getResources().getColor(R.color.tab_background));
        } else if (type == "main" || type == "detail") {
            v.setBackgroundColor(context.getResources().getColor(R.color.transparent_black));
            title.setTextColor(context.getResources().getColor(R.color.app_white));
        }
    }
}
