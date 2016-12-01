/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.florent37.materialviewpager.worldmovies.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.materialviewpager.worldmovies.BuildConfig;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.favorite.MoviesFavoritePreference;
import com.github.florent37.materialviewpager.worldmovies.model.ScheduleItem;
import com.github.florent37.materialviewpager.worldmovies.nytimes.Movie;
import com.github.florent37.materialviewpager.worldmovies.provider.ScheduleContract;
import com.github.florent37.materialviewpager.worldmovies.provider.ScheduleContract.Rooms;
import com.github.florent37.materialviewpager.worldmovies.settings.SettingsUtils;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.TimeZone;
import java.util.regex.Pattern;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGE;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.makeLogTag;

/**
 * An assortment of UI helpers.
 */
public class UIUtils {
    private static final String TAG = makeLogTag(UIUtils.class);

    /**
     * Factor applied to session color to derive the background color on panels and when
     * a session photo could not be downloaded (or while it is being downloaded)
     */
    public static final float SESSION_BG_COLOR_SCALE_FACTOR = 0.75f;

    private static final float SESSION_PHOTO_SCRIM_ALPHA = 0.25f; // 0=invisible, 1=visible image
    private static final float SESSION_PHOTO_SCRIM_SATURATION = 0.2f; // 0=gray, 1=color image

    /**
     * Flags used with {@link DateUtils#formatDateRange}.
     */
    private static final int TIME_FLAGS = DateUtils.FORMAT_SHOW_TIME
            | DateUtils.FORMAT_SHOW_DATE;

    /**
     * Regex to search for HTML escape sequences.
     *
     * <p></p>Searches for any continuous string of characters starting with an ampersand and ending with a
     * semicolon. (Example: &amp;amp;)
     */
    private static final Pattern REGEX_HTML_ESCAPE = Pattern.compile(".*&\\S;.*");
    public static final String MOCK_DATA_PREFERENCES = "mock_data";
    public static final String PREFS_MOCK_CURRENT_TIME = "mock_current_time";

    public static final String GOOGLE_PLUS_PACKAGE_NAME = "com.google.android.apps.plus";
    public static final String YOUTUBE_PACKAGE_NAME = "com.google.android.youtube";
    public static final String TWITTER_PACKAGE_NAME = "com.twitter.app";

    public static final String GOOGLE_PLUS_COMMON_NAME = "Google Plus";
    public static final String TWITTER_COMMON_NAME = "Twitter";

    public static String formatSessionSubtitle(long intervalStart, long intervalEnd, String roomName, StringBuilder recycle,
                                               Context context) {
        return formatSessionSubtitle(intervalStart, intervalEnd, roomName, recycle, context, false);
    }

    /**
     * Format and return the given session time and {@link Rooms} values using
     * {@link Config#CONFERENCE_TIMEZONE}.
     */
    public static String formatSessionSubtitle(long intervalStart, long intervalEnd, String roomName, StringBuilder recycle,
                                               Context context, boolean shortFormat) {

        // Determine if the session is in the past
        long currentTimeMillis = UIUtils.getCurrentTime(context);
        boolean conferenceEnded = currentTimeMillis > Config.CONFERENCE_END_MILLIS;
        boolean sessionEnded = currentTimeMillis > intervalEnd;
        if (sessionEnded && !conferenceEnded) {
            return context.getString(R.string.session_finished);
        }

        if (roomName == null) {
            roomName = context.getString(R.string.unknown_room);
        }

        if (shortFormat) {
            TimeZone timeZone = SettingsUtils.getDisplayTimeZone(context);
            Date intervalStartDate = new Date(intervalStart);
            SimpleDateFormat shortDateFormat = new SimpleDateFormat("MMM dd");
            DateFormat shortTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
            shortDateFormat.setTimeZone(timeZone);
            shortTimeFormat.setTimeZone(timeZone);
            return shortDateFormat.format(intervalStartDate) + " "
                    + shortTimeFormat.format(intervalStartDate);
        } else {
            String timeInterval = formatIntervalTimeString(intervalStart, intervalEnd, recycle,
                    context);
            return context.getString(R.string.session_subtitle, timeInterval, roomName);
        }
    }

