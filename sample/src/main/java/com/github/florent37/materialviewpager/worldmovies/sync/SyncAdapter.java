package com.github.florent37.materialviewpager.worldmovies.sync;

/**
 * Created by aaron on 2016/2/24.
 */

import android.accounts.Account;
import android.content.*;
import android.os.Bundle;
import android.util.Log;

import com.github.florent37.materialviewpager.worldmovies.BuildConfig;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.*;
import java.util.regex.Pattern;


/**
 * Sync adapter for Google I/O data. Used for download sync only. For upload sync,
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = makeLogTag(SyncAdapter.class);

    private static final Pattern sSanitizeAccountNamePattern = Pattern.compile("(.).*?(.?)@");
    public static final String EXTRA_SYNC_USER_DATA_ONLY =
            "com.google.samples.apps.iosched.EXTRA_SYNC_USER_DATA_ONLY";

    private final Context mContext;
    ContentResolver mContentResolver;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        //noinspection ConstantConditions,PointlessBooleanExpression
        if (!BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    LOGE(TAG, "Uncaught sync exception, suppressing UI in release build.",
                            throwable);
                }
            });
        }
    }

    @Override
    public void onPerformSync(final Account account, Bundle extras, String authority,
                              final ContentProviderClient provider, final SyncResult syncResult) {
        final boolean uploadOnly = extras.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, false);
        final boolean initialize = extras.getBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, false);
        final boolean userScheduleDataOnly = extras.getBoolean(EXTRA_SYNC_USER_DATA_ONLY,
                false);

        final String logSanitizedAccountName = sSanitizeAccountNamePattern
                .matcher(account.name).replaceAll("$1...$2@");


        // This Adapter is declared not to support uploading in its xml file.
        // {@code ContentResolver.SYNC_EXTRAS_UPLOAD} is set by the system in some cases, but never
        // by the app. Conference data only is a download sync, user schedule data sync is both
        // ways and uses {@code EXTRA_SYNC_USER_DATA_ONLY}, session feedback data sync is
        // upload only and isn't managed by this SyncAdapter as it doesn't need periodic sync.
        if (uploadOnly) {
            return;
        }

        LOGI(TAG, "Beginning sync for account " + logSanitizedAccountName + "," +
                " uploadOnly=" + uploadOnly +
                " userScheduleDataOnly =" + userScheduleDataOnly +
                " initialize=" + initialize);

        Log.d("0224", "Beginning sync for account " + logSanitizedAccountName + "," +
                " uploadOnly=" + uploadOnly +
                " userScheduleDataOnly =" + userScheduleDataOnly +
                " initialize=" + initialize);

        // Sync from bootstrap and remote data, as needed
//        new SyncHelper(mContext).performSync(syncResult, account, extras);
    }

}
