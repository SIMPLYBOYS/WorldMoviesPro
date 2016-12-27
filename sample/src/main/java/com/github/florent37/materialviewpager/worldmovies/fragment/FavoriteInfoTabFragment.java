package com.github.florent37.materialviewpager.worldmovies.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.adapter.FavoriteMoviesRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.adapter.nyTimesFavoriteRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.favorite.MoviesFavoriteDetail;
import com.github.florent37.materialviewpager.worldmovies.favorite.MoviesFavoritePreference;
import com.github.florent37.materialviewpager.worldmovies.favorite.nyTimesFavoriteDetail;
import com.github.florent37.materialviewpager.worldmovies.framework.FlatButton;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONArrayRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.model.TrendsObject;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesMovie;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesDetailActivity;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesFavoritePreference;
import com.github.florent37.materialviewpager.worldmovies.trends.TrendsDetail;
import com.github.florent37.materialviewpager.worldmovies.util.BuildModelUtils;
import com.github.florent37.materialviewpager.worldmovies.util.UIUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;
import static com.github.florent37.materialviewpager.worldmovies.util.UIUtils.checkMoviesBookmark;

/**
 * Created by aaron on 2016/8/13.
 */
public class FavoriteInfoTabFragment extends InfoTabFragment {

    private RequestQueue mQueue;
    private List<nyTimesMovie> nyTimesList, trendsList;
    private ImageView nyTimesPicNumView, trendsPicNumView;
    private User user;
    private RecyclerView nytimesRecyclerview, moviesRecyclerview;
    private FlatButton nyTimesAllButton, trendsAllButton;
    private nyTimesFavoriteRecycleViewAdapter nyTimesFavoriteAdapter;
    private FavoriteMoviesRecycleViewAdapter moviesFavoriteAdapter;
    private LinearLayoutManager nylinearLayoutManager, trlinearLayoutManager;
    private nyTimesFavoritePreference nytimesFavor;
    private MoviesFavoritePreference moviesFavor;
    private TextView nytimes_picNum, trends_picNum;

    public static FavoriteInfoTabFragment newInstance(User user) {
        FavoriteInfoTabFragment fragment = new FavoriteInfoTabFragment();
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        args.putInt("index", 0);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = (User) getArguments().getSerializable("user");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("1118", "onCreateView");
        View view = inflater.inflate(R.layout.favorite_info_fragment, container, false);
        nyTimesList = new ArrayList<>();
        trendsList = new ArrayList<>();
        mQueue = CustomVolleyRequestQueue.getInstance(getActivity()).getRequestQueue();
        nylinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        trlinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        nytimesFavor = new nyTimesFavoritePreference();
        moviesFavor = new MoviesFavoritePreference();
        nytimesFavor.clearFavorite(getActivity());
        moviesFavor.clearFavorite(getActivity());
        nyTimesAllButton = (FlatButton) view.findViewById(R.id.nytimes_all);
        nyTimesAllButton.setButtonColor(getResources().getColor(R.color.material_grey_500));
        trendsAllButton = (FlatButton) view.findViewById(R.id.trends_all);
        trendsAllButton.setButtonColor(getResources().getColor(R.color.material_deep_orange_A100));
        /*fbLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        facebookAllButton =(FlatButton) view.findViewById(R.id.facebook_all);
        facebookAllButton.setButtonColor(getResources().getColor(R.color.facebook));*/
        return view;
    }

    private boolean checkNytimesBookmark(String headline) {
        headline = headline.indexOf(":") != -1 ? headline.split(":")[1].trim() : headline;
        ArrayList list = nytimesFavor.loadFavorites(getActivity().getApplicationContext());

        if (list == null)
            return false;

        for (int i=0; i<list.size(); i++) {
            if (headline.compareTo((String) list.get(i)) == 0) return true;
        }

        return false;
    };

