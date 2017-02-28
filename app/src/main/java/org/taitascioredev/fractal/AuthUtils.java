package org.taitascioredev.fractal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.pnikosis.materialishprogress.ProgressWheel;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Created by saleventa7 on 7/9/2016.
 */
public final class AuthUtils {

    private static UserAgent agent;
    private static RedditClient client;
    private static UUID uuid;
    private static Credentials credentials;
    private static OAuthHelper helper;

    private static AppCompatActivity context;
    private static App app;
    private static Uri uri;

    public static void init(AppCompatActivity context) {
        AuthUtils.context = context;
        app = (App) context.getApplication();

        agent = UserAgent.of(Constants.PLATFORM, Constants.PACKAGE, Constants.VERSION, Constants.USERNAME);
        client = new RedditClient(agent);
        uuid = UUID.randomUUID();
        credentials = Credentials.userlessApp(Constants.CLIENT_ID, uuid);
        helper = client.getOAuthHelper();

        uri = context.getIntent().getData();

        long time = Utils.getTime(context);
        int minutes = (int) Utils.minutesSince(time);
        Log.d("debug", "MINUTES: " + minutes);

        if (app.getClient() == null) {
            app.setClient(client);
            if (refreshTokenExists()) {
                Log.d("debug", "REFRESH TOKEN EXISTS");
                credentials = Credentials.installedApp(Constants.CLIENT_ID, Constants.REDIRECT_URI);
                helper = client.getOAuthHelper();

                String token = Utils.getRefreshToken(context);
                String username = Utils.getUsername(context);
                int karma = Utils.getLinkKarma(context);

                Log.d("debug", "USER: " + username);
                Log.d("debug", "KARMA: " + karma);
                Log.d("debug", "REFRESH TOKEN: " + token);

                new RefreshTokenTask().execute(token);
            }
            else {
                Log.d("debug", "REFRESH TOKEN DOES NOT EXIST");
                new AuthTask().execute();
            }
        }
        else {
            int constant = 5;

            if (minutes + constant >= 60) {
                if (refreshTokenExists()) {
                    Log.d("debug", "REFRESH TOKEN EXISTS");
                    credentials = Credentials.installedApp(Constants.CLIENT_ID, Constants.REDIRECT_URI);
                    helper = client.getOAuthHelper();

                    String token = Utils.getRefreshToken(context);
                    String username = Utils.getUsername(context);
                    int karma = Utils.getLinkKarma(context);

                    Log.d("debug", "USER: " + username);
                    Log.d("debug", "KARMA: " + karma);
                    Log.d("debug", "REFRESH TOKEN: " + token);

                    new RefreshTokenTask().execute(token);
                }
                else {
                    Log.d("debug", "REFRESH TOKEN DOES NOT EXIST");
                    new AuthTask().execute();
                }
            }
            else {
                Log.d("debug", "ACCESS TOKEN: " + app.getClient().getOAuthData().getAccessToken());
                Log.d("debug", "REFRESH TOKEN: " + app.getClient().getOAuthData().getRefreshToken());

                Intent i = new Intent(context, MainActivity.class);

                if (uri != null)
                    i.putExtra("url", uri.toString());

                Bundle b = new Bundle();
                if (context.getIntent() != null) {
                    Uri uri = context.getIntent().getData();

                    if (uri != null) {
                        List<String> segments = uri.getPathSegments();

                        if (segments.size() > 0) {
                            String segment = uri.getPathSegments().get(0);

                            switch (segment) {
                                case "subreddits":
                                    b.putString("action", "subreddits");
                                    break;
                                case "r":
                                    b.putString("action", "subreddit");
                                    b.putString("subreddit_url", uri.getPathSegments().get(1));

                                    if (segments.size() == 5)
                                        b.putString("id", segments.get(3));

                                    break;
                            }
                        }
                        else
                            b.putString("action", "main");

                        i.putExtras(b);
                    }
                }
                context.startActivity(i);
                context.finish();
            }
        }
    }

