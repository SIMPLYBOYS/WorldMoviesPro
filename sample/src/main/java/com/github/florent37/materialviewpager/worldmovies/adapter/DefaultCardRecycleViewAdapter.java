package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.model.MyObject;
import com.joooonho.SelectableRoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaron on 2016/3/28.
 */

public class DefaultCardRecycleViewAdapter extends RecyclerView.Adapter<DefaultCardRecycleViewAdapter.ViewHolder> {

    List<MyObject> mContentItems;

    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;
    final Context context;

    private String cardType;

    //the constants value of the header view
    static final int TYPE_PLACEHOLDER = Integer.MIN_VALUE;

    private AdapterView.OnItemClickListener mOnItemClickListener;

    //the size taken by the header
    private int mPlaceholderSize = 1;

    public DefaultCardRecycleViewAdapter(Context context) {
        mContentItems = new ArrayList<>();
        this.context = context;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public void clearItem(){
        mContentItems.clear();
    }

    public List <MyObject> getItem(){
        return mContentItems;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    /*
    * Inserting a new item at the head of the list. This uses a specialized
    * RecyclerView method, notifyItemInserted(), to trigger any enabled item
    * animations in addition to updating the view.
    */
    public void addItem(int position, MyObject object) {
//        Log.d("0414", "mContentItems size: " + mContentItems.size() + " position: " +position);
        if (position > mContentItems.size()) return;
        mContentItems.add(position, object);
        notifyItemInserted(mContentItems.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView textViewView;
        private TextView desciptView;
        private ImageView imageView;
        private ImageView thumbnailView;
        SelectableRoundedImageView iv2;
        private DefaultCardRecycleViewAdapter mAdapter;

        public ViewHolder(View itemView, DefaultCardRecycleViewAdapter adapter) {
            super(itemView);
            int tag = (Integer)itemView.getTag();
            if (tag != TYPE_PLACEHOLDER){
                itemView.setOnClickListener(this);
                mAdapter = adapter;
                textViewView = (TextView) itemView.findViewById(R.id.text);
                desciptView = (TextView) itemView.findViewById(R.id.description);
                imageView = (ImageView) itemView.findViewById(R.id.image);
                thumbnailView = (ImageView) itemView.findViewById(R.id.thumbnail);
                Picasso.with(imageView.getContext()).load(R.drawable.parisguidetower).into(imageView);
            }
        }

        @Override
        public void onClick(View v) {
            mAdapter.onItemHolderClick(this);;
        }

        public void bind(MyObject myObject){

            Log.d("0320", "bind viewholder");
            String location = myObject.getLocation();
            if (textViewView == null)
                return;

            textViewView.setText(location);
            desciptView.setText(myObject.getDescription());
            Picasso.with(imageView.getContext()).load(myObject.getImageUrl()).centerCrop().fit().into(imageView);

            switch (location){
                case "France":
                    Picasso.with(thumbnailView.getContext()).load(R.drawable.fr).into(thumbnailView);
                    break;
                case "Angleterre":
                    Picasso.with(thumbnailView.getContext()).load(R.drawable.gb).into(thumbnailView);
                    break;
                case "Allemagne":
                    Picasso.with(thumbnailView.getContext()).load(R.drawable.de).into(thumbnailView);
                    break;
                case "Espagne":
                    Picasso.with(thumbnailView.getContext()).load(R.drawable.es).into(thumbnailView);
                    break;
                case "Italie":
                    Picasso.with(thumbnailView.getContext()).load(R.drawable.it).into(thumbnailView);
                    break;
                case "Russie":
                    Picasso.with(thumbnailView.getContext()).load(R.drawable.ru).into(thumbnailView);
                    break;
                default:
                    Picasso.with(thumbnailView.getContext()).load(R.drawable.tw).into(thumbnailView);
                    break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mPlaceholderSize)
            return TYPE_PLACEHOLDER;

        position = position - mPlaceholderSize;

        if (position == 0)
            return TYPE_HEADER;
        else
            return TYPE_CELL;
    }

    @Override
    public int getItemCount() {

        Log.d("0324", "getItemCount: " + mContentItems.size() + mPlaceholderSize);

        if (mContentItems != null){
            return mContentItems.size() + mPlaceholderSize;
        }
        return 0;
    }

    private void onItemHolderClick(ViewHolder itemHolder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, itemHolder.itemView,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup container, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(container.getContext());

        View root;
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        Boolean cardType = settings.getBoolean("miniCard", false);

        Log.d("0216", "viewType: " + viewType);
        Log.d("0415", "cardType: " + cardType);
        switch (viewType) {
            case TYPE_PLACEHOLDER: {
                Log.d("0320","PAGER PLACEHOLDER");
                if (cardType)
                    root = inflater.inflate(R.layout.material_view_pager_mini_placeholder, container, false);
                else
                    root = inflater.inflate(R.layout.material_view_pager_placeholder, container, false);
                root.setTag(viewType);
                return new ViewHolder(root, this);
            }
            case TYPE_HEADER: {
                Log.d("0320", "TOP HEADER");
                if (cardType)
                    root = inflater.inflate(R.layout.list_item_card_small, container, false);
                else
                    root = inflater.inflate(R.layout.list_item_card_big, container, false);
                root.setTag(viewType);
                return new ViewHolder(root, this);
            }
            case TYPE_CELL: {
                Log.d("0320","TOP CELL");
                if (cardType)
                    root = inflater.inflate(R.layout.list_item_card_small, container, false);
                else
                    root = inflater.inflate(R.layout.list_item_card_big, container, false);
                root.setTag(viewType);
                return new ViewHolder(root, this);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d("0327","position: " + position + " getItemViewType(position): " + getItemViewType(position));

        switch (getItemViewType(position)) {
            case TYPE_PLACEHOLDER:
                Log.d("0327", "MaterialViewPager->onBindViewHolder@ placeHolder");
                break;
            default:
                Log.d("0414", "mContentItems: " +mContentItems.size());

                MyObject myObject = mContentItems.get(position - mPlaceholderSize);
                final String location = myObject.getLocation();

                Log.d("0327", String.valueOf(position) + " location:" + location);
                holder.bind(myObject);
                break;
        }
    }
}