    @Override
    public void onResume() {
        super.onResume();
        Log.d("1118", "onResume");
        nyTimesAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), nyTimesFavoriteDetail.class);
                intent.putExtra("user", user);
                ActivityCompat.startActivity(getActivity(), intent, null);
            }
        });

        trendsAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MoviesFavoriteDetail.class);
                intent.putExtra("user", user);
                ActivityCompat.startActivity(getActivity(), intent, null);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LOGD("1118", "onActivityCreated");
        nytimesRecyclerview = (RecyclerView) getView().findViewById(R.id.nytimes_recyclerview);
        moviesRecyclerview = (RecyclerView) getView().findViewById(R.id.trends_recyclerview);
        nytimesRecyclerview.setLayoutManager(nylinearLayoutManager);
        moviesRecyclerview.setLayoutManager(trlinearLayoutManager);
        /*facebookRecyclerview = (RecyclerView) getView().findViewById(R.id.facebook_recyclerview);
        facebookRecyclerview.setLayoutManager(fbLayoutManager);
        facebookFavoriteRecycleViewAdapter = new FacebookFavoriteRecycleViewAdapter(facebookList);
        facebookRecyclerview.setAdapter(facebookFavoriteRecycleViewAdapter);*/
        nyTimesFavoriteAdapter = new nyTimesFavoriteRecycleViewAdapter(nyTimesList);
        moviesFavoriteAdapter = new FavoriteMoviesRecycleViewAdapter(trendsList);
        nytimesRecyclerview.setAdapter(nyTimesFavoriteAdapter);
        nytimesRecyclerview.setNestedScrollingEnabled(false);
        moviesRecyclerview.setAdapter(moviesFavoriteAdapter);
        moviesRecyclerview.setNestedScrollingEnabled(false);

        nyTimesFavoriteAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final nyTimesMovie movie = nyTimesList.get(position);
                CustomJSONObjectRequest jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, Config.HOST_NAME + "nyTimes?url=" + movie.getLink(), new JSONObject(), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String story,imageUrl, head, description, editor, date, url;
                            JSONArray contents = response.getJSONArray("contents");
                            JSONObject reviewObj = contents.getJSONObject(0);
                            story = reviewObj.getString("story");
                            editor = reviewObj.getString("editor");
                            date = reviewObj.getString("date");
                            url = reviewObj.getString("url");
                            JSONObject imgObj = reviewObj.getJSONObject("image");

                            if (imgObj.has("src")) {
                                imageUrl = imgObj.getString("src");
                                description = imgObj.getString("description");
                            } else {
                                imageUrl = null;
                                description = null;
                            }

                            head = movie.getHeadline();
                            nyTimesMovie movie = new nyTimesMovie(head, description, story, url, imageUrl, editor ,date);
                            if (checkNytimesBookmark(head))
                                movie.setBookmark(true);
                            Intent intent = new Intent(getActivity(), nyTimesDetailActivity.class);
                            intent.putExtra("movie", movie);
                            ActivityCompat.startActivity(getActivity(), intent, null);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener () {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "Remote Server not working!", Toast.LENGTH_LONG).show();
                    }
                });
                mQueue.add(jsonRequest_q);
            }
        });

        moviesFavoriteAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final nyTimesMovie movie = trendsList.get(position);
                String url = UIUtils.getTrendsUrl(movie);
                Log.d("1115", url);

                CustomJSONObjectRequest jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray contents = response.getJSONArray("contents");
                            TrendsObject item = BuildModelUtils.buildTrendsModel(contents, true, movie.getChannel());
                            String title = item.getTitle();
                            Intent intent = new Intent(getActivity(), TrendsDetail.class);
                            if (checkMoviesBookmark(title, moviesFavor, getApplicationContext()))
                                item.setBookmark(true);
                            intent.putExtra(TrendsDetail.TRENDS_OBJECT, item);
                            ActivityCompat.startActivity(getActivity(), intent, null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener () {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "Remote Server not working!", Toast.LENGTH_LONG).show();
                    }
                });

                mQueue.add(jsonRequest_q);
            }
        });

        /*facebookFavoriteRecycleViewAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final nyTimesMovie movie = facebookList.get(position);
                Context context = view.getContext();
                Intent intent = new Intent(context, ContentWebViewActivity.class);
                intent.putExtra("url", movie.getLink());
                context.startActivity(intent);
            }
        });
        fetch_watched_movies();*/
        if (user != null) {
            fetch_nytimes();
            fetch_movies();
        }
    }

    private void fetch_watched_movies() {
        /*facebookPicNumView = (ImageView) getView().findViewById(R.id.facebook_media);
        Picasso.with(facebookPicNumView.getContext()).load(R.drawable.facebook).into(facebookPicNumView);*/

        CustomJSONObjectRequest jsonRequest_f = new CustomJSONObjectRequest(Request.Method.GET, "https://graph.facebook.com/me/video.watches?access_token="+user.accessToken, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray contents = ((JSONObject) response).getJSONArray("data");
                    for (int i=0; i< contents.length(); i++) {
                        JSONObject movieObj = contents.getJSONObject(i);
                        String head = movieObj.getJSONObject("data").getJSONObject("movie").getString("title");
                        String link = movieObj.getJSONObject("data").getJSONObject("movie").getString("url");
                        nyTimesMovie movie = new nyTimesMovie(head, null, null, link, null, null, null);
//                        facebookList.add(facebookList.size(), movie);
                    }
//                    facebook_picNum = (TextView) getView().findViewById(R.id.facebook_picNum);
//                    facebook_picNum.setText(" " + facebookList.size());
//                    fbLayoutManager.scrollToPositionWithOffset(1,650);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Remote Server connect fail from MainActivity!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest_f);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("1118","onDestory");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("1118","onStart");
    }

    private void fetch_nytimes() {
        nyTimesPicNumView = (ImageView) getView().findViewById(R.id.nytimes_media);
        Picasso.with(nyTimesPicNumView.getContext()).load(R.drawable.nytimes).into(nyTimesPicNumView);

        CustomJSONArrayRequest jsonRequest = new CustomJSONArrayRequest(Config.HOST_NAME + "my_nyTimes/"+user.id, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONArray contents = ((JSONArray) response);
                try {
                    for (int i = 0; i < contents.length(); i++) {
                        JSONObject movieObj = contents.getJSONObject(i);
                        String head = movieObj.getString("headline");
                        nytimesFavor.addFavorite(getActivity(),head);
                        String link = movieObj.getString("link");
                        String picUrl = movieObj.getString("picUrl");
                        nyTimesMovie movie = new nyTimesMovie(head, null, null, link, picUrl, null, null);
                        nyTimesList.add(nyTimesList.size(), movie);
                    }

                    if (getView() == null)
                        return;

                    nytimes_picNum = (TextView) getView().findViewById(R.id.nytimes_picNum);
                    nytimes_picNum.setText("  "+nyTimesList.size());
                    nylinearLayoutManager.scrollToPositionWithOffset(1,650);
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

    private void fetch_movies() {
        CustomJSONArrayRequest jsonRequest = new CustomJSONArrayRequest(Config.HOST_NAME + "my_movies/"+user.id, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONArray contents = ((JSONArray) response);
                try {
                    for (int i = 0; i < contents.length(); i++) {
                        JSONObject movieObj = contents.getJSONObject(i);
                        String title = movieObj.getString("title");
                        String link = movieObj.getString("link");
                        int channel = 14;
                        moviesFavor.addFavorite(getActivity(), title);
                        String picUrl = movieObj.getString("picUrl");
                        nyTimesMovie movie = new nyTimesMovie(title, null, null, link, picUrl, null, null);
                        if (movieObj.has("channel"))
                            channel = movieObj.getInt("channel");
                        if (movieObj.has("country"))
                            movie.setCountry(movieObj.getString("country"));
                        movie.setChannel(channel);
                        trendsList.add(trendsList.size(), movie);
                    }

                    if (getView() == null)
                        return;
                    trends_picNum = (TextView) getView().findViewById(R.id.trends_picNum);
                    trendsPicNumView = (ImageView) getView().findViewById(R.id.trends_media);
                    Picasso.with(trendsPicNumView.getContext()).load(R.drawable.ic_movie).into(trendsPicNumView);
                    trends_picNum.setText("  "+trendsList.size());
                    trlinearLayoutManager.scrollToPositionWithOffset(1,650);
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
