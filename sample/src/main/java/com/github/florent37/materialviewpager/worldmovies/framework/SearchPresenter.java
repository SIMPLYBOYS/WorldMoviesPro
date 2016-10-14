package com.github.florent37.materialviewpager.worldmovies.framework;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.github.florent37.materialviewpager.worldmovies.service.PlayerService;
import com.github.florent37.materialviewpager.worldmovies.settings.Setting;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.PlaybackBitrate;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;

import java.util.List;
import java.util.Map;
import java.util.Set;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Track;

public class SearchPresenter implements Search.ActionListener, PlayerNotificationCallback, ConnectionStateCallback {

    private final String TAG = SearchPresenter.class.getSimpleName();
    public  final int PAGE_SIZE = 20;
    private boolean playerIsPlaying = false;
    private String playingTrack = "";
    private int position = 0;
    private final Context mContext;
    private final Search.View mView;
    private String mCurrentQuery;
    private String accessToken;
    private Setting mSetting;

    private SearchPager mSearchPager;
    private SearchPager.CompleteListener mSearchListener;
    private Player mPlayer;
//    private Player mPlayer;
    private com.spotify.sdk.android.player.Player fullPlayer;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayer = ((PlayerService.PlayerBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayer = null;
        }
    };

    public SearchPresenter(Context context, Search.View view, com.spotify.sdk.android.player.Player spotifyPlayer) {
        mContext = context;
        mView = view;
        fullPlayer = spotifyPlayer;
        fullPlayer.addConnectionStateCallback(this);
        fullPlayer.addPlayerNotificationCallback(this);
        mSetting = Setting.getInstance();
    }

    @Override
    public void init(String accessToken) {
        logMessage("Api Client created");
        accessToken = accessToken;
        SpotifyApi spotifyApi = new SpotifyApi();

        if (accessToken != null) {
            spotifyApi.setAccessToken(accessToken);
        } else {
            logError("No valid access token");
        }

        mSearchPager = new SearchPager(spotifyApi.getService());

        mContext.bindService(PlayerService.getIntent(mContext), mServiceConnection, Activity.BIND_AUTO_CREATE);
    }

    @Override
    public void search(@Nullable String searchQuery) {
        if (searchQuery != null && !searchQuery.isEmpty() && !searchQuery.equals(mCurrentQuery)) {
            logMessage("query text submit " + searchQuery);
            mCurrentQuery = searchQuery;
            mView.reset();
            mSearchListener = new SearchPager.CompleteListener() {
                @Override
                public void onComplete(List<Track> items) {
                    mView.addData(items);
                }

                @Override
                public void onError(Throwable error) {
                    logError(error.getMessage());
                }
            };
            mSearchPager.getFirstPage(searchQuery, PAGE_SIZE, mSearchListener);
        }
    }

    @Override
    public void destroy() {
        mContext.unbindService(mServiceConnection);
    }

    @Override
    @Nullable
    public String getCurrentQuery() {
        return mCurrentQuery;
    }

    @Override
    public void resume() {
        if (!mSetting.getBackgroundPlay()) {
            fullPlayer.resume();
//            mPlayer.resume();
        }
        mContext.stopService(PlayerService.getIntent(mContext));
    }

    @Override
    public void pause() {
        if (!mSetting.getBackgroundPlay()) {
            fullPlayer.pause();
//            mPlayer.pause();
        }
        mContext.startService(PlayerService.getIntent(mContext));
    }

    @Override
    public void loadMoreResults() {
        Log.d(TAG, "Load more...");
        mSearchPager.getNextPage(mSearchListener);
    }

    @Override
    public void selectTrack(Track item) {
        boolean avaliable = false;
        String previewUrl = item.preview_url;
        String token = CredentialsHandler.getToken(mContext);

        if (previewUrl == null) {
            logMessage("Track doesn't have a preview");
            return;
        }

        Log.d("0826", String.valueOf(item.duration_ms) +" "+ fullPlayer.isLoggedIn());

        if (mPlayer == null && fullPlayer == null) return;

        if (token == null) mPlayer.play(item.preview_url); //case A: normal user

        Set<Map.Entry<String, String>> set = item.external_urls.entrySet();
        String trackUrl = "";

        for (Map.Entry<String, String> entry : set) {
            trackUrl = entry.getValue();
            Log.d("0826", trackUrl.split("track/")[1]);
        }

        if (item.available_markets.contains("TW")) avaliable = true;

        Log.d("0828", "final --> "+String.valueOf(mSetting.getPlayBySpotify()));

        if (mSetting.getPlayBySpotify()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("spotify:track:" + item.id));
                mContext.startActivity(intent);
            } catch (Exception e) {
                playTrack(item, avaliable, token);
            }
        } else {
            playTrack(item, avaliable, token);
        }
    }

    private void playTrack(Track item, boolean avaliable, String token) {
        if (playingTrack != item.id && avaliable && token != null) {
            playingTrack = item.id;
            fullPlayer.setPlaybackBitrate(PlaybackBitrate.BITRATE_HIGH);
            fullPlayer.play("spotify:track:" + item.id);
            if (mSetting.getReat())
                fullPlayer.setRepeat(true);
        } else if (!playerIsPlaying && avaliable) {
            fullPlayer.resume();
        } else if (avaliable) {
            fullPlayer.pause();
        }

        if (playingTrack != item.id && !avaliable) {
            playingTrack = item.id;
            mPlayer.play(item.preview_url );
        } else if (!mPlayer.isPlaying() && !avaliable) {
            mPlayer.resume();
        } else if (mPlayer.isPlaying() && !avaliable){
            mPlayer.pause();
        }
    }

    private void logError(String msg) {
        Toast.makeText(mContext, "Error: " + msg, Toast.LENGTH_SHORT).show();
        Log.e(TAG, msg);
    }

    private void logMessage(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        Log.d(TAG, msg);
    }

    @Override
    public void onLoggedIn() {
        Log.d("SearchPresenter", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("SearchPresenter", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d("SearchPresenter", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("SearchPresenter", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("SearchPresenter", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("SearchPresenter", "Playback error received: " + errorType.name());
        switch (errorType) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("SearchPresenter", "Playback event received: " + eventType.name());
        playerIsPlaying = playerState.playing;
        position = playerState.positionInMs;
        Log.d("SearchPresenter", String.valueOf(playerState.positionInMs));
        switch (eventType) {
            // Handle event type as necessary
            default:
                break;
        }
    }
}
