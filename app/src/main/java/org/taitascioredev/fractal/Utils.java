package org.taitascioredev.fractal;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.google.gson.Gson;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.LoggedInAccount;
import net.dean.jraw.models.Submission;

import org.taitascioredev.adapters.CommentAdapter;
import org.taitascioredev.viewholders.CommentVH;
import org.taitascioredev.viewholders.SubmissionVH;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.lanwen.verbalregex.VerbalExpression;
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

/**
 * Created by roberto on 21/03/15.
 */
public class Utils {

    private static final String KEY_PREF_DISPLAY_STYLE = "pref_displayStyle";
    private static final int LIMIT_WORDS = 50;

    public static boolean refresh = false;
    public static boolean hasActiveUser = false;
    public static LoggedInAccount me;
    public static boolean logout;
    public static RedditClient client;
    public static Submission submission;
    public static boolean updateSubreddits = true;
    public static String username = null;
    public static ImgurImage[] images;
    public static Map<String, Data> galleryData = new HashMap<String, Data>();

    public static ArrayList<CommentNode> list;
    public static ArrayList<CommentInfo> listInfo;
    public static Submission sub;

    private static OkHttpClient okclient = new OkHttpClient();
    private static Gson gson = new Gson();

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
        VerbalExpression exp = VerbalExpression.regex()
                .startOfLine().then("http://")
                .oneOf("i.imgur", "www.imgur", "imgur").then(".com/")
                .anything().build();

        return exp.test(url);

