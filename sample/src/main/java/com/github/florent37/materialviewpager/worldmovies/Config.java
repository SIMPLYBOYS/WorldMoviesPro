package com.github.florent37.materialviewpager.worldmovies;

/**
 * Created by aaron on 2016/2/24.
 */
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Config {

    public static final String HOST_NAME = "http://ec2-52-196-226-25.ap-northeast-1.compute.amazonaws.com/";

    public static final String NYTimesKey = "f063537e60c46d5a68f8d6eeb7f036cc:6:74862166";

    public static final String URL_NY_TIMES = "https://api.nytimes.com/svc/movies/v2/reviews/search.json?";

    public static final String YOUTUBE_API_KEY = "AIzaSyC1rMU-mkhoyTvBIdTnYU0dss0tU9vtK48";

    public static final String CLIENT_ID = "713961bd892a424f84585a57067750bf";

    public static final String CLIENT_SECRET = "7db9e0e7761a4fd5b2e4653a7229e1b4";

    public static final String REDIRECT_URI = "worldmovie-login://callback";

    public static final String VIDEO_KEY = "mzhX2PD6Srw";

    // Warning messages for dogfood build
    public static final String DOGFOOD_BUILD_WARNING_TITLE = "DOGFOOD BUILD";

    public static final String DOGFOOD_BUILD_WARNING_TEXT = "Shhh! This is a pre-release build "
            + "of the I/O app. Don't show it around.";

    // Turn the hard-coded conference dates in gradle.properties into workable objects.
    public static final long[][] CONFERENCE_DAYS = new long[][]{
            /*// start and end of day 1
            {ParserUtils.parseTime(BuildConfig.CONFERENCE_DAY1_START),
                    ParserUtils.parseTime(BuildConfig.CONFERENCE_DAY1_END)},
            // start and end of day 2
            {ParserUtils.parseTime(BuildConfig.CONFERENCE_DAY2_START),
                    ParserUtils.parseTime(BuildConfig.CONFERENCE_DAY2_END)},*/
    };

    public static final TimeZone CONFERENCE_TIMEZONE = TimeZone.getDefault();
//            TimeZone.getTimeZone(BuildConfig.INPERSON_TIMEZONE);

    public static final long CONFERENCE_START_MILLIS = CONFERENCE_DAYS[0][0];

    public static final long CONFERENCE_END_MILLIS = CONFERENCE_DAYS[CONFERENCE_DAYS.length - 1][1];

    /*public static final long SHOW_IO15_REQUEST_SOCIAL_PANEL_TIME = ParserUtils.parseTime(
            BuildConfig.SHOW_IO15_REQUEST_SOCIAL_PANEL_TIME);*/

    // YouTube share URL
    public static final String YOUTUBE_SHARE_URL_PREFIX = "http://youtu.be/";

    // Live stream captions config
    public static final String LIVESTREAM_CAPTIONS_DARK_THEME_URL_PARAM = "&theme=dark";

    // When do we start to offer to set up the user's wifi?
    public static final long WIFI_SETUP_OFFER_START = (BuildConfig.DEBUG ?
            System.currentTimeMillis() - 1000 :
            CONFERENCE_START_MILLIS - TimeUnit.MILLISECONDS.convert(3L, TimeUnit.DAYS));

    // Format of the youtube link to a Video Library video
    public static final String VIDEO_LIBRARY_URL_FMT = "https://www.youtube.com/watch?v=%s";

    // Fallback URL to get a youtube video thumbnail in case one is not provided in the data
    // (normally it should, but this is a safety fallback if it doesn't)
    public static final String VIDEO_LIBRARY_FALLBACK_THUMB_URL_FMT =
            "http://img.youtube.com/vi/%s/default.jpg";

    // Link to Google I/O Extended events presented in Explore screen
    public static final String IO_EXTENDED_LINK = "http://www.google.com/events/io/io-extended";

    // Auto sync interval. Shouldn't be too small, or it might cause battery drain.
    public static final long AUTO_SYNC_INTERVAL_LONG_BEFORE_CONFERENCE =
            TimeUnit.MILLISECONDS.convert(6L, TimeUnit.HOURS);

    public static final long AUTO_SYNC_INTERVAL_AROUND_CONFERENCE =
            TimeUnit.MILLISECONDS.convert(2L, TimeUnit.HOURS);

    // Disable periodic sync after the conference and rely entirely on GCM push for syncing data.
    public static final long AUTO_SYNC_INTERVAL_AFTER_CONFERENCE = -1L;

    // How many days before the conference we consider to be "around the conference date"
    // for purposes of sync interval (at which point the AUTO_SYNC_INTERVAL_AROUND_CONFERENCE
    // interval kicks in)
    public static final long AUTO_SYNC_AROUND_CONFERENCE_THRESH =
            TimeUnit.MILLISECONDS.convert(3L, TimeUnit.DAYS);

    // Minimum interval between two consecutive syncs. This is a safety mechanism to throttle
    // syncs in case conference data gets updated too often or something else goes wrong that
    // causes repeated syncs.
    public static final long MIN_INTERVAL_BETWEEN_SYNCS =
            TimeUnit.MILLISECONDS.convert(10L, TimeUnit.MINUTES);

    // If data is not synced in this much time, we show the "data may be stale" warning
    public static final long STALE_DATA_THRESHOLD_NOT_DURING_CONFERENCE =
            TimeUnit.MILLISECONDS.convert(2L, TimeUnit.DAYS);

    public static final long STALE_DATA_THRESHOLD_DURING_CONFERENCE =
            TimeUnit.MILLISECONDS.convert(12L, TimeUnit.HOURS);

    // How long we snooze the stale data notification for after the user has acted on it
    // (to keep from showing it repeatedly and being annoying)
    public static final long STALE_DATA_WARNING_SNOOZE =
            TimeUnit.MILLISECONDS.convert(10L, TimeUnit.MINUTES);

    // Play store URL prefix
    public static final String PLAY_STORE_URL_PREFIX
            = "https://play.google.com/store/apps/details?id=";

    // Known session tags that induce special behaviors
    public interface Tags {

        // tag that indicates a session is a live session
        public static final String SESSIONS = "TYPE_SESSIONS";

        // the tag category that we use to group sessions together when displaying them
        public static final String SESSION_GROUPING_TAG_CATEGORY = "TYPE";

        // tag categories
        public static final String CATEGORY_THEME = "THEME";
        public static final String CATEGORY_TOPIC = "TOPIC";
        public static final String CATEGORY_GENRE = "GENRE";
        public static final String CATEGORY_TYPE = "TYPE";
        public static final String CATEGORY_COUNTRY = "COUNTRY";

        public static final Map<String, Integer> CATEGORY_DISPLAY_ORDERS
                = new HashMap<String, Integer>();

        public static final String SPECIAL_KEYNOTE = "FLAG_KEYNOTE";

        public static final String[] EXPLORE_CATEGORIES =
                {CATEGORY_THEME,CATEGORY_GENRE, CATEGORY_TOPIC, CATEGORY_TYPE, CATEGORY_COUNTRY};

        public static final int[] EXPLORE_CATEGORY_ALL_STRING = {
                R.string.all_themes, R.string.all_topics, R.string.all_types
        };

        public static final int[] EXPLORE_CATEGORY_TITLE = {
                R.string.themes, R.string.topics, R.string.types
        };
    }

    static {
        Tags.CATEGORY_DISPLAY_ORDERS.put(Tags.CATEGORY_THEME, 0);
        Tags.CATEGORY_DISPLAY_ORDERS.put(Tags.CATEGORY_TOPIC, 1);
        Tags.CATEGORY_DISPLAY_ORDERS.put(Tags.CATEGORY_TYPE, 2);
        Tags.CATEGORY_DISPLAY_ORDERS.put(Tags.CATEGORY_COUNTRY,3);
        Tags.CATEGORY_DISPLAY_ORDERS.put(Tags.CATEGORY_GENRE,4);
    }

    // URL prefix for web links to session pages
    public static final String SESSION_DETAIL_WEB_URL_PREFIX
            = "https://www.google.com/events/io/schedule/session/";

}
