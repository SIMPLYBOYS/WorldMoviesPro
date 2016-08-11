package com.github.florent37.materialviewpager.sample.fragment;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.github.florent37.materialviewpager.sample.adapter.ImdbGalleryRecycleViewAdapter;
import com.github.florent37.materialviewpager.sample.framework.CustomLightBoxActivity;
import com.github.florent37.materialviewpager.sample.framework.CustomTextView;
import com.github.florent37.materialviewpager.sample.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.sample.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.sample.imdb.AlbumActivity;
import com.github.florent37.materialviewpager.sample.imdb.MovieDetailActivity;
import com.github.florent37.materialviewpager.sample.imdb.SlideActivity;
import com.github.florent37.materialviewpager.sample.model.ImdbObject;
import com.github.florent37.materialviewpager.sample.trends.TrendsSlideActivity;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.formatter.SimpleLineChartValueFormatter;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;

/**
 * Created by aaron on 2016/7/28.
 */
public class ImdbInfoTabFragment extends InfoTabFragment implements AdapterView.OnItemClickListener,
        YouTubeThumbnailView.OnInitializedListener {

    private ImageView thumbnailView;
    private View buttonLayout;
    private TextView plot, metascrore, genre, runtime, country, moreButton, allButton, picNum, year, studio, trailer_title;
    private CustomTextView description;
    private RecyclerView galleryRecyclerView;
    private String VIDEO_KEY;
    private RequestQueue mQueue;
    private ExpandableRelativeLayout expandableLayout;
    private ImdbGalleryRecycleViewAdapter imdbGalleryAdapter;
    private LinearLayoutManager linearLayoutManager;
    private List<ImdbObject.GalleryItem> list = null;
    private String type;
    YouTubeThumbnailView youTubeThumbnailView;
    //    ObservableScrollView nested_scrollview;
    NestedScrollView nested_scrollview;
    private ImdbObject imdbObject;
    MaterialDialog.Builder builder;
    MaterialDialog dialog;
    private LineChartView ratingChart, positionChart;
    private PreviewLineChartView ratingPreview, positionPreView;
    private LineChartData ratingData, positionData;
    private LineChartData ratingPreviewData, positionPreviewData;
    private String HOST_NAME = Config.HOST_NAME;
    private final String TAG_RECORDS = "records";

    public static ImdbInfoTabFragment newInstance(Object object) {
        ImdbInfoTabFragment fragment = new ImdbInfoTabFragment();
        Bundle args = new Bundle();
        args.putSerializable("imdb", (ImdbObject) object);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Gson gson = new Gson();
        View view = inflater.inflate(R.layout.imdb_info_fragment, container, false);
        nested_scrollview = (NestedScrollView) view.findViewById(R.id.nested_scrollview);
        imdbObject = (ImdbObject) getArguments().getSerializable("imdb");
        description = (CustomTextView) view.findViewById(R.id.description);
        trailer_title =  (TextView) view.findViewById(R.id.trailer_title);
        plot = (TextView) view.findViewById(R.id.plot);
        year = (TextView) view.findViewById(R.id.year);
        genre = (TextView) view.findViewById(R.id.genre);
        country = (TextView) view.findViewById(R.id.country);
        studio = (TextView) view.findViewById(R.id.studio);
        runtime = (TextView) view.findViewById(R.id.runtime);
        metascrore = (TextView) view.findViewById(R.id.metascrore);
        thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
        moreButton = (TextView) view.findViewById(R.id.button_more);
        allButton = (TextView) view.findViewById(R.id.button_all);
        picNum = (TextView) view.findViewById(R.id.picNum);
        description.setText(imdbObject.getPlot());

        if (imdbObject.getPlot() != "")
            plot.setText(imdbObject.getSummery());

        if (!imdbObject.getMetaScore().equals("null"))
            metascrore.setText("MetaScore: "+imdbObject.getMetaScore());

        if (imdbObject.getType().compareTo("imdb")!=0) {
            View chart = view.findViewById(R.id.chart_layout);
            ViewGroup parent = (ViewGroup) chart.getParent();
            parent.removeView(chart);
        } else {
            fetchRecords(imdbObject.getTitle());
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
        Log.d("0801", imdbObject.getTrailerUrl());
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

    private void fetchRecords(String title) {
        if (mQueue == null)
            mQueue = CustomVolleyRequestQueue.getInstance(getActivity()).getRequestQueue();

        CustomJSONObjectRequest jsonRequest_q = null;

        if (title != null) {
            // launch query from searchview
            try {
                title = URLEncoder.encode(title, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 is unknown");
            }

            jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME + "imdb_records?title=" + title + "&ascending=1", new JSONObject(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray contents = ((JSONObject) response).getJSONArray("contents");

                        JSONObject c = contents.getJSONObject(0);
                        Log.d("0514", String.valueOf(c));
                        JSONArray records = c.getJSONArray(TAG_RECORDS);

                        View view = getView();

                        ratingChart = (LineChartView) view.findViewById(R.id.ratingChart);
                        positionChart = (LineChartView) view.findViewById(R.id.postitionChart);
                        ratingPreview = (PreviewLineChartView) view.findViewById(R.id.rating_preview);
                        positionPreView = (PreviewLineChartView) view.findViewById(R.id.position_preview);
                        generateData(records);
                        ratingChart.setLineChartData(ratingData);
                        positionChart.setLineChartData(positionData);

                        // Disable zoom/scroll for previewed chart, visible chart ranges depends on preview chart viewport so
                        // zoom/scroll is unnecessary.
                        ratingChart.setZoomEnabled(false);
                        ratingChart.setScrollEnabled(false);
                        ratingPreview.setLineChartData(ratingPreviewData);
                        ratingPreview.setViewportChangeListener(new ViewportListener());
                        ratingPreview.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
                        previewX(ratingChart, ratingPreview, false);

                        positionChart.setZoomEnabled(false);
                        positionChart.setScrollEnabled(false);
                        positionPreView.setLineChartData(positionPreviewData);
                        positionPreView.setViewportChangeListener(new ViewportListener());
                        positionPreView.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
                        previewX(positionChart, positionPreView, false);

                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Remote Server error!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getActivity(), "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
                }
            } );
            mQueue.add(jsonRequest_q);
            return;
        }
    }

    /**
     * Viewport listener for preview chart(lower one). in {@link #onViewportChanged(Viewport)} method change
     * viewport of upper chart.
     */
    private class ViewportListener implements ViewportChangeListener {

        @Override
        public void onViewportChanged(Viewport newViewport) {
            // don't use animation, it is unnecessary when using preview chart.
            ratingChart.setCurrentViewport(newViewport);
            positionChart.setCurrentViewport(newViewport);
        }

    }

    private void previewX(LineChartView chart, PreviewLineChartView previewChart, boolean animate) {
        Viewport tempViewport = new Viewport(chart.getMaximumViewport());
        float dx = tempViewport.width() / 4;
        tempViewport.inset(dx, 0);
        if (animate) {
            previewChart.setCurrentViewportWithAnimation(tempViewport);
        } else {
            previewChart.setCurrentViewport(tempViewport);
        }
        previewChart.setZoomType(ZoomType.HORIZONTAL);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Clicked: " + position, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(view.getContext(), TrendsSlideActivity.class);
        intent.putExtra(MovieDetailActivity.IMDB_OBJECT, imdbObject);
        intent.putExtra(SlideActivity.PIC_POSITION, position);
        startActivityForVersion(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
                                Uri.parse("http://www.rottentomatoes.com/search/?search=" + imdbObject.getTitle())));
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

    private void generateData(JSONArray records) {
        try {

            Line line;
            List<PointValue> values;
            List<Line> lines = new ArrayList<Line>();

            //---------- Position line ----------//
            values = new ArrayList<PointValue>();
            for (int i = 0; i < records.length(); ++i) {
                // Some random height values, add +200 to make line a little more natural
                JSONObject jsonItem = (JSONObject) records.getJSONObject(i);

                int position = Integer.parseInt(jsonItem.getString("position"));
                Log.d("0511", new PointValue(i, position).toString());
                values.add(new PointValue(i, position));
            }

            line = new Line(values);
            line.setColor(Color.BLACK);
            line.setShape(ValueShape.CIRCLE);
            line.setHasLabelsOnlyForSelected(true);
            line.setHasPoints(true);
            line.setFilled(true);
            line.setStrokeWidth(1);
            lines.add(line);

            positionData = new LineChartData(lines);
            //---------- Position line ----------//

            //---------- Rating line -----------//
            values = new ArrayList<PointValue>();
            for (int i = 0; i < records.length(); ++i) {
                JSONObject jsonItem = records.getJSONObject(i);
                float rating = Float.parseFloat(jsonItem.getString("rating"));
                Log.d("0510", new PointValue(i, rating).toString());
                values.add(new PointValue(i, rating));
            }

            line = new Line(values);
            lines = new ArrayList<Line>();
            line.setShape(ValueShape.SQUARE);
            line.setColor(ChartUtils.COLOR_RED);
            line.setHasLabelsOnlyForSelected(true);
            line.setFormatter(new SimpleLineChartValueFormatter(1));
            line.setCubic(false);
            line.setFilled(true);
            line.setHasPoints(true);
            line.setStrokeWidth(1);
            lines.add(line);

            // Data and axes
            ratingData = new LineChartData(lines);
            //---------- Rating line -----------//

            List<AxisValue> axisXValues = new ArrayList<AxisValue>();

            // Distance axis(bottom X) with formatter that will ad [km] to values, remember to modify max label charts
            // value.
            for (int i = 0; i <records.length(); ++i) {
                JSONObject jsonItem = records.getJSONObject(i);
                String year = jsonItem.getString("year");
                String month = jsonItem.getString("month");
                String date = jsonItem.getString("date");
                axisXValues.add(new AxisValue(i).setLabel(year+"-"+month+"-"+date));
            }

            Axis distanceAxis = new Axis(axisXValues);
            distanceAxis.setName("Year");
            distanceAxis.setTextColor(ChartUtils.COLOR_ORANGE);
            distanceAxis.setMaxLabelChars(8);
            distanceAxis.setHasLines(true);
            distanceAxis.setHasTiltedLabels(true);

            ratingData.setAxisXBottom(distanceAxis);
            positionData.setAxisXBottom(distanceAxis);

            List<AxisValue> axisValues = new ArrayList<AxisValue>();
            for (int i = 1; i <= 10; i += 1) {
                axisValues.add(new AxisValue(i).setLabel(String.valueOf(i)));
            }

            ratingData.setAxisYLeft(new Axis(axisValues).setName("Rating").setMaxLabelChars(3).setTextColor(ChartUtils.COLOR_RED)
                    .setHasLines(true).setInside(false));

            axisValues = new ArrayList<AxisValue>();
            for (int j = 1; j <= 250; j += 1) {
                axisValues.add(new AxisValue(j).setLabel(String.valueOf(j)));
            }
            positionData.setAxisYLeft(new Axis(axisValues).setName("Position").setMaxLabelChars(3).setTextColor(Color.BLACK)
                    .setHasLines(true).setInside(false));

            ratingPreviewData = new LineChartData(ratingData);
            ratingPreviewData.setAxisXBottom(distanceAxis);
            positionPreviewData = new LineChartData(positionData);
            positionPreviewData.setAxisXBottom(distanceAxis);

        } catch (JSONException e) {
            Toast.makeText(getActivity(), "Remote Server data format error!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
