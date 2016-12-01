package com.github.florent37.materialviewpager.worldmovies.framework;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.squareup.picasso.Transformation;

/**
 * Created by aaron on 2016/5/15.
 */
public class ImageTrasformation {

    public static Transformation getTransformation(final ImageView imageView) {
        return new Transformation() {
            @Override
            public Bitmap transform(Bitmap source) {
                int targetWidth = imageView.getWidth();

                double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
                int targetHeight = (int) (targetWidth * aspectRatio);
                Bitmap result;

                if (targetWidth == 0 && targetHeight == 0) {
                    result = Bitmap.createScaledBitmap(source, 400, 400, false);
                } else {
                    result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                }

                if (result != source) {
                    source.recycle();
                }

                return result;
            }

            @Override
            public String key() {
                return "transformation" + " desiredWidth";
            }
        };
    }
}
