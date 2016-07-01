package com.github.florent37.materialviewpager.sample.fragment;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.aakira.expandablelayout.ExpandableLayout;
import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.github.aakira.expandablelayout.Utils;
import com.github.florent37.materialviewpager.sample.Config;
import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.adapter.TrendsGalleryRecycleViewAdapter;
import com.github.florent37.materialviewpager.sample.framework.CustomLightBoxActivity;
import com.github.florent37.materialviewpager.sample.framework.CustomTextView;
import com.github.florent37.materialviewpager.sample.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.sample.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.sample.imdb.SlideActivity;
import com.github.florent37.materialviewpager.sample.model.TrendsObject;
import com.github.florent37.materialviewpager.sample.trends.TrendsAlbumActivity;
import com.github.florent37.materialviewpager.sample.trends.TrendsDetail;
import com.github.florent37.materialviewpager.sample.trends.TrendsSlideActivity;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaron on 2016/6/21.
 */
public class InfoTabFragment extends Fragment implements AdapterView.OnItemClickListener,
        YouTubeThumbnailView.OnInitializedListener {

    private ImageView thumbnailView;
    private View buttonLayout;
    private TextView plot, genre, runtime, country, moreButton, allButton, picNum, year, studio, trailer_title;
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
    MaterialDialog.Builder builder;
    MaterialDialog dialog;

    public static InfoTabFragment newInstance(TrendsObject trendsObject) {
        InfoTabFragment fragment = new InfoTabFragment();
        Bundle args = new Bundle();
        args.putSerializable("trends", trendsObject);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Gson gson = new Gson();
        View view = inflater.inflate(R.layout.info_fragment, container, false);
        nested_scrollview = (NestedScrollView) view.findViewById(R.id.nested_scrollview);
        trendsObject = (TrendsObject) getArguments().getSerializable("trends");
        description = (CustomTextView) view.findViewById(R.id.description);
        trailer_title =  (TextView) view.findViewById(R.id.trailer_title);
        plot = (TextView) view.findViewById(R.id.plot);
        year = (TextView) view.findViewById(R.id.year);
        genre = (TextView) view.findViewById(R.id.genre);
        country = (TextView) view.findViewById(R.id.country);
        studio = (TextView) view.findViewById(R.id.studio);
        runtime = (TextView) view.findViewById(R.id.runtime);
        thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
        moreButton = (TextView) view.findViewById(R.id.button_more);
        allButton = (TextView) view.findViewById(R.id.button_all);
        picNum = (TextView) view.findViewById(R.id.picNum);
        description.setText(trendsObject.getMainInfo());
        plot.setText(trendsObject.getStory());

        JsonArray dataInfo = new JsonParser().parse(trendsObject.getData()).getAsJsonArray();
        JsonElement jsonElement = null;
        TrendsObject.DataItem dataItem = gson.fromJson(jsonElement, TrendsObject.DataItem.class);

        if (dataInfo.size() == 5) {
            jsonElement = dataInfo.get(4);
        } else {
            jsonElement = dataInfo.get(3);
        }

        dataItem = gson.fromJson(jsonElement, TrendsObject.DataItem.class);
        runtime.setText(dataItem.getData());

        if (dataInfo.size() == 5) {
            jsonElement = dataInfo.get(2);
        } else {
            jsonElement = dataInfo.get(1);
        }

        dataItem = gson.fromJson(jsonElement, TrendsObject.DataItem.class);
        country.setText(dataItem.getData());
        countryFlag(dataItem.getData().split(":")[1]);

        if (dataInfo.size() == 5) {
            jsonElement = dataInfo.get(1);
        } else {
            jsonElement = dataInfo.get(0);
        }

        dataItem = gson.fromJson(jsonElement, TrendsObject.DataItem.class);
        year.setText(dataItem.getData());

        if (dataInfo.size() == 5) {
            jsonElement = dataInfo.get(3);
        } else {
            jsonElement = dataInfo.get(2);
        }

        dataItem = gson.fromJson(jsonElement, TrendsObject.DataItem.class);
        studio.setText(dataItem.getData());
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
            trendsGalleryAdapter.addItem(i,obj);
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

        linearLayoutManager.scrollToPositionWithOffset(1,650);

        return view;
    }

    @Override
    public void onInitializationSuccess(YouTubeThumbnailView view,
                                        YouTubeThumbnailLoader loader) {
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
        VIDEO_KEY = trendsObject.getTrailerUrl().split("[?]")[1].split("[=]")[1];
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

        mQueue = CustomVolleyRequestQueue.getInstance(getActivity())
                .getRequestQueue();

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
        builder = new MaterialDialog.Builder(getContext())
                .iconRes(R.drawable.ic_launcher)
                .limitIconToDefaultSize() // limits the displayed icon size to 48dp
                .title("Rottentomatoes")
                .titleColor(Color.BLACK)
                .backgroundColor(Color.WHITE)
                .contentColor(Color.BLACK)
                .content("Redirect to Rottenntomatoes.com ?")
                .positiveText("Agree")
                .negativeText("Disagree")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        startActivityForVersion(new Intent("android.intent.action.VIEW",
                                Uri.parse("http://www.rottentomatoes.com/search/?search=" + trendsObject.getTitle())));
                    }
                });

        dialog = builder.build();
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();

            }
        });

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

    public void countryFlag ( String location) {

        switch (location) {
            case "France":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.fr).into(thumbnailView);
                break;
            case "Germany":
            case "West Germany":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.gm).into(thumbnailView);
                break;
            case "日本":
            case "Japan":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.japan).into(thumbnailView);
                break;
            case "Brazil":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.brazil).into(thumbnailView);
                break;
            case "Espagne":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.es).into(thumbnailView);
                break;
            case "Italy":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.it).into(thumbnailView);
                break;
            case "New Zealand":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.newzealand).into(thumbnailView);
                break;
            case "South Korea":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.korea).into(thumbnailView);
                break;
            case "UK":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.uk).into(thumbnailView);
                break;
            case "Iran":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.iran).into(thumbnailView);
                break;
            case "India":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.india).into(thumbnailView);
                break;
            case "Lebanon":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.lebanon).into(thumbnailView);
                break;
            case "Spain":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.es).into(thumbnailView);
                break;
            case "Sweden":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.sweden).into(thumbnailView);
                break;
            case "Argentina":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.argentina).into(thumbnailView);
                break;
            case "Canada":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.canada).into(thumbnailView);
                break;
            case "Australia":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.australia).into(thumbnailView);
                break;
            case "Ireland":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.ireland).into(thumbnailView);
                break;
            case "Mexico":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.mexico).into(thumbnailView);
                break;
            case "Soviet Union":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.ru).into(thumbnailView);
                break;
            case "Hong Kong":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.hong_kong).into(thumbnailView);
                break;
            case "Denmark":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.denmark).into(thumbnailView);
                break;
            case "アメリカ":
            case "USA":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.usa).into(thumbnailView);
                break;
        }
    }
}
