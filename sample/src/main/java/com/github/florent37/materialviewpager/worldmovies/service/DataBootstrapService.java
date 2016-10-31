package com.github.florent37.materialviewpager.worldmovies.service;

/**
 * Created by aaron on 2016/2/24.
 */

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.florent37.materialviewpager.worldmovies.BuildConfig;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.io.JSONHandler;
import com.github.florent37.materialviewpager.worldmovies.settings.SettingsUtils;
import com.github.florent37.materialviewpager.worldmovies.sync.MoviesDataHandler;
import com.github.florent37.materialviewpager.worldmovies.sync.SyncHelper;
import com.github.florent37.materialviewpager.worldmovies.util.AccountUtils;
import com.github.florent37.materialviewpager.worldmovies.util.LogUtils;

import java.io.IOException;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGE;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGI;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGW;

/**
 * An {@code IntentService} that performs the one-time data bootstrap. It takes the prepackaged
 * conference data from the R.raw.bootstrap_data resource, and populates the database. This data
 * contains the sessions, speakers, etc.
 */
public class DataBootstrapService extends IntentService {

    private static final String TAG = LogUtils.makeLogTag(DataBootstrapService.class);

    /**
     * Start the {@link DataBootstrapService} if the bootstrap is either not done or complete yet.
     *
     * @param context The context for starting the {@link IntentService} as well as checking if the
     *                shared preference to mark the process as done is set.
     */
    public static void startDataBootstrapIfNecessary(Context context) {
        Log.d("0224", "startDataBootstrapIfNessary");
        if (!SettingsUtils.isDataBootstrapDone(context)) {
            LOGW(TAG, "One-time data bootstrap not done yet. Doing now.");
            context.startService(new Intent(context, DataBootstrapService.class));
            Log.d("0222-2", "startDataBootstrapIfNessary");
        } else {
            context.startService(new Intent(context, DataBootstrapService.class));
        }
    }

    /**
     * Creates a DataBootstrapService.
     */
    public DataBootstrapService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context appContext = getApplicationContext();

        if (SettingsUtils.isDataBootstrapDone(appContext)) {
            LOGD(TAG, "Data bootstrap already done.");
            return;
        }

        try {
            LOGD(TAG, "Starting data bootstrap process.");
            // Load data from bootstrap raw resource.reads a JSON-encoded stream from in.
            String bootstrapJson = JSONHandler.parseResource(appContext, R.raw.bootstrap_data);
            // Apply the data we read to the database with the help of the MoviesDataHandler.
            MoviesDataHandler dataHandler = new MoviesDataHandler(appContext);
            dataHandler.applyMoviesData(new String[]{bootstrapJson}, BuildConfig.BOOTSTRAP_DATA_TIMESTAMP, false);
            LOGI(TAG, "End of bootstrap -- successful. Marking bootstrap as done.");
            SettingsUtils.markSyncSucceededNow(appContext);
            SettingsUtils.markDataBootstrapDone(appContext);

        } catch (IOException ex) {
            // This is serious -- if this happens, the app won't work :-(
            // This is unlikely to happen in production, but IF it does, we apply
            // this workaround as a fallback: we pretend we managed to do the bootstrap
            // and hope that a remote sync will work.
            LOGE(TAG, "*** ERROR DURING BOOTSTRAP! Problem in bootstrap data?", ex);
            LOGE(TAG,
                    "Applying fallback -- marking boostrap as done; sync might fix problem.");
            SettingsUtils.markDataBootstrapDone(appContext);
        } finally {
            // Request a manual sync immediately after the bootstrapping process, in case we
            // have an active connection. Otherwise, the scheduled sync could take a while.
            SyncHelper.requestManualSync(AccountUtils.getActiveAccount(appContext));
        }
    }
}