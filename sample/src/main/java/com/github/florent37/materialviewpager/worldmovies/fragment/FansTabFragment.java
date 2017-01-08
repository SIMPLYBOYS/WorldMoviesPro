package com.github.florent37.materialviewpager.worldmovies.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.adapter.FriendsListAdapter;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.github.florent37.materialviewpager.worldmovies.util.UsersUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;

/**
 * Created by @vitovalov on 30/9/15.
 */
public class FansTabFragment extends Fragment {

    private FriendsListAdapter mAdapter;
    private RequestQueue mQueue;
    private Activity activity;
    private RecyclerView recyclerView;
    private String type;


    public static FansTabFragment newInstance(String type) {
        FansTabFragment fragment = new FansTabFragment();
        Bundle args = new Bundle();
        args.putSerializable("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        View view = inflater.inflate(R.layout.fragment, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_list_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        type = (String) getArguments().getSerializable("type");
        /*String friends = UsersUtils.getCurrentFriends(getActivity());
        Gson gson = new Gson();
        List<User> friendslist = gson.fromJson(friends, new TypeToken<List<User>>(){}.getType());
        mAdapter = new LearderBoardListAdapter(friendslist, getActivity());*/
        requestDataRefresh();
        return view;
    }

    public void requestDataRefresh() {
        mQueue = CustomVolleyRequestQueue.getInstance(activity).getRequestQueue();
        CustomJSONObjectRequest jsonRequest_q = null;
        final List<User> fansList = new ArrayList<>();
        final User user = UsersUtils.getCurrentUser(getApplicationContext());

        if (user == null)
            return;

        String url= Config.HOST_NAME + "social/"+type+"/"+user.id;
        jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray contents = response.getJSONArray("contents");
                    for (int i=0; i < contents.length(); i++) {
                        User person = new User();
                        LOGD("1120", "get fans complete " + i + ": " + contents.optJSONObject(i));
                        JSONObject dataobj= contents.optJSONObject(i);
                        LOGD("0106", String.valueOf(dataobj.optInt("total")));
                        person.name = dataobj.optString("name");
                        if (!person.name.equals(user.name)) {
                            person.id = dataobj.optString("fbId");
                            person.link = dataobj.optString("link");
                            person.pictureUrl = "https://graph.facebook.com/" + person.id + "/picture?type=large";
                            person.description = "";
                            person.total = dataobj.optInt("total");
                            fansList.add(person);
                        } else {
//                            user.description = person.description;
                            user.description = "";
                            user.total = dataobj.optInt("total");
                        }
                    }

                    mAdapter = new FriendsListAdapter(fansList, activity);
                    recyclerView.setAdapter(mAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(activity, "Remote Server connect not react!", Toast.LENGTH_SHORT).show();
            }
        });

        mQueue.add(jsonRequest_q);
    }
}


