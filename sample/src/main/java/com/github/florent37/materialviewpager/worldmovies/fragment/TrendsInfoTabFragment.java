package com.github.florent37.materialviewpager.worldmovies.fragment;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.aakira.expandablelayout.ExpandableLayout;
import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.github.aakira.expandablelayout.Utils;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.adapter.TrendsGalleryRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.framework.CustomLightBoxActivity;
import com.github.florent37.materialviewpager.worldmovies.framework.CustomTextView;
import com.github.florent37.materialviewpager.worldmovies.framework.FlatButton;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.imdb.SlideActivity;
import com.github.florent37.materialviewpager.worldmovies.model.TrendsObject;
import com.github.florent37.materialviewpager.worldmovies.trends.TrendsAlbumActivity;
import com.github.florent37.materialviewpager.worldmovies.trends.TrendsDetail;
import com.github.florent37.materialviewpager.worldmovies.trends.TrendsSlideActivity;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaron on 2016/6/21.
 */
public class TrendsInfoTabFragment extends InfoTabFragment implements AdapterView.OnItemClickListener {
    private ImageView thumbnailView, criticsView, audienceView;
    private View buttonLayout;
    private TextView plot, genre, runtime, country, picNum, year, studio, trailer_title, imdb_point, critics_point, audience_point, director;
    private String REQUEST_TAG = "TrendsInfoTabFragment";
    private FlatButton allButton;
    private CustomTextView description;
    private RecyclerView galleryRecyclerView;
    private String VIDEO_KEY;
    private RequestQueue mQueue;
    private ExpandableRelativeLayout expandableLayout;
    private TrendsGalleryRecycleViewAdapter trendsGalleryAdapter;
    private LinearLayoutManager linearLayoutManager;
    private List<TrendsObject.GalleryItem> list = null;
    YouTubeThumbnailView youTubeThumbnailView;
//    ObservableScrollView nested_scrollview;
    NestedScrollView nested_scrollview;
    private TrendsObject trendsObject;

