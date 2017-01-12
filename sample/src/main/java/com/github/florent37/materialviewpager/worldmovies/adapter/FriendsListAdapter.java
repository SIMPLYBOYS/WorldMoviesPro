package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.favorite.FavoriteActivity;
import com.github.florent37.materialviewpager.worldmovies.framework.ImageTrasformation;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by aaron on 2016/11/20.
 */

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.MyViewHolder> {
    private List<User> mListData;
    private Activity activity;
    private final int TYPE_HEADER = 0;
    private final int TYPE_CELL = 1;
    public static final int REQUEST_CODE_VIEW_SHOT = 5407;

    public FriendsListAdapter(List<User> mListData, Activity activity) {
        this.mListData = mListData;
        this.activity = activity;
    }

    @Override
    public FriendsListAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view ;

        switch (viewType) {
            case TYPE_CELL:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_user,
                        viewGroup, false);
                return new FriendsListAdapter.MyViewHolder(view);
        }

        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_CELL;
    }

    @Override
    public void onBindViewHolder(FriendsListAdapter.MyViewHolder myViewHolder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                break;
            case TYPE_CELL:
                User user = mListData.get(position);
                myViewHolder.bind(myViewHolder, user);
        }
    }

    @Override
    public int getItemCount() {
        return mListData == null ? 0 : mListData.size();
    }

    protected class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView title, total, description;
        private ImageView pictureView;
        private LinearLayout linearLayout;
        private User user;

        public MyViewHolder(View itemView) {
            super(itemView);
            pictureView = (ImageView) itemView.findViewById(R.id.avatar);
            title = (TextView) itemView.findViewById(R.id.listitem_name);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
            total = (TextView) itemView.findViewById(R.id.listitem_total);
            description = (TextView) itemView.findViewById(R.id.listitem_description);
        }

        public void bind(FriendsListAdapter.MyViewHolder itemHolder, final User user) {
            this.user = user;
            Picasso.with(pictureView.getContext()).load(user.pictureUrl).placeholder(R.drawable.placeholder)
                    .transform(ImageTrasformation.getTransformation(itemHolder.pictureView))
                    .into(pictureView, new Callback() {
                        @Override
                        public void onSuccess() {}

                        @Override
                        public void onError() {}
                    });
            title.setText(user.name);
            if (user.total != 0)
                total.setText("共"+String.valueOf(user.total)+"則電影訊息");
            else
                total.setText("");
            description.setText(user.description);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity, FavoriteActivity.class);
                    intent.putExtra("user", user);
                    activity.startActivity(intent);
//                    startActivityForVersion(intent, view);
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                linearLayout.setBackgroundResource(R.drawable.item_click_background);
        }
    }

    private void startActivityForVersion(Intent intent, View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options =
                    ActivityOptions.makeSceneTransitionAnimation(activity,
                            Pair.create(view, activity.getString(R.string.transition_shot)),
                            Pair.create(view, activity.getString(R.string
                                    .transition_shot_background)));
            activity.startActivityForResult(intent, REQUEST_CODE_VIEW_SHOT, options.toBundle());
        }
        else {
            activity.startActivity(intent);
        }
    }
}
