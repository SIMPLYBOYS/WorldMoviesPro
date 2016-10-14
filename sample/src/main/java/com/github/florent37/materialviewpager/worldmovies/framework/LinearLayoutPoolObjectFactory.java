package com.github.florent37.materialviewpager.worldmovies.framework;

import android.content.Context;
import android.widget.LinearLayout;

public class LinearLayoutPoolObjectFactory implements PoolObjectFactory<LinearLayout> {

  private final Context context;

  public LinearLayoutPoolObjectFactory(final Context context) {
    this.context = context;
  }

  @Override public LinearLayout createObject() {
    return new LinearLayout(context, null);
  }
}