    public static TrendsInfoTabFragment newInstance(Object object) {
        TrendsInfoTabFragment fragment = new TrendsInfoTabFragment();
        Bundle args = new Bundle();
        args.putSerializable("trends", (TrendsObject) object);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Gson gson = new Gson();
        View view = inflater.inflate(R.layout.trends_info_fragment, container, false);
        RelativeLayout rating_layout = (RelativeLayout) view.findViewById(R.id.message_card_rating_layout);
        RelativeLayout story_layout = (RelativeLayout) view.findViewById(R.id.message_card_story_layout);
        nested_scrollview = (NestedScrollView) view.findViewById(R.id.nested_scrollview);
        trendsObject = (TrendsObject) getArguments().getSerializable("trends");
        description = (CustomTextView) view.findViewById(R.id.description);
        trailer_title = (TextView) view.findViewById(R.id.trailer_title);
        plot = (TextView) view.findViewById(R.id.plot);
        year = (TextView) view.findViewById(R.id.year);
        genre = (TextView) view.findViewById(R.id.genre);
        director = (TextView) view.findViewById(R.id.director);
        country = (TextView) view.findViewById(R.id.country);
        studio = (TextView) view.findViewById(R.id.studio);
        runtime = (TextView) view.findViewById(R.id.runtime);
        thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
        allButton = (FlatButton) view.findViewById(R.id.button_all);
        picNum = (TextView) view.findViewById(R.id.picNum);
        description.setText(trendsObject.getMainInfo());
        allButton.setButtonColor(getResources().getColor(R.color.material_grey_400));
        JsonArray directorInfo = new JsonParser().parse(trendsObject.getStaff()).getAsJsonArray();

        if (trendsObject.getChannel() != 1) {
            ViewGroup group = (ViewGroup) rating_layout.getParent();
            group.removeView(rating_layout);
        } else {
            JsonElement ratingJson, tomatoJson;
            JsonObject ratingInfo = new JsonParser().parse(trendsObject.getRating()).getAsJsonObject();
            JsonObject tomatoInfo = new JsonParser().parse(trendsObject.getTomato()).getAsJsonObject();
            ratingJson = ratingInfo.getAsJsonObject();
            TrendsObject.RatingItem ratingItem = gson.fromJson(ratingJson, TrendsObject.RatingItem.class);;
            tomatoJson = tomatoInfo.getAsJsonObject();
            TrendsObject.TomatoItem tomatoItem = gson.fromJson(tomatoJson, TrendsObject.TomatoItem.class);
            imdb_point = (TextView) view.findViewById(R.id.imdb_point);
            imdb_point.setText(ratingItem.getScore());
            critics_point = (TextView) view.findViewById(R.id.critics_point);
            audience_point = (TextView) view.findViewById(R.id.audience_point);
            criticsView = (ImageView) view.findViewById(R.id.critics);;
            audienceView = (ImageView) view.findViewById(R.id.audience);
            if (tomatoItem.getCritics_score() == null) {
                ((ViewGroup) critics_point.getParent()).removeView(critics_point);
                ((ViewGroup) audience_point.getParent()).removeView(audience_point);
                ((ViewGroup) criticsView.getParent()).removeView(criticsView);
                ((ViewGroup) audienceView.getParent()).removeView(audienceView);
            } else {
                critics_point.setText(tomatoItem.getCritics_score()+"%");
                audience_point.setText(tomatoItem.getAudience_score()+"%");
                rottenTomatoInfo(tomatoItem);
            }
        }

        if (directorInfo.size() > 0) {
            TrendsObject.StaffItem directorItem = gson.fromJson(directorInfo.get(0), TrendsObject.StaffItem.class);
            director.setText("Directed by: " + directorItem.getStaff());
        }

        if (trendsObject.getStory().compareTo("null") != 0) {
            plot.setText(trendsObject.getStory());
            expandableLayout = (ExpandableRelativeLayout) view.findViewById(R.id.expandableLayout);
            buttonLayout = view.findViewById(R.id.expandableButton);

            expandableLayout.setListener(new ExpandableLayoutListenerAdapter() {
                @Override
                public void onPreOpen() {
                    createRotateAnimator(buttonLayout, 0f, 180f).start();
                }

                @Override
                public void onPreClose() {
                    createRotateAnimator(buttonLayout, 180f, 0f).start();
                }
            });

            expandableLayout.setInterpolator(new FastOutLinearInInterpolator());

            buttonLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    onClickButton(expandableLayout);
                }
            });
        } else {
            ViewGroup group = (ViewGroup) story_layout.getParent();
            group.removeView(story_layout);
        }

        if (trendsObject.getData().length() > 2) {
            JsonArray dataInfo = new JsonParser().parse(trendsObject.getData()).getAsJsonArray();
            JsonElement jsonElement = null;
            TrendsObject.DataItem dataItem;

            if (dataInfo.size() == 5) {
                jsonElement = dataInfo.get(4);
            } else {
                jsonElement = dataInfo.get(3);
            }

            dataItem = gson.fromJson(jsonElement, TrendsObject.DataItem.class);

            if (dataItem.getData().indexOf(":") != -1) {
                runtime.setText(dataItem.getData());
            } else {
                runtime.setText("RunTime: " + dataItem.getData());
            }

            if (dataInfo.size() == 5) {
                jsonElement = dataInfo.get(2);
            } else {
                jsonElement = dataInfo.get(1);
            }

            dataItem = gson.fromJson(jsonElement, TrendsObject.DataItem.class);

            if (dataItem.getData().indexOf(":") != -1) {
                country.setText(dataItem.getData());
                countryFlag(dataItem.getData().split(":")[1], thumbnailView);
            } else {
                country.setText("Country: " + dataItem.getData());
                countryFlag(dataItem.getData(), thumbnailView);
            }

            if (dataInfo.size() == 5) {
                jsonElement = dataInfo.get(1);
            } else {
                jsonElement = dataInfo.get(0);
            }

            dataItem = gson.fromJson(jsonElement, TrendsObject.DataItem.class);

            if (dataItem.getData().indexOf(":") != -1) {
                year.setText(dataItem.getData());
            } else {
                year.setText("Year: " + dataItem.getData());
            }

            if (dataInfo.size() == 5) {
                jsonElement = dataInfo.get(3);
            } else {
                jsonElement = dataInfo.get(2);
            }

            dataItem = gson.fromJson(jsonElement, TrendsObject.DataItem.class);
            studio.setText("Studio: " + dataItem.getData());
        }

        setupYoutube(view);

        //------- Gallery RecyclerView -------//
        galleryRecyclerView = (RecyclerView) view.findViewById(R.id.gallery_recyclerview);
        galleryRecyclerView.getItemAnimator().setAddDuration(1000);
        galleryRecyclerView.getItemAnimator().setChangeDuration(1000);
        galleryRecyclerView.getItemAnimator().setMoveDuration(1000);
        galleryRecyclerView.getItemAnimator().setRemoveDuration(1000);
        trendsGalleryAdapter = new TrendsGalleryRecycleViewAdapter(trendsObject, false);
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        galleryRecyclerView.setLayoutManager(linearLayoutManager);
        //------- Gallery RecyclerView -------//

        //------- deserialize Gallery JSON object -------//
        JsonArray galleryInfo = new JsonParser().parse(trendsObject.getGalleryUrl()).getAsJsonArray();
        list = new ArrayList<TrendsObject.GalleryItem>();
        for (int i = 0; i < galleryInfo.size(); i++) {
            JsonElement str = galleryInfo.get(i);
            TrendsObject.GalleryItem obj = gson.fromJson(str, TrendsObject.GalleryItem.class);
            list.add(obj);
            trendsGalleryAdapter.addItem(i, obj);
        }
        //------- deserialize Gallery JSON object -------//

        picNum.setText(String.valueOf(list.size()));

        if (galleryInfo.size() > 1) {
            galleryRecyclerView.setAdapter(trendsGalleryAdapter);
            trendsGalleryAdapter.setOnItemClickListener(this);
        } else {
            View gallery = (LinearLayout) view.findViewById(R.id.gallery_container);
            ViewGroup parent = (ViewGroup) gallery.getParent();
            parent.removeView(gallery);
        }

        linearLayoutManager.scrollToPositionWithOffset(1, 650);
        return view;
    }

    private void rottenTomatoInfo(TrendsObject.TomatoItem tomatoItem) {
        if (Integer.parseInt(tomatoItem.getCritics_score()) < 60 )
            criticsView.setImageResource(R.drawable.tomato_leaf);
        else if (Integer.parseInt(tomatoItem.getCritics_score()) < 80)
            criticsView.setImageResource(R.drawable.tomato);
        else
            criticsView.setImageResource(R.drawable.tomato_fresh);

        if (Integer.parseInt(tomatoItem.getAudience_score()) < 60 )
            audienceView.setImageResource(R.drawable.tomato_garbage);
        else
            audienceView.setImageResource(R.drawable.tomato_popcorn);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*if (mActionListener.getCurrentQuery() != null) {
            outState.putString(KEY_CURRENT_QUERY, mActionListener.getCurrentQuery());
        }*/
    }

    public android.animation.ObjectAnimator createRotateAnimator(final View target, final float from, final float to) {
        android.animation.ObjectAnimator animator = android.animation.ObjectAnimator.ofFloat(target, "rotation", from, to);
        animator.setDuration(300);
        animator.setInterpolator(Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR));
        return animator;
    }

    private void onClickButton(final ExpandableLayout expandableLayout) {
        expandableLayout.toggle();
    }

    private void setupYoutube(View view) {
        /**
         * This API depends on the main YouTube application so it’s necessary to check whether it exists on
         * the device before we start trying to use it.
         */
        final YouTubeInitializationResult result = YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(getActivity());

        if (result != YouTubeInitializationResult.SUCCESS) {
            //If there are any issues we can show an error dialog.
            result.getErrorDialog(getActivity(), 0).show();
        }

        VIDEO_KEY = trendsObject.getTrailerUrl().split("[?]")[1].split("[=]")[1];
        youTubeThumbnailView = (YouTubeThumbnailView) view.findViewById(R.id.imageView_thumbnail);
        youTubeThumbnailView.initialize(Config.YOUTUBE_API_KEY, new YouTubeThumbnailView.OnInitializedListener() {
            @Override
            public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView,
                                                YouTubeInitializationResult youTubeInitializationResult) {
                Toast.makeText(getContext(), "Initialization failure: Unable to play video", Toast.LENGTH_LONG).show();
            };
            @Override
            public void onInitializationSuccess(YouTubeThumbnailView view,
                                                YouTubeThumbnailLoader loader) {
                loader.setVideo(VIDEO_KEY);
            }

        });

        youTubeThumbnailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent lightBoxIntent = new Intent(v.getContext(), CustomLightBoxActivity.class);
                lightBoxIntent.putExtra(CustomLightBoxActivity.KEY_VIDEO_ID, VIDEO_KEY);
                startActivity(lightBoxIntent);
            }
        });

        mQueue = CustomVolleyRequestQueue.getInstance(getActivity()).getRequestQueue();
        CustomJSONObjectRequest jsonRequest_q = null;
        String url = "http://www.youtube.com/oembed?url=https://www.youtube.com/watch?v="+ VIDEO_KEY +"&format=json";

        jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String title = response.getString("title");
                    trailer_title.setText(title);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
            }
        });

        jsonRequest_q.setTag(REQUEST_TAG);
        mQueue.add(jsonRequest_q);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Clicked: " + position, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(view.getContext(), TrendsSlideActivity.class);
        intent.putExtra(TrendsDetail.TRENDS_OBJECT, trendsObject);
        intent.putExtra(SlideActivity.PIC_POSITION, position);
        startActivityForVersion(intent);
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link Activity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();
        int toolbarHeight = getActivity().findViewById(R.id.toolbar).getHeight();
        nested_scrollview.startNestedScroll(View.SCROLL_AXIS_VERTICAL);
        nested_scrollview.dispatchNestedPreScroll(0, toolbarHeight, null, null);
        nested_scrollview.dispatchNestedScroll(0, 0, 0, 0, new int[]{0, -toolbarHeight});
        nested_scrollview.smoothScrollTo(0, nested_scrollview.getMaxScrollAmount());

        allButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TrendsAlbumActivity.class);
                intent.putExtra(TrendsDetail.TRENDS_OBJECT, trendsObject);
                startActivityForVersion(intent);
            }
        });

        plot.setTextIsSelectable(true);
        description.setTextIsSelectable(true);
    }

    private void startActivityForVersion(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(
                            getActivity()).toBundle());
        }
        else {
            startActivity(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mQueue != null)
            mQueue.cancelAll(REQUEST_TAG);
        if (youTubeThumbnailView != null)
            youTubeThumbnailView.destroyDrawingCache();
    }

}
