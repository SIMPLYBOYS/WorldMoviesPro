package com.github.florent37.materialviewpager.sample.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.github.florent37.materialviewpager.sample.framework.AsymmetricItem;


public class BlockItem implements AsymmetricItem {

  private int columnSpan;
  private int rowSpan;
  private int position;

  public BlockItem() {
    this(1, 1, 0);
  }

  public BlockItem(int columnSpan, int rowSpan, int position) {
    this.columnSpan = columnSpan;
    this.rowSpan = rowSpan;
    this.position = position;
  }

  public BlockItem(Parcel in) {
    readFromParcel(in);
  }

  @Override public int getColumnSpan() {
    return columnSpan;
  }

  @Override public int getRowSpan() {
    return rowSpan;
  }

  public int getPosition() {
    return position;
  }

  @Override public String toString() {
    return String.format("%s: %sx%s", position, rowSpan, columnSpan);
  }

  @Override public int describeContents() {
    return 0;
  }

  private void readFromParcel(Parcel in) {
    columnSpan = in.readInt();
    rowSpan = in.readInt();
    position = in.readInt();
  }

  @Override public void writeToParcel(@NonNull Parcel dest, int flags) {
    dest.writeInt(columnSpan);
    dest.writeInt(rowSpan);
    dest.writeInt(position);
  }

  /* Parcelable interface implementation */
  public static final Parcelable.Creator<BlockItem> CREATOR = new Parcelable.Creator<BlockItem>() {

    @Override public BlockItem createFromParcel(@NonNull Parcel in) {
      return new BlockItem(in);
    }

    @Override @NonNull
    public BlockItem[] newArray(int size) {
      return new BlockItem[size];
    }
  };
}
