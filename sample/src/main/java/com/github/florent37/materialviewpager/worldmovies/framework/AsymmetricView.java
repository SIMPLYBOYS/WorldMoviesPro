package com.github.florent37.materialviewpager.worldmovies.framework;

import android.view.View;

interface AsymmetricView {
  boolean isDebugging();
  int getNumColumns();
  boolean isAllowReordering();
  void fireOnItemClick(int index, View v);
  boolean fireOnItemLongClick(int index, View v);
  int getColumnWidth();
  int getDividerHeight();
  int getRequestedHorizontalSpacing();
}
