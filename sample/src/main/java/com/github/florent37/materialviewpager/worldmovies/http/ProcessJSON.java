package com.github.florent37.materialviewpager.worldmovies.http;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;
import com.github.florent37.materialviewpager.worldmovies.adapter.TestRecyclerViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by aaron on 2016/3/4.
 */
public class ProcessJSON extends AsyncTask<String, Void, String> {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private Fragment fragment;

    private int CONTENT_SIZE;
    //---------------- JSON practice start------------//

    // JSON Node names
    private static final String TAG_CONTACTS = "contacts";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_GENDER = "gender";
    private static final String TAG_PHONE = "phone";
    private static final String TAG_PHONE_MOBILE = "mobile";
    private static final String TAG_PHONE_HOME = "home";
    private static final String TAG_PHONE_OFFICE = "office";
    private static final String TAG_COUNTRY = "country";
    private static final String TAG_ARRIVAL = "arrival";
    private static final String TAG_IMAGE_URL = "imageUrl";
    private ProcessJSON pJSON; //AsyncTask object

    private static List<TestRecyclerViewAdapter.MyObject> mContentItems;

    // contacts JSONArray
    JSONArray contacts = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> contactList;

    //---------------- JSON practice end------------//
    public ProcessJSON(RecyclerView view, List<TestRecyclerViewAdapter.MyObject> mContentItems, Fragment fragment) {
        Log.d("0305", "new ProcessJSON");
        this.mRecyclerView = view;
        this.mContentItems = mContentItems;
        this.fragment = fragment;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d("0304", "onPreExcure");
        // Showing progress dialog
            /*pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();*/

    }

    protected String doInBackground(String... params){
        Log.d("0304", "doInBackground");
        String jsonStr = null;
        String urlString = params[0];

        HTTPDataHandler hh = new HTTPDataHandler();
        jsonStr = hh.GetHTTPData(urlString);

        // Return the data from specified url
        return jsonStr;
    }

    private List<TestRecyclerViewAdapter.MyObject> getRandomSublist(List<TestRecyclerViewAdapter.MyObject> array, int amount) {

        ArrayList<TestRecyclerViewAdapter.MyObject> list = new ArrayList<>(amount);
        Random random = new Random();
        while (list.size() < amount) {
            list.add(array.get(random.nextInt(amount)));
        }
        return list;
    }

    protected void onPostExecute(String jsonStr){
            /*
                Important in JSON DATA
                -------------------------
                * Square bracket ([) represents a JSON array
                * Curly bracket ({) represents a JSON object
                * JSON object contains key/value pairs
                * Each key is a String and value may be different data types
             */

        //..........Process JSON DATA................
        if(jsonStr !=null){
            try{
                // Get the full HTTP Data as JSONObject
                contactList = new ArrayList<HashMap<String, String>>();
                JSONObject jsonObj = new JSONObject(jsonStr);

                // Getting JSON Array node
                contacts = jsonObj.getJSONArray(TAG_CONTACTS);

                // looping through All Contacts
                for (int i = 0; i < contacts.length(); i++) {
                    JSONObject c = contacts.getJSONObject(i);

                    String id = c.getString(TAG_ID);
                    String name = c.getString(TAG_NAME);
                    String email = c.getString(TAG_EMAIL);
                    String address = c.getString(TAG_ADDRESS);
                    String gender = c.getString(TAG_GENDER);

                    // Phone node is JSON Object
                    JSONObject phone = c.getJSONObject(TAG_PHONE);
                    String mobile = phone.getString(TAG_PHONE_MOBILE);
                    String home = phone.getString(TAG_PHONE_HOME);
                    String office = phone.getString(TAG_PHONE_OFFICE);

                    // tmp hashmap for single contact
                    HashMap<String, String> contact = new HashMap<String, String>();

                    // adding each child node to HashMap key => value
                    contact.put(TAG_ID, id);
                    contact.put(TAG_NAME, name);
                    contact.put(TAG_EMAIL, email);
                    contact.put(TAG_PHONE_MOBILE, mobile);

                    Log.d("0304", "item: " + id + ", " + name + ", " + email + ", " + mobile);

                    // adding contact to contact list
                    //TODO mContentItems initiate
                    contactList.add(contact);
                }

            }catch(JSONException e){
                e.printStackTrace();
            }finally {
                CONTENT_SIZE =  mContentItems.size();
                mAdapter = new RecyclerViewMaterialAdapter(new TestRecyclerViewAdapter(getRandomSublist(mContentItems, CONTENT_SIZE)));
                mRecyclerView.setAdapter(mAdapter);

                /*{
                    for (int i = 0; i < ITEM_COUNT; ++i)
                        mContentItems.add(new Object());
                    mAdapter.notifyDataSetChanged();
                }*/

                mAdapter.notifyDataSetChanged();
                MaterialViewPagerHelper.registerRecyclerView(fragment.getActivity(), mRecyclerView);
            }

        } else {
            //offline case
            Log.d("0311", "Offline case");
            CONTENT_SIZE =  mContentItems.size();
            mAdapter = new RecyclerViewMaterialAdapter(new TestRecyclerViewAdapter(getRandomSublist(mContentItems, CONTENT_SIZE)));
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            MaterialViewPagerHelper.registerRecyclerView(fragment.getActivity(), mRecyclerView);
        }
    } // onPostExecute() end
}
