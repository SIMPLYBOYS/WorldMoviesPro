package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.framework.FlatButton;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.imdb.ImdbActivity;
import com.github.florent37.materialviewpager.worldmovies.model.ImdbObject;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesMovie;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesActivity;
import com.github.florent37.materialviewpager.worldmovies.ptt.pttActivity;
import com.github.florent37.materialviewpager.worldmovies.upcoming.upComingActivity;
import com.github.florent37.materialviewpager.worldmovies.util.UsersUtils;
import com.moxun.tagcloudlib.view.TagCloudView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.github.florent37.materialviewpager.worldmovies.Config.HOST_NAME;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;
import static com.github.florent37.materialviewpager.worldmovies.util.UIUtils.getReleaseDate;

/**
 * Created by aaron on 2016/12/16.
 */

public class HomeRecycleViewAdapter extends RecyclerView.Adapter<HomeRecycleViewAdapter.HomeItemHolder> {

    private List<nyTimesMovie> mItems;
    private AdapterView.OnItemClickListener mOnItemClickListener;
    private ImageView PicView;
    private int mPlaceholderSize = 1; //default value
    private final int TYPE_NYTIMES = 0;
    private final int TYPE_IMDB = 1;
    private final int TYPE_UPCOMING = 2;
    private final int TYPE_PTT = 3;
    private TagCloudView tagCloudView;
    private TextTagsAdapter textTagsAdapter;
    private final int TYPE_PLACEHOLDER = Integer.MIN_VALUE;
    private RecyclerView nytimesRecyclerview, imdbRecyclerview, upcomingRecycleview;
    private LinearLayoutManager nylinearLayoutManager, trlinearLayoutManager, uclinearLayoutManager;
    private nyTimesFavoriteRecycleViewAdapter nyTimesAdapter;
    private FavoriteMoviesRecycleViewAdapter imdbAdapter, upcomingAdapter;
    private ProgressBar progressBar;
    private List<nyTimesMovie> nyTimesList;
    private List<ImdbObject> imdbList, upcomingList;
    private List<String> tagList;
    private ProgressBar mProgressBar;
    private User user;
    private RequestQueue mQueue;
    private FlatButton AllButton;
    private View root = null;
    private Activity activity;

    public HomeRecycleViewAdapter(List<nyTimesMovie> movieList, Activity activity) {
        mItems = movieList;
        user = UsersUtils.getCurrentUser(getApplicationContext());
        mProgressBar = (ProgressBar) activity.findViewById(R.id.progressBar);
        this.activity = activity;
    }

    @Override
    public HomeRecycleViewAdapter.HomeItemHolder onCreateViewHolder(ViewGroup container, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        switch (viewType) {
            case TYPE_PLACEHOLDER: {
                root = inflater.inflate(R.layout.material_view_pager_home_placeholder, container, false);
                root.setTag(viewType);
                mQueue = CustomVolleyRequestQueue.getInstance(root.getContext()).getRequestQueue();
                return new HomeRecycleViewAdapter.HomeItemHolder(root, this);
            }
            case TYPE_NYTIMES: {
                root = inflater.inflate(R.layout.nytimes_home, container, false);
                root.setTag(viewType);
                mQueue = CustomVolleyRequestQueue.getInstance(root.getContext()).getRequestQueue();
                nyTimesList = new ArrayList<>();
                nytimesRecyclerview = (RecyclerView) root.findViewById(R.id.nytimes_recyclerview);
                nylinearLayoutManager = new LinearLayoutManager(root.getContext(), LinearLayoutManager.HORIZONTAL, false);
                nytimesRecyclerview.setLayoutManager(nylinearLayoutManager);
                nyTimesAdapter = new nyTimesFavoriteRecycleViewAdapter(activity, nyTimesList);
                nytimesRecyclerview.setAdapter(nyTimesAdapter);
                nytimesRecyclerview.setNestedScrollingEnabled(false);
                return new HomeRecycleViewAdapter.HomeItemHolder(root, this);
            }
            case TYPE_IMDB: {
                root = inflater.inflate(R.layout.imdb_home, container, false);
                root.setTag(viewType);
                mQueue = CustomVolleyRequestQueue.getInstance(root.getContext()).getRequestQueue();
                imdbList = new ArrayList<>();
                imdbRecyclerview = (RecyclerView) root.findViewById(R.id.imdb_recyclerview);
                trlinearLayoutManager = new LinearLayoutManager(root.getContext(), LinearLayoutManager.HORIZONTAL, false);
                imdbAdapter = new FavoriteMoviesRecycleViewAdapter(activity, imdbList);
                imdbRecyclerview.setLayoutManager(trlinearLayoutManager);
                imdbRecyclerview.setAdapter(imdbAdapter);
                imdbRecyclerview.setNestedScrollingEnabled(false);
                return new HomeRecycleViewAdapter.HomeItemHolder(root, this);
            }
            case TYPE_UPCOMING: {
                root = inflater.inflate(R.layout.upcoming_home, container, false);
                root.setTag(viewType);
                mQueue = CustomVolleyRequestQueue.getInstance(root.getContext()).getRequestQueue();
                upcomingList = new ArrayList<>();
                upcomingRecycleview = (RecyclerView) root.findViewById(R.id.upcoming_recyclerview);
                uclinearLayoutManager = new LinearLayoutManager(root.getContext(), LinearLayoutManager.HORIZONTAL, false);
                upcomingAdapter =  new FavoriteMoviesRecycleViewAdapter(activity, upcomingList);
                upcomingRecycleview.setLayoutManager(uclinearLayoutManager);
                upcomingRecycleview.setAdapter(upcomingAdapter);
                upcomingRecycleview.setNestedScrollingEnabled(false);
                return new HomeRecycleViewAdapter.HomeItemHolder(root, this);
            }
            case TYPE_PTT: {
                root = inflater.inflate(R.layout.ptt_home, container, false);
                root.setTag(viewType);
                tagCloudView = (TagCloudView) root.findViewById(R.id.tag_cloud);
                tagCloudView.setBackgroundColor(activity.getResources().getColor(R.color.material_grey_200));
                tagList = new ArrayList<>();
                textTagsAdapter = new TextTagsAdapter(tagList, this.activity);
                tagCloudView.setAdapter(textTagsAdapter);
                tagCloudView.setNestedScrollingEnabled(false);
                return new HomeRecycleViewAdapter.HomeItemHolder(root, this);
            }
        }
        return null;
    }

