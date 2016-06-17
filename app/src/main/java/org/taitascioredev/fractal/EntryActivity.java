package org.taitascioredev.fractal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.util.Calendar;
import java.util.UUID;

/**
 * Created by roberto on 29/04/15.
 */
public class EntryActivity extends AppCompatActivity {

    private UserAgent agent;
    private RedditClient client;
    private Credentials credentials;
    private OAuthHelper helper;
    private MyApp app;
    private Uri uri;

    private OkHttpClient okhttp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        //getSupportActionBar().hide();
        app = (MyApp) getApplication();
        agent = UserAgent.of(Constants.PLATFORM, Constants.PACKAGE, Constants.VERSION, Constants.USERNAME);
        client = new RedditClient(agent);
        UUID uuid = UUID.randomUUID();
        credentials = Credentials.userlessApp(Constants.CLIENT_ID, uuid);
        helper = client.getOAuthHelper();
        uri = getIntent().getData();
        long time = Utils.getTime(this);
        int minutes = (int) Utils.minutesSince(time);
        Log.d("debug", "MINUTES: " + minutes);
        if (app.getClient() == null) {
            if (refreshTokenExists()) {
                Log.d("debug", "REFRESH TOKEN EXISTS");
                credentials = Credentials.installedApp(Constants.CLIENT_ID, Constants.REDIRECT_URI);
                helper = client.getOAuthHelper();
                //String currentAccount = getCurrentAccount();
                //Log.d("debug", "USER: " + currentAccount);
                //Log.d("debug", "REFRESH TOKEN: " + Utils.findTokenByAccount(this, currentAccount));
                String token = Utils.getRefreshToken(this);
                String username = Utils.getUsername(this);
                int karma = Utils.getLinkKarma(this);
                Log.d("debug", "USER: " + username);
                Log.d("debug", "KARMA: " + karma);
                Log.d("debug", "REFRESH TOKEN: " + token);
                new RefreshTokenTask().execute(token);
                //if (currentAccount != null)
                    //new RefreshTokenTask().execute(Utils.findTokenByAccount(this, currentAccount));
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
                    String token = Utils.getRefreshToken(this);
                    String username = Utils.getUsername(this);
                    int karma = Utils.getLinkKarma(this);
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
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                //Intent i = new Intent(getApplicationContext(), MainActivity2.class);
                if (uri != null)
                    i.putExtra("url", uri.toString());
                startActivity(i);
                finish();
            }
        }
    }

    private class Async extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                return run();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Gson gson = new Gson();
            ImgurResponse response = gson.fromJson(s, ImgurResponse.class);
            if (response != null) {
                Image[] img = response.data.images;
                for (int i = 0; i < img.length; i++)
                    Log.d("debug", img[i].id + ": " + img[i].link);
            }
            else
                Log.d("debug", "null");
        }
    }

    public String run() throws Exception {
        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/gallery/oIPPg")
                .header("Authorization", ImgurApi.AUTHORIZATION_HEADER)
                .build();

        Response response = okhttp.newCall(request).execute();
        return response.body().string();
    }

    private boolean refreshTokenExists() {
        SharedPreferences pref = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);
        return pref.contains(Constants.SHARED_PREF_REFRESH_TOKEN);
    }

    private String getCurrentAccount() {
        SharedPreferences pref = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);
        return pref.getString(Constants.SHARED_PREF_CURRENT_ACCOUNT, null);
    }

    private class AuthTask extends AsyncTask<Void, Void, OAuthData> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("debug", "AUTHENTICATING (USERLESS)...");
            ProgressWheel wheel = (ProgressWheel) findViewById(R.id.progress_wheel);
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
                client.authenticate(oAuthData);
                Log.d("debug", "ACCESS TOKEN: " + oAuthData.getAccessToken());
                app.setClient(client);
                SharedPreferences pref = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putLong(Constants.SHARED_PREF_TIME, Calendar.getInstance().getTimeInMillis());
                editor.commit();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                if (uri != null)
                    i.putExtra("url", uri.toString());
                startActivity(i);
                finish();
            }
        }
    }

    private class RefreshTokenTask extends AsyncTask<String, Void, OAuthData> {

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
                client.authenticate(oAuthData);
                app.setClient(client);
                Log.d("debug", "ACCESS TOKEN: " + oAuthData.getAccessToken());
                Log.d("debug", "REFRESH TOKEN: " + oAuthData.getRefreshToken());
                SharedPreferences pref = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putLong(Constants.SHARED_PREF_TIME, Calendar.getInstance().getTimeInMillis());
                editor.commit();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                //Intent i = new Intent(getApplicationContext(), MainActivity2.class);
                if (uri != null)
                    i.putExtra("url", uri.toString());
                startActivity(i);
                finish();
            }
        }
    }
}
