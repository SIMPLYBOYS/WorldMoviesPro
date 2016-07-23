package com.github.florent37.materialviewpager.sample.framework;

import android.os.Parcelable;

public interface AsymmetricItem extends Parcelable {
  int getColumnSpan();
  int getRowSpan();
}
