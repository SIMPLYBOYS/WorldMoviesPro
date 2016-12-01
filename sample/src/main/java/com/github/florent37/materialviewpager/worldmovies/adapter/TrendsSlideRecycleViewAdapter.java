package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.model.TrendsObject;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by aaron on 2016/6/19.
 */
public class TrendsSlideRecycleViewAdapter extends RecyclerView.Adapter<TrendsSlideRecycleViewAdapter.SlideItemHolder> {
    private ArrayList<TrendsObject.GalleryItem> mItems;

    private AdapterView.OnItemClickListener mOnItemClickListener;

    private int mPlaceholderSize = 0; //default value

    private ProgressBar mProgressBar;

    private TrendsObject trendsObject;

    public TrendsSlideRecycleViewAdapter(TrendsObject trendsObject) {
        mItems = new ArrayList<TrendsObject.GalleryItem>();
        this.trendsObject = trendsObject;
    }

    /*
     * A common adapter modification or reset mechanism. As with LearderBoardListAdapter,
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
    public SlideItemHolder onCreateViewHolder(ViewGroup container, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View root = null;
        root = inflater.inflate(R.layout.slide_pic, container, false);
        mProgressBar = (ProgressBar)root.findViewById(android.R.id.progress);
        return new SlideItemHolder(root, this);
    }

    @Override
    public void onBindViewHolder(SlideItemHolder itemHolder, int position) {
        TrendsObject.GalleryItem item = mItems.get(position - mPlaceholderSize);
        if (item!= null) {
            final String Url = item.getUrl();
            Log.d("0502", String.valueOf(position) + " Url:" + Url);
            mProgressBar.setVisibility(View.VISIBLE);
            itemHolder.bind(item, mProgressBar);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    private void onItemHolderClick(SlideItemHolder itemHolder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.getAdapterPosition(), itemHolder.getItemId());
        }
    }

    public class SlideItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TrendsSlideRecycleViewAdapter mAdapter;
        private PhotoView pictureView;

        public SlideItemHolder(View itemView, TrendsSlideRecycleViewAdapter adapter) {
            super(itemView);
            itemView.setOnClickListener(this);

            mAdapter = adapter;
            pictureView = (PhotoView) itemView.findViewById(R.id.picture);
            pictureView.setAdjustViewBounds(true);
            Picasso.with(pictureView.getContext()).load(R.drawable.parisguidetower).into(pictureView);
        }

        public void bind(TrendsObject.GalleryItem item, final ProgressBar mProgressBar) {
            Picasso.with(pictureView.getContext()).load(item.getUrl()).placeholder(R.drawable.placeholder)
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

        @Override
        public void onClick(View v) {
            mAdapter.onItemHolderClick(this);
        }
    }
}