        //String regexp = "http://(i.)*imgur.com/\\p{Alnum}+(.jpg)?(\\?\\p{Alnum}+)?";
        //return Pattern.matches(regexp, url);
    }

    /*
    public static String getImageUrl(String url, char ch) {
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
    */

    public static String getImageUrl(String url, char suffix) {
        Uri uri = Uri.parse(url);
        String imgId = uri.getLastPathSegment();
        Log.d("File name", imgId);

        if (!url.contains(".jpg") && !url.contains(".png")) {
            StringBuilder builder = new StringBuilder();
            builder.append("http://i.imgur.com/");

            /*
            builder.append(imgId);
            builder.append(suffix);
            builder.append(".jpg");
            */

            if (!endsWithSuffix(imgId)) {
                builder.append(imgId);
                builder.append(suffix);
                builder.append(".jpg");

                return builder.toString();
            }

            builder.append(imgId);
            builder.append(".jpg");

            return builder.toString();
        }

        int index = imgId.indexOf(".jpg");
        if (index == -1) index = imgId.indexOf(".png");

        imgId = imgId.substring(0, index);
        Log.d("Image ID", imgId);
        StringBuilder builder = new StringBuilder();
        builder.append("http://i.imgur.com/");

        if (!endsWithSuffix(imgId)) {
            builder.append(imgId);
            builder.append(suffix);
            builder.append(".jpg");

            return builder.toString();
        }

        builder.append(imgId);
        builder.append(".jpg");

        return builder.toString();
    }

    public static String getImageUrl(AppCompatActivity context, String url) {
        char suffix = context.getResources().getBoolean(R.bool.is_tablet) ? 'h' : 'l';
        Uri uri = Uri.parse(url);
        String imgId = uri.getPathSegments().get(0);

        if (!url.contains(".jpg") && !url.contains(".png")) {
            //if (!endsWithSuffix(url))
                //return url + suffix + ".jpg";
            StringBuilder builder = new StringBuilder();
            builder.append("http://i.imgur.com/");
            //builder.append(imgId.substring(0, imgId.length() - 1));
            builder.append(imgId.substring(0, imgId.length()));
            builder.append(suffix);
            builder.append(".jpg");
            return builder.toString();
            //return url + ".jpg";
        }

        int index = imgId.indexOf(".jpg");
        if (index == -1)
            index = imgId.indexOf(".png");

        imgId = imgId.substring(0, index);
        StringBuilder builder = new StringBuilder();
        builder.append("http://i.imgur.com/");

        Log.d("IMG ID", imgId);

        if (!endsWithSuffix(imgId)) {
            builder.append(imgId);
            builder.append(suffix);
            builder.append(".jpg");
            return builder.toString();
        }

        //builder.append(imgId.substring(0, imgId.length() - 1));
        builder.append(imgId.substring(0, imgId.length()));
        builder.append(suffix);
        builder.append(".jpg");
        return builder.toString();
        //return imgId + ".jpg`";
    }

    private static boolean endsWithSuffix(String str) {
        char[] suffices = "sbtmlh".toCharArray();

        for (int i = 0; i < suffices.length; i++) {
            char lastChar = str.charAt(str.length() - 1);
            if (lastChar == suffices[i])
                return true;
        }

        return false;
    }

    public static void launchIntent(AppCompatActivity context, String domain, String url) {
        int j;
        Intent i;
        if (domain.contains("youtube.com")) {
            try {
                int index = url.indexOf("?v=");
                for (j = index + 3; j < url.length(); j++) {
                    char c = url.charAt(j);
                    if (!Character.isLetterOrDigit(c) && c != '_' && c != '-')
                        break;
                }
                String videoId = url.substring(index + 3, j);
                i = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
                context.startActivity(i);
            } catch (ActivityNotFoundException e) {
                i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(i);
            }
        }
        else if (domain.contains("youtu.be")) {
            try {
                int index = url.indexOf("youtu.be/");
                for (j = index + "youtu.be/".length(); j < url.length(); j++) {
                    char c = url.charAt(j);
                    if (!Character.isLetterOrDigit(c) && c != '_' && c != '-')
                        break;
                }
                String videoId = url.substring(index + "youtu.be/".length(), j);
                i = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
                context.startActivity(i);
            } catch (ActivityNotFoundException e) {
                i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(i);
            }
        }
        else {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            builder.setCloseButtonIcon(BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.ic_action_back));
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(context, Uri.parse(url));
        }
    }

    public static void launchIntent(AppCompatActivity context, String url) {
        int j;
        Intent i;
        if (url.contains("youtube.com")) {
            try {
                int index = url.indexOf("?v=");
                for (j = index + 3; j < url.length(); j++) {
                    char c = url.charAt(j);
                    if (!Character.isLetterOrDigit(c) && c != '_' && c != '-')
                        break;
                }
                String videoId = url.substring(index + 3, j);
                i = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
                context.startActivity(i);
            } catch (ActivityNotFoundException e) {
                i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(i);
            }
        }
        else if (url.contains("youtu.be")) {
            try {
                int index = url.indexOf("youtu.be/");
                for (j = index + "youtu.be/".length(); j < url.length(); j++) {
                    char c = url.charAt(j);
                    if (!Character.isLetterOrDigit(c) && c != '_' && c != '-')
                        break;
                }
                String videoId = url.substring(index + "youtu.be/".length(), j);
                i = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
                context.startActivity(i);
            } catch (ActivityNotFoundException e) {
                i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(i);
            }
        }
        else {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            builder.setCloseButtonIcon(BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.ic_action_back));
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(context, Uri.parse(url));
        }
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
            Log.d("URL SPAN", url);

            Uri uri = Uri.parse(url);
            Log.d("SEGMENTS", uri.getPathSegments().size()+"");

            ssb.removeSpan(spans[i]);

            if ((!url.startsWith("/r/") && !url.startsWith("r/"))
                    && (!url.contains("/u/") && !url.startsWith("u/"))
                    && (!url.startsWith("/m/") && !url.startsWith("m/"))) {
                MyClickableSpan clickableSpan = new MyClickableSpan(ContextCompat.getColor(context, R.color.colorPrimary)) {
                    @Override
                    public void onClick(View widget) {
                        launchIntent(context, url);
                    }
                };
                ssb.setSpan(clickableSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                typefaceSpan = new CalligraphyTypefaceSpan(
                        TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));
                ssb.setSpan(typefaceSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }

            for (final FormattedLink link : FormattedLink.extract(url)) {
                Log.d("URL", url);
                Log.d("LINK", link.getText());
                //start = link.getStart();
                //end = link.getEnd();
                //ssb.removeSpan(spans[i]);

                MyClickableSpan span = new MyClickableSpan(ContextCompat.getColor(context, R.color.colorPrimary)) {
                    @Override
                    public void onClick(View widget) {
                        Log.d("onClick", link.getText());
                        switch (link.getType()) {
                            case FormattedLink.TYPE_SUBREDDIT:
                                App app = (App) context.getApplication();
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
                            default:
                                Toast.makeText(context, "Link not yet supported", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                ssb.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                typefaceSpan = new CalligraphyTypefaceSpan(
                        TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));
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
                            App app = (App) context.getApplication();
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
            String[] words = ssb.toString().split(" ");
            if (words.length > LIMIT_WORDS) {
                SpannableStringBuilder b = new SpannableStringBuilder();

                for (int i = 0; i < LIMIT_WORDS - 1; i++)
                    b.append(words[i] + " ");
                b.append(words[LIMIT_WORDS - 1]);

                SpannableStringBuilder cutSpan = new SpannableStringBuilder(ssb.subSequence(0, b.toString().length()));
                cutSpan.append(" (...)");

                return cutSpan;
            }
        }

        return ssb;
    }

    public static CharSequence setUrlSpans(final AppCompatActivity context, CommentVH vh, Spanned spanned, boolean cutContent, boolean showThumbnails, CommentAdapter adapter) {
        String str = spanned.toString();
        str = str.replaceAll("<div class=\"md\">", "");
        str = str.replaceAll("</div>", "");
        StringBuilder builder = new StringBuilder(str);
        /*
        int index = builder.indexOf("<p>");
        if (index != -1) {
            builder = builder.replace(index, index + 3, "");
            index = builder.lastIndexOf("</p>");
            builder = builder.replace(index, index + 4, "");
        }
        */
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

        vh.thumbnail.setVisibility(View.GONE);
        vh.btnPlay.setVisibility(View.GONE);

        for (int i = 0; i < spans.length; i++) {
            Log.d("Span", spans[i].toString());
            int start = ssb.getSpanStart(spans[i]);
            int end = ssb.getSpanEnd(spans[i]);
            final String url = spans[i].getURL();
            Log.d("URL SPAN", url);

            Uri uri = Uri.parse(url);
            Log.d("SEGMENTS", uri.getPathSegments().size()+"");

            if (showThumbnails) setThumbnail(context, url, vh, adapter);

            ssb.removeSpan(spans[i]);

            if ((!url.startsWith("/r/") && !url.startsWith("r/"))
                    && (!url.contains("/u/") && !url.startsWith("u/"))
                    && (!url.startsWith("/m/") && !url.startsWith("m/"))) {
                MyClickableSpan clickableSpan = new MyClickableSpan(ContextCompat.getColor(context, R.color.colorPrimary)) {
                    @Override
                    public void onClick(View widget) {
                        launchIntent(context, url);
                    }
                };
                ssb.setSpan(clickableSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                typefaceSpan = new CalligraphyTypefaceSpan(
                        TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));
                ssb.setSpan(typefaceSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }

            for (final FormattedLink link : FormattedLink.extract(url)) {
                Log.d("URL", url);
                Log.d("LINK", link.getText());
                //start = link.getStart();
                //end = link.getEnd();
                //ssb.removeSpan(spans[i]);

                MyClickableSpan span = new MyClickableSpan(ContextCompat.getColor(context, R.color.colorPrimary)) {
                    @Override
                    public void onClick(View widget) {
                        Log.d("onClick", link.getText());
                        switch (link.getType()) {
                            case FormattedLink.TYPE_SUBREDDIT:
                                App app = (App) context.getApplication();
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
                            default:
                                Toast.makeText(context, "Link not yet supported", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                ssb.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                typefaceSpan = new CalligraphyTypefaceSpan(
                        TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));
                ssb.setSpan(typefaceSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }

        if (cutContent) {
            String[] words = ssb.toString().split(" ");
            if (words.length > LIMIT_WORDS) {
                SpannableStringBuilder b = new SpannableStringBuilder();

                for (int i = 0; i < LIMIT_WORDS - 1; i++)
                    b.append(words[i] + " ");
                b.append(words[LIMIT_WORDS - 1]);

                SpannableStringBuilder cutSpan = new SpannableStringBuilder(ssb.subSequence(0, b.toString().length()));
                cutSpan.append(" (...)");

                return cutSpan;
            }
        }

        return ssb;
    }

    public static void setThumbnail(final AppCompatActivity context, String url, final CommentVH vh, CommentAdapter adapter) {
        final Uri uri;

        if (url.contains("imgur") && !url.contains("gallery") && !url.contains(".gif") && !url.contains("/a/")) {
            final String img = url;
            Log.d("IMAGE URL", Utils.getImageUrl(url, 'l'));
            uri = Uri.parse(Utils.getImageUrl(url, 'l'));
            vh.thumbnail.setImageURI(uri);
            vh.thumbnail.setVisibility(View.VISIBLE);
            vh.btnPlay.setVisibility(View.GONE);

            vh.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("IMG", img);
                    Log.d("getImageUrl", Utils.getImageUrl(img, 'l'));
                    Intent i = new Intent(context, PostImageActivity.class);
                    i.putExtra("url", Utils.getImageUrl(img, 'l'));
                    context.startActivity(i);
                }
            });
        }
        else if (url.contains("imgur") && (url.contains("gallery") || url.contains("/a/"))) {
            Log.d("GALLERY URL", url);
            vh.thumbnail.setVisibility(View.VISIBLE);
            Uri nullUri = null;
            vh.thumbnail.setImageURI(nullUri);
            //vh.thumbnail.setImageAlpha(Constants.ALPHA);
            vh.btnPlay.setImageResource(R.drawable.ic_gallery_48dp);
            //vh.btnPlay.setVisibility(View.VISIBLE);

            uri = Uri.parse(url);
            List<String> segments = uri.getPathSegments();
            Log.d("CONTAINS KEY", galleryData.containsKey(segments.get(segments.size() - 1))+"");
            if (!galleryData.containsKey(segments.get(segments.size() - 1))) {
                vh.thumbnail.setOnClickListener(null);
                new GetGalleryDataAsync(vh, adapter).execute(segments.get(segments.size() - 1));
            }
            else {
                final Data data = galleryData.get(segments.get(segments.size() - 1));

                if (data.is_album && data.images != null && data.images.length > 0) {
                    Log.d("Image count", data.images.length+"");
                    String coverUrl = Utils.getImageUrl(data.images[0].link, 'l');
                    Log.d("COVER URL", coverUrl);
                    Uri coverUri = Uri.parse(coverUrl);
                    vh.thumbnail.setImageURI(coverUri);
                    vh.thumbnail.setImageAlpha(Constants.ALPHA);
                    vh.btnPlay.setVisibility(View.VISIBLE);

                    vh.thumbnail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(context, GalleryActivity.class);
                            i.putExtra("images", data.images);
                            context.startActivity(i);
                        }
                    });
                }
                else {
                    String coverUrl = Utils.getImageUrl(data.link, 'l');
                    Log.d("COVER URL", coverUrl);
                    Uri coverUri = Uri.parse(coverUrl);
                    vh.thumbnail.setImageURI(coverUri);
                    vh.thumbnail.setOnClickListener(null);
                    vh.thumbnail.setImageAlpha(255);
                    vh.btnPlay.setVisibility(View.GONE);
                }
            }
        }
        else if (url.contains(".gif") && !url.contains(".gifv")) {
            final String img = url;
            Log.d("GIF URL", url);
            uri = Uri.parse(url);

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(uri)
                    .setAutoPlayAnimations(true)
                    .build();

            vh.thumbnail.setController(controller);
            vh.thumbnail.setVisibility(View.VISIBLE);
            vh.btnPlay.setVisibility(View.GONE);

            vh.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("IMG", img);
                    Log.d("getImageUrl", Utils.getImageUrl(img, 'l'));
                    Intent i = new Intent(context, PostImageActivity.class);
                    i.putExtra("url", img);
                    //i.putExtra("url", Utils.getImageUrl(img, 'l'));
                    context.startActivity(i);
                }
            });
        }
        else if (url.contains(".gifv")) {
            /*
            vh.thumbnail.setVisibility(View.GONE);
            vh.lySmallThumb.setVisibility(View.GONE);
            vh.divider.setVisibility(View.GONE);
            */

            final String videoUrl = url;
            final String img = Utils.getImageUrl(url.replaceAll(".gifv", ""), 'm');

            Log.d("THUMBNAIL URL", img);
            Log.d("GIFV URL", url);
            uri = Uri.parse(img);

            vh.thumbnail.setImageURI(uri);
            vh.thumbnail.setVisibility(View.VISIBLE);
            vh.btnPlay.setVisibility(View.VISIBLE);

            vh.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String video = videoUrl.replace("gifv", "mp4");
                    Intent i;

                    try {
                        i = new Intent();
                        i.setDataAndType(Uri.parse(video), "video/*");
                        context.startActivity(i);
                    } catch (ActivityNotFoundException e) {
                        i = new Intent(context, PostVideoActivity.class);
                        i.putExtra("url", video);
                    }
                }
            });
        }
        else if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")) {
            final String img = url;
            Log.d("IMAGE URL", img);
            uri = Uri.parse(img);
            vh.thumbnail.setImageURI(uri);
            vh.thumbnail.setVisibility(View.VISIBLE);
            vh.btnPlay.setVisibility(View.GONE);

            vh.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("IMG", img);
                    Intent i = new Intent(context, PostImageActivity.class);
                    i.putExtra("url", img);
                    context.startActivity(i);
                }
            });
        }
        else {
            vh.thumbnail.setVisibility(View.GONE);
            vh.btnPlay.setVisibility(View.GONE);
        }
    }

    public static void logout(final AppCompatActivity context) {
        App app = (App) context.getApplication();
        //SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = pref.edit();
        //editor.clear();

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

        //editor.commit();
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
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    AuthenticationManager.get().getRedditClient().getOAuthHelper().revokeAccessToken(LoginFragment.CREDENTIALS);
                    return true;
                } catch (NetworkException e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    context.startActivity(new Intent(context, EntryActivity.class));
                    context.finish();
                }
            }
        }.execute();
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

    public static Fragment getFragment(AppCompatActivity context, String name) {
        List<Fragment> list = context.getSupportFragmentManager().getFragments();

        if (list != null)
            for (Fragment f : list)
                if (f != null && f.getClass().getName().contains(name) && f.isVisible())
                    return f;

        return null;
    }

    private static String run(String galleryId) throws IOException {
        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/gallery/" + galleryId)
                .header("Authorization", ImgurApi.AUTHORIZATION_HEADER)
                .build();

        Response response = okclient.newCall(request).execute();
        return response.body().string();
    }

    public static Data getGalleryData(String galleryId) {
        try {
            String json = run(galleryId);
            Log.d("JSON GALLERY", json);
            ImgurResponse response = gson.fromJson(json, ImgurResponse.class);
            return response.data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MaterialDialog showProgressDialog(Activity context, String msg) {
        return new MaterialDialog.Builder(context)
                .theme(Theme.LIGHT)
                .content(msg)
                .progress(true, 0)
                .autoDismiss(false)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();
    }

    public static String toHtml(String str) {
        return Html.fromHtml(str).toString();
    }

    public static void hideKeyboard(Activity context) {
        View view = context.getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    public static void showSnackbar(View v, String msg) {
        Snackbar.make(v, msg, Snackbar.LENGTH_SHORT).show();
    }

    public static void showSnackbar(Activity context, View.OnClickListener listener) {
        int color = ContextCompat.getColor(context, R.color.colorAccent);
        Snackbar.make(context.findViewById(R.id.main_content), "Something went wrong", Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(color)
                .setAction("retry", listener)
                .show();
    }

    public static boolean isValid(OAuthData data) {
        return data != null && data.getExpirationDate().after(new Date());
    }

    static class GetGalleryDataAsync extends AsyncTask<String, Void, Data> {

        String id;
        RecyclerView.ViewHolder vh;
        CommentAdapter adapter;

        public GetGalleryDataAsync(RecyclerView.ViewHolder vh, CommentAdapter adapter) {
            this.vh  = vh;
            this.adapter = adapter;
        }

        @Override
        protected Data doInBackground(String... strings) {
            id = strings[0];
            return Utils.getGalleryData(id);
        }

        @Override
        protected void onPostExecute(Data data) {
            super.onPostExecute(data);
            if (data != null) {
                Log.d("Debug", "data NOT NULL");
                galleryData.put(id, data);

                if (data.images != null) {
                    ImgurImage[] images = data.images;
                    String[] list = new String[images.length];
                    for (int i = 0; i < list.length; i++) list[0] = images[i].link;
                    Uri uri = Uri.parse(images[0].link);

                    if (vh instanceof SubmissionVH)
                        ((SubmissionVH) vh).thumbnail.setImageURI(uri);
                    else if (vh instanceof CommentVH)
                        ((CommentVH) vh).thumbnail.setImageURI(uri);

                    adapter.notifyItemChanged(vh.getAdapterPosition());
                }
            }
            else {

            }
        }
    }

    public static void saveExpirationDate(Context context, long date) {
        SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong("expiration_date", date);
        editor.commit();
    }

    public static void saveExpirationDate(Context context, Date date) {
        SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong("expiration_date", date.getTime());
        editor.commit();
    }

    public static Date getExpirationDate(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        long date = pref.getLong("expiration_date", 0);
        return date > 0 ? new Date(date) : null;
    }

    public static boolean hasExpired(Context context) {
        Date expirationDate = getExpirationDate(context);
        if (expirationDate == null) return true;
        return new Date().after(expirationDate);
    }
}
