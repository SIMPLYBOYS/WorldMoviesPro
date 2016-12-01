package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.framework.CustomTextView;
import com.squareup.picasso.Picasso;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;

/**
 * Created by aaron on 2016/8/3.
 */
public class ImageCursorAdapter extends SimpleCursorAdapter {
    private Cursor cusor;
    private Context context;
    private String type;

    public ImageCursorAdapter(Context context, int layout, Cursor cusor, String[] from, int[] to, String type) {
        super(context, layout, cusor, from, to);
        this.cusor = cusor;
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
            Log.d("0906", "view is null");
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.search_row, null);
        }

        if (cursor != null) {
            Log.d("0906", "view is not null");
            titleCol = cursor.getColumnIndex("filmName");
            descriptionCol = cursor.getColumnIndex("filmDescription");
            posterCol = cursor.getColumnIndex("filmPoster");
            titleStr = cursor.getString(titleCol);
            descriptionStr = cursor.getString(descriptionCol);
            posterUrl = cursor.getString(posterCol);
        }

        ImageView iv = (ImageView) v.findViewById(R.id.pic);
        Picasso.with(iv.getContext())
                .load(posterUrl)
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .fit()
                .into(iv);
        CustomTextView title = (CustomTextView) v.findViewById(R.id.title);
        title.setText(titleStr);
        TextView description = (TextView) v.findViewById(R.id.description);
        description.setText(descriptionStr);

        LOGD("1113", type);

        if (type == "genre") {
            v.setBackgroundColor(context.getResources().getColor(R.color.material_blue_300));
            title.setTextColor(context.getResources().getColor(R.color.io15_white));
        } else if (type == "imdb") {
            v.setBackgroundColor(context.getResources().getColor(R.color.imdb_yellow));
            title.setTextColor(context.getResources().getColor(R.color.black_opacity_66));
            description.setTextColor(context.getResources().getColor(R.color.material_grey_600));
        } else if (type == "upcoming") {
            v.setBackgroundColor(context.getResources().getColor(R.color.tab_background));
        } else if (type == "detail") {
            v.setBackgroundColor(context.getResources().getColor(R.color.transparent_black));
            title.setTextColor(context.getResources().getColor(R.color.app_white));
            description.setTextColor(context.getResources().getColor(R.color.material_indigo_100));
        } else if (type == "main") {
            v.setBackgroundColor(context.getResources().getColor(R.color.primary_dark_material_dark));
            title.setTextColor(context.getResources().getColor(R.color.app_white));
            description.setTextColor(context.getResources().getColor(R.color.material_indigo_100));
        }
    }
}