    public void setItemCount(int count) {
        mItems.clear();
//        mItems.addAll(generateDummyData(count));
        notifyDataSetChanged();
    }

    public List <nyTimesMovie> getItem() {
        return mItems;
    }

    public void addItem(int position, nyTimesMovie Item) {
        if (position > mItems.size()) return;
        mItems.add(position, Item);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        if (position >= mItems.size()) return;

        mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mPlaceholderSize)
            return TYPE_PLACEHOLDER;

        position = position - mPlaceholderSize;

        if (position == 0)
            return TYPE_NYTIMES;
        else if (position == 1)
            return TYPE_IMDB;
        else if (position == 2)
            return TYPE_UPCOMING;
        else
            return TYPE_PTT;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    private void onItemHolderClick(HomeRecycleViewAdapter.HomeItemHolder itemHolder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, itemHolder.pictureView,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        }
    }

    @Override
    public void onBindViewHolder(HomeRecycleViewAdapter.HomeItemHolder itemHolder, int position) {
        LOGD("1216","position: " + position + " getItemViewType(position): " + getItemViewType(position));
        switch (getItemViewType(position)) {
            case TYPE_PLACEHOLDER:
                LOGD("0327", "MaterialViewPager->onBindViewHolder@placeHolder");
                break;
            default:
                nyTimesMovie item = mItems.get(position - mPlaceholderSize);
                if (item!= null) {
                    final String Url = item.getPicUrl();
                    if (!Url.isEmpty()) {
//                        mProgressBar.setVisibility(View.VISIBLE);
                        itemHolder.bind(itemHolder, item, null);
                    }
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (mItems != null) {
            return mItems.size() + mPlaceholderSize;
        }
        return 0;
    }

    public class HomeItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private HomeRecycleViewAdapter mAdapter;
        private ImageView pictureView;
        private TextView review;
        private int tag;

        public HomeItemHolder(View itemView, HomeRecycleViewAdapter adapter) {
            super(itemView);
            tag = (Integer) itemView.getTag();
            if (tag != TYPE_PLACEHOLDER) {
                itemView.setOnClickListener(this);
                mAdapter = adapter;
                pictureView = (ImageView) itemView.findViewById(R.id.picture);
                review = (TextView) itemView.findViewById(R.id.review_title);
            }
        }

        public void bind(HomeRecycleViewAdapter.HomeItemHolder itemHolder, nyTimesMovie item, final ProgressBar mProgressBar) {
            switch (tag) {
                case TYPE_NYTIMES:
                    fetch_nytimes();
                    PicView = (ImageView) root.findViewById(R.id.nytimes_media);
                    Picasso.with(root.getContext()).load(R.drawable.nytimes).into(PicView);
                    AllButton = (FlatButton) root.findViewById(R.id.nytimes_all);
                    AllButton.setButtonColor(root.getResources().getColor(R.color.material_grey_500));
                    AllButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), nyTimesActivity.class);
                            ActivityCompat.startActivity(v.getContext(), intent, null);
                        }
                    });
                    break;
                case TYPE_IMDB:
                    fetch_imdb();
                    PicView = (ImageView) root.findViewById(R.id.imdb_media);
                    Picasso.with(root.getContext()).load(R.drawable.imdb).into(PicView);
                    AllButton = (FlatButton) root.findViewById(R.id.imdb_all);
                    AllButton.setButtonColor(root.getResources().getColor(R.color.material_deep_orange_A100));
                    AllButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), ImdbActivity.class);
                            ActivityCompat.startActivity(v.getContext(), intent, null);
                        }
                    });
                    break;
                case TYPE_UPCOMING:
                    fetch_upcoming();
                    PicView = (ImageView) root.findViewById(R.id.upcoming_media);
                    Picasso.with(root.getContext()).load(R.drawable.ic_movie).into(PicView);
                    AllButton = (FlatButton) root.findViewById(R.id.upcoming_all);
                    AllButton.setButtonColor(root.getResources().getColor(R.color.material_brown_200));
                    AllButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), upComingActivity.class);
                            ActivityCompat.startActivity(v.getContext(), intent, null);
                        }
                    });
                    break;
                case TYPE_PTT:
                    fetch_ptt();
                    PicView = (ImageView) root.findViewById(R.id.ptt_media);
                    Picasso.with(root.getContext()).load(R.drawable.ic_bubble_chart).into(PicView);
                    AllButton = (FlatButton) root.findViewById(R.id.ptt_all);
                    AllButton.setButtonColor(root.getResources().getColor(R.color.facebook));
                    AllButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), pttActivity.class);
                            ActivityCompat.startActivity(v.getContext(), intent, null);
                        }
                    });
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            mAdapter.onItemHolderClick(this);
        }
    }

    private void fetch_nytimes() {
        String url = Config.HOST_NAME + "nyTimes_home";
        CustomJSONObjectRequest jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray contents = response.getJSONArray("contents");
                    for (int i = 0; i < contents.length(); i++) {
                        JSONObject movieObj = contents.getJSONObject(i);
                        String head = movieObj.getString("headline");
                        String picUrl = movieObj.getString("picUrl");
                        String link = movieObj.getString("link");
                        nyTimesMovie movie = new nyTimesMovie(head, null, null, link, picUrl, null, null);
                        nyTimesList.add(nyTimesList.size(), movie);
                    }

                    if (root == null)
                        return;
                    nylinearLayoutManager.scrollToPositionWithOffset(1,650);
                    mProgressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(getActivity(), "Remote Server connect fail from FavoriteInfoTabFragment!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
    }

    private void fetch_imdb() {
        Random random = new Random();
        int start = random.nextInt(240);
        String url = HOST_NAME + "/imdb_home?from="+start+"&to="+(start+10);
        CustomJSONObjectRequest jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray contents = response.getJSONArray("contents");
                    for (int i = 0; i < contents.length(); i++) {
                        JSONObject movieObj = contents.getJSONObject(i);
                        String title = movieObj.getString("title");
                        String link = movieObj.getString("detailUrl");
                        String picUrl = movieObj.getString("posterUrl");

                        ImdbObject movie = new ImdbObject(title, null, null, null,
                                null, picUrl, null, null, null,
                                null, null, null, null, null, null,
                                null, null, null, link);
                        imdbList.add(imdbList.size(), movie);
                    }

                    if (root == null)
                        return;
                    trlinearLayoutManager.scrollToPositionWithOffset(1,650);
                    mProgressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LOGD("0928", "Remote Server connect fail from GenreActivity!");
//                Toast.makeText(getActivity(), "Remote Server connect fail from GenreActivity!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
    }

    private void fetch_upcoming() {
        int skipSize = 0;
        Calendar c = Calendar.getInstance();
        String url = HOST_NAME + "upcoming?release_from=" + getReleaseDate(c.get(Calendar.MONTH), 0, 1)+"&skip="+skipSize;
        CustomJSONObjectRequest jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray contents = response.getJSONArray("contents");
                    for (int i = 0; i < contents.length(); i++) {
                        JSONObject movieObj = contents.getJSONObject(i);
                        String title = movieObj.getString("title");
                        String picUrl = movieObj.getString("posterUrl");
                        ImdbObject movie = new ImdbObject(title, null, null, null,
                                null, picUrl, null, null, null,
                                null, null, null, null, null, null,
                                null, null, null, null);
                        upcomingList.add(upcomingList.size(), movie);
                    }

                    if (root == null)
                        return;
                    uclinearLayoutManager.scrollToPositionWithOffset(1,650);
                    mProgressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LOGD("0928", "Remote Server connect fail from GenreActivity!");
//                Toast.makeText(getActivity(), "Remote Server connect fail from GenreActivity!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
    }

    public void fetch_ptt() {
        String url = HOST_NAME + "/ptt_home";
        CustomJSONObjectRequest jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray contents = response.getJSONArray("contents");
                    LOGD("1226", String.valueOf(contents));
                    for (int i = 0; i < contents.length(); i++) {
                        String tag = contents.getString(i);
                        tagList.add(tagList.size(), tag);
                    }
                    textTagsAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LOGD("0928", "Remote Server connect fail from GenreActivity!");
//                Toast.makeText(getActivity(), "Remote Server connect fail from GenreActivity!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
    }
}