    private static boolean refreshTokenExists() {
        SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return pref.contains(Constants.SHARED_PREF_REFRESH_TOKEN);
    }

    private static String getCurrentAccount() {
        SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(Constants.SHARED_PREF_CURRENT_ACCOUNT, null);
    }

    private static class AuthTask extends AsyncTask<Void, Void, OAuthData> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("debug", "AUTHENTICATING (USERLESS)...");
            ProgressWheel wheel = (ProgressWheel) context.findViewById(R.id.progress_wheel);
            wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected OAuthData doInBackground(Void... params) {
            try {
                return helper.easyAuth(credentials);
            } catch (OAuthException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(OAuthData oAuthData) {
            super.onPostExecute(oAuthData);
            if (oAuthData != null) {
                Log.d("debug", "ACCESS TOKEN: " + oAuthData.getAccessToken());
                client.authenticate(oAuthData);
                app.setClient(client);

                SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putLong(Constants.SHARED_PREF_TIME, Calendar.getInstance().getTimeInMillis());
                editor.commit();

                Intent i = new Intent(context, MainActivity.class);
                if (uri != null)
                    i.putExtra("url", uri.toString());

                if (context.getIntent() != null) {
                    Uri uri = context.getIntent().getData();
                    Bundle b = new Bundle();

                    if (uri != null) {
                        List<String> segments = uri.getPathSegments();

                        if (segments.size() > 0) {
                            String segment = uri.getPathSegments().get(0);

                            switch (segment) {
                                case "subreddits":
                                    b.putString("action", "subreddits");
                                    break;
                                case "r":
                                    b.putString("action", "subreddit");
                                    b.putString("subreddit_url", uri.getPathSegments().get(1));

                                    if (segments.size() == 5)
                                        b.putString("id", segments.get(3));

                                    break;
                            }
                        }
                        else
                            b.putString("action", "main");

                        i.putExtras(b);
                    }
                }

                //startActivity(i);
                //finish();
            }
        }
    }

    private static class RefreshTokenTask extends AsyncTask<String, Void, OAuthData> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("debug", "REFRESHING TOKEN...");
        }

        @Override
        protected OAuthData doInBackground(String... params) {
            client.getOAuthHelper().setRefreshToken(params[0]);
            try {
                return helper.refreshToken(credentials);
            } catch (OAuthException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(OAuthData oAuthData) {
            super.onPostExecute(oAuthData);
            if (oAuthData != null) {
                Log.d("debug", "TOKEN REFRESHED");
                Log.d("debug", "ACCESS TOKEN: " + oAuthData.getAccessToken());
                Log.d("debug", "REFRESH TOKEN: " + oAuthData.getRefreshToken());
                client.authenticate(oAuthData);
                app.setClient(client);

                SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putLong(Constants.SHARED_PREF_TIME, Calendar.getInstance().getTimeInMillis());
                editor.commit();

                Intent i = new Intent(context, MainActivity.class);
                if (uri != null)
                    i.putExtra("url", uri.toString());

                if (context.getIntent() != null) {
                    Uri uri = context.getIntent().getData();
                    Bundle b = new Bundle();

                    if (uri != null) {
                        List<String> segments = uri.getPathSegments();

                        if (segments.size() > 0) {
                            String segment = uri.getPathSegments().get(0);

                            switch (segment) {
                                case "subreddits":
                                    b.putString("action", "subreddits");
                                    break;
                                case "r":
                                    b.putString("action", "subreddit");
                                    b.putString("subreddit_url", uri.getPathSegments().get(1));

                                    if (segments.size() == 5)
                                        b.putString("id", segments.get(3));

                                    break;
                            }
                        }
                        else
                            b.putString("action", "main");

                        i.putExtras(b);
                    }
                }

                //startActivity(i);
                //finish();
            }
        }
    }
}
