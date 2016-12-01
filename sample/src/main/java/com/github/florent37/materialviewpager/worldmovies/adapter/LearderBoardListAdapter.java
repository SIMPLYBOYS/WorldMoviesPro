package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.favorite.FavoriteActivity;
import com.github.florent37.materialviewpager.worldmovies.framework.ImageTrasformation;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.github.florent37.materialviewpager.worldmovies.util.UsersUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by @vitovalov on 30/9/15.
 */
public class LearderBoardListAdapter extends RecyclerView.Adapter<LearderBoardListAdapter.MyViewHolder> {

    private List<User> mListData;
    private Activity activity;
    private final int TYPE_HEADER = 0;
    private final int TYPE_USER = 1;
    private final int TYPE_CELL = 2;

    public LearderBoardListAdapter(List<User> mListData, Activity activity) {
        this.mListData = mListData;
        this.activity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view ;

        switch (viewType) {
            case TYPE_HEADER:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_header,
                        viewGroup, false);
                return new MyViewHolder(view);
            case TYPE_CELL:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_person,
                        viewGroup, false);
                return new MyViewHolder(view);
            case TYPE_USER:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_user,
                        viewGroup, false);
                return new MyViewHolder(view);
        }

        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_USER;
        else if (position == 1)
            return TYPE_HEADER;
        else
            return TYPE_CELL;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                break;
            case TYPE_USER:
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

        private TextView title, description, totalView;
        private ImageView pictureView, followView;
        private LinearLayout linearLayout;
        private RequestQueue mQueue;
        private User person;

        public MyViewHolder(View itemView) {
            super(itemView);
            pictureView = (ImageView) itemView.findViewById(R.id.avatar);
            followView = (ImageView) itemView.findViewById(R.id.follow);
            totalView = (TextView) itemView.findViewById(R.id.listitem_total);
            title = (TextView) itemView.findViewById(R.id.listitem_name);
            description = (TextView) itemView.findViewById(R.id.listitem_description);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
            mQueue = CustomVolleyRequestQueue.getInstance(activity).getRequestQueue();
        }

        public void bind(MyViewHolder itemHolder, final User user) {
            this.person = user;
            Picasso.with(pictureView.getContext()).load(user.pictureUrl).placeholder(R.drawable.placeholder)
                    .transform(ImageTrasformation.getTransformation(itemHolder.pictureView))
                    .into(pictureView, new Callback() {
                        @Override
                        public void onSuccess() {}

                        @Override
                        public void onError() {}
                    });
            title.setText(person.name);
            description.setText(person.description);
            totalView.setText("共"+String.valueOf(person.total)+"項電影訊息");
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity, FavoriteActivity.class);
                    intent.putExtra("user", person);
//                    startActivityForVersion(intent);
                    activity.startActivity(intent);
                }
            });

            if (followView != null) {
                if (person.follow) {
                    followView.setImageResource(R.drawable.ic_remove);
                    followView.setTag("add");
                } else {
                    followView.setImageResource(R.drawable.ic_add);
                    followView.setTag("remove");
                }
                followView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CustomJSONObjectRequest jsonRequest_q = null;
                        User user = UsersUtils.getCurrentUser(getApplicationContext());
                        String url = Config.HOST_NAME + "/follow/"+person.id+"/"+user.id;;

                        if (view.getTag() == "remove") {
                            view.setTag("add");
                            followView.setImageResource(R.drawable.ic_remove);
                            jsonRequest_q = new CustomJSONObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        String result = response.getString("content");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(), "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
                                }
                            });


                        } else if (view.getTag() == "add") {
                            view.setTag("remove");
                            followView.setImageResource(R.drawable.ic_add);
                            jsonRequest_q = new CustomJSONObjectRequest(Request.Method.DELETE, url, null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        String result = response.getString("content");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(), "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        mQueue.add(jsonRequest_q);

                    }
                });
            }
        }
    }

    private void startActivityForVersion(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(
                            activity).toBundle());
        }
        else {
            activity.startActivity(intent);
        }
    }

}

