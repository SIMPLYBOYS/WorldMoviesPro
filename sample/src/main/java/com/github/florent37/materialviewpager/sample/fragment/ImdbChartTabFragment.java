package com.github.florent37.materialviewpager.sample.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.sample.Config;
import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.sample.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.sample.model.ImdbObject;

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
 * Created by aaron on 2016/8/31.
 */
public class ImdbChartTabFragment extends Fragment implements Response.ErrorListener {
    private ImdbObject imdbObject;
    private RequestQueue mQueue;
    private String REQUEST_TAG = "ImdbChartTabFragment";
    private String HOST_NAME = Config.HOST_NAME;
    private final String TAG_RECORDS = "records";
    private Activity mActivity;
    private LineChartView ratingChart, positionChart;
    private PreviewLineChartView ratingPreview, positionPreView;
    private LineChartData ratingData, positionData;
    private LineChartData ratingPreviewData, positionPreviewData;

    public static ImdbChartTabFragment newInstance(ImdbObject imdbObject) {
        ImdbChartTabFragment fragment = new ImdbChartTabFragment();
        Bundle args = new Bundle();
        args.putSerializable("imdb", imdbObject);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        mActivity = getActivity();
        imdbObject = (ImdbObject) getArguments().getSerializable("imdb");
        final View view = inflater.inflate(R.layout.detail_imdb_chart, container, false);
        fetchRecords(imdbObject.getTitle());
        return view;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(getContext(), "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
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
                        JSONArray records = c.getJSONArray(TAG_RECORDS);

                        ratingChart = (LineChartView) mActivity.findViewById(R.id.ratingChart);
                        positionChart = (LineChartView) mActivity.findViewById(R.id.postitionChart);
                        ratingPreview = (PreviewLineChartView) mActivity.findViewById(R.id.rating_preview);
                        positionPreView = (PreviewLineChartView) mActivity.findViewById(R.id.position_preview);
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
            });

            jsonRequest_q.setTag(REQUEST_TAG);
            mQueue.add(jsonRequest_q);
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
//                Log.d("0511", new PointValue(i, position).toString());
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
//                Log.d("0510", new PointValue(i, rating).toString());
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

            ratingData.setAxisYLeft(new Axis(axisValues).setName("Rating").setMaxLabelChars(3)
                    .setTextColor(ChartUtils.COLOR_RED)
                    .setHasLines(true)
                    .setInside(false));
            axisValues = new ArrayList<AxisValue>();

            for (int j = 1; j <= 250; j += 1) {
                axisValues.add(new AxisValue(j).setLabel(String.valueOf(j)));
            }

            positionData.setAxisYLeft(new Axis(axisValues).setName("Position").setMaxLabelChars(3)
                    .setTextColor(Color.BLACK)
                    .setHasLines(true)
                    .setInside(false));
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
