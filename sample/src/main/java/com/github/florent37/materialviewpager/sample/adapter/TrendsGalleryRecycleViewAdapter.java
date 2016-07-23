package com.github.florent37.materialviewpager.sample.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.framework.ImageTrasformation;
import com.github.florent37.materialviewpager.sample.model.TrendsObject;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by aaron on 2016/6/19.
 */
public class TrendsGalleryRecycleViewAdapter extends RecyclerView.Adapter<TrendsGalleryRecycleViewAdapter.GalleryItemHolder> {

    private ArrayList<TrendsObject.GalleryItem> mItems;

    private AdapterView.OnItemClickListener mOnItemClickListener;

    private int mPlaceholderSize = 0; //default value

    private ProgressBar mProgressBar;

    private TrendsObject trendsObject;

    public boolean inAlbum;

    public TrendsGalleryRecycleViewAdapter(TrendsObject trendsObject, boolean album) {
        mItems = new ArrayList<TrendsObject.GalleryItem>();
        inAlbum = album;
        this.trendsObject = trendsObject;
    }

    /*
     * A common adapter modification or reset mechanism. As with ListAdapter,
     * calling notifyDataSetChanged() will trigger the RecyclerView to update
     * the view. However, this method will not trigger any of the RecyclerView
     * animation features.
     */
    public void setItemCount(int count) {
        mItems.clear();
//        mItems.addAll(generateDummyData(count));
        notifyDataSetChanged();
    }

    /*
     * Inserting a new item at the head of the list. This uses a specialized
     * RecyclerView method, notifyItemInserted(), to trigger any enabled item
     * animations in addition to updating the view.
     */
    public void addItem(int position, TrendsObject.GalleryItem Item) {
        if (position > mItems.size()) return;
        mItems.add(position, Item);
        notifyItemInserted(position);
    }

    /*
     * Inserting a new item at the head of the list. This uses a specialized
     * RecyclerView method, notifyItemRemoved(), to trigger any enabled item
     * animations in addition to updating the view.
     */
    public void removeItem(int position) {
        if (position >= mItems.size()) return;

        mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public GalleryItemHolder onCreateViewHolder(ViewGroup container, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View root = null;
        if (inAlbum) {
            root = inflater.inflate(R.layout.album_pic, container, false);
        } else {
            root = inflater.inflate(R.layout.gallery_pic, container, false);
        }
        mProgressBar = (ProgressBar)root.findViewById(android.R.id.progress);

        return new GalleryItemHolder(root, this);
    }

    @Override
    public void onBindViewHolder(GalleryItemHolder itemHolder, int position) {
        TrendsObject.GalleryItem item = mItems.get(position - mPlaceholderSize);
        if (item!= null) {
            final String Url = item.getUrl();
            if (!Url.isEmpty()) {
                mProgressBar.setVisibility(View.VISIBLE);
                itemHolder.bind(itemHolder, item, mProgressBar);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    private void onItemHolderClick(GalleryItemHolder itemHolder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, itemHolder.itemView,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        }
    }

    public class GalleryItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TrendsGalleryRecycleViewAdapter mAdapter;
        private ImageView pictureView;

        public GalleryItemHolder(View itemView, TrendsGalleryRecycleViewAdapter adapter) {
            super(itemView);
            itemView.setOnClickListener(this);

            mAdapter = adapter;
            pictureView = (ImageView) itemView.findViewById(R.id.picture);
        }

        public void bind(GalleryItemHolder itemHolder, TrendsObject.GalleryItem item, final ProgressBar mProgressBar) {
            if (inAlbum) {
                Picasso.with(pictureView.getContext()).load(item.getUrl()).placeholder(R.drawable.placeholder)
                        .transform(ImageTrasformation.getTransformation(itemHolder.pictureView))
                        .into(pictureView, new Callback() {
                            @Override
                            public void onSuccess() {
                                mProgressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            } else {

                Picasso.with(pictureView.getContext()).load(item.getUrl()).placeholder(R.drawable.placeholder)
                        .fit()
                        .centerCrop()
                        .into(pictureView, new Callback() {
                            @Override
                            public void onSuccess() {
                                mProgressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            }

        }

        @Override
        public void onClick(View v) {
            mAdapter.onItemHolderClick(this);
        }
    }

}
