package com.github.florent37.materialviewpager.sample.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by aaron on 2016/5/2.
 */
public class ProportionalImageView extends ImageView {

    public ProportionalImageView(Context context) {
        super(context);
    }

    public ProportionalImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProportionalImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = getDrawable();
        if (d != null) {
            if (d.getIntrinsicWidth() >= d.getIntrinsicHeight()) {
                Log.d("0504", "case1");
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = width * d.getIntrinsicHeight() / d.getIntrinsicWidth();
                setMeasuredDimension(width, height);
            } else {
                Log.d("0504", "case2");
                int height = MeasureSpec.getSize(heightMeasureSpec);
                int width = height * d.getIntrinsicWidth() / d.getIntrinsicHeight();
                setMeasuredDimension(width, height);
            }
        }
        else
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}