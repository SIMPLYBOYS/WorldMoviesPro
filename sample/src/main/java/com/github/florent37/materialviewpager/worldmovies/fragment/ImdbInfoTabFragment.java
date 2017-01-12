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

import com.afollestad.materialdialogs.MaterialDialog;
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
import com.github.florent37.materialviewpager.worldmovies.adapter.ImdbGalleryRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.framework.CustomLightBoxActivity;
import com.github.florent37.materialviewpager.worldmovies.framework.CustomTextView;
import com.github.florent37.materialviewpager.worldmovies.framework.FlatButton;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.imdb.AlbumActivity;
import com.github.florent37.materialviewpager.worldmovies.imdb.MovieDetailActivity;
import com.github.florent37.materialviewpager.worldmovies.imdb.SlideActivity;
import com.github.florent37.materialviewpager.worldmovies.model.ImdbObject;
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
 * Created by aaron on 2016/7/28.
 */
public class ImdbInfoTabFragment extends InfoTabFragment implements AdapterView.OnItemClickListener,
        YouTubeThumbnailView.OnInitializedListener {

    private ImageView thumbnailView;
    private ImdbInfoTabFragment infoTabFragment;
    private String REQUEST_TAG = "ImdbInfoTabFragment";
    private View buttonLayout;
    private Activity mActivity;
    private TextView plot, genre, runtime, country, picNum, year, studio, trailer_title, imdb_point;
    private String CLIENT_ID;
    private FlatButton moreButton, allButton;
    private CustomTextView description;
    private RecyclerView galleryRecyclerView;
    private String VIDEO_KEY;
    private RequestQueue mQueue;
    private ExpandableRelativeLayout expandableLayout;
    private ImdbGalleryRecycleViewAdapter imdbGalleryAdapter;
    private LinearLayoutManager linearLayoutManager;
    private RelativeLayout message_card_rating_layout;
    private List<ImdbObject.GalleryItem> list = null;
    private String type;
    YouTubeThumbnailView youTubeThumbnailView;
    //    ObservableScrollView nested_scrollview;
    NestedScrollView nested_scrollview;
    private ImdbObject imdbObject;
    MaterialDialog.Builder builder;
    MaterialDialog dialog;

    public static ImdbInfoTabFragment newInstance(Object object) {
        ImdbInfoTabFragment fragment = new ImdbInfoTabFragment();
        Bundle args = new Bundle();
        args.putSerializable("imdb", (ImdbObject) object);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        infoTabFragment = this;
        Gson gson = new Gson();
        mActivity = getActivity();
        View view = inflater.inflate(R.layout.imdb_info_fragment, container, false);
        CLIENT_ID = Config.CLIENT_ID;
        nested_scrollview = (NestedScrollView) view.findViewById(R.id.nested_scrollview);
        imdb_point = (TextView) view.findViewById(R.id.imdb_point);
        imdbObject = (ImdbObject) getArguments().getSerializable("imdb");
        JsonElement ratingJson;
        JsonObject ratingInfo = new JsonParser().parse(imdbObject.getRating()).getAsJsonObject();
        ratingJson = ratingInfo.getAsJsonObject();
        ImdbObject.RatingItem ratingItem = gson.fromJson(ratingJson, ImdbObject.RatingItem.class);
        description = (CustomTextView) view.findViewById(R.id.description);
        trailer_title =  (TextView) view.findViewById(R.id.trailer_title);
        plot = (TextView) view.findViewById(R.id.plot);
        year = (TextView) view.findViewById(R.id.year);
        genre = (TextView) view.findViewById(R.id.genre);
        country = (TextView) view.findViewById(R.id.country);
        studio = (TextView) view.findViewById(R.id.studio);
        runtime = (TextView) view.findViewById(R.id.runtime);
        /*metascrore = (TextView) view.findViewById(R.id.metascrore);*/
        thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
        message_card_rating_layout = (RelativeLayout) view.findViewById(R.id.message_card_rating_layout);
        allButton = (FlatButton) view.findViewById(R.id.button_all);
        picNum = (TextView) view.findViewById(R.id.picNum);
        description.setText(imdbObject.getPlot());
        allButton.setButtonColor(getResources().getColor(R.color.material_grey_400));

        if (imdbObject.getPlot() != "")
            plot.setText(imdbObject.getSummery());

        if (ratingItem.getScore() != null)
            imdb_point.setText(ratingItem.getScore());
        else {
            ViewGroup parent = (ViewGroup) message_card_rating_layout.getParent();
            parent.removeView(message_card_rating_layout);
        }

        runtime.setText("RunTime: "+imdbObject.getRunTime());
        country.setText("Country: "+imdbObject.getCountry().split(",")[0]);
        countryFlag(imdbObject.getCountry().split(",")[0], thumbnailView);
        year.setText("ReleaseYear: "+imdbObject.getYear());
        genre.setText("Genre: "+imdbObject.getGenre());
//        studio.setText(dataItem.getData());
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

        setupYoutube(view);
        //------- Gallery RecyclerView -------//
        galleryRecyclerView = (RecyclerView) view.findViewById(R.id.gallery_recyclerview);
        galleryRecyclerView.getItemAnimator().setAddDuration(1000);
        galleryRecyclerView.getItemAnimator().setChangeDuration(1000);
        galleryRecyclerView.getItemAnimator().setMoveDuration(1000);
        galleryRecyclerView.getItemAnimator().setRemoveDuration(1000);
        imdbGalleryAdapter = new ImdbGalleryRecycleViewAdapter(imdbObject, false);
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        galleryRecyclerView.setLayoutManager(linearLayoutManager);
        //------- Gallery RecyclerView -------//
        //------- deserialize Gallery JSON object -------//
        JsonArray galleryInfo = new JsonParser().parse(imdbObject.getGalleryUrl()).getAsJsonArray();
        list = new ArrayList<ImdbObject.GalleryItem>();

        for (int i = 0; i < galleryInfo.size(); i++) {
            JsonElement str = galleryInfo.get(i);
            ImdbObject.GalleryItem obj = gson.fromJson(str, ImdbObject.GalleryItem.class);
            list.add(obj);
            imdbGalleryAdapter.addItem(i,obj);
        }

        //------- deserialize Gallery JSON object -------//
        picNum.setText(String.valueOf(list.size()));

        if (galleryInfo.size() > 1) {
            galleryRecyclerView.setAdapter(imdbGalleryAdapter);
            imdbGalleryAdapter.setOnItemClickListener(this);
        } else {
            View gallery = (LinearLayout) view.findViewById(R.id.gallery_container);
            ViewGroup parent = (ViewGroup) gallery.getParent();
            parent.removeView(gallery);
        }

        linearLayoutManager.scrollToPositionWithOffset(1,650);
        return view;
    }

    @Override
    public void onInitializationSuccess(YouTubeThumbnailView view, YouTubeThumbnailLoader loader) {
        loader.setVideo(VIDEO_KEY);
    }

    @Override
    public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView,
                                        YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(getContext(), "Initialization failure: Unable to play video", Toast.LENGTH_LONG).show();
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

        if (imdbObject.getTrailerUrl().compareTo("N/A") != 0) {
            VIDEO_KEY = imdbObject.getTrailerUrl().split("[?]")[1].split("[=]")[1];
            youTubeThumbnailView = (YouTubeThumbnailView) view.findViewById(R.id.imageView_thumbnail);
            youTubeThumbnailView.initialize(Config.YOUTUBE_API_KEY, this);

            youTubeThumbnailView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent lightBoxIntent = new Intent(v.getContext(), CustomLightBoxActivity.class);
                    lightBoxIntent.putExtra(CustomLightBoxActivity.KEY_VIDEO_ID, VIDEO_KEY);
                    startActivity(lightBoxIntent);
                }
            });
        }

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
//        Toast.makeText(getActivity(), "Clicked: " + position, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(view.getContext(), SlideActivity.class);
        intent.putExtra(MovieDetailActivity.IMDB_OBJECT, imdbObject);
        intent.putExtra(SlideActivity.PIC_POSITION, position);
        startActivityForVersion(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mQueue != null)
            mQueue.cancelAll(REQUEST_TAG);
        if (youTubeThumbnailView != null)
            youTubeThumbnailView.destroyDrawingCache();
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
                Intent intent = new Intent(v.getContext(), AlbumActivity.class);
                intent.putExtra(MovieDetailActivity.IMDB_OBJECT, imdbObject);
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
}
