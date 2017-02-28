package org.taitascioredev.fractal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.auth.NoSuchTokenException;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by roberto on 29/04/15.
 */
public class EntryActivity extends AppCompatActivity {

    boolean repeatAnimation = true;

    UUID uuid;
    Credentials credentials;

    @BindView(R.id.coordinator_layout) View layout;
    @BindView(R.id.logo) ImageView logo;
    @BindView(R.id.tv_wait) TextView tvWait;

    AnimatorSet mAnimatorSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        ButterKnife.bind(this);
        animate();

        OAuthHelper helper = AuthenticationManager.get().getRedditClient().getOAuthHelper();

        AuthenticationState state = AuthenticationManager.get().checkAuthState();
        Log.d("Authentication state", state.name());
        Log.d("Authentication status", AuthenticationManager.get().getRedditClient().getOAuthHelper().getAuthStatus().name());

        switch (state) {
            case NONE:
                uuid = UUID.randomUUID();
                credentials = Credentials.userlessApp(Constants.CLIENT_ID, uuid);
                new AuthenticateAsync().execute();
                break;
            case NEED_REFRESH:
                credentials = LoginFragment.CREDENTIALS;
                new RefreshTokenAsync().execute();
                break;
            case READY:
                if (Utils.hasExpired(this))  {
                    if (AuthenticationManager.get().getRedditClient().isAuthenticated()) {
                        credentials = LoginFragment.CREDENTIALS;
                        new RefreshTokenAsync().execute();
                    }
                    else {
                        uuid = UUID.randomUUID();
                        credentials = Credentials.userlessApp(Constants.CLIENT_ID, uuid);
                        new AuthenticateAsync().execute();
                    }
                }
                else {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    putExtras(i);
                    startActivity(i);
                    finish();
                }

                break;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private Bundle putExtras(Intent i) {
        Bundle b = new Bundle();

        if (i != null) {
            Uri uri = getIntent().getData();
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

        return b;
    }

    private void animate() {
        ObjectAnimator alphaAnimFade = ObjectAnimator.ofFloat(logo, "alpha", 1, 0.7f);
        alphaAnimFade.setDuration(1000);

        ObjectAnimator alphaAnimShow = ObjectAnimator.ofFloat(logo, "alpha", 0.7f, 1);
        alphaAnimShow.setDuration(1000);

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (repeatAnimation)
                    mAnimatorSet.start();
            }
        });
        mAnimatorSet.playSequentially(alphaAnimFade, alphaAnimShow);
        mAnimatorSet.start();
    }

    private class AuthenticateAsync extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("debug", "AUTHENTICATING (USERLESS)...");
            if (!mAnimatorSet.isRunning()) {
                repeatAnimation = true;
                mAnimatorSet.start();
            }
            tvWait.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean isConnected = Utils.isConnected(getApplicationContext());
            Log.d("IS CONNECTED", isConnected+"");
            if (!isConnected) return false;

            try {
                OAuthData data = AuthenticationManager.get().getRedditClient().getOAuthHelper().easyAuth(credentials);
                AuthenticationManager.get().getRedditClient().authenticate(data);
                Utils.saveExpirationDate(getApplicationContext(), data.getExpirationDate());
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                //Log.d("debug", "ACCESS TOKEN: " + result.getAccessToken());
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                putExtras(i);
                startActivity(i);
                finish();
            } else {
                repeatAnimation = false;
                Utils.showSnackbar(EntryActivity.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AuthenticateAsync().execute();
                    }
                });
                tvWait.setVisibility(View.GONE);
            }
        }
    }

    private class RefreshTokenAsync extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("debug", "REFRESHING TOKEN...");
            if (!mAnimatorSet.isRunning()) {
                repeatAnimation = true;
                mAnimatorSet.start();
            }
            tvWait.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean isConnected = Utils.isConnected(getApplicationContext());
            Log.d("IS CONNECTED", isConnected+"");
            if (!isConnected) return false;

            try {
                AuthenticationManager.get().refreshAccessToken(LoginFragment.CREDENTIALS);
                Date expirationDate = new Date();
                expirationDate.setTime(expirationDate.getTime() + 60 * 1000 * 60);
                Utils.saveExpirationDate(getApplicationContext(), expirationDate);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                putExtras(i);
                startActivity(i);
                finish();
            } else {
                repeatAnimation = false;
                Snackbar.make(layout, getString(R.string.err_network), Snackbar.LENGTH_INDEFINITE)
                        .setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new RefreshTokenAsync().execute();
                            }
                        })
                        .setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent))
                        .show();
                tvWait.setVisibility(View.GONE);
            }
        }
    }
}
