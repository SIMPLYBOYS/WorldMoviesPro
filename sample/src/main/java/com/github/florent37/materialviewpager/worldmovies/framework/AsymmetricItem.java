package com.github.florent37.materialviewpager.worldmovies.framework;

import android.os.Parcelable;

public interface AsymmetricItem extends Parcelable {
  int getColumnSpan();
  int getRowSpan();
}
