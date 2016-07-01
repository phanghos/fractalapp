package org.taitascioredev.fractal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.LoggedInAccount;
import net.dean.jraw.models.Submission;

import java.util.Calendar;
import java.util.Set;
import java.util.regex.Pattern;

import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

/**
 * Created by roberto on 21/03/15.
 */
public class Utils {

    private static final String KEY_PREF_DISPLAY_STYLE = "pref_displayStyle";

    public static boolean refresh = false;
    public static boolean hasActiveUser = false;
    public static LoggedInAccount me;
    public static boolean logout;
    public static RedditClient client;
    public static Submission submission;
    public static boolean update = true;
    public static boolean updateSubreddits = true;
    public static String username = null;

    public static float minutesSince(long milliseconds) {
        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        return (now - milliseconds) / 60000f;
    }

    public static int dpToPixels(Context context, int dp) {
        //final float scale = context.getResources().getDisplayMetrics().density;
        //return (int) (dp * scale + 0.5f);
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

    public static int pixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return (int) dp;
    }

    public static boolean isImageUrl(String url) {
        String regexp = "http://(i.)*imgur.com/\\p{Alnum}+(.jpg)?(\\?\\p{Alnum}+)?";
        return Pattern.matches(regexp, url);
    }

    public static String getImageUrl(String url, char ch) {
        //Log.d("debug", "IMG URL: " + url);
        char lastChar = url.charAt(url.length() - 1);

        if (!url.endsWith(".jpg")) {
            if (lastChar != ch && lastChar != 'h')
                return url + ch + ".jpg";
            return url + ".jpg";
        }
        int index = url.indexOf(".jpg");
        String sub = url.substring(0, index);
        lastChar = sub.charAt(sub.length() - 1);
        if (lastChar != ch && lastChar != 'h')
            return sub + ch + ".jpg";
        return sub + ".jpg";
    }

    public static CharSequence setUrlSpans(final AppCompatActivity context, Spanned spanned, boolean cutContent) {
        String str = spanned.toString();
        str = str.replaceAll("<div class=\"md\">", "");
        str = str.replaceAll("</div>", "");
        StringBuilder builder = new StringBuilder(str);
        int index = builder.lastIndexOf("<p>");
        if (index != -1) {
            builder = builder.replace(index, index + 3, "");
            index = builder.lastIndexOf("</p>");
            builder = builder.replace(index, index + 4, "");
        }
        str = builder.toString();
        SpannableStringBuilder ssb = new SpannableStringBuilder(Html.fromHtml(str));
        URLSpan[] spans = ssb.getSpans(0, ssb.length(), URLSpan.class);

        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(
                TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));

        for (int i = 0; i < spans.length; i++) {
            Log.d("debug", spans[i].toString());
            int start = ssb.getSpanStart(spans[i]);
            int end = ssb.getSpanEnd(spans[i]);
            final String url = spans[i].getURL();

            for (final FormattedLink link : FormattedLink.extract(url)) {
                //start = link.getStart();
                //end = link.getEnd();
                ssb.removeSpan(spans[i]);

                MyClickableSpan span = new MyClickableSpan(context.getResources().getColor(R.color.colorPrimary)) {
                    @Override
                    public void onClick(View widget) {
                        Log.d("onClick", link.getText());
                        switch (link.getType()) {
                            case FormattedLink.TYPE_SUBREDDIT:
                                MyApp app = (MyApp) context.getApplication();
                                app.setSubredditPaginator(null);
                                app.setSubmissionsSubreddit(null);

                                SubredditPageFragment fragment = new SubredditPageFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("subreddit_url", link.getText().substring(3));
                                fragment.setArguments(bundle);

                                context.getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, fragment)
                                        .addToBackStack(null).commit();
                                break;
                        }
                    }
                };
                ssb.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                ssb.setSpan(typefaceSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }

            /*
            //String regexp = "/[rum]/\\S{3,20}";
            String regexp = "/[rum]/[\\w[-]]{3,20}";
            boolean b = Pattern.matches(regexp, url);
            if (b) {
                ssb.removeSpan(spans[i]);
                ClickableSpan span = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        Log.d("debug", "CLICKED ON SUBREDDIT/USER/MULTIREDDIT");
                        String name = url.substring(3);
                        if (url.startsWith("/r/")) {
                            MyApp app = (MyApp) context.getApplication();
                            app.setSubredditPaginator(null);
                            app.setSubmissionsSubreddit(null);

                            SubredditPageFragment fragment = new SubredditPageFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("subreddit_url", name);
                            fragment.setArguments(bundle);

                            context.getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null).commit();
                        }
                    }
                };
                ssb.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
            */
        }
        if (cutContent) {
            int limit = 200;
            if (ssb.length() > limit) {
                int lastSpaceIndex = ssb.toString().indexOf(" ", limit);
                return ssb.subSequence(0, lastSpaceIndex) + " (...)";
            }
        }
        return ssb;
    }

    public static void logout(AppCompatActivity context) {
        MyApp app = (MyApp) context.getApplication();
        SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();

        /*
        Set<String> accounts = pref.getStringSet(Constants.SHARED_PREF_ACCOUNTS, null);
        Set<String> tokens = pref.getStringSet(Constants.SHARED_PREF_REFRESH_TOKEN, null);
        if (accounts.size() == 1 && tokens.size() == 1) {
            editor.clear();
        }
        else {
            MaterialAccount account = context.getCurrentAccount();
            String title = account.getTitle();
            accounts.remove(title);
            String token = findTokenByAccount(context, title);
            if (token != null)
                tokens.remove(token);
            editor.remove(Constants.SHARED_PREF_CURRENT_ACCOUNT);
        }
        */

        editor.commit();
        app.setContext(null);
        app.setClient(null);
        app.setPaginator(null);
        app.setSubmissions(null);
        app.setSubredditStream(null);
        app.setUserSubredditsPaginator(null);
        app.setSubreddits(null);
        app.setUserSubreddits(null);
        app.setSubredditPaginator(null);
        app.setSubmissionsSubreddit(null);
        app.setInboxPaginator(null);
        app.setMessage(null);
        app.setMessages(null);
        context.startActivity(new Intent(context, EntryActivity.class));
        context.finish();
    }

    public static String findTokenByAccount(Activity context, String account) {
        SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Set<String> tokens = pref.getStringSet(Constants.SHARED_PREF_REFRESH_TOKEN, null);
        if (tokens == null)
            return null;
        for (String s : tokens)
            if (s.startsWith(account))
                return s.split("_")[1];

        return null;
    }

    public static String getRefreshToken(AppCompatActivity context) {
        SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(Constants.SHARED_PREF_REFRESH_TOKEN, null);
    }

    public static String getUsername(AppCompatActivity context) {
        SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(Constants.SHARED_PREF_ACCOUNT_USERNAME, null);
    }

    public static int getLinkKarma(AppCompatActivity context) {
        SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(Constants.SHARED_PREF_ACCOUNT_LINK_KARMA, -1);
    }

    public static long getTime(Activity context) {
        SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return pref.getLong(Constants.SHARED_PREF_TIME, -1);
    }

    public static String trimSpaces(String str) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isWhitespace(c))
                builder.append(c);
        }
        return builder.toString();
    }

    public static boolean isValidUrl(String url) {
        String regexp = "(http|https)://\\w+\\.[\\w[-]]+\\.\\w+\\p{Graph}*";
        return Pattern.matches(regexp, url);
    }

    public static String getDisplayPreference(AppCompatActivity context) {
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(KEY_PREF_DISPLAY_STYLE, "");
    }
}
