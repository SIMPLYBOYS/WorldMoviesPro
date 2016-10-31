package com.github.florent37.materialviewpager.worldmovies.sync;

/**
 * Created by aaron on 2016/2/24.
 */
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.github.florent37.materialviewpager.worldmovies.io.JSONHandler;
import com.github.florent37.materialviewpager.worldmovies.io.TagsHandler;
import com.github.florent37.materialviewpager.worldmovies.provider.ScheduleContract;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGE;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGW;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.makeLogTag;

/**
 * Helper class that parses conference data and imports them into the app's
 * Content Provider.
 */
public class MoviesDataHandler {
    private static final String TAG = makeLogTag(SyncHelper.class);

    // Shared settings_prefs key under which we store the timestamp that corresponds to
    // the data we currently have in our content provider.
    private static final String SP_KEY_DATA_TIMESTAMP = "data_timestamp";

    // symbolic timestamp to use when we are missing timestamp data (which means our data is
    // really old or nonexistent)
    private static final String DEFAULT_TIMESTAMP = "Sat, 1 Jan 2000 00:00:00 GMT";
    private static final String DATA_KEY_TAGS = "tags";

    private static final String[] DATA_KEYS_IN_ORDER = {
            DATA_KEY_TAGS
    };

    Context mContext = null;

    // Handlers for each entity type:
    TagsHandler mTagsHandler = null;

    // Convenience map that maps the key name to its corresponding handler (e.g.
    // "blocks" to mBlocksHandler (to avoid very tedious if-elses)
    HashMap<String, JSONHandler> mHandlerForKey = new HashMap<String, JSONHandler>();

    // Tally of total content provider operations we carried out (for statistical purposes)
    private int mContentProviderOperationsDone = 0;

    public MoviesDataHandler(Context ctx) {
        mContext = ctx;
    }

    /**
     * Parses the movies data in the given objects and imports the data into the
     * content provider. The format of the data is documented at https://code.google.com/p/iosched.
     *
     * @param dataBodies The collection of JSON objects to parse and import.
     * @param dataTimestamp The timestamp of the data. This should be in RFC1123 format.
     * @param downloadsAllowed Whether or not we are supposed to download data from the internet if needed.
     * @throws IOException If there is a problem parsing the data.
     */
    public void applyMoviesData(String[] dataBodies, String dataTimestamp,
                                    boolean downloadsAllowed) throws IOException {
        LOGD(TAG, "Applying data from " + dataBodies.length + " files, timestamp " + dataTimestamp);

        // create handlers for each data type
        mHandlerForKey.put(DATA_KEY_TAGS, mTagsHandler = new TagsHandler(mContext));

        // process the jsons. This will call each of the handlers when appropriate to deal
        // with the objects we see in the data.
        LOGD(TAG, "Processing " + dataBodies.length + " JSON objects.");
        for (int i = 0; i < dataBodies.length; i++) {
            LOGD(TAG, "Processing json object #" + (i + 1) + " of " + dataBodies.length);
            processDataBody(dataBodies[i]);
        }

        // the sessions handler needs to know the tag and speaker maps to process sessions
       /* mSessionsHandler.setTagMap(mTagsHandler.getTagMap());
        mSessionsHandler.setSpeakerMap(mSpeakersHandler.getSpeakerMap());*/

        // produce the necessary content provider operations
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        for (String key : DATA_KEYS_IN_ORDER) {
            Log.d("0222", "Building content provider operations for: " + key);
            LOGD(TAG, "Building content provider operations for: " + key);
            mHandlerForKey.get(key).makeContentProviderOperations(batch);
            Log.d("0222", "Content provider operations so far: " + batch.size());
            LOGD(TAG, "Content provider operations so far: " + batch.size());
        }
        LOGD(TAG, "Total content provider operations: " + batch.size());

        // download or process local map tile overlay files (SVG files)
        LOGD(TAG, "Processing map overlay files");
//        processMapOverlayFiles(mMapPropertyHandler.getTileOverlays(), downloadsAllowed);

        // finally, push the changes into the Content Provider
        LOGD(TAG, "Applying " + batch.size() + " content provider operations.");
        try {
            int operations = batch.size();
            if (operations > 0) {
                mContext.getContentResolver().applyBatch(ScheduleContract.CONTENT_AUTHORITY, batch);
            }
            LOGD(TAG, "Successfully applied " + operations + " content provider operations.");
            mContentProviderOperationsDone += operations;
        } catch (RemoteException ex) {
            LOGE(TAG, "RemoteException while applying content provider operations.");
            throw new RuntimeException("Error executing content provider batch operation", ex);
        } catch (OperationApplicationException ex) {
            LOGE(TAG, "OperationApplicationException while applying content provider operations.");
            throw new RuntimeException("Error executing content provider batch operation", ex);
        }

        // notify all top-level paths
        LOGD(TAG, "Notifying changes on all top-level paths on Content Resolver.");
        ContentResolver resolver = mContext.getContentResolver();
        for (String path : ScheduleContract.TOP_LEVEL_PATHS) {
            Uri uri = ScheduleContract.BASE_CONTENT_URI.buildUpon().appendPath(path).build();
            Log.d("0222", "notify: " + uri);
            resolver.notifyChange(uri, null);
        }

        LOGD(TAG, "Done applying conference data.");
    }

    public int getContentProviderOperationsDone() {
        return mContentProviderOperationsDone;
    }

    /**
     * Processes a conference data body and calls the appropriate data type handlers
     * to process each of the objects represented therein.
     *
     * @param dataBody The body of data to process
     * @throws IOException If there is an error parsing the data.
     */
    private void processDataBody(String dataBody) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(dataBody));
        JsonParser parser = new JsonParser();
        try {
            reader.setLenient(true); // To err is human

            // the whole file is a single JSON object
            reader.beginObject();

            while (reader.hasNext()) {
                // the key is "rooms", "speakers", "tracks", etc.
                String key = reader.nextName();
                if (mHandlerForKey.containsKey(key)) {
                    // pass the value to the corresponding handler
                    mHandlerForKey.get(key).process(parser.parse(reader));
                } else {
                    LOGW(TAG, "Skipping unknown key in conference data json: " + key);
                    reader.skipValue();
                }
            }
            reader.endObject();
        } finally {
            reader.close();
        }
    }
}
