package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.model.PagerObject;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Random;

/**
 * Created by aaron on 2016/9/26.
 */

public class CustomPagerAdapter extends PagerAdapter {
    private final Random random = new Random();
    private Context context;
    private List<PagerObject> pagerObjectList;
    private LayoutInflater layoutInflater;
    private WindowManager windowManager;

    public CustomPagerAdapter(Context context, List<PagerObject> pagerObjectList) {
        this.context = context;
        this.pagerObjectList = pagerObjectList;
        this.layoutInflater = (LayoutInflater)this.context.getSystemService(this.context.LAYOUT_INFLATER_SERVICE);
    }

    @Override public int getCount() {
        return pagerObjectList.size();
    }

    @Override public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override public void destroyItem(ViewGroup view, int position, Object object) {
        view.removeView((View) object);
    }

    @Override public Object instantiateItem(ViewGroup container, int position) {
        View view = this.layoutInflater.inflate(R.layout.pager_list_items, container, false);
        ImageView displayImage = (ImageView)view.findViewById(R.id.large_image);
        TextView imageText = (TextView)view.findViewById(R.id.image_name);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

//        displayImage.setImageResource(this.pagerObjectList.get(position).getImageId());
        Picasso.with(displayImage.getContext()).load(this.pagerObjectList.get(position)
                .getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(displayImage);

        imageText.setText(this.pagerObjectList.get(position).getImageName());
        imageText.setGravity(Gravity.CENTER);
        imageText.setTextColor(Color.WHITE);
        view.setBackgroundColor(0xff000000 | random.nextInt(0x00ffffff));
        container.addView(view, params);
        return view;
    }
}