    /**
     * Format and return the given session speakers and {@link Rooms} values.
     */
    public static String formatSessionSubtitle(String roomName, String speakerNames,
                                               Context context) {

        // Determine if the session is in the past
        if (roomName == null) {
            roomName = context.getString(R.string.unknown_room);
        }

        if (!TextUtils.isEmpty(speakerNames)) {
            return speakerNames + "\n" + roomName;
        } else {
            return roomName;
        }
    }

    /**
     * Format and return the given time interval using {@link Config#CONFERENCE_TIMEZONE}
     * (unless local time was explicitly requested by the user).
     */
    public static String formatIntervalTimeString(long intervalStart, long intervalEnd,
                                                  StringBuilder recycle, Context context) {
        if (recycle == null) {
            recycle = new StringBuilder();
        } else {
            recycle.setLength(0);
        }
        Formatter formatter = new Formatter(recycle);
        return DateUtils.formatDateRange(context, formatter, intervalStart, intervalEnd, TIME_FLAGS,
                SettingsUtils.getDisplayTimeZone(context).getID()).toString();
    }

    public static boolean isSameDayDisplay(long time1, long time2, Context context) {
        TimeZone displayTimeZone = SettingsUtils.getDisplayTimeZone(context);
        Calendar cal1 = Calendar.getInstance(displayTimeZone);
        Calendar cal2 = Calendar.getInstance(displayTimeZone);
        cal1.setTimeInMillis(time1);
        cal2.setTimeInMillis(time2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Populate the given {@link TextView} with the requested text, formatting
     * through {@link Html#fromHtml(String)} when applicable. Also sets
     * {@link TextView#setMovementMethod} so inline links are handled.
     */
    public static void setTextMaybeHtml(TextView view, String text) {
        if (TextUtils.isEmpty(text)) {
            view.setText("");
            return;
        }
        if ((text.contains("<") && text.contains(">")) || REGEX_HTML_ESCAPE.matcher(text).find()) {
            view.setText(Html.fromHtml(text));
            view.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            view.setText(text);
        }
    }

    public static String getLiveBadgeText(final Context context, long start, long end) {
        long now = getCurrentTime(context);

        if (now < start) {
            // Will be live later
            return context.getString(R.string.live_available);
        } else if (start <= now && now <= end) {
            // Live right now!
            // Indicated by a visual live now badge
            return "";
        } else {
            // Too late.
            return "";
        }
    }

    /**
     * Given a snippet string with matching segments surrounded by curly
     * braces, turn those areas into bold spans, removing the curly braces.
     */
    public static Spannable buildStyledSnippet(String snippet) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(snippet);

        // Walk through string, inserting bold snippet spans
        int startIndex, endIndex = -1, delta = 0;
        while ((startIndex = snippet.indexOf('{', endIndex)) != -1) {
            endIndex = snippet.indexOf('}', startIndex);

            // Remove braces from both sides
            builder.delete(startIndex - delta, startIndex - delta + 1);
            builder.delete(endIndex - delta - 1, endIndex - delta);

            // Insert bold style
            builder.setSpan(new StyleSpan(Typeface.BOLD),
                    startIndex - delta, endIndex - delta - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //builder.setSpan(new ForegroundColorSpan(0xff111111),
            //        startIndex - delta, endIndex - delta - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            delta += 2;
        }

        return builder;
    }

    /**
     * This allows the app to specify a {@code packageName} to handle the {@code intent}, if the
     * {@code packageName} is available on the device and can handle it. An example use is to open
     * a Google + stream directly using the Google + app.
     */
    public static void preferPackageForIntent(Context context, Intent intent, String packageName) {
        PackageManager pm = context.getPackageManager();
        if (pm != null) {
            for (ResolveInfo resolveInfo : pm.queryIntentActivities(intent, 0)) {
                if (resolveInfo.activityInfo.packageName.equals(packageName)) {
                    intent.setPackage(packageName);
                    break;
                }
            }
        }
    }

    private static final int BRIGHTNESS_THRESHOLD = 130;

    /**
     * Calculate whether a color is light or dark, based on a commonly known
     * brightness formula.
     *
     * @see {@literal http://en.wikipedia.org/wiki/HSV_color_space%23Lightness}
     */
    public static boolean isColorDark(int color) {
        return ((30 * Color.red(color) +
                59 * Color.green(color) +
                11 * Color.blue(color)) / 100) <= BRIGHTNESS_THRESHOLD;
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    // Shows whether a notification was fired for a particular session time block. In the
    // event that notification has not been fired yet, return false and set the bit.
    public static boolean isNotificationFiredForBlock(Context context, String blockId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        final String key = String.format("notification_fired_%s", blockId);
        boolean fired = sp.getBoolean(key, false);
        sp.edit().putBoolean(key, true).apply();
        return fired;
    }

    private static final long sAppLoadTime = System.currentTimeMillis();

    /**
     * Retrieve the current time. If the current build is a debug build the mock time is returned
     * when set.
     */
    public static long getCurrentTime(final Context context) {
        if (BuildConfig.DEBUG) {
            return context.getSharedPreferences(MOCK_DATA_PREFERENCES, Context.MODE_PRIVATE)
                    .getLong(PREFS_MOCK_CURRENT_TIME, System.currentTimeMillis())
                    + System.currentTimeMillis() - sAppLoadTime;
        } else {
            return System.currentTimeMillis();
        }
    }

    /**
     * Set the current time only when the current build is a debug build.
     */
    public static void setCurrentTime(Context context, long newTime) {
        if (BuildConfig.DEBUG) {
            context.getSharedPreferences(MOCK_DATA_PREFERENCES, Context.MODE_PRIVATE).edit()
                    .putLong(PREFS_MOCK_CURRENT_TIME, newTime).apply();
        }
    }

    @Deprecated
    public static boolean shouldShowLiveSessionsOnly(final Context context) {
        return !SettingsUtils.isAttendeeAtVenue(context)
                && getCurrentTime(context) < Config.CONFERENCE_END_MILLIS;
    }

    /**
     * If an activity's intent is for a Google I/O web URL that the app can handle
     * natively, this method translates the intent to the equivalent native intent.
     */
    public static void tryTranslateHttpIntent(Activity activity) {
        Intent intent = activity.getIntent();
        if (intent == null) {
            return;
        }

        Uri uri = intent.getData();
        if (uri == null || TextUtils.isEmpty(uri.getPath())) {
            return;
        }

        Uri sessionDetailWebUrlPrefix = Uri.parse(Config.SESSION_DETAIL_WEB_URL_PREFIX);
        String prefixPath = sessionDetailWebUrlPrefix.getPath();
        String path = uri.getPath();

        if (sessionDetailWebUrlPrefix.getScheme().equals(uri.getScheme()) &&
                sessionDetailWebUrlPrefix.getHost().equals(uri.getHost()) &&
                path.startsWith(prefixPath)) {
            String sessionId = path.substring(prefixPath.length());
            activity.setIntent(new Intent(
                    Intent.ACTION_VIEW,
                    ScheduleContract.Sessions.buildSessionUri(sessionId)));
        }
    }

    private static final int[] RES_IDS_ACTION_BAR_SIZE = { R.attr.actionBarSize };

    /** Calculates the Action Bar height in pixels. */
    public static int calculateActionBarSize(Context context) {
        if (context == null) {
            return 0;
        }

        Resources.Theme curTheme = context.getTheme();
        if (curTheme == null) {
            return 0;
        }

        TypedArray att = curTheme.obtainStyledAttributes(RES_IDS_ACTION_BAR_SIZE);
        if (att == null) {
            return 0;
        }

        float size = att.getDimension(0, 0);
        att.recycle();
        return (int) size;
    }

    public static int setColorOpaque(int color){
        return Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
    }

    public static int scaleColor(int color, float factor, boolean scaleAlpha) {
        return Color.argb(scaleAlpha ? (Math.round(Color.alpha(color) * factor)) : Color.alpha(color),
                Math.round(Color.red(color) * factor), Math.round(Color.green(color) * factor),
                Math.round(Color.blue(color) * factor));
    }

    public static int scaleSessionColorToDefaultBG(int color) {
        return scaleColor(color, SESSION_BG_COLOR_SCALE_FACTOR, false);
    }


    public static void fireSocialIntent(Context context, Uri uri, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        UIUtils.preferPackageForIntent(context, intent, packageName);
        context.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRtl(final Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return false;
        } else {
            return context.getResources().getConfiguration().getLayoutDirection()
                    == View.LAYOUT_DIRECTION_RTL;
        }
    }

    public static void setAccessibilityIgnore(View view) {
        view.setClickable(false);
        view.setFocusable(false);
        view.setContentDescription("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }

    public static void setUpButterBar(View butterBar, String messageText, String actionText,
                                      View.OnClickListener listener) {
        if (butterBar == null) {
            LOGE(TAG, "Failed to set up butter bar: it's null.");
            return;
        }

        TextView textView = (TextView) butterBar.findViewById(R.id.butter_bar_text);
        if (textView != null) {
            textView.setText(messageText);
        }

        Button button = (Button) butterBar.findViewById(R.id.butter_bar_button);
        if (button != null) {
            button.setText(actionText == null ? "" : actionText);
            button.setVisibility(!TextUtils.isEmpty(actionText) ? View.VISIBLE : View.GONE);
        }

        button.setOnClickListener(listener);
        butterBar.setVisibility(View.VISIBLE);
    }

    public static float getProgress(int value, int min, int max) {
        if (min == max) {
            throw new IllegalArgumentException("Max (" + max + ") cannot equal min (" + min + ")");
        }

        return (value - min) / (float) (max - min);
    }

    public static @DrawableRes int getSessionIcon(int sessionType) {
        switch (sessionType) {
            case ScheduleItem.SESSION_TYPE_SESSION:
                return R.drawable.ic_session;
            case ScheduleItem.SESSION_TYPE_CODELAB:
                return R.drawable.ic_codelab;
            case ScheduleItem.SESSION_TYPE_BOXTALK:
                return R.drawable.ic_sandbox;
            case ScheduleItem.SESSION_TYPE_MISC:
            default:
                return R.drawable.ic_misc;
        }
    }

    public static @DrawableRes int getBreakIcon(String breakTitle) {
        if (!TextUtils.isEmpty(breakTitle)) {
            if (breakTitle.contains("After")) {
                return R.drawable.ic_after_hours;
            } else if (breakTitle.contains("Badge")) {
                return R.drawable.ic_badge_pickup;
            } else if (breakTitle.contains("Pre-Keynote")) {
                return R.drawable.ic_session;
            }
        }
        return R.drawable.ic_food;
    }

    /**
     * @param startTime The start time of a session in millis.
     * @param context The context to be used for getting the display timezone.
     * @return Formats a given startTime to the specific short time.
     *         example: 12:00 AM
     */
    public static String formatTime(long startTime, Context context) {
        StringBuilder sb = new StringBuilder();
        DateUtils.formatDateRange(context, new Formatter(sb), startTime, startTime,
                DateUtils.FORMAT_SHOW_TIME,
                SettingsUtils.getDisplayTimeZone(context).getID());
        return sb.toString();
    }

    /**
     * @param startTime The start time of a session.
     * @return Returns the Day index such as 1 or 2 based on the given start time.
     */
    public static int startTimeToDayIndex(long startTime) {
        if (startTime <= Config.CONFERENCE_DAYS[0][1] &&
                startTime >= Config.CONFERENCE_DAYS[0][0]) {
            return 1;
        } else if (startTime <= Config.CONFERENCE_DAYS[1][1] &&
                startTime >= Config.CONFERENCE_DAYS[1][0]) {
            return 2;
        }
        return 0;
    }

    // Desaturates and color-scrims the image
    public static ColorFilter makeSessionImageScrimColorFilter(int sessionColor) {
        float a = SESSION_PHOTO_SCRIM_ALPHA;
//        return new ColorMatrixColorFilter(new float[]{
//                a, 0, 0, 0, 0,
//                0, a, 0, 0, 0,
//                0, 0, a, 0, 0,
//                0, 0, 0, 0, 255
//        });
//        return new ColorMatrixColorFilter(new float[]{
//                a, 0, 0, 0, Color.red(sessionColor) * (1 - a),
//                0, a, 0, 0, Color.green(sessionColor) * (1 - a),
//                0, 0, a, 0, Color.blue(sessionColor) * (1 - a),
//                0, 0, 0, 0, 255
//        });
//        return new ColorMatrixColorFilter(new float[]{
//                0.213f * a, 0.715f * a, 0.072f * a, 0, Color.red(sessionColor) * (1 - a),
//                0.213f * a, 0.715f * a, 0.072f * a, 0, Color.green(sessionColor) * (1 - a),
//                0.213f * a, 0.715f * a, 0.072f * a, 0, Color.blue(sessionColor) * (1 - a),
//                0, 0, 0, 0, 255
//        });
//        ColorMatrix cm = new ColorMatrix();
//        cm.setSaturation(0f);
//        cm.postConcat(alphaMatrix(0.5f, Color.WHITE));
//        cm.postConcat(multiplyBlendMatrix(sessionColor, 0.9f));
//        return new ColorMatrixColorFilter(cm);
        float sat = SESSION_PHOTO_SCRIM_SATURATION; // saturation (0=gray, 1=color)
        return new ColorMatrixColorFilter(new float[]{
                ((1 - 0.213f) * sat + 0.213f) * a, ((0 - 0.715f) * sat + 0.715f) * a, ((0 - 0.072f) * sat + 0.072f) * a, 0, Color.red(sessionColor) * (1 - a),
                ((0 - 0.213f) * sat + 0.213f) * a, ((1 - 0.715f) * sat + 0.715f) * a, ((0 - 0.072f) * sat + 0.072f) * a, 0, Color.green(sessionColor) * (1 - a),
                ((0 - 0.213f) * sat + 0.213f) * a, ((0 - 0.715f) * sat + 0.715f) * a, ((1 - 0.072f) * sat + 0.072f) * a, 0, Color.blue(sessionColor) * (1 - a),
                0, 0, 0, 0, 255
        });
//        a = 0.2f;
//        return new ColorMatrixColorFilter(new float[]{
//                0.213f * a, 0.715f * a, 0.072f * a, 0, Color.red(sessionColor) - 255 * a / 2,
//                0.213f * a, 0.715f * a, 0.072f * a, 0, Color.green(sessionColor) - 255 * a / 2,
//                0.213f * a, 0.715f * a, 0.072f * a, 0, Color.blue(sessionColor) - 255 * a / 2,
//                0, 0, 0, 0, 255
//        });
    }

//    private static final float[] mAlphaMatrixValues = {
//            0, 0, 0, 0, 0,
//            0, 0, 0, 0, 0,
//            0, 0, 0, 0, 0,
//            0, 0, 0, 1, 0
//    };
//    private static final ColorMatrix mMultiplyBlendMatrix = new ColorMatrix();
//    private static final float[] mMultiplyBlendMatrixValues = {
//            0, 0, 0, 0, 0,
//            0, 0, 0, 0, 0,
//            0, 0, 0, 0, 0,
//            0, 0, 0, 1, 0
//    };
//    private static final ColorMatrix mWhitenessColorMatrix = new ColorMatrix();
//
//    /**
//     * Simulates alpha blending an image with {@param color}.
//     */
//    private static ColorMatrix alphaMatrix(float alpha, int color) {
//        mAlphaMatrixValues[0] = 255 * alpha / 255;
//        mAlphaMatrixValues[6] = Color.green(color) * alpha / 255;
//        mAlphaMatrixValues[12] = Color.blue(color) * alpha / 255;
//        mAlphaMatrixValues[4] = 255 * (1 - alpha);
//        mAlphaMatrixValues[9] = 255 * (1 - alpha);
//        mAlphaMatrixValues[14] = 255 * (1 - alpha);
//        mWhitenessColorMatrix.set(mAlphaMatrixValues);
//        return mWhitenessColorMatrix;
//    }
//    /**
//     * Simulates multiply blending an image with a single {@param color}.
//     *
//     * Multiply blending is [Sa * Da, Sc * Dc]. See {@link android.graphics.PorterDuff}.
//     */
//    private static ColorMatrix multiplyBlendMatrix(int color, float alpha) {
//        mMultiplyBlendMatrixValues[0] = multiplyBlend(Color.red(color), alpha);
//        mMultiplyBlendMatrixValues[6] = multiplyBlend(Color.green(color), alpha);
//        mMultiplyBlendMatrixValues[12] = multiplyBlend(Color.blue(color), alpha);
//        mMultiplyBlendMatrix.set(mMultiplyBlendMatrixValues);
//        return mMultiplyBlendMatrix;
//    }
//
//    private static float multiplyBlend(int color, float alpha) {
//        return color * alpha / 255.0f + (1 - alpha);
//    }

    /**
     * This helper method creates a 'nice' scrim or background protection for layering text over
     * an image. This non-linear scrim is less noticable than a linear or constant one.
     *
     * Borrowed from github.com/romannurik/muzei
     *
     * Creates an approximated cubic gradient using a multi-stop linear gradient. See
     * <a href="https://plus.google.com/+RomanNurik/posts/2QvHVFWrHZf">this post</a> for more
     * details.
     */
    public static Drawable makeCubicGradientScrimDrawable(int baseColor, int numStops, int gravity) {
        numStops = Math.max(numStops, 2);

        PaintDrawable paintDrawable = new PaintDrawable();
        paintDrawable.setShape(new RectShape());

        final int[] stopColors = new int[numStops];

        int alpha = Color.alpha(baseColor);

        for (int i = 0; i < numStops; i++) {
            double x = i * 1f / (numStops - 1);
            double opacity = Math.max(0, Math.min(1, Math.pow(x, 3)));
            stopColors[i] = (baseColor & 0x00ffffff) | ((int) (alpha * opacity) << 24);
        }

        final float x0, x1, y0, y1;
        switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.LEFT:  x0 = 1; x1 = 0; break;
            case Gravity.RIGHT: x0 = 0; x1 = 1; break;
            default:            x0 = 0; x1 = 0; break;
        }
        switch (gravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.TOP:    y0 = 1; y1 = 0; break;
            case Gravity.BOTTOM: y0 = 0; y1 = 1; break;
            default:             y0 = 0; y1 = 0; break;
        }

        paintDrawable.setShaderFactory(new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                LinearGradient linearGradient = new LinearGradient(
                        width * x0,
                        height * y0,
                        width * x1,
                        height * y1,
                        stopColors, null,
                        Shader.TileMode.CLAMP);
                return linearGradient;
            }
        });

        return paintDrawable;
    }

    public static boolean checkMoviesBookmark(String headline, MoviesFavoritePreference moviesFavor, Context context) {
        headline = headline.indexOf(":") != -1 ? headline.split(":")[1].trim() : headline;
        ArrayList list = moviesFavor.loadFavorites(context);

        if (list == null)
            return false;

        for (int i=0; i<list.size(); i++) {
            if (headline.compareTo((String) list.get(i)) == 0) return true;
        }

        return false;
    };

    public static String getTrendsUrl(Movie movie) {
        String url = "";
        String Query = movie.getHeadline();

        try {
            Query = URLEncoder.encode(Query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        }

        switch (movie.getChannel()) {
            case 0:
                url = Config.HOST_NAME + "jpTrends?title=" + Query;
                break;
            case 1:
                url = Config.HOST_NAME + "usTrends?title=" + Query;
                break;
            case 2:
                url = Config.HOST_NAME + "twTrends?title=" + Query;
                break;
            case 3:
                url = Config.HOST_NAME + "krTrends?title=" + Query;
                break;
            case 4:
                url = Config.HOST_NAME + "frTrends?title=" + Query;
                break;
            case 5:
                url = Config.HOST_NAME + "cnTrends?title=" + Query;
                break;
            case 6:
                url = Config.HOST_NAME + "gmTrends?title=" + Query;
                break;
            default:
                Log.d("1115", movie.getCountry());
                int country = getCountryCode(movie.getCountry());
                url = Config.HOST_NAME + "world/"+country+"/?title=" + Query;
        }

        return url;
    }

    public static void drawCountryFlag(View view, int order) {
        switch (order) {
            case 1:
                Picasso.with(view.getContext()).load(R.drawable.australia).into((ImageView) view.findViewById(R.id.pic));
                break;
            case 2:
                Picasso.with(view.getContext()).load(R.drawable.china).into((ImageView) view.findViewById(R.id.pic));
                break;
            case 3:
                Picasso.with(view.getContext()).load(R.drawable.fr).into((ImageView) view.findViewById(R.id.pic));
                break;
            case 4:
                Picasso.with(view.getContext()).load(R.drawable.gm).into((ImageView) view.findViewById(R.id.pic));
                break;
            case 5:
                Picasso.with(view.getContext()).load(R.drawable.hong_kong).into((ImageView) view.findViewById(R.id.pic));
                break;
            case 6:
                Picasso.with(view.getContext()).load(R.drawable.india).into((ImageView) view.findViewById(R.id.pic));
                break;
            case 7:
                Picasso.with(view.getContext()).load(R.drawable.it).into((ImageView) view.findViewById(R.id.pic));
                break;
            case 8:
                Picasso.with(view.getContext()).load(R.drawable.japan).into((ImageView) view.findViewById(R.id.pic));
                break;
            case 9:
                Picasso.with(view.getContext()).load(R.drawable.korea).into((ImageView) view.findViewById(R.id.pic));
                break;
            case 10:
                Picasso.with(view.getContext()).load(R.drawable.poland).into((ImageView) view.findViewById(R.id.pic));
                break;
            case 11:
                Picasso.with(view.getContext()).load(R.drawable.es).into((ImageView) view.findViewById(R.id.pic));
                break;
            case 12:
                Picasso.with(view.getContext()).load(R.drawable.taiwan).into((ImageView) view.findViewById(R.id.pic));
                break;
            case 13:
                Picasso.with(view.getContext()).load(R.drawable.thailand).into((ImageView) view.findViewById(R.id.pic));
                break;
            case 15:
                Picasso.with(view.getContext()).load(R.drawable.uk).into((ImageView) view.findViewById(R.id.pic));
                break;
            default:
                Picasso.with(view.getContext()).load(R.drawable.usa).into((ImageView) view.findViewById(R.id.pic));
                break;
        }
    }

    public static int getCountryCode(String country) {
        int code = 0;
        Log.d("1115", country);
        switch (country) {
            case "France":
            case "Français":
                code = 3;
                break;
            case "Germany":
            case "Deutschland":
            case "West Germany":
            case "德國":
                code = 4;
                break;
            case "일본":
            case "日本":
            case "Japan":
                code = 8;
                break;
            case "Brazil":
                code = 14;
                break;
            case "Italy":
            case "義大利":
                code = 7;
                break;
            case "New Zealand":
                code = 14;
                break;
            case "한국":
            case "Korea":
            case "South Korea":
                code = 9;
                break;
            case "UK":
            case "Britannique":
                code = 15;
                break;
            case "Iran":
                code = 14;
                break;
            case "India":
            case "印度":
                code = 6;
                break;
            case "Lebanon":
                code = 14;
                break;
            case "Spain":
            case "西班牙":
            case "Espagne":
                code = 11;
                break;
            case "Turkey":
                code = 14;
                break;
            case "Sweden":
                code = 14;
                break;
            case "Argentina":
                code = 14;
                break;
            case "Canada":
                code = 14;
                break;
            case "Australia":
            case "澳大利亞":
                code = 1;
                break;
            case "Ireland":
                code = 14;
                break;
            case "Mexico":
                code = 14;
                break;
            case "Soviet Union":
                code = 14;
                break;
            case "Hong Kong":
            case "香港":
            case "中國香港":
                code = 5;
                break;
            case "China":
            case "中國大陸":
                code = 2;
                break;
            case "Denmark":
                code = 14;
                break;
            case "Taiwan":
            case "臺灣":
                code = 12;
                break;
            case "Thailand":
            case "泰國":
                code = 13;
                break;
            case "Russia":
                code = 14;
                break;
            case "Américain":
            case "미국":
            case "アメリカ":
            case "USA":
                code = 14;
                break;
            case "Poland":
            case "波蘭":
                code = 10;
                break;
            default:
                code = 14;
                break;
        }
        return code;
    }
}
