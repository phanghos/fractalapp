package org.taitascioredev.fractal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;
import net.dean.jraw.models.LoggedInAccount;

import java.net.URL;
import java.util.Calendar;

/**
 * Created by roberto on 16/05/15.
 */
public class LoginFragment extends Fragment {

    private UserAgent agent;
    private RedditClient client;
    private Credentials credentials;
    private OAuthHelper helper;
    private MyApp app;

    private WebView webview;
    private AppCompatActivity context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_login3, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = (AppCompatActivity) getActivity();
        app = (MyApp) context.getApplication();
        context.getSupportActionBar().hide();
        webview = (WebView) context.findViewById(R.id.webview);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("code="))
                    new AuthTask().execute(url);
                return false;
            }
        });
        agent = UserAgent.of(Constants.PLATFORM, Constants.PACKAGE, Constants.VERSION, Constants.USERNAME);
        client = new RedditClient(agent);
        credentials = Credentials.installedApp(Constants.CLIENT_ID, Constants.REDIRECT_URI);
        helper = client.getOAuthHelper();
        String[] scopes = new String[] {"identity", "mysubreddits", "privatemessages", "read", "save", "submit", "subscribe", "vote", "history", "report"};
        URL authUrl = helper.getAuthorizationUrl(credentials, true, scopes);
        webview.loadUrl(authUrl.toExternalForm());
    }

    public interface OnAccountLoggedListener {

        public void OnAccountLoggedIn();
        public void OnAccountLoggedOut();
    }

    private class AuthTask extends AsyncTask<String, Void, OAuthData> {

        @Override
        protected OAuthData doInBackground(String... params) {
            try {
                return helper.onUserChallenge(params[0], credentials);
            } catch (OAuthException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(OAuthData oAuthData) {
            super.onPostExecute(oAuthData);
            if (oAuthData != null) {
                client.authenticate(oAuthData);
                app.setClient(client);
                Log.d("debug", "LOGGED IN AS:");
                Log.d("debug", app.getClient().getAuthenticatedUser() + "");
                Log.d("debug", "REFRESH TOKEN: " + oAuthData.getRefreshToken());
                new GetMeTask(oAuthData.getRefreshToken()).execute();
            }
        }
    }

    private class GetMeTask extends AsyncTask<Void, Void, LoggedInAccount> {

        private String token;

        public GetMeTask(String token) { this.token = token; }

        @Override
        protected LoggedInAccount doInBackground(Void... params) {
            return app.getClient().me();
        }

        @Override
        protected void onPostExecute(LoggedInAccount loggedInAccount) {
            super.onPostExecute(loggedInAccount);
            Log.d("debug", "AUTHENTICATED USER: " + loggedInAccount.getFullName());
            SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(Constants.SHARED_PREF_REFRESH_TOKEN, token);
            editor.putString(Constants.SHARED_PREF_ACCOUNT_USERNAME, loggedInAccount.getFullName());
            editor.putInt(Constants.SHARED_PREF_ACCOUNT_LINK_KARMA, loggedInAccount.getLinkKarma());
            /*
            Set<String> accounts = pref.getStringSet(Constants.SHARED_PREF_ACCOUNTS, null);
            if (accounts == null)
                accounts = new HashSet<>();
            accounts.add(loggedInAccount.getFullName());
            Set<String> tokens = pref.getStringSet(Constants.SHARED_PREF_REFRESH_TOKEN, null);
            if (tokens == null)
                tokens = new HashSet<>();
            tokens.add(loggedInAccount.getFullName() + "_" + token);
            */
            //editor.putStringSet(Constants.SHARED_PREF_ACCOUNTS, accounts);
            //editor.putStringSet(Constants.SHARED_PREF_REFRESH_TOKEN, tokens);
            //editor.putString(Constants.SHARED_PREF_CURRENT_ACCOUNT, loggedInAccount.getFullName());
            Calendar c = Calendar.getInstance();
            editor.putLong(Constants.SHARED_PREF_TIME, c.getTimeInMillis());
            editor.commit();

            app.setPaginator(null);
            app.setSubmissions(null);

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

            NavigationView navView = (NavigationView) context.findViewById(R.id.navigation_view);
            MenuItem item = navView.getMenu().findItem(R.id.subreeddits);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    SubredditsFragmentOAuth fragment = new SubredditsFragmentOAuth();
                    context.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
                    return true;
                }
            });

            if (!Utils.hasActiveUser) {
                item = navView.getMenu().findItem(R.id.log);
                item.setTitle("Log Out");
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Utils.logout(context);
                        return true;
                    }
                });
            }

            context.getSupportActionBar().show();

            /*
            Utils.username = loggedInAccount.getFullName();
            TextView username = (TextView) context.findViewById(R.id.username);
            TextView email = (TextView) context.findViewById(R.id.email);
            username.setText(loggedInAccount.getFullName());
            email.setText(loggedInAccount.getLinkKarma() + " link karma");
            item = navView.getMenu().findItem(R.id.log);
            item.setTitle("Log Out");
            */

            //Utils.refresh = true;
            //Utils.hasActiveUser = true;
            webview.destroy();
            MainFragment fragment = new MainFragment();
            context.startActivity(new Intent(context, EntryActivity.class));
            context.finish();
            //context.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

            //Utils.me = loggedInAccount;
            //context.startActivity(new Intent(context, EntryActivity.class));
            //context.finish();
            //getActivity().onBackPressed();
        }
    }
}
