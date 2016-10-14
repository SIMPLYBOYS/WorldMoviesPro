package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.materialviewpager.worldmovies.R;
import com.joooonho.SelectableRoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TestRecyclerViewAdapter extends RecyclerView.Adapter<TestRecyclerViewAdapter.ViewHolder> {

    final List<MyObject> contents;

    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;

    //the constants value of the header view
    static final int TYPE_PLACEHOLDER = Integer.MIN_VALUE;

    //the size taken by the header
    private int mPlaceholderSize = 1;

    private Listener listener;

    public TestRecyclerViewAdapter(List<MyObject> contents) {
        this.contents = contents;
    }

    public static interface Listener {
        public void onClick(int position, final String location);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public static class MyObject {
        private String text;
        private String imageUrl;
        private String description;

        public MyObject(String location, String description, String imageUrl) {
            this.text = location;
            this.imageUrl = imageUrl;
            this.description = description;
        }

        public String getLocation() {
            return text;
        }

        public void setLocation(String text) {
            this.text = text;
        }

        public String getDescription(){
            return description;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        private TextView textViewView;
        private TextView desciptView;
        private ImageView imageView;
        private ImageView thumbnailView;
        SelectableRoundedImageView iv2;

        public ViewHolder(View itemView){
            super(itemView);
            int tag = (Integer) itemView.getTag();
            if (tag != TYPE_PLACEHOLDER) {
                mView = itemView;
                textViewView = (TextView) itemView.findViewById(R.id.text);
                desciptView = (TextView) itemView.findViewById(R.id.description);
                imageView = (ImageView) itemView.findViewById(R.id.image);
                thumbnailView = (ImageView) itemView.findViewById(R.id.thumbnail);
                Picasso.with(imageView.getContext()).load(R.drawable.parisguidetower).into(imageView);
            }
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

        Log.d("0324", "getItemCount: " + contents.size() + mPlaceholderSize);

        if (contents != null){
            return contents.size() + mPlaceholderSize;
        }
        return 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        Log.d("0216", "viewType: " + viewType);
        switch (viewType) {
            case TYPE_PLACEHOLDER: {
                Log.d("0320","PAGER PLACEHOLDER");
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.material_view_pager_placeholder, parent, false);
                view.setTag(viewType);
                return new ViewHolder(view);
            }
            case TYPE_HEADER: {
                Log.d("0320","TOP HEADER");
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_card_big, parent, false);
                view.setTag(viewType);
                return new ViewHolder(view);
            }
            case TYPE_CELL: {
                Log.d("0320","TOP CELL");
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_card_big, parent, false);
                view.setTag(viewType);
                return new ViewHolder(view);
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
                MyObject myObject = contents.get(position - mPlaceholderSize);
                final String location = myObject.getLocation();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(), " Item " + position + " from " + location + " got clicked! with listener " + listener, Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onClick(position, location);
                        }
                    }
                });

                Log.d("0327", String.valueOf(position) + " location:" + location);
                holder.bind(myObject);//TODO different binding logic base on position
                break;
        }
    }
}